package com.urlshortener.redirect_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

//  We don't use hardcoded urls
    @Value("${app.link-service.base-url}")
    private String baseUrl;

    @Bean
    public RestClient myRestClient(RestClient.Builder builder){
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
