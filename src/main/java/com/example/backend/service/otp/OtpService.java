package com.example.backend.service.otp;

import com.example.backend.dto.request.EmailDtoRequest;
import com.example.backend.entity.Account;
import com.example.backend.service.AccountService;
import com.example.backend.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Description(value = "Service responsible for handling OTP related functionality.")
@Service
public class OtpService {

    private final Logger LOGGER = LoggerFactory.getLogger(OtpService.class);

    private final OtpGenerator otpGenerator;
    private final EmailService emailService;
    private final AccountService accountService;

    public OtpService(OtpGenerator otpGenerator, EmailService emailService, AccountService accountService) {
        this.otpGenerator = otpGenerator;
        this.emailService = emailService;
        this.accountService = accountService;
    }

//    /**
//     * Method for generate OTP number
//     *
//     * @param key - provided key (username in this case)
//     * @return boolean value (true|false)
//     */
//    public Boolean generateOtp(String key) {
//        // generate otp
//        Integer otpValue = otpGenerator.generateOTP(key);
//        if (otpValue == -1) {
//            LOGGER.error("OTP generator is not working...");
//            return false;
//        }
//
//        LOGGER.info("Generated OTP: {}", otpValue);
//
//        // fetch user e-mail from database
//        Account account = accountService.getAccount(key);
//        String userEmail = account.getEmail();
//        List<String> recipients = new ArrayList<>();
//        recipients.add(userEmail);
//
//        // generate emailDTO object
//        EmailDtoRequest emailDTO = new EmailDtoRequest();
//        emailDTO.setSubject("Spring Boot OTP Password.");
//        emailDTO.setBody("OTP Password: " + otpValue);
//        emailDTO.setRecipients(recipients);
//
//        // send generated e-mail
//        return emailService.sendSimpleMessage(emailDTO);
//    }

    public int generateOtp(String key) {
        Integer otpValue = otpGenerator.generateOTP(key);
        if (otpValue == -1) {
            LOGGER.error("OTP generator is not working...");
            return 0;
        }

        LOGGER.info("Generated OTP: {}", otpValue);

        // send generated e-mail
        return otpValue;
    }


    public Boolean validateOTP(String key, Integer otpNumber) {
        Integer cacheOTP = otpGenerator.getOPTByKey(key);
        if (cacheOTP != null && cacheOTP.equals(otpNumber)) {
            otpGenerator.clearOTPFromCache(key);
            return true;
        }
        return false;
    }
}