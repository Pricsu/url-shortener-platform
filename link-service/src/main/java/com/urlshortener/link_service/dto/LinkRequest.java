package com.urlshortener.link_service.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class LinkRequest {
    @NotBlank
    private String originalUrl;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}
