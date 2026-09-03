package com.urlshortener.analytics_service;

import com.urlshortener.analytics_service.controller.ClickEventListener;
import com.urlshortener.analytics_service.service.ClickService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class ClickListenerTest {

    @Mock
    private ClickService clickService;

    private ClickEventListener clickListener;

    @BeforeEach
    void set_up(){
        clickListener = new ClickEventListener(clickService);
    }

    @Test
    void handleClick_delegatesToClickService(){
        clickListener.handleClick("somecode");
        verify(clickService).recordClick("somecode");
    }
}
