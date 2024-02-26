package com.example.backend.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AccountDto {
    private String email;

    private String phone;

    private String username;

    private String password;

    private String firstName;

    private String lastName;

    private String fatherName;

    private String address;

    private String zipCode;

    private String city;

    private String country;
}
