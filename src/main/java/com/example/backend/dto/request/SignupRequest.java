package com.example.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Data
@Getter
@Setter
public class SignupRequest {
  @NotBlank
  @Size(min = 3, max = 20)
  private String username;

  @NotBlank
  @Size(max = 50)
  @Email
  private String email;

  private String phone;

  private String referralCode;

  @NotBlank
//  @Size(min = 6, max = 40)
  private String password;
}
