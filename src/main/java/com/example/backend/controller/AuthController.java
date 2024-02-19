package com.example.backend.controller;

import com.example.backend.dto.request.IdTokenRequest;
import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.SignupRequest;
import com.example.backend.dto.request.VerifyTokenDtoRequest;
import com.example.backend.entity.Account;
import com.example.backend.service.AccountService;
import com.example.backend.service.otp.OtpService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountService accountService;
    private final OtpService otpService;
    AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        Account account = accountService.registerUser(signUpRequest);
        otpService.generateOtp(signUpRequest.getEmail());

        return ResponseEntity.ok(account);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String authToken = accountService.loginUser(authentication);

        final ResponseCookie cookie = ResponseCookie.from("AUTH-TOKEN", authToken)
                .httpOnly(true)
                .maxAge(7 * 24 * 3600)
                .path("/")
                .secure(false)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(authToken);
    }

    @PostMapping("/oauth")
    public ResponseEntity<?> loginWithGoogleOauth2(@RequestBody IdTokenRequest requestBody, HttpServletResponse response) {
        String authToken = accountService.loginOAuthGoogle(requestBody);
        final ResponseCookie cookie = ResponseCookie.from("AUTH-TOKEN", authToken)
                .httpOnly(true)
                .maxAge(7 * 24 * 3600)
                .path("/")
                .secure(false)
                .build();
        response.addHeader(com.google.common.net.HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(authToken);
    }

    @PostMapping(value = "/verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyTokenDtoRequest verifyTokenRequest) {
        String email = verifyTokenRequest.getEmail();
        Integer otp = verifyTokenRequest.getOtp();
        Boolean rememberMe = verifyTokenRequest.getRememberMe();

        boolean isOtpValid = otpService.validateOTP(email, otp);
        if (!isOtpValid) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        accountService.activateUserAccount(email);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
