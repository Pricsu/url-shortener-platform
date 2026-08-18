package com.urlshortener.redirect_service.service;

import com.urlshortener.redirect_service.dto.UrlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;



@Service
public class RedirectService {
    private final RestClient restClient;


    public RedirectService(RestClient restClient) {
        this.restClient = restClient;
    }

    public ResponseEntity<Void> redirect(String shortCode){
        try{
            UrlResponse urlResponse = restClient.get()
                    .uri("/internal/links/{shortCode}", shortCode)
                    .retrieve()
                    .body(UrlResponse.class);

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
