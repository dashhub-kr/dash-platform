package com.ssafy.dash.youtube.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "youtube")
public class YouTubeProperties {

    private final String apiKey;

    @ConstructorBinding
    public YouTubeProperties(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }
}
