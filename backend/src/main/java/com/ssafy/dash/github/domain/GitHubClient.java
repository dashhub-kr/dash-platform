package com.ssafy.dash.github.domain;

public interface GitHubClient {

    void registerWebhook(String repositoryFullName, String accessToken);

}
