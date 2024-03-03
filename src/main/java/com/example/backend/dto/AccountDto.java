package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class AccountDto {
    private String email;

    private String phone;

    private String username;

    private String firstName;

    private String lastName;

    private String fatherName;

    private String address;

    private String zipCode;

    private String city;

    private String country;

    private String documentCountry;
}
