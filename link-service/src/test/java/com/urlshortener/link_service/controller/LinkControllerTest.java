package com.urlshortener.link_service.controller;

import com.urlshortener.link_service.dto.LinkRequest;
import com.urlshortener.link_service.dto.LinkResponse;
import com.urlshortener.link_service.security.JwtUtil;
import com.urlshortener.link_service.service.LinkService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LinkController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LinkControllerTest {

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LinkService linkService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void create_returnsCreatedLink() throws Exception {
        LinkRequest request = new LinkRequest();
        request.setOriginalUrl("oko@gmail.com");
        request.setClickLimit(5L);

        String jsonLoad = objectMapper.writeValueAsString(request);

        LinkResponse response = new LinkResponse("somecode", "oko@gmail.com", 5L, LocalDateTime.now());
        when(linkService.create(any(LinkRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/links").contentType(MediaType.APPLICATION_JSON)
                .content(jsonLoad))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("somecode"))
                .andExpect(jsonPath("$.originalUrl").value("oko@gmail.com"));

    }

    @Test
    @WithMockUser
    void create_rejectBlankUrl() throws Exception {
        LinkRequest request = new LinkRequest();
        request.setOriginalUrl("");

        String jsonLoad = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/links").contentType(MediaType.APPLICATION_JSON)
                .content(jsonLoad))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void delete_returnsNoContent() throws Exception{
        mockMvc.perform(delete("/api/links/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void delete_returnsForbiddenWhenAccessDenied() throws Exception{
        doThrow(new AccessDeniedException("")).when(linkService).deleteLink(any());
        mockMvc.perform(delete("/api/links/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void findLinks_returnList() throws Exception{
        LinkResponse response = new LinkResponse("somecode", "oko@gmail.com", 5L, LocalDateTime.now());

        when(linkService.findMyLinks()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/links")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortCode").value("somecode"))
                .andExpect(jsonPath("$[0].originalUrl").value("oko@gmail.com"));
    }
}
