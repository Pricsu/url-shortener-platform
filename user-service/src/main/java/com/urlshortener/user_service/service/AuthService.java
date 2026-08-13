package com.urlshortener.user_service.service;

import com.urlshortener.user_service.dto.AuthRequest;
import com.urlshortener.user_service.dto.AuthResponse;
import com.urlshortener.user_service.entity.User;
import com.urlshortener.user_service.repository.UserRepository;
import com.urlshortener.user_service.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(AuthRequest request){

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new  IllegalArgumentException("Incorrect Username or Password"));
        if (!passwordEncoder.matches(request.getPasswordRaw(), user.getPasswordHash())){
            throw new IllegalArgumentException("Incorrect Username or Password");
        }

        String token = jwtUtil.generateToken(request.getUsername());
        return new AuthResponse(request.getUsername(), token);
    }

    public AuthResponse register(AuthRequest request){

        if (userRepository.findByUsername(request.getUsername()).isPresent()){
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPasswordRaw()));
        user.setEmail(request.getEmail());

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(user.getUsername(), token);
    }
}
