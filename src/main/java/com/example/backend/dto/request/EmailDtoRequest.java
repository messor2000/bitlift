package com.example.backend.dto.request;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailDtoRequest {
    private List<String> recipients;
    private List<String> ccList;
    private List<String> bccList;
    private String subject;
    private String body;
    private Boolean isHtml;
    private String attachmentPath;
}
