package com.urlshortener.link_service.dto;

import com.urlshortener.link_service.entity.Link;

import java.time.LocalDateTime;

public class LinkResponse {

    private String shortCode;
    private String originalUrl;
    private Long ownerId;
    private LocalDateTime createdAt;

    public LinkResponse(String shortCode, String originalUrl, Long ownerId, LocalDateTime createdAt) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }

    public static LinkResponse fromEntity(Link link){
         return new LinkResponse(
                 link.getShortCode(),
                 link.getOriginalUrl(),
                 link.getOwnerId(),
                 link.getCreatedAt()
         );
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
