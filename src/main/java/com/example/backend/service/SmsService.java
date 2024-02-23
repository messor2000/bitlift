package com.example.backend.service;

import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.twilio.rest.api.v2010.account.Message;

@Service
public class SmsService {
    @Value("${twilio.account.sid}")
    private String sid;
    @Value("${twilio.auth.token}")
    private String token;

    public void sendSms(String phoneNumber, int opt) {
        // TODO: do not push code until i remove all tokens from properties
        Twilio.init(sid, token);

        Message message = Message.creator(
                        new com.twilio.type.PhoneNumber(phoneNumber),
                        new com.twilio.type.PhoneNumber("+1 816 873 8499"),
                        "Code: " + opt)
                .create();

        System.out.println(message.getSid());
    }
}
