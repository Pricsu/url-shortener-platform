package com.urlshortener.analytics_service.service;

import com.urlshortener.analytics_service.dto.ClickRequest;
import com.urlshortener.analytics_service.entity.ClickEvent;
import com.urlshortener.analytics_service.repository.ClickRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ClickService {

    private final ClickRepository clickRepository;

    public ClickService(ClickRepository clickRepository) {
        this.clickRepository = clickRepository;
    }

    public void recordClick(ClickRequest request){
        ClickEvent clickEvent = new ClickEvent();
        clickEvent.setShortCode(request.getShortCode());
        clickEvent.setTimestamp(LocalDateTime.now());
        clickRepository.save(clickEvent);

    }
}
