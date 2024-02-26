package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
