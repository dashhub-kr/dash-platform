package com.ssafy.dash.ai.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.ai.client.AiServerClient;
import com.ssafy.dash.ai.client.dto.CodeReviewRequest;
import com.ssafy.dash.ai.client.dto.CodeReviewResponse;
import com.ssafy.dash.ai.client.dto.CodeReviewResponse;
import com.ssafy.dash.ai.client.dto.AiCounterExampleResponse;
import com.ssafy.dash.ai.domain.CodeAnalysisResult;
import com.ssafy.dash.ai.infrastructure.CodeAnalysisResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.Optional;

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

    /**
     * 코드 분석 요청 및 결과 저장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CodeAnalysisResult analyzeAndSave(Long algorithmRecordId, String code, String language,
            String problemNumber, String platform, String problemTitle) {
        log.info("Analyzing code for record: {}", algorithmRecordId);

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

        // 3. 기존 분석 결과가 있으면 삭제 후 새로 저장
        resultMapper.deleteByAlgorithmRecordId(algorithmRecordId);
        resultMapper.insert(result);

        log.info("Code analysis saved for record: {}, score: {}", algorithmRecordId, result.getScore());
        return result;
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
}
