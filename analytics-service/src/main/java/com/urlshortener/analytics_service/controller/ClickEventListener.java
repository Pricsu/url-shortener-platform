package com.urlshortener.analytics_service.controller;

import com.urlshortener.analytics_service.service.ClickService;
import jakarta.persistence.Column;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ClickEventListener {

    private final ClickService clickService;


    public ClickEventListener(ClickService clickService) {
        this.clickService = clickService;
    }

    @KafkaListener(topics = "link-clicks", groupId = "analytics-service-group")
    public void handleClick(String shortCode){
        clickService.recordClick(shortCode);
    }
}
