package com.ssafy.dash.ai.client;

import com.ssafy.dash.ai.client.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
        } catch (Exception e) {
            log.warn("Failed to analyze code: {}. Using fallback.", e.getMessage());
            return CodeReviewResponse.builder()
                    .summary("AI 서버 연결에 실패했습니다. (임시 응답)")
                    .complexity(CodeReviewResponse.ComplexityInfo.builder()
                            .time("O(N)")
                            .space("O(1)")
                            .explanation("AI 분석을 수행할 수 없어 기본값을 반환합니다.")
                            .build())
                    .algorithm(CodeReviewResponse.AlgorithmInfo.builder()
                            .patterns(java.util.List.of("Unknown"))
                            .intuition("AI 분석 서버가 응답하지 않습니다.")
                            .build())
                    .build();
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
        } catch (Exception e) {
            log.warn("Failed to generate hint: {}. Using fallback.", e.getMessage());
            return HintResponse.builder()
                    .level(request.getLevel())
                    .hint("AI 서버 연결 실패: 스스로 생각해보는 것도 좋은 공부가 됩니다! (임시 힌트)")
                    .relatedConcepts(java.util.List.of("Algorithm", "Problem Solving"))
                    .encouragement("서버가 잠시 쉬고 있네요. 화이팅!")
                    .nextStepSuggestion("서버가 돌아오면 더 좋은 힌트를 드릴게요.")
                    .build();
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
        } catch (Exception e) {
            log.warn("Failed to generate learning path: {}. Using fallback.", e.getMessage());
            return LearningPathResponse.builder()
                    .overallAssessment("AI 서버 연결 실패")
                    .personalizedAdvice("현재 AI 분석을 이용할 수 없습니다.")
                    .phases(java.util.List.of())
                    .build();
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
        } catch (Exception e) {
            log.warn("Failed to analyze coding style: {}. Using fallback.", e.getMessage());
            return CodingStyleResponse.builder()
                    .mbtiCode("NONE")
                    .nickname("연결되지 않은 코더")
                    .summary("AI 서버와 연결할 수 없습니다.")
                    .build();
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
        } catch (Exception e) {
            log.warn("Failed to chat with tutor: {}. Using fallback.", e.getMessage());
            return TutorChatResponse.builder()
                    .reply("죄송해요, AI 선생님이 잠시 자리를 비웠어요. 다시 시도해주세요.")
                    .teachingStyle("socratic")
                    .build();
        }
    }

    @Override
    public AiCounterExampleResponse generateCounterExample(AiCounterExampleRequest request) {
        try {
            log.debug("Generating counter example for problem: {}", request.problemNumber());

            return restClient.post()
                    .uri(baseUrl + "/debug/counter-example")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AiCounterExampleResponse.class);
        } catch (Exception e) {
            log.warn("Failed to generate counter example: {}. Using fallback.", e.getMessage());
            return new AiCounterExampleResponse(
                    "Error",
                    "Error",
                    "Error",
                    "AI 서버 연결 실패: " + e.getMessage());
        }
    }

    @Override
    public AiSimulatorResponse simulate(AiSimulatorRequest request) {
        try {
            log.debug("Requesting code simulation");

            return restClient.post()
                    .uri(baseUrl + "/simulator/run")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AiSimulatorResponse.class);
        } catch (Exception e) {
            log.warn("Failed to simulate code: {}. Using fallback.", e.getMessage());
            return AiSimulatorResponse.builder()
                    .stdout("")
                    .stderr("AI 서버 연결 실패: " + e.getMessage())
                    .timeComplexity("Unknown")
                    .spaceComplexity("Unknown")
                    .analysis("시뮬레이션을 수행할 수 없습니다.")
                    .build();
        }
    }
}
