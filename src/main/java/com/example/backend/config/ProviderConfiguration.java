package com.example.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "email")
public class ProviderConfiguration {

    @Value("${email.host}")
    private String host;
    @Value("${email.port}")
    private Integer port;
    @Value("${email.username}")
    private String username;
    @Value("${email.password}")
    private String password;
    private Boolean debug;
}

