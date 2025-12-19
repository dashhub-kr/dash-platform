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

    public GitHubPushService(ObjectMapper objectMapper, GitHubPushEventRepository pushEventRepository) {
        this.objectMapper = objectMapper;
        this.pushEventRepository = pushEventRepository;
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
            GitHubPushEvent event = buildEvent(deliveryId, root, payload);
            pushEventRepository.save(event);
        } catch (JsonProcessingException ex) {
            throw new GitHubPayloadProcessingException("GitHub push payload 파싱에 실패했습니다.", ex);
        }
    }

    private GitHubPushEvent buildEvent(String deliveryId, JsonNode root, String payload) {
        String repositoryName = root.path("repository").path("full_name").asText("unknown/unknown");
        String reference = root.path("ref").asText("refs/heads/main");
        JsonNode headCommit = root.path("head_commit");
        String headSha = headCommit.path("id").asText(root.path("after").asText());
        String authorLogin = headCommit.path("author").path("username").asText(root.path("pusher").path("name").asText());
        String authorEmail = headCommit.path("author").path("email").asText();
        String commitMessage = headCommit.path("message").asText();

        List<PushFileMetadata> files = collectTargetFiles(root.path("commits"));
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

        // Check for common non-code files
        if (normalized.contains("readme") || normalized.contains("project") || normalized.contains("settings")) {
            log.debug("Skipping non-code file: {}", path);
            return false;
        }

        // Supported extensions
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
