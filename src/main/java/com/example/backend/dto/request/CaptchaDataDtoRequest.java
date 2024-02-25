package com.example.backend.dto.request;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaDataDtoRequest {
    private String captchaValue;
}
