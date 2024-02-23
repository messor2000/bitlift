package com.example.backend.service;

import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class SmsService {
    @Value("${twilio.account.sid}")
    private String sid;
    @Value("${twilio.auth.token}")
    private String token;

    public void sendSms(String phoneNumber, int opt) {
//        Twilio.init(System.getenv("TWILIO_ACCOUNT_SID"), System.getenv("TWILIO_AUTH_TOKEN"));
        // TODO: do not push code until i remove all tokens from properties
        Twilio.init(sid, token);

        Message.creator(new PhoneNumber("<TO number - " + phoneNumber + ">"),
                new PhoneNumber("<FROM number - +380980528270>"), "Code: " + opt).create();
    }
}
