package com.ssafy.dash.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * RestClient 설정
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(
            @Value("${solvedac.api.timeout:10000}") int timeout) {
        return RestClient.builder()
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
