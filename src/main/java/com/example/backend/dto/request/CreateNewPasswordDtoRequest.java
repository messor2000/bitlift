package com.example.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateNewPasswordDtoRequest {
    @JsonProperty("accountEmail")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String accountEmail;

    @JsonProperty("accountPhone")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String accountPhone;

    @NotNull
    private Integer otp;

    @NotNull
    private String newPassword;

    @NotNull
    private String confirmNewPassword;
}
