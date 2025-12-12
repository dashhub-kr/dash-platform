package com.ssafy.dash.ai.client;

import com.ssafy.dash.ai.client.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * AI 서버 통신 클라이언트 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiServerClientImpl implements AiServerClient {

    @Value("${ai.server.base-url:http://localhost:8000}")
    private String baseUrl;

    private final RestClient restClient;

    @Override
    public CodeReviewResponse analyzeCode(CodeReviewRequest request) {
        try {
            log.debug("Requesting code analysis for problem: {}", request.getProblemNumber());

            return restClient.post()
                    .uri(baseUrl + "/review")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(CodeReviewResponse.class);
        } catch (RestClientException e) {
            log.error("Failed to analyze code: {}", e.getMessage(), e);
            throw new AiServerException("코드 분석 요청에 실패했습니다", e);
        }
    }

    @Override
    public HintResponse generateHint(HintRequest request) {
        try {
            log.debug("Requesting hint for problem: {}, level: {}",
                    request.getProblemNumber(), request.getLevel());

            return restClient.post()
                    .uri(baseUrl + "/hint")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(HintResponse.class);
        } catch (RestClientException e) {
            log.error("Failed to generate hint: {}", e.getMessage(), e);
            throw new AiServerException("힌트 생성 요청에 실패했습니다", e);
        }
    }

    @Override
    public LearningPathResponse generateLearningPath(LearningPathRequest request) {
        try {
            log.debug("Requesting learning path for level: {}", request.getCurrentLevel());

            return restClient.post()
                    .uri(baseUrl + "/learning-path")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(LearningPathResponse.class);
        } catch (RestClientException e) {
            log.error("Failed to generate learning path: {}", e.getMessage(), e);
            throw new AiServerException("학습 경로 생성 요청에 실패했습니다", e);
        }
    }

    @Override
    public CodingStyleResponse analyzeCodingStyle(CodingStyleRequest request) {
        try {
            log.debug("Analyzing coding style with {} code samples",
                    request.getCodeSamples() != null ? request.getCodeSamples().size() : 0);

            return restClient.post()
                    .uri(baseUrl + "/coding-style")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(CodingStyleResponse.class);
        } catch (RestClientException e) {
            log.error("Failed to analyze coding style: {}", e.getMessage(), e);
            throw new AiServerException("코딩 스타일 분석 요청에 실패했습니다", e);
        }
    }

    @Override
    public TutorChatResponse chat(TutorChatRequest request) {
        try {
            log.debug("Sending chat message to tutor");

            return restClient.post()
                    .uri(baseUrl + "/tutor/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(TutorChatResponse.class);
        } catch (RestClientException e) {
            log.error("Failed to chat with tutor: {}", e.getMessage(), e);
            throw new AiServerException("튜터 대화 요청에 실패했습니다", e);
        }
    }
}
