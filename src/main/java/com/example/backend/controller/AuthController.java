package com.example.backend.controller;

import com.example.backend.dto.request.*;
import com.example.backend.entity.Account;
import com.example.backend.service.AccountService;
import com.example.backend.service.EmailService;
import com.example.backend.service.SmsService;
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

import java.security.Principal;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountService accountService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final SmsService smsService;
    AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest, HttpServletResponse response) {
        String authToken = accountService.registerUser(signUpRequest);

        final ResponseCookie cookie = ResponseCookie.from("AUTH-TOKEN", authToken)
                .httpOnly(true)
                .maxAge(7 * 24 * 3600)
                .path("/")
                .secure(false)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(authToken);
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

    @PostMapping("/send/otp/email")
    public ResponseEntity<?> sendOptViaEmail(Principal principal) {
        String accountEmail = principal.getName();

        int otp = otpService.generateOtp(accountEmail);

        EmailDtoRequest emailDTO = null;
        if (otp != 0) {
            emailDTO = emailService.generateOptMail(accountEmail, otp);
        }

        if (emailDTO != null) {
            if (emailService.sendSimpleMessage(emailDTO)) {
                System.out.println("OTP Confirmed");
                return ResponseEntity.ok().build();
            }
        }

        return ResponseEntity.ok(ResponseEntity.badRequest());
    }

    @PostMapping("/send/otp/sms")
    public ResponseEntity<?> sendOptViaPhone(Principal principal) {
        Account account = accountService.getAccount(principal.getName());

        int otp = otpService.generateOtp(account.getEmail());

        smsService.sendSms(account.getPhone(), otp);

        return ResponseEntity.ok(ResponseEntity.ok());
    }

    @PostMapping(value = "/verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyTokenDtoRequest verifyTokenRequest, Principal principal) {
        String email = principal.getName();
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
