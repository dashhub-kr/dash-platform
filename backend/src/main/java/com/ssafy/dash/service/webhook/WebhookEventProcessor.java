package com.ssafy.dash.service.webhook;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.entity.WebhookEvent;
import com.ssafy.dash.repository.WebhookEventRepository;
import com.ssafy.dash.repository.OAuthTokenRepository;
import com.ssafy.dash.service.github.GitHubApiService;
import com.ssafy.dash.repository.RepoFileRepository;
import com.ssafy.dash.entity.RepoFile;

@Service
public class WebhookEventProcessor {

    private static final Logger logger = LoggerFactory.getLogger(WebhookEventProcessor.class);

    private final WebhookEventRepository webhookEventRepository;
    private final OAuthTokenRepository oauthTokenRepository;
    private final GitHubApiService gitHubApiService;
    private final RepoFileRepository repoFileRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebhookEventProcessor(WebhookEventRepository webhookEventRepository, OAuthTokenRepository oauthTokenRepository, GitHubApiService gitHubApiService, RepoFileRepository repoFileRepository) {
        this.webhookEventRepository = webhookEventRepository;
        this.oauthTokenRepository = oauthTokenRepository;
        this.gitHubApiService = gitHubApiService;
        this.repoFileRepository = repoFileRepository;
    }

    @Scheduled(fixedDelayString = "60000")
    public void processPending() {
        List<WebhookEvent> pending = webhookEventRepository.findByStatus("PENDING");
        for (WebhookEvent e : pending) {
            try {
                logger.info("Processing webhook event id={}", e.getId());
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> payload = objectMapper.readValue(e.getPayload(), java.util.Map.class);
                java.util.Map<String, Object> repo = (java.util.Map<String, Object>) payload.get("repository");
                if (repo == null) { e.setStatus("FAILED"); webhookEventRepository.save(e); continue; }
                String fullName = (String) repo.get("full_name");
                String[] parts = fullName.split("/");
                String owner = parts[0];
                String repoName = parts[1];

                com.ssafy.dash.entity.OAuthToken token = oauthTokenRepository.findByProviderAndProviderLogin("github", owner).orElse(null);
                if (token == null) {
                    continue; // 여전히 토큰 없음
                }
                String accessToken = token.getAccessToken();

                // sha와 변경된 파일 추출 (컨트롤러와 유사)
                String sha = null;
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> headCommit = (java.util.Map<String, Object>) payload.get("head_commit");
                if (headCommit != null) {
                    sha = (String) headCommit.get("id");
                }
                if (sha == null) {
                    Object after = payload.get("after");
                    if (after instanceof String) sha = (String) after;
                }

                java.util.List<String> changedFiles = new java.util.ArrayList<>();
                if (headCommit != null) {
                    Object added = headCommit.get("added");
                    Object modified = headCommit.get("modified");
                    if (added instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Object> a = (java.util.List<Object>) added;
                        for (Object o : a) if (o != null) changedFiles.add(o.toString());
                    }
                    if (modified instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Object> m = (java.util.List<Object>) modified;
                        for (Object o : m) if (o != null) changedFiles.add(o.toString());
                    }
                }

                int take = Math.min(2, changedFiles.size());
                for (int i = 0; i < take; i++) {
                    String path = changedFiles.get(i);
                    String content = gitHubApiService.getFileContent(accessToken, owner, repoName, path, sha);
                    if (content != null) {
                        RepoFile rf = new RepoFile();
                        rf.setRepoFullName(fullName);
                        rf.setPath(path);
                        rf.setContent(content);
                        rf.setFetchedAt(LocalDateTime.now());
                        repoFileRepository.save(rf);
                    }
                }

                e.setStatus("DONE");
                e.setLastAttemptAt(LocalDateTime.now());
                webhookEventRepository.save(e);
            } catch (Exception ex) {
                logger.error("Error processing webhook event id={}", e.getId(), ex);
                e.setAttempts(e.getAttempts() + 1);
                e.setLastAttemptAt(LocalDateTime.now());
                if (e.getAttempts() > 10) e.setStatus("FAILED");
                webhookEventRepository.save(e);
            }
        }
    }

}
