package com.ssafy.dash.github.domain;

public interface GitHubClient {

    void registerWebhook(String repositoryFullName, String accessToken);

    String fetchFileContent(String repositoryFullName, String filePath, String reference, String accessToken);

}
