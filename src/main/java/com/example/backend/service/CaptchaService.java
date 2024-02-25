package com.example.backend.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class CaptchaService {

    private final String SITE_SECRET = "6LfzYX8pAAAAABESD823-rxP51fOBfMRlKQwxHrt";

    public String verifyCaptcha(String captchaValue) throws IOException {
        String verificationUrl = "https://www.google.com/recaptcha/api/siteverify?secret=" + SITE_SECRET + "&response=" + captchaValue;

        URL url = new URL(verificationUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            return "Failed to verify captcha. Response code: " + responseCode;
        }
    }
}
