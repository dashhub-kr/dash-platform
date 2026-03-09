package com.ssafy.dash.ai.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.ai.infrastructure.client.AiServerClient;
import com.ssafy.dash.ai.infrastructure.client.dto.request.CodeReviewRequest;
import com.ssafy.dash.ai.infrastructure.client.dto.response.CodeReviewResponse;
import com.ssafy.dash.ai.infrastructure.client.dto.response.AiCounterExampleResponse;
import com.ssafy.dash.ai.domain.CodeAnalysisResult;
import com.ssafy.dash.ai.infrastructure.CodeAnalysisResultMapper;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 코드 리뷰 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeReviewService {

    private final AiServerClient aiClient;
    private final CodeAnalysisResultMapper resultMapper;
    private final ObjectMapper objectMapper;
    private final AlgorithmRecordRepository algorithmRecordRepository;
    private final UserRepository userRepository;
    private final ConcurrentMap<Long, Object> analyzeLocks = new ConcurrentHashMap<>();

    /**
     * 코드 분석 요청 및 결과 저장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CodeAnalysisResult analyzeAndSave(Long algorithmRecordId, String code, String language,
            String problemNumber, String platform, String problemTitle) {
        log.info("Analyzing code for record: {}", algorithmRecordId);

        Optional<CodeAnalysisResult> existing = resultMapper.findByAlgorithmRecordId(algorithmRecordId);

        // 1. AI 서버에 분석 요청
        CodeReviewRequest request = CodeReviewRequest.builder()
                .code(code)
                .language(language)
                .problemNumber(problemNumber)
                .platform(platform)
                .problemTitle(problemTitle)
                .build();

        CodeReviewResponse response = aiClient.analyzeCode(request);

        // 2. 응답을 엔티티로 변환
        CodeAnalysisResult result = convertToEntity(algorithmRecordId, response);
        existing.ifPresent(previous -> copyCounterExampleFields(previous, result));

        // 3. 기존 분석 결과가 있으면 삭제 후 새로 저장
        resultMapper.deleteByAlgorithmRecordId(algorithmRecordId);
        resultMapper.insert(result);

        log.info("Code analysis saved for record: {}, score: {}", algorithmRecordId, result.getScore());
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CodeAnalysisResult analyzeOnDemand(Long algorithmRecordId, Long requesterUserId, boolean force) {
        if (algorithmRecordId == null) {
            throw new IllegalArgumentException("algorithmRecordId는 필수입니다.");
        }
        if (requesterUserId == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        AlgorithmRecord record = requireAuthorizedRecord(algorithmRecordId, requesterUserId);
        Object lock = analyzeLocks.computeIfAbsent(algorithmRecordId, key -> new Object());

        try {
            synchronized (lock) {
                Optional<CodeAnalysisResult> existing = resultMapper.findByAlgorithmRecordId(algorithmRecordId);
                if (existing.isPresent() && !force && hasReviewContent(existing.get())) {
                    return existing.get();
                }

                try {
                    return analyzeAndSave(
                            algorithmRecordId,
                            record.getCode(),
                            record.getLanguage(),
                            record.getProblemNumber(),
                            record.getPlatform(),
                            record.getTitle());
                } catch (DuplicateKeyException duplicate) {
                    return resultMapper.findByAlgorithmRecordId(algorithmRecordId)
                            .orElseThrow(() -> duplicate);
                }
            }
        } finally {
            analyzeLocks.remove(algorithmRecordId, lock);
        }
    }

    /**
     * 반례 결과 저장
     */
    @Transactional
    public void saveCounterExample(Long algorithmRecordId, AiCounterExampleResponse response) {
        // 이미 결과가 있으면 업데이트, 없으면 신규 생성
        Optional<CodeAnalysisResult> existing = resultMapper.findByAlgorithmRecordId(algorithmRecordId);

        CodeAnalysisResult result;
        if (existing.isPresent()) {
            result = existing.get();
        } else {
            result = CodeAnalysisResult.builder()
                    .algorithmRecordId(algorithmRecordId)
                    .analyzedAt(LocalDateTime.now())
                    .build();
        }

        result.setCounterExampleInput(response.inputExample());
        result.setCounterExampleExpected(response.expectedOutput());
        result.setCounterExamplePredicted(response.predictedOutput());
        result.setCounterExampleReason(response.explanation());

        if (existing.isPresent()) {
            resultMapper.updateCounterExample(result);
        } else {
            resultMapper.insert(result);
        }

        log.info("Counter example saved for record: {}", algorithmRecordId);
    }

    /**
     * 저장된 분석 결과 조회
     */
    public Optional<CodeAnalysisResult> getAnalysisResult(Long algorithmRecordId) {
        return resultMapper.findByAlgorithmRecordId(algorithmRecordId);
    }

    public Optional<CodeAnalysisResult> getAnalysisResultAuthorized(Long algorithmRecordId, Long requesterUserId) {
        requireAuthorizedRecord(algorithmRecordId, requesterUserId);
        return resultMapper.findByAlgorithmRecordId(algorithmRecordId);
    }

    /**
     * AI 응답을 엔티티로 변환
     */
    private CodeAnalysisResult convertToEntity(Long algorithmRecordId, CodeReviewResponse response) {
        var builder = CodeAnalysisResult.builder()
                .algorithmRecordId(algorithmRecordId)
                .summary(response.getSummary())
                .timeComplexity(response.getComplexity() != null ? response.getComplexity().getTime() : null)
                .spaceComplexity(response.getComplexity() != null ? response.getComplexity().getSpace() : null)
                .complexityExplanation(
                        response.getComplexity() != null ? response.getComplexity().getExplanation() : null)
                .patterns(toJson(response.getAlgorithm() != null ? response.getAlgorithm().getPatterns() : null))
                .algorithmIntuition(response.getAlgorithm() != null ? response.getAlgorithm().getIntuition() : null)
                .algorithmIntuition(response.getAlgorithm() != null ? response.getAlgorithm().getIntuition() : null)
                .pitfalls(toJson(response.getPitfalls() != null ? response.getPitfalls().getItems() : null))
                .improvements(toJson(response.getPitfalls() != null ? response.getPitfalls().getImprovements() : null))
                .keyBlocks(toJson(response.getKeyBlocks()))
                .refactorProvided(response.getRefactor() != null && response.getRefactor().isProvided())
                .refactorCode(response.getRefactor() != null ? response.getRefactor().getCode() : null)
                .refactorExplanation(response.getRefactor() != null ? response.getRefactor().getExplanation() : null)
                .score(calculateScore(response))
                .analyzedAt(LocalDateTime.now());

        try {
            builder.fullResponse(objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize full response", e);
            builder.fullResponse("{}");
        }

        return builder.build();
    }

    /**
     * 분석 결과 기반 점수 계산 (0-100)
     */
    private Integer calculateScore(CodeReviewResponse response) {
        int score = 70; // 기본 점수

        // 최적화된 복잡도면 가점
        if (response.getComplexity() != null) {
            String time = response.getComplexity().getTime();
            if (time != null) {
                if (time.contains("O(1)") || time.contains("O(log")) {
                    score += 15;
                } else if (time.contains("O(n)") || time.contains("O(N)")) {
                    score += 10;
                } else if (time.contains("O(n log") || time.contains("O(N log")) {
                    score += 8;
                }
            }
        }

        // 개선점이 적으면 가점
        if (response.getPitfalls() != null && response.getPitfalls().getItems() != null) {
            int pitfallCount = response.getPitfalls().getItems().size();
            if (pitfallCount == 0) {
                score += 15;
            } else if (pitfallCount <= 2) {
                score += 5;
            } else {
                score -= (pitfallCount - 2) * 5;
            }
        }

        return Math.min(100, Math.max(0, score));
    }

    private String toJson(Object obj) {
        if (obj == null)
            return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert to JSON: {}", e.getMessage());
            return null;
        }
    }

    private AlgorithmRecord requireAuthorizedRecord(Long algorithmRecordId, Long requesterUserId) {
        AlgorithmRecord record = algorithmRecordRepository.findById(algorithmRecordId)
                .orElseThrow(() -> new NoSuchElementException("해당 풀이 기록을 찾을 수 없습니다: " + algorithmRecordId));

        if (Objects.equals(record.getUserId(), requesterUserId)) {
            return record;
        }

        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new AccessDeniedException("로그인이 필요합니다."));

        if ("ROLE_ADMIN".equals(requester.getRole())) {
            return record;
        }

        if (requester.getStudyId() != null && Objects.equals(requester.getStudyId(), record.getStudyId())) {
            return record;
        }

        throw new AccessDeniedException("이 기록의 AI 분석을 볼 권한이 없습니다.");
    }

    private boolean hasReviewContent(CodeAnalysisResult result) {
        return result != null && (
                hasText(result.getSummary()) ||
                        hasText(result.getTimeComplexity()) ||
                        hasText(result.getSpaceComplexity()) ||
                        hasText(result.getComplexityExplanation()) ||
                        hasText(result.getPatterns()) ||
                        hasText(result.getAlgorithmIntuition()) ||
                        hasText(result.getPitfalls()) ||
                        hasText(result.getImprovements()) ||
                        hasText(result.getKeyBlocks()) ||
                        hasText(result.getFullResponse()) ||
                        result.isRefactorProvided() ||
                        hasText(result.getRefactorCode()) ||
                        hasText(result.getRefactorExplanation()));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void copyCounterExampleFields(CodeAnalysisResult source, CodeAnalysisResult target) {
        target.setCounterExampleInput(source.getCounterExampleInput());
        target.setCounterExampleExpected(source.getCounterExampleExpected());
        target.setCounterExamplePredicted(source.getCounterExamplePredicted());
        target.setCounterExampleReason(source.getCounterExampleReason());
    }
}
