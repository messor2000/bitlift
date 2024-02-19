package com.example.backend.service;

import com.example.backend.dto.request.EmailDtoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender emailSender;

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


}
