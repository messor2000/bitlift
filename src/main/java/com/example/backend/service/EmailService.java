package com.example.backend.service;

import com.example.backend.dto.request.EmailDtoRequest;
import com.example.backend.entity.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmailService {

    private final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private AccountService accountService;

    /**
     * Method for sending simple e-mail message.
     *
     * @param emailDTO - data to be send.
     */
    public Boolean sendSimpleMessage(EmailDtoRequest emailDTO) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(String.join(",", emailDTO.getRecipients()));
        mailMessage.setSubject(emailDTO.getSubject());
        mailMessage.setText(emailDTO.getBody());

        boolean isSent = false;
        try {
            emailSender.send(mailMessage);
            isSent = true;
        } catch (Exception e) {
            LOGGER.error("Sending e-mail error: {}", e.getMessage());
        }
        return isSent;
    }

    public EmailDtoRequest generateOptMail(String key, int otpValue) {
        Account account = accountService.getAccount(key);
        String userEmail = account.getEmail();
        List<String> recipients = new ArrayList<>();
        recipients.add(userEmail);

        EmailDtoRequest emailDTO = new EmailDtoRequest();
        emailDTO.setSubject("Spring Boot OTP Password.");
        emailDTO.setBody("OTP Password: " + otpValue);
        emailDTO.setRecipients(recipients);

        return emailDTO;
    }

}
