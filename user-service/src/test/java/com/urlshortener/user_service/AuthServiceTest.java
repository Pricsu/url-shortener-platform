package com.urlshortener.user_service;

import com.urlshortener.user_service.dto.AuthRequest;
import com.urlshortener.user_service.dto.AuthResponse;
import com.urlshortener.user_service.entity.User;
import com.urlshortener.user_service.repository.UserRepository;
import com.urlshortener.user_service.security.JwtUtil;
import com.urlshortener.user_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    private AuthService authService;

    @BeforeEach
    void setUp(){
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void login_userNotFound(){
        AuthRequest request = new AuthRequest();
        request.setUsername("Alfred");
        when(userRepository.findByUsername("Alfred")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void login_passwordsNotMatch(){
        AuthRequest request = new AuthRequest();
        request.setUsername("user");

        request.setPasswordRaw("pass1");

        User user = new User();
        user.setPasswordHash("pass2");

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass1", "pass2")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> authService.login(request));

    }

    @Test
    void login_successful(){
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        request.setEmail("elekes@gmail.com");
        request.setPasswordRaw("pass1");
        User user = new User();
        user.setPasswordHash("pass2");
        user.setUsername("user");
        user.setId(1L);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass1", "pass2")).thenReturn(true);

        AuthResponse response = authService.login(request);

        verify(jwtUtil).generateToken("user", 1L);
        assertThat(response.getUsername()).isEqualTo("user");
    }

    @Test
    void register_userExist(){
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(new User()));
        assertThrows(IllegalArgumentException.class, () -> authService.register(request));

    }
    @Test
    void register_successful(){
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        request.setPasswordRaw("oke");
        request.setEmail("elekes@gmail.com");

        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("oke")).thenReturn("encode");

        AuthResponse response = authService.register(request);

        assertThat(response.getUsername()).isEqualTo("user");

        verify(jwtUtil).generateToken(eq("user"), any());
        verify(userRepository).save(any(User.class));
    }
}
