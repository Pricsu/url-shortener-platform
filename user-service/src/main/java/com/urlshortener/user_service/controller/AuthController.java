package com.urlshortener.user_service.controller;

import com.urlshortener.user_service.dto.AuthRequest;
import com.urlshortener.user_service.dto.AuthResponse;
import com.urlshortener.user_service.repository.UserRepository;
import com.urlshortener.user_service.service.AuthService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/api/auth/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request){
        return authService.login(request);
    }

    @PostMapping("/api/auth/register")
    public AuthResponse register(@Valid @RequestBody AuthRequest request){
        return authService.register(request);
    }

}
