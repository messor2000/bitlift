package com.example.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerifyTokenDtoRequest {

    @NotNull
    private String email;

    @NotNull
    private Integer otp;

    private Boolean rememberMe;
}
