package com.urlshortener.analytics_service.controller;

import com.urlshortener.analytics_service.dto.ClickRequest;
import com.urlshortener.analytics_service.service.ClickService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClickController {

    private final ClickService clickService;


    public ClickController(ClickService clickService) {
        this.clickService = clickService;
    }

    @PostMapping("/internal/clicks")
    public ResponseEntity<Void> recordClick(@RequestBody ClickRequest request){
        clickService.recordClick(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }
}
