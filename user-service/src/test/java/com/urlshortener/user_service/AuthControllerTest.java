package com.urlshortener.user_service;

import com.urlshortener.user_service.controller.AuthController;
import com.urlshortener.user_service.dto.AuthRequest;
import com.urlshortener.user_service.dto.AuthResponse;
import com.urlshortener.user_service.security.JwtUtil;
import com.urlshortener.user_service.service.AuthService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void register_success() throws Exception{
        AuthRequest request = new AuthRequest();
        request.setUsername("Alfred");
        request.setPasswordRaw("oko");
        request.setEmail("oko@gmail.com");


        when(authService.register(any(AuthRequest.class))).thenReturn(new AuthResponse("Alfred", "token"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Alfred"));
    }

    @Test
    @WithMockUser
    void register_rejectsBlankUsername() throws Exception{
        AuthRequest request = new AuthRequest();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @WithMockUser
    void login_success() throws Exception{
        AuthRequest request = new AuthRequest();
        request.setUsername("Alfred");
        request.setPasswordRaw("oko");

        when(authService.login(any(AuthRequest.class))).thenReturn(new AuthResponse("Alfred", "token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Alfred"));
    }

    @Test
    @WithMockUser
    void login_rejectsBlankUsername() throws Exception{
        AuthRequest request = new AuthRequest();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void login_returnsBadRequestOnWrongCredentials() throws Exception{
        AuthRequest request = new AuthRequest();
        request.setUsername("Alfred");
        request.setPasswordRaw("oko");

        when(authService.login(any(AuthRequest.class))).thenThrow(new IllegalArgumentException(""));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
