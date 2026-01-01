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
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.mockexam.application.MockExamService;
import com.ssafy.dash.defense.application.DefenseService;
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
import java.util.ArrayList;
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
    private final UserRepository userRepository;
    private final com.ssafy.dash.acorn.application.AcornService acornService;
    private final com.ssafy.dash.ai.application.CodeReviewService codeReviewService;
    private final com.ssafy.dash.study.application.StudyMissionService studyMissionService;
    private final MockExamService mockExamService;
    private final DefenseService defenseService;

    public GitHubPushEventWorker(GitHubPushEventRepository pushEventRepository,
            OnboardingRepository onboardingRepository,
            OAuthTokenService oauthTokenService,
            GitHubClient gitHubClient,
            AlgorithmRecordRepository algorithmRecordRepository,
            ObjectMapper objectMapper,
            GitHubSubmissionMetadataExtractor metadataExtractor,
            PlatformTransactionManager transactionManager,
            UserRepository userRepository,
            com.ssafy.dash.acorn.application.AcornService acornService,
            com.ssafy.dash.ai.application.CodeReviewService codeReviewService,
            com.ssafy.dash.study.application.StudyMissionService studyMissionService,
            MockExamService mockExamService,
            DefenseService defenseService,
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
        this.userRepository = userRepository;
        this.acornService = acornService;
        this.codeReviewService = codeReviewService;
        this.studyMissionService = studyMissionService;
        this.mockExamService = mockExamService;
        this.defenseService = defenseService;
    }

    @Scheduled(fixedDelayString = "${github.push-worker.fixed-delay:10000}")
    public void run() {
        for (int processed = 0; processed < maxBatchSize; processed++) {
            Optional<GitHubPushEvent> optional = pushEventRepository.findNextQueued();
            if (optional.isEmpty()) {
                return;
            }
            GitHubPushEvent event = optional.get();
            // 트랜잭션 내에서 레코드 저장 후, 생성된 레코드 목록 반환
            List<AlgorithmRecord> newRecords = transactionTemplate.execute(status -> processEvent(event));

            // 트랜잭션 커밋 후 AI 분석 실행 (별도 트랜잭션)
            if (newRecords != null) {
                for (AlgorithmRecord record : newRecords) {
                    try {
                        codeReviewService.analyzeAndSave(
                                record.getId(),
                                record.getCode(),
                                record.getLanguage(),
                                record.getProblemNumber(),
                                record.getPlatform(),
                                record.getTitle());
                    } catch (Exception e) {
                        log.error("Auto-analysis failed via worker for record: {}", record.getId(), e);
                    }
                }
            }
        }
    }

    List<AlgorithmRecord> processEvent(GitHubPushEvent event) {
        if (event.getStatus() != PushEventStatus.QUEUED) {
            return Collections.emptyList();
        }

        LocalDateTime now = LocalDateTime.now();
        event.markProcessing(now);
        pushEventRepository.update(event);

        List<AlgorithmRecord> createdRecords = new ArrayList<>();

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
                AlgorithmRecord record = storeAlgorithmRecord(event, onboarding.getUserId(), token.getAccessToken(),
                        file);
                createdRecords.add(record);
                processed = true;
            }

            if (!processed) {
                throw new GitHubWebhookException("처리 가능한 알고리즘 파일이 없습니다.");
            }

            event.markCompleted(LocalDateTime.now());
            return createdRecords;

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
        return Collections.emptyList();
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

    private AlgorithmRecord storeAlgorithmRecord(GitHubPushEvent event,
            Long userId,
            String accessToken,
            QueuedPushFile file) {
        GitHubSubmissionMetadataExtractor.SubmissionMetadata metadata = metadataExtractor.extract(file.commitMessage(),
                file.path());

        String reference = StringUtils.hasText(file.commitSha()) ? file.commitSha() : event.getHeadCommitSha();
        String code = gitHubClient.fetchFileContent(event.getRepositoryName(), file.path(), reference, accessToken);
        LocalDateTime now = LocalDateTime.now();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GitHubWebhookException("User not found: " + userId));

        AlgorithmRecord record = AlgorithmRecord.create(
                userId,
                user.getStudyId(),
                metadata.problemNumber(),
                metadata.title(),
                metadata.language(),
                code,
                now);

        LocalDateTime committedAt = metadataExtractor.parseCommittedAt(file.committedAt());
        if (committedAt != null) {
            committedAt = committedAt.plusHours(9);
        }

        record.enrichMetadata(
                metadata.platform(),
                metadata.difficulty(),
                metadata.runtimeMs(),
                metadata.memoryKb(),
                event.getRepositoryName(),
                file.path(),
                file.commitSha(),
                file.commitMessage(),
                committedAt);

        // --- Tagging Logic ---
        String problemIdStr = metadata.problemNumber();
        Integer problemId = null;
        try {
            problemId = Integer.parseInt(problemIdStr);
        } catch (NumberFormatException ignored) {
        }

        String tag = "GENERAL";

        if (problemId != null) {
            // 1. Mission Check
            if (user.getStudyId() != null
                    && studyMissionService.isActiveMissionProblem(user.getStudyId(), problemId, now)) {
                tag = "MISSION";
            }
            // 2. Mock Exam Check
            else if (mockExamService.isActiveProblem(userId, problemId)) {
                tag = "MOCK_EXAM";
            }
            // 3. Random Defense Check (Check defenseProblemId)
            else if (user.getDefenseType() != null && user.getDefenseProblemId() != null
                    && user.getDefenseProblemId().equals(problemId)) {
                tag = "DEFENSE";
            }
        }
        record.setTag(tag);
        // ---------------------

        algorithmRecordRepository.save(record);
        log.info("AlgorithmRecord saved: id={}, problem={}, tag={}", record.getId(), record.getProblemNumber(), tag);

        if (problemId != null) {
            Integer streak = defenseService.verifyDefense(userId, problemId);
            if (streak != null) {
                record.setTag("DEFENSE");
                record.setDefenseStreak(streak);
                algorithmRecordRepository.update(record);
            }
            mockExamService.verifyExam(userId, problemId);
        }

        // Acorn Accumulation Logic
        if (record.getStudyId() != null
                && metadata.runtimeMs() != null && metadata.runtimeMs() > 0
                && metadata.memoryKb() != null && metadata.memoryKb() > 0) {

            try {
                acornService.accumulate(
                        record.getStudyId(),
                        userId,
                        10,
                        "Problem Solved: " + metadata.problemNumber());
            } catch (Exception e) {
                log.error("Acorn accumulation failed for record: {}", record.getId(), e);
            }
        }

        return record;
    }

    private boolean isProblemInList(String problemsJson, Integer problemId) {
        if (!StringUtils.hasText(problemsJson) || problemId == null) {
            return false;
        }
        try {
            List<Integer> problems = objectMapper.readValue(problemsJson, new TypeReference<List<Integer>>() {
            });
            return problems.contains(problemId);
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    private record QueuedPushFile(
            String path,
            String status,
            String commitSha,
            String commitMessage,
            String committedAt) {

        boolean isProcessable() {
            return StringUtils.hasText(path) && !"removed".equalsIgnoreCase(status);
        }

    }

}
