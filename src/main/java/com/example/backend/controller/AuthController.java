package com.example.backend.controller;

import com.example.backend.dto.AccountDto;
import com.example.backend.dto.request.*;
import com.example.backend.entity.Account;
import com.example.backend.error.AccountAlreadyExistsException;
import com.example.backend.error.AccountNotFoundException;
import com.example.backend.error.PasswordMissmatchException;
import com.example.backend.service.CaptchaService;
import com.example.backend.service.EmailService;
import com.example.backend.service.SmsService;
import com.example.backend.service.interfaces.AccountService;
import com.example.backend.service.otp.OtpService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountService accountService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final CaptchaService captchaService;
    AuthenticationManager authenticationManager;

    @PostMapping("/user")
    public ResponseEntity<?> getAccountByMailOrPhone(@RequestBody FindAccountDtoRequest findAccountDtoRequest,
                                                     HttpServletResponse response) throws AccountNotFoundException {
        AccountDto account = accountService.getAccountByEmailOrPhone(findAccountDtoRequest);

        return ResponseEntity.ok(Objects.requireNonNullElse(account, HttpStatus.NOT_FOUND));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest, HttpServletResponse response) throws AccountAlreadyExistsException {
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
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletResponse response) throws AccountNotFoundException {
        try {
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
        } catch (BadCredentialsException e) {
            accountService.lockedUserAccount(loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Bad credentials");
        }
    }

    @PostMapping("/oauth")
    public ResponseEntity<?> loginWithGoogleOauth2(@RequestBody IdTokenRequest requestBody, HttpServletResponse response) throws AccountNotFoundException {
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
    public ResponseEntity<?> sendOptViaEmail(Principal principal) throws AccountNotFoundException {
        String accountEmail = principal.getName();

        int otp = otpService.generateOtp(accountEmail);

        EmailDtoRequest emailDTO = null;
        if (otp != 0) {
            emailDTO = emailService.generateOptMail(accountEmail, otp);
        }

        if (emailDTO != null) {
            if (emailService.sendSimpleMessage(emailDTO)) {
                System.out.println("OTP sent to user email");
                return ResponseEntity.ok(HttpStatus.OK);
            }
        }

        return ResponseEntity.ok(ResponseEntity.badRequest());
    }

    @PostMapping("/send/otp/sms")
    public ResponseEntity<?> sendOptViaPhone(Principal principal) throws AccountNotFoundException {
        Account account = accountService.getAccount(principal.getName());

        int otp = otpService.generateOtp(account.getEmail());

        if (smsService.sendSms(account.getPhone(), otp)) {
            return ResponseEntity.ok(HttpStatus.OK);
        }

        return ResponseEntity.ok(HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "/captcha/verify")
    public ResponseEntity<?> verifyCaptcha(@Valid @RequestBody CaptchaDataDtoRequest captchaDataDtoRequest) throws IOException, AccountNotFoundException {
        String captchaValue = captchaDataDtoRequest.getCaptchaValue();

        String resultCaptcha = captchaService.verifyCaptcha(captchaValue);

        if (Objects.equals(resultCaptcha, "{  success: false,  error-codes: [    timeout-or-duplicate  ]}")) {
            return ResponseEntity.badRequest().build();
        }
        if (Objects.equals(resultCaptcha, "{  success: false,  error-codes: [    invalid-input-response  ]}")) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(resultCaptcha);
    }

    @PostMapping(value = "/verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyTokenDtoRequest verifyTokenRequest, Principal principal) throws AccountNotFoundException {
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

    @PostMapping(value = "/complete-registration")
    public ResponseEntity<?> completeRegistration(HttpServletResponse response, HttpServletRequest request) throws AccountNotFoundException {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("AUTH-TOKEN".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    break;
                }
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/forget/password")
    public ResponseEntity<?> forgetPassword(@Valid @RequestBody FindAccountDtoRequest findAccountDtoRequest) throws AccountNotFoundException {
        if (findAccountDtoRequest.getAccountEmail() != null) {
            AccountDto account = accountService.getAccountByEmailOrPhone(findAccountDtoRequest);
            String accountEmail = account.getEmail();

            if (accountEmail != null) {
                int otp = otpService.generateOtp(accountEmail);

                EmailDtoRequest emailDTO = null;
                if (otp != 0) {
                    emailDTO = emailService.generateOptMail(accountEmail, otp);
                }

                if (emailDTO != null) {
                    if (emailService.sendSimpleMessage(emailDTO)) {
                        System.out.println("OTP sent to user email");
                        return ResponseEntity.ok(HttpStatus.OK);
                    }
                }
            }
        } else if (findAccountDtoRequest.getAccountPhone() != null) {
            AccountDto account = accountService.getAccountByEmailOrPhone(findAccountDtoRequest);
            String accountPhone = account.getPhone();

            if (accountPhone != null) {
                int otp = otpService.generateOtp(account.getEmail());

                if (smsService.sendSms(account.getPhone(), otp)) {
                    return ResponseEntity.ok(HttpStatus.OK);
                }
            }
        }

        return ResponseEntity.ok(ResponseEntity.badRequest());
    }

    @PostMapping(value = "/create/password")
    public ResponseEntity<?> createNewPassword(@Valid @RequestBody CreateNewPasswordDtoRequest createNewPasswordDtoRequest) throws AccountNotFoundException, PasswordMissmatchException {
        FindAccountDtoRequest findAccountDtoRequest = new FindAccountDtoRequest(createNewPasswordDtoRequest.getAccountEmail(), createNewPasswordDtoRequest.getAccountPhone());
        AccountDto account = accountService.getAccountByEmailOrPhone(findAccountDtoRequest);
        String email = account.getEmail();
        Integer otp = createNewPasswordDtoRequest.getOtp();

        boolean isOtpValid = otpService.validateOTP(email, otp);
        if (!isOtpValid) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        AccountDto accountDto = accountService.createNewPassword(createNewPasswordDtoRequest, account);

        if (accountDto != null) {
            return ResponseEntity.ok(HttpStatus.OK);
        }

        return ResponseEntity.ok(ResponseEntity.badRequest());
    }

    @ExceptionHandler(value = AccountAlreadyExistsException.class)
    public ResponseEntity<String> AccountAlreadyExistException(AccountAlreadyExistsException AccountAlreadyExistsException) {
        return new ResponseEntity<>("Account already exists", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = AccountNotFoundException.class)
    public ResponseEntity<String> AccountNotFoundException(AccountNotFoundException accountNotFoundException) {
        return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = PasswordMissmatchException.class)
    public ResponseEntity<String> PasswordMissmatchException(PasswordMissmatchException passwordMissmatchException) {
        return new ResponseEntity<>("Password mismatch", HttpStatus.CONFLICT);
    }
}
