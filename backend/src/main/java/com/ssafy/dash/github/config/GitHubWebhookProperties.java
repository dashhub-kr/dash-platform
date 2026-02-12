package com.ssafy.dash.github.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "github.webhook")
public class GitHubWebhookProperties {

    private String callbackUrl;
    private String secret;
    private String events;

}
