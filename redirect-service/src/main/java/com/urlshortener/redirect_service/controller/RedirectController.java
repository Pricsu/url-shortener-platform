package com.urlshortener.redirect_service.controller;

import com.urlshortener.redirect_service.service.RedirectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {
    
    private final RedirectService redirectService;


    public RedirectController(RedirectService redirectService) {
        this.redirectService = redirectService;
    }
    
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode){
        return redirectService.redirect(shortCode);
    } 
}
