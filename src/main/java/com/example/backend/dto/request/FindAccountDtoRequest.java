package com.example.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindAccountDtoRequest {
    @JsonProperty("accountEmail")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String accountEmail;

    @JsonProperty("accountPhone")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String accountPhone;
}