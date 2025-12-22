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
@Tag(name = "AI", description = "AI 기반 코드 분석, 학습 경로, 스타일 분석, 대화형 튜터 API")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

        private final CodeReviewService codeReviewService;
        private final TutorChatService tutorChatService;
        private final AiLearningPathService learningPathService;
        private final CodingStyleService codingStyleService;
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

        @Operation(summary = "AI 튜터 대화", description = "맞은 문제/틀린 문제 모두 지원하는 대화형 AI 튜터")
        @PostMapping("/tutor/chat")
        public ResponseEntity<HintChatResponseDto> hintChat(@RequestBody HintChatRequestDto request) {
                // recordId가 있으면 DB에서 조회, 없으면 기존 방식 사용
                HintChatResponse response;
                if (request.recordId() != null) {
                        List<HintChatRequest.ChatMessage> history = request.history() != null
                                        ? request.history().stream()
                                                        .map(m -> HintChatRequest.ChatMessage.builder()
                                                                        .role(m.role())
                                                                        .content(m.content())
                                                                        .build())
                                                        .toList()
                                        : List.of();

                        response = tutorChatService.hintChatWithRecord(
                                        request.userId(),
                                        request.recordId(),
                                        request.message(),
                                        request.solveStatus(),
                                        request.wrongReason(),
                                        history);
                } else {
                        // Fallback: 기존 방식 (deprecated)
                        HintChatRequest aiRequest = HintChatRequest.builder()
                                        .message(request.message())
                                        .problemNumber(request.problemNumber())
                                        .problemTitle(request.problemTitle())
                                        .code(request.code())
                                        .language(request.language())
                                        .history(request.history() != null ? request.history().stream()
                                                        .map(m -> HintChatRequest.ChatMessage.builder()
                                                                        .role(m.role())
                                                                        .content(m.content())
                                                                        .build())
                                                        .toList() : List.of())
                                        .userContext(request.userContext() != null ? HintChatRequest.UserContext
                                                        .builder()
                                                        .tierName(request.userContext().tierName())
                                                        .solvedCount(request.userContext().solvedCount())
                                                        .weakTags(request.userContext().weakTags())
                                                        .build() : null)
                                        .build();
                        response = tutorChatService.hintChat(aiRequest);
                }

                return ResponseEntity.ok(HintChatResponseDto.from(response));
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

        // === AI Tutor Chat DTOs ===

        public record HintChatRequestDto(
                        Long userId, // 사용자 ID (필수)
                        Long recordId, // 알고리즘 기록 ID (권장 - DB에서 코드/문제 조회)
                        String message, // 사용자 메시지 (필수)
                        String solveStatus, // "solved" | "wrong" (필수)
                        String wrongReason, // 틀린 이유 (시간초과, 틀렸습니다 등)
                        List<ChatMessageDto> history,
                        // Fallback fields (recordId가 없을 때 사용)
                        String problemNumber,
                        String problemTitle,
                        String code,
                        String language,
                        UserContextDto userContext) {
        }

        public record ChatMessageDto(String role, String content) {
        }

        public record UserContextDto(String tierName, int solvedCount, List<String> weakTags) {
        }

        public record HintChatResponseDto(
                        String reply,
                        String teachingStyle,
                        List<String> followUpQuestions,
                        List<String> relatedConcepts,
                        String encouragement) {

                public static HintChatResponseDto from(HintChatResponse response) {
                        return new HintChatResponseDto(
                                        response.getReply(),
                                        response.getTeachingStyle(),
                                        response.getFollowUpQuestions(),
                                        response.getRelatedConcepts(),
                                        response.getEncouragement());
                }
        }
}
