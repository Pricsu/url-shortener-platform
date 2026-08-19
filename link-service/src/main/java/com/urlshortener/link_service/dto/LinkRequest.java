package com.urlshortener.link_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;



public class LinkRequest {
    @NotBlank
    private String originalUrl;

    private Long clickLimit;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public Long getClickLimit() {
        return clickLimit;
    }

    public void setClickLimit(Long clickLimit) {
        this.clickLimit = clickLimit;
    }
}
