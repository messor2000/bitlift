package com.example.backend.service;

import com.example.backend.config.jwt.JwtUtils;
import com.example.backend.dto.payload.request.IdTokenRequestDto;
import com.example.backend.dto.payload.request.SignupRequest;
import com.example.backend.entity.Account;
import com.example.backend.entity.ERole;
import com.example.backend.entity.Role;
import com.example.backend.repo.RoleRepository;
import com.example.backend.repo.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class AccountService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    PasswordEncoder encoder;
    private final GoogleIdTokenVerifier verifier;

    public AccountService(@Value("${app.googleClientId}") String clientId, UserRepository userRepository,
                          JwtUtils jwtUtils, RoleRepository roleRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtils = jwtUtils;
        this.encoder = encoder;
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public Account registerUser(SignupRequest signupRequest) {
        Account account = new Account();
        Set<Role> roles = new HashSet<>();

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        account.setEmail(signupRequest.getEmail());
        account.setPassword(encoder.encode(signupRequest.getPassword()));

        account.setRoles(roles);
        return userRepository.save(account);
    }

    public String loginUser(Authentication authentication) {
        String jwt = jwtUtils.generateJwtToken(authentication, false);
        return jwt;
    }

    public Account getAccount(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public String loginOAuthGoogle(IdTokenRequestDto requestBody) {
        Account account = verifyIDToken(requestBody.getIdToken());
        if (account == null) {
            throw new IllegalArgumentException();
        }
        account = createOrUpdateUser(account);
        return jwtUtils.createToken(account, false);
    }

    @Transactional
    public Account createOrUpdateUser(Account account) {
        Account existingAccount = userRepository.findByEmail(account.getEmail()).orElse(null);
        if (existingAccount == null) {
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
            account.setRoles(roles);
            userRepository.save(account);
            return account;
        }
        existingAccount.setFirstName(account.getFirstName());
        existingAccount.setLastName(account.getLastName());
        existingAccount.setPictureUrl(account.getPictureUrl());
        userRepository.save(existingAccount);
        return existingAccount;
    }

    private Account verifyIDToken(String idToken) {
        try {
            GoogleIdToken idTokenObj = verifier.verify(idToken);
            if (idTokenObj == null) {
                return null;
            }
            GoogleIdToken.Payload payload = idTokenObj.getPayload();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String email = payload.getEmail();
            String pictureUrl = (String) payload.get("picture");

            return new Account(firstName, lastName, email);
        } catch (GeneralSecurityException | IOException e) {
            return null;
        }
    }
}
