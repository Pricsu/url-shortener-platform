package com.urlshortener.user_service.dto;

import java.time.LocalDate;

public class ErrorResponse {
    private int status;
    private String message;
    private String timestamp;

    public ErrorResponse( int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDate.now().toString();
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }
}
