package com.ssafy.dash.controller;

import java.time.LocalDateTime;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.entity.WebhookEvent;
import com.ssafy.dash.repository.WebhookEventRepository;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.entity.RepoFile;
import com.ssafy.dash.repository.RepoFileRepository;
import com.ssafy.dash.service.github.GitHubApiService;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private final GitHubApiService gitHubApiService;
    private final RepoFileRepository repoFileRepository;
    private final com.ssafy.dash.repository.OAuthTokenRepository oauthTokenRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String computeHmacSHA256(String payload, String secret) throws Exception {

        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        
        return sb.toString();
    }

    @Value("${github.webhook.secret:}")
    private String webhookSecret;

    public WebhookController(GitHubApiService gitHubApiService, RepoFileRepository repoFileRepository, com.ssafy.dash.repository.OAuthTokenRepository oauthTokenRepository, WebhookEventRepository webhookEventRepository) {

        this.gitHubApiService = gitHubApiService;
        this.repoFileRepository = repoFileRepository;
        this.oauthTokenRepository = oauthTokenRepository;
        this.webhookEventRepository = webhookEventRepository;

    }

    @PostMapping("/github")
    public ResponseEntity<String> handleGithubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String event,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String sig256,
            @RequestBody String payloadRaw) {

        // 시크릿이 설정된 경우 서명 확인
        if (webhookSecret != null && !webhookSecret.isBlank()) {
            try {
                String expected = "sha256=" + computeHmacSHA256(payloadRaw, webhookSecret);
                if (sig256 == null || !MessageDigest.isEqual(expected.getBytes(), sig256.getBytes())) {

                    return new ResponseEntity<>("invalid signature", HttpStatus.UNAUTHORIZED);
                }
            } catch (Exception ex) {

                return new ResponseEntity<>("signature verification failed", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        Map<String, Object> payload;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> tmp = objectMapper.readValue(payloadRaw, Map.class);
            payload = tmp;
        } catch (Exception e) {
            
            return new ResponseEntity<>("invalid json", HttpStatus.BAD_REQUEST);
        }

        if (payload == null || payload.isEmpty()) return new ResponseEntity<>("no payload", HttpStatus.BAD_REQUEST);

        @SuppressWarnings("unchecked")
        Map<String, Object> repo = (Map<String, Object>) payload.get("repository");
        if (repo == null) return new ResponseEntity<>("no repository", HttpStatus.BAD_REQUEST);

        String fullName = (String) repo.get("full_name"); // owner/repo
        if (!StringUtils.hasText(fullName)) return new ResponseEntity<>("no full_name", HttpStatus.BAD_REQUEST);

        String[] parts = fullName.split("/");
        String owner = parts[0];
        String repoName = parts[1];

        // 액세스 토큰: 이 리포지토리 소유자/사용자에 대해 저장된 토큰 조회
        String accessToken = null;
        com.ssafy.dash.entity.OAuthToken token = oauthTokenRepository.findByProviderAndProviderLogin("github", owner).orElse(null);
        if (token != null) accessToken = token.getAccessToken();

        // 토큰이 없으면 이벤트를 비동기 재시도를 위해 저장하고 수락 응답 반환
        if (!StringUtils.hasText(accessToken)) {
            WebhookEvent ev = new WebhookEvent();
            ev.setEventType(event);
            ev.setPayload(payloadRaw);
            ev.setStatus("PENDING");
            ev.setAttempts(0);
            ev.setCreatedAt(LocalDateTime.now());
            webhookEventRepository.save(ev);

            return new ResponseEntity<>("no token available for fetching files; queued", HttpStatus.ACCEPTED);
        }

        // 커밋 SHA 및 변경된 파일을 페이로드에서 결정. head_commit 우선, 없으면 commits[0] 사용
        String sha = null;
        @SuppressWarnings("unchecked")
        Map<String, Object> headCommit = (Map<String, Object>) payload.get("head_commit");
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
        else {
            Object commitsObj = payload.get("commits");
            if (commitsObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> commits = (java.util.List<Object>) commitsObj;
                if (!commits.isEmpty()) {
                    Object first = commits.get(0);
                    if (first instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> firstCommit = (java.util.Map<String, Object>) first;
                        Object added = firstCommit.get("added");
                        Object modified = firstCommit.get("modified");
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
                }
            }
        }

        // 사용자의 계약에 따라 정확히 두 개의 파일을 기대함
        // 변경된 목록에서 최대 2개의 파일 선택
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

        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

}
