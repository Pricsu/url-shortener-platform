package com.urlshortener.analytics_service;


import com.urlshortener.analytics_service.repository.ClickRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


@SpringBootTest
@Testcontainers
public class AnalyticsServiceIT {

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:latest");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private ClickRepository clickRepository;

    @Autowired
    private KafkaTemplate<String , String> kafkaTemplate;

    @Test
    void kafkaMessage_getsSavedAsClickEvent() throws InterruptedException {
        Thread.sleep(2000);
        kafkaTemplate.send("link-clicks", "somecode");

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    assertThat(clickRepository.findAll()).hasSize(1);
                });
    }
}
