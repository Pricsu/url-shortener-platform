package com.urlshortener.redirect_service.service;

import com.urlshortener.redirect_service.dto.UrlResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RedirectService {
    private final RestClient linkRestClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public RedirectService(@Qualifier("linkRestClient") RestClient linkRestClient1, KafkaTemplate<String, String> kafkaTemplate){
        this.linkRestClient = linkRestClient1;
        this.kafkaTemplate = kafkaTemplate;
    }


    public ResponseEntity<Void> redirect(String shortCode){
        try{
            UrlResponse urlResponse = linkRestClient.get()
                    .uri("/internal/links/{shortCode}", shortCode)
                    .retrieve()
                    .body(UrlResponse.class);

            kafkaTemplate.send("link-clicks", shortCode);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, urlResponse.getOriginalUrl())
                    .build();
        }catch (Exception ex){
//            Treats both "short code not found" and "Link Service unreachable" as 404 for now
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }
}
