package com.example.backend.config.jwt;

import com.example.backend.entity.Account;
import com.example.backend.entity.ERole;
import com.example.backend.service.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;
    private static final long TOKEN_VALIDITY = 86400000L;
    private static final long TOKEN_VALIDITY_REMEMBER = 2592000000L;
    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication, boolean rememberMe) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        long now = (new Date()).getTime();
        Date validity = rememberMe ? new Date(now + TOKEN_VALIDITY_REMEMBER) : new Date(now + TOKEN_VALIDITY);
        Map<String, Object> claims = new HashMap<>();
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.equals(new SimpleGrantedAuthority("ROLE_ADMIN")))) {
            claims.put("role", ERole.ROLE_ADMIN);
        } else {
            claims.put("role", ERole.ROLE_USER);
        }

        return Jwts.builder()
                .setSubject(userPrincipal.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(validity)
                .addClaims(claims)
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String createToken(Account account, boolean rememberMe) {
        long now = (new Date()).getTime();
        Date validity = rememberMe ? new Date(now + TOKEN_VALIDITY_REMEMBER) : new Date(now + TOKEN_VALIDITY);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", ERole.ROLE_USER);

        return Jwts.builder()
                .setSubject(account.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(validity)
                .addClaims(claims)
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Authentication verifyAndGetAuthentication(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(claims.get("role", String.class));
            return new UsernamePasswordAuthenticationToken(claims.getSubject(), token, authorities);
        } catch (JwtException | IllegalArgumentException ignored) {
            return null;
        }
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
