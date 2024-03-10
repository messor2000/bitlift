package com.example.backend.dto.request;

import lombok.*;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChangeAccountVerificationStatusDtoRequest {
    private String accountEmail;
    private boolean verificationStatus;
}
