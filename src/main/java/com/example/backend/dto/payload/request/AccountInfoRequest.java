package com.example.backend.dto.payload.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AccountInfoRequest {
    private String firstName;

    private String lastName;

    private String fatherName;

    private String address;

    private String zipCode;

    private String city;

    private String country;

    private String linkToFirstPassportPage;

    private String linkToSecondPassportPage;
}
