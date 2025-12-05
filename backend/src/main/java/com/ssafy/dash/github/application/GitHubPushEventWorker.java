package com.ssafy.dash.github.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.github.domain.GitHubClient;
import com.ssafy.dash.github.domain.GitHubPushEvent;
import com.ssafy.dash.github.domain.GitHubPushEventRepository;
import com.ssafy.dash.github.domain.PushEventStatus;
import com.ssafy.dash.github.domain.exception.GitHubClientException;
import com.ssafy.dash.github.domain.exception.GitHubFileDownloadException;
import com.ssafy.dash.github.domain.exception.GitHubWebhookException;
import com.ssafy.dash.oauth.application.OAuthTokenService;
import com.ssafy.dash.oauth.domain.UserOAuthToken;
import com.ssafy.dash.onboarding.domain.Onboarding;
import com.ssafy.dash.onboarding.domain.OnboardingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(value = "github.push-worker.enabled", havingValue = "true", matchIfMissing = true)
public class GitHubPushEventWorker {

    private static final Logger log = LoggerFactory.getLogger(GitHubPushEventWorker.class);

    private final GitHubPushEventRepository pushEventRepository;
    private final OnboardingRepository onboardingRepository;
    private final OAuthTokenService oauthTokenService;
    private final GitHubClient gitHubClient;
    private final AlgorithmRecordRepository algorithmRecordRepository;
    private final ObjectMapper objectMapper;
    private final GitHubSubmissionMetadataExtractor metadataExtractor;
    private final int maxBatchSize;
    private final TransactionTemplate transactionTemplate;

    public GitHubPushEventWorker(GitHubPushEventRepository pushEventRepository,
                                 OnboardingRepository onboardingRepository,
                                 OAuthTokenService oauthTokenService,
                                 GitHubClient gitHubClient,
                                 AlgorithmRecordRepository algorithmRecordRepository,
                                 ObjectMapper objectMapper,
                                 GitHubSubmissionMetadataExtractor metadataExtractor,
                                 PlatformTransactionManager transactionManager,
                                 @Value("${github.push-worker.max-batch:5}") int maxBatchSize) {
        this.pushEventRepository = pushEventRepository;
        this.onboardingRepository = onboardingRepository;
        this.oauthTokenService = oauthTokenService;
        this.gitHubClient = gitHubClient;
        this.algorithmRecordRepository = algorithmRecordRepository;
        this.objectMapper = objectMapper;
        this.metadataExtractor = metadataExtractor;
        this.maxBatchSize = Math.max(1, maxBatchSize);
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Scheduled(fixedDelayString = "${github.push-worker.fixed-delay:10000}")
    public void run() {
        for (int processed = 0; processed < maxBatchSize; processed++) {
            Optional<GitHubPushEvent> optional = pushEventRepository.findNextQueued();
            if (optional.isEmpty()) {
                return;
            }
            GitHubPushEvent event = optional.get();
            transactionTemplate.executeWithoutResult(status -> processEvent(event));
        }
    }

    void processEvent(GitHubPushEvent event) {
        if (event.getStatus() != PushEventStatus.QUEUED) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        event.markProcessing(now);
        pushEventRepository.update(event);

        try {
            Onboarding onboarding = onboardingRepository.findByRepositoryName(event.getRepositoryName())
                    .orElseThrow(() -> new GitHubWebhookException("해당 저장소의 온보딩 정보가 없습니다."));

            UserOAuthToken token = oauthTokenService.requireValidAccessToken(onboarding.getUserId());
            List<QueuedPushFile> files = readFiles(event.getFilesJson());

            boolean processed = false;
            for (QueuedPushFile file : files) {
                if (!file.isProcessable()) {
                    continue;
                }
                storeAlgorithmRecord(event, onboarding.getUserId(), token.getAccessToken(), file);
                processed = true;
            }

            if (!processed) {
                throw new GitHubWebhookException("처리 가능한 알고리즘 파일이 없습니다.");
            }

            event.markCompleted(LocalDateTime.now());
        } catch (GitHubFileDownloadException ex) {
            log.warn("GitHub push event {} 파일 다운로드 실패: {}", event.getDeliveryId(), ex.getMessage());
            event.markFailed("GitHub 파일 다운로드 실패: " + ex.getMessage(), LocalDateTime.now());
        } catch (GitHubClientException ex) {
            log.error("GitHub push event {} 처리 중 GitHub API 오류", event.getDeliveryId(), ex);
            event.markFailed(ex.getMessage(), LocalDateTime.now());
        } catch (Exception ex) {
            log.error("GitHub push event {} 처리 중 오류", event.getDeliveryId(), ex);
            event.markFailed(ex.getMessage(), LocalDateTime.now());
        } finally {
            pushEventRepository.update(event);
        }
    }

    private List<QueuedPushFile> readFiles(String filesJson) {
        if (!StringUtils.hasText(filesJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(filesJson, new TypeReference<List<QueuedPushFile>>() {
            });
        } catch (JsonProcessingException ex) {
            throw new GitHubWebhookException("files_json 파싱에 실패했습니다.", ex);
        }
    }

    private void storeAlgorithmRecord(GitHubPushEvent event,
                                      Long userId,
                                      String accessToken,
                                      QueuedPushFile file) {
        GitHubSubmissionMetadataExtractor.SubmissionMetadata metadata =
                metadataExtractor.extract(file.commitMessage(), file.path());

        String reference = StringUtils.hasText(file.commitSha()) ? file.commitSha() : event.getHeadCommitSha();
        String code = gitHubClient.fetchFileContent(event.getRepositoryName(), file.path(), reference, accessToken);
        LocalDateTime now = LocalDateTime.now();

        AlgorithmRecord record = AlgorithmRecord.create(
                userId,
                metadata.problemNumber(),
                metadata.title(),
                metadata.language(),
                code,
                now);

        record.enrichMetadata(
                metadata.platform(),
                metadata.difficulty(),
                metadata.runtimeMs(),
                metadata.memoryKb(),
                event.getRepositoryName(),
                file.path(),
                file.commitSha(),
                file.commitMessage(),
                metadataExtractor.parseCommittedAt(file.committedAt()));

        algorithmRecordRepository.save(record);
    }

    private record QueuedPushFile(
            String path,
            String status,
            String commitSha,
            String commitMessage,
            String committedAt
        ) {

        boolean isProcessable() {
            return StringUtils.hasText(path) && !"removed".equalsIgnoreCase(status);
        }
        
    }

}
