package com.ssafy.dash.youtube.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(YouTubeProperties.class)
public class YouTubeConfig {
}
