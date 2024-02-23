package com.example.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    private String linkToFirstPassportPage;

    @JsonIgnore
    private String linkToSecondPassportPage;
}
