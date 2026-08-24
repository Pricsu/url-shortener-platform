package com.urlshortener.redirect_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

//  We don't use hardcoded urls
    @Value("${app.link-service.base-url}")
    private String linkServiceUrl;

    @Value("${app.analytic-service.base-url}")
    private String analyticServiceUrl;

    @Bean
    public RestClient linkRestClient(RestClient.Builder builder){
        return builder
                .baseUrl(linkServiceUrl)
                .build();
    }

//    @Bean
//    public RestClient analyticRestClient(RestClient.Builder builder){
//        return builder
//                .baseUrl(analyticServiceUrl)
//                .build();
//    }
}
