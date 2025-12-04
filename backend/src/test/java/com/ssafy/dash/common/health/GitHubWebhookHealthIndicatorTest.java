package com.ssafy.dash.common.health;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;

import com.ssafy.dash.github.config.GitHubWebhookProperties;

@DisplayName("GitHubWebhookHealthIndicator 테스트")
class GitHubWebhookHealthIndicatorTest {

    @Test
    @DisplayName("필수 속성이 모두 존재하면 UP 상태를 반환한다")
    void healthUpWhenPropertiesPresent() {
        GitHubWebhookProperties properties = new GitHubWebhookProperties();
        properties.setCallbackUrl("https://example.com/webhook");
        properties.setSecret("secret");
        properties.setEvents("push");

        GitHubWebhookHealthIndicator indicator = new GitHubWebhookHealthIndicator(properties);

        Health health = indicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("UP");
        assertThat(health.getDetails()).containsEntry("events", "push");
    }

    @Test
    @DisplayName("필수 속성이 비어 있으면 DOWN 상태와 누락 정보를 반환한다")
    void healthDownWhenPropertiesMissing() {
        GitHubWebhookProperties properties = new GitHubWebhookProperties();
        properties.setEvents("push");

        GitHubWebhookHealthIndicator indicator = new GitHubWebhookHealthIndicator(properties);

        Health health = indicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("DOWN");
        assertThat(health.getDetails()).containsKey("missing");
    }
    
}
