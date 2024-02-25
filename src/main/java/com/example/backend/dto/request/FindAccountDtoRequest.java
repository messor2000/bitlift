package com.example.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindAccountDtoRequest {
    @JsonIgnore
    private String accountEmail;

    @JsonIgnore
    private String accountPhone;
}