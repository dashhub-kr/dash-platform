package com.ssafy.dash.github.domain;

import java.util.List;

public interface GitHubClient {

    void registerWebhook(String repositoryFullName, String accessToken);

    String fetchFileContent(String repositoryFullName, String filePath, String reference, String accessToken);

    /**
     * 사용자의 GitHub 저장소 목록 조회
     */
    List<RepositoryInfo> listUserRepositories(String accessToken);

}
