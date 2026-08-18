package com.urlshortener.redirect_service.dto;

public class UrlResponse {

    private String originalUrl;

    public UrlResponse(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }
}
