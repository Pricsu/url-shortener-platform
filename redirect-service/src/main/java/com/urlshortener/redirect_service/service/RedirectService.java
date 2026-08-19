package com.urlshortener.redirect_service.service;

import com.urlshortener.redirect_service.dto.ClickCreateRequest;
import com.urlshortener.redirect_service.dto.UrlResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RedirectService {
    private final RestClient linkRestClient;
    private final RestClient analyticRestClient;

    public RedirectService(@Qualifier("linkRestClient") RestClient linkRestClient1, @Qualifier("analyticRestClient") RestClient analyticRestClient1) {
        this.linkRestClient = linkRestClient1;
        this.analyticRestClient = analyticRestClient1;
    }


    public ResponseEntity<Void> redirect(String shortCode){
        try{
            UrlResponse urlResponse = linkRestClient.get()
                    .uri("/internal/links/{shortCode}", shortCode)
                    .retrieve()
                    .body(UrlResponse.class);
            try {
                ClickCreateRequest request = new ClickCreateRequest(shortCode);
                analyticRestClient.post()
                        .uri("/internal/clicks")
                        .body(request)
                        .retrieve()
                        .toBodilessEntity();

            } catch (Exception e) {

            }
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
