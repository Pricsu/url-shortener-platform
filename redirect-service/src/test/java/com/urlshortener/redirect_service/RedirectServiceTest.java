package com.urlshortener.redirect_service;

import com.urlshortener.redirect_service.dto.UrlResponse;
import com.urlshortener.redirect_service.service.RedirectService;
import io.micrometer.common.annotation.ValueExpressionResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestClient;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedirectServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private RedirectService redirectService;

    @BeforeEach
    void setUp(){
        redirectService = new RedirectService(restClient, kafkaTemplate);
    }

    @Test
    void redirect_successful(){
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(UrlResponse.class)).thenReturn(new UrlResponse("oko.com"));

        ResponseEntity<Void> response = redirectService.redirect("somecode");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo("oko.com");

        verify(kafkaTemplate).send("link-clicks","somecode");
    }

    @Test
    void redirect_LinkServiceFails(){
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(UrlResponse.class)).thenThrow(new RuntimeException(""));

        ResponseEntity<Void>response = redirectService.redirect("somecode");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(kafkaTemplate, never()).send("link-clicks", "somecode");

    }
}
