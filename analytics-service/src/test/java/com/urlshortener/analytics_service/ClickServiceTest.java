package com.urlshortener.analytics_service;

import com.urlshortener.analytics_service.entity.ClickEvent;
import com.urlshortener.analytics_service.repository.ClickRepository;
import com.urlshortener.analytics_service.service.ClickService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClickServiceTest {

    private ClickService clickService;

    @Mock
    private ClickRepository clickRepository;

    @BeforeEach
    void setUp(){
        clickService =new ClickService(clickRepository);
    }

    @Test
    void record_success(){

        clickService.recordClick("somecode");

        ArgumentCaptor<ClickEvent> captor = ArgumentCaptor.forClass(ClickEvent.class);
        verify(clickRepository).save(captor.capture());
        ClickEvent savedClick = captor.getValue();

        assertThat(savedClick.getShortCode()).isEqualTo("somecode");
        assertThat(savedClick.getTimestamp()).isNotNull();
    }
}
