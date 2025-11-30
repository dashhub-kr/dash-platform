package com.ssafy.dash.github.application;

import org.springframework.stereotype.Service;

import com.ssafy.dash.github.domain.GitHubClient;

@Service
public class GitHubWebhookService {

    private final GitHubClient gitHubClient;

    public GitHubWebhookService(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    public void ensureWebhook(String repositoryFullName, String accessToken) {
        gitHubClient.registerWebhook(repositoryFullName, accessToken);
    }
}
