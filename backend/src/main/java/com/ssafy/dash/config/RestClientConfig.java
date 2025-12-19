package com.ssafy.dash.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

/**
 * HTTP Client 설정
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        // JDK 11+ HttpClient 사용 (Modern RestClient 방식)
        // 단, FastAPI(Uvicorn)와의 h2c 업그레이드 호환성 문제를 피하기 위해 HTTP/1.1 강제 설정
        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();

        return builder
                .requestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory(httpClient))
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .messageConverters(converters -> {
                    converters.clear();
                    converters.add(new MappingJackson2HttpMessageConverter());
                })
                .build();
    }
}
