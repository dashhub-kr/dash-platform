package com.ssafy.dash.common.health;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ssafy.dash.github.config.GitHubWebhookProperties;

@Component("githubWebhook")
public class GitHubWebhookHealthIndicator implements HealthIndicator {

    private final GitHubWebhookProperties properties;

    public GitHubWebhookHealthIndicator(GitHubWebhookProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        List<String> missing = new ArrayList<>();
        if (!StringUtils.hasText(properties.getCallbackUrl())) {
            missing.add("callbackUrl");
        }
        if (!StringUtils.hasText(properties.getSecret())) {
            missing.add("secret");
        }
        if (!StringUtils.hasText(properties.getEvents())) {
            missing.add("events");
        }

        if (!missing.isEmpty()) {
            return Health.down()
                    .withDetail("missing", missing)
                    .build();
        }

        return Health.up()
                .withDetail("events", properties.getEvents())
                .build();
    }
    
}
