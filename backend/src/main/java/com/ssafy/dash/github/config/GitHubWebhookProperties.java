package com.ssafy.dash.github.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "github.webhook")
public class GitHubWebhookProperties {

    private String callbackUrl;
    private String secret;
    private String events;

}
