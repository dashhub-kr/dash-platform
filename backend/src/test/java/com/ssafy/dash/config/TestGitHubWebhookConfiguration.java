package com.ssafy.dash.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.ssafy.dash.onboarding.github.GitHubWebhookService;

@Configuration
public class TestGitHubWebhookConfiguration {

    @Bean
    @Primary
    public GitHubWebhookService gitHubWebhookServiceStub() {
        return Mockito.mock(GitHubWebhookService.class);
    }
    
}
