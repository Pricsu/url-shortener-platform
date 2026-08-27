package com.urlshortener.link_service.dto;

import com.urlshortener.link_service.entity.Link;

public class UrlResponse {

    private String originalUrl;

    public UrlResponse(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public static UrlResponse fromEntity(Link link){
        return new UrlResponse(link.getOriginalUrl());
    }


    public String getOriginalUrl() {
        return originalUrl;
    }
}
