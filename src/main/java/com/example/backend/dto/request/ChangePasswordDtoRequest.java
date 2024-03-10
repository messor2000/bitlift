package com.example.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDtoRequest {
    @NotNull
    private String oldPassword;
    @NotNull
    private String newPassword;

    @NotNull
    private String confirmNewPassword;
}
