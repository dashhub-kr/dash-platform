package com.ssafy.dash.ai.presentation;

import com.ssafy.dash.ai.application.*;
import com.ssafy.dash.ai.client.dto.*;
import com.ssafy.dash.ai.presentation.dto.LearningDashboardResponse;
import com.ssafy.dash.ai.domain.CodeAnalysisResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI API 컨트롤러
 */
@Tag(name = "AI", description = "AI 기반 코드 분석, 힌트, 학습 경로, 스타일 분석, 대화형 튜터 API")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

        private final CodeReviewService codeReviewService;
        private final HintService hintService;
        private final AiLearningPathService learningPathService;
        private final CodingStyleService codingStyleService;

        private final TutorService tutorService;
        private final DebugService debugService;

        @Operation(summary = "코드 분석 요청", description = "알고리즘 풀이 코드를 AI로 분석합니다")
        @PostMapping("/review")
        public ResponseEntity<CodeAnalysisResult> analyzeCode(@RequestBody CodeReviewRequest request) {
                CodeAnalysisResult result = codeReviewService.analyzeAndSave(
                                request.algorithmRecordId(),
                                request.code(),
                                request.language(),
                                request.problemNumber());
                return ResponseEntity.ok(result);
        }

        @Operation(summary = "분석 결과 조회", description = "저장된 분석 결과를 조회합니다")
        @GetMapping("/review/{algorithmRecordId}")
        public ResponseEntity<CodeAnalysisResult> getAnalysisResult(@PathVariable Long algorithmRecordId) {
                return codeReviewService.getAnalysisResult(algorithmRecordId)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @Operation(summary = "힌트 요청", description = "문제에 대한 맞춤형 힌트를 생성합니다 (레벨 1: 유형, 2: 접근법, 3: 상세)")
        @PostMapping("/hint")
        public ResponseEntity<HintResponse> getHint(@RequestBody HintRequest request) {
                HintResponse response = hintService.generateHint(
                                request.userId(),
                                request.problemNumber(),
                                request.problemTitle(),
                                request.level());
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "AI 학습 경로", description = "분석 데이터 기반 개인화 학습 경로를 생성합니다")
        @GetMapping("/learning-path/{userId}")
        public ResponseEntity<LearningDashboardResponse> getLearningPath(@PathVariable Long userId) {
                LearningDashboardResponse response = learningPathService.generateAiLearningPath(userId);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "코딩 스타일 분석", description = "사용자의 코드 패턴을 분석하여 MBTI 스타일 결과를 생성합니다")
        @GetMapping("/coding-style/{userId}")
        public ResponseEntity<CodingStyleResponse> getCodingStyle(@PathVariable Long userId) {
                CodingStyleResponse response = codingStyleService.analyzeCodingStyle(userId);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "튜터 대화 (세션 기반)", description = "DB에 대화 저장. sessionId가 null이면 새 세션 생성, 응답에 sessionId 포함")
        @PostMapping("/tutor/chat")
        public ResponseEntity<TutorSessionResponseDto> chat(@RequestBody TutorChatRequestDto request) {
                TutorService.TutorSessionResponse response = tutorService.chat(
                                request.userId(),
                                request.sessionId(),
                                request.message(),
                                request.problemNumber(),
                                request.code());

                return ResponseEntity.ok(TutorSessionResponseDto.from(response));

        }

        @Operation(summary = "반례 생성", description = "오답 코드에 대한 반례를 생성합니다")
        @PostMapping("/debug/counter-example")
        public ResponseEntity<AiCounterExampleResponse> generateCounterExample(
                        @RequestBody AiCounterExampleRequest request) {
                AiCounterExampleResponse response = debugService.generateCounterExample(
                                request.problemNumber(),
                                request.code(),
                                request.language());
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "코드 시뮬레이션", description = "AI 가상 컴파일러를 통해 코드 실행 결과를 예측합니다")
        @PostMapping("/simulator/run")
        public ResponseEntity<AiSimulatorResponse> runSimulation(@RequestBody AiSimulatorRequest request) {
                return ResponseEntity.ok(debugService.simulate(request.getCode(), request.getLanguage()));
        }

        // === DTOs ===

        public record CodeReviewRequest(
                        Long algorithmRecordId,
                        String code,
                        String language,
                        String problemNumber) {
        }

        public record HintRequest(
                        Long userId,
                        String problemNumber,
                        String problemTitle,
                        int level) {
        }

        public record TutorChatRequestDto(
                        Long userId,
                        String sessionId, // nullable - 새 세션이면 null
                        String message,
                        String problemNumber,
                        String code) {
        }

        public record TutorSessionResponseDto(
                        String sessionId,
                        String reply,
                        String teachingStyle,
                        List<String> followUpQuestions,
                        String conceptExplanation,
                        String encouragement) {

                public static TutorSessionResponseDto from(TutorService.TutorSessionResponse response) {
                        return new TutorSessionResponseDto(
                                        response.getSessionId(),
                                        response.getReply(),
                                        response.getTeachingStyle(),
                                        response.getFollowUpQuestions(),
                                        response.getConceptExplanation(),
                                        response.getEncouragement());
                }
        }
}
