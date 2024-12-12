package com.example.spring.bzsellerservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UrlResponseDTO {
    private String url;
    private String message;
}
