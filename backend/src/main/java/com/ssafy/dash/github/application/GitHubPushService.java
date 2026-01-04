package com.ssafy.dash.github.application;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.github.domain.GitHubPushEvent;
import com.ssafy.dash.github.domain.GitHubPushEventRepository;
import com.ssafy.dash.github.domain.exception.GitHubPayloadProcessingException;

@Service
public class GitHubPushService {

    private static final Logger log = LoggerFactory.getLogger(GitHubPushService.class);

    private final ObjectMapper objectMapper;
    private final GitHubPushEventRepository pushEventRepository;
    private final com.ssafy.dash.defense.application.DefenseService defenseService;
    private final com.ssafy.dash.onboarding.domain.OnboardingRepository onboardingRepository;

    public GitHubPushService(ObjectMapper objectMapper, 
                             GitHubPushEventRepository pushEventRepository,
                             com.ssafy.dash.defense.application.DefenseService defenseService,
                             com.ssafy.dash.onboarding.domain.OnboardingRepository onboardingRepository) {
        this.objectMapper = objectMapper;
        this.pushEventRepository = pushEventRepository;
        this.defenseService = defenseService;
        this.onboardingRepository = onboardingRepository;
    }

    @Transactional
    public void handlePushEvent(String deliveryId, String payload) {
        Optional<GitHubPushEvent> existing = pushEventRepository.findByDeliveryId(deliveryId);
        if (existing.isPresent()) {
            log.info("Duplicate delivery id {} ignored", deliveryId);
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            
            // 이벤트 생성을 위한 정보 추출
            JsonNode headCommit = root.path("head_commit");
            String message = headCommit.path("message").asText("");
            
            if (!message.endsWith(" - DashHub")) {
                log.info("Ignoring non-DashHub commit. DeliveryId: {}", deliveryId);
                return;
            }

            String repositoryName = root.path("repository").path("full_name").asText("unknown/unknown");
            List<PushFileMetadata> files = collectTargetFiles(root.path("commits"));
            
            GitHubPushEvent event = buildEvent(deliveryId, root, payload, files, repositoryName);
            pushEventRepository.save(event);
            
            // 랜덤 디펜스 검증
            verifyDefenseAttempt(repositoryName, files);
            
        } catch (JsonProcessingException ex) {
            throw new GitHubPayloadProcessingException("GitHub push payload 파싱에 실패했습니다.", ex);
        }
    }
    
    private void verifyDefenseAttempt(String repositoryName, List<PushFileMetadata> files) {
        onboardingRepository.findByRepositoryName(repositoryName).ifPresent(onboarding -> {
            Long userId = onboarding.getUserId();
            for (PushFileMetadata file : files) {
                extractProblemId(file.path()).ifPresent(problemId -> {
                    defenseService.verifyDefense(userId, problemId);
                });
            }
        });
    }

    private Optional<Integer> extractProblemId(String path) {
        // 파일명에서 마지막 숫자 시퀀스를 찾기 위한 간단한 정규식 (디렉토리 무시)
        // 예: "src/BOJ_1234.java" -> 1234
        // "1000.py" -> 1000
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(path);
            String lastMatch = null;
            while (matcher.find()) {
                lastMatch = matcher.group(1);
            }
            return lastMatch != null ? Optional.of(Integer.parseInt(lastMatch)) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private GitHubPushEvent buildEvent(String deliveryId, JsonNode root, String payload, List<PushFileMetadata> files, String repositoryName) {
        String reference = root.path("ref").asText("refs/heads/main");
        JsonNode headCommit = root.path("head_commit");
        String headSha = headCommit.path("id").asText(root.path("after").asText());
        String authorLogin = headCommit.path("author").path("username").asText(root.path("pusher").path("name").asText());
        String authorEmail = headCommit.path("author").path("email").asText();
        String commitMessage = headCommit.path("message").asText();

        String filesJson = writeFilesJson(files);

        LocalDateTime receivedAt = LocalDateTime.now();
        return GitHubPushEvent.enqueue(deliveryId, repositoryName, reference, headSha, blankToNull(authorLogin), blankToNull(authorEmail), blankToNull(commitMessage), filesJson, payload, receivedAt);
    }

    private List<PushFileMetadata> collectTargetFiles(JsonNode commits) {
        List<PushFileMetadata> files = new ArrayList<>();
        if (commits == null || !commits.isArray()) {
            return files;
        }
        for (JsonNode commit : commits) {
            String sha = commit.path("id").asText();
            String message = commit.path("message").asText();
            LocalDateTime committedAt = parseTimestamp(commit.path("timestamp").asText());
            String committedAtText = formatTimestamp(committedAt);
            collectByStatus(files, commit.path("added"), sha, message, committedAtText, "added");
            collectByStatus(files, commit.path("modified"), sha, message, committedAtText, "modified");
        }
        return files;
    }

    private void collectByStatus(List<PushFileMetadata> files,
            JsonNode paths,
            String sha,
            String message,
            String committedAt,
            String status) {
        if (paths == null || !paths.isArray()) {
            return;
        }
        for (JsonNode pathNode : paths) {
            String path = pathNode.asText();
            if (isAlgorithmFile(path)) {
                files.add(new PushFileMetadata(path, status, sha, message, committedAt));
            }
        }
    }

    private boolean isAlgorithmFile(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String normalized = path.toLowerCase();

        // 일반적인 비코드 파일 확인
        if (normalized.contains("readme") || normalized.contains("project") || normalized.contains("settings")) {
            log.debug("Skipping non-code file: {}", path);
            return false;
        }

        // 지원되는 확장자
        boolean isSupported = normalized.endsWith(".java") || 
                            normalized.endsWith(".py") || 
                            normalized.endsWith(".cpp") || 
                            normalized.endsWith(".cc") || 
                            normalized.endsWith(".c") || 
                            normalized.endsWith(".cs") || 
                            normalized.endsWith(".kt") || 
                            normalized.endsWith(".js") || 
                            normalized.endsWith(".ts") || 
                            normalized.endsWith(".rb") || 
                            normalized.endsWith(".swift") || 
                            normalized.endsWith(".go") || 
                            normalized.endsWith(".rs") || 
                            normalized.endsWith(".php") || 
                            normalized.endsWith(".scala") || 
                            normalized.endsWith(".dart");

        if (!isSupported) {
            log.debug("Skipping unsupported file extension: {}", path);
        }

        return isSupported;
    }

    private String writeFilesJson(List<PushFileMetadata> files) {
        try {
            return objectMapper.writeValueAsString(files);
        } catch (JsonProcessingException ex) {
            throw new GitHubPayloadProcessingException("파일 메타데이터 직렬화에 실패했습니다.", ex);
        }
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return null;
        }
        try {
            Instant instant = Instant.parse(timestamp);
            return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        } catch (Exception ex) {
            return null;
        }
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private String formatTimestamp(LocalDateTime timestamp) {
        return timestamp == null ? null : timestamp.toString();
    }

    public record PushFileMetadata(String path, String status, String commitSha, String commitMessage, String committedAt) { }

}
