package com.ssafy.dash.ai.presentation;

import com.ssafy.dash.ai.application.AiLearningPathService;
import com.ssafy.dash.ai.application.CodeReviewService;
import com.ssafy.dash.ai.application.CodingStyleService;
import com.ssafy.dash.ai.application.DebugService;
import com.ssafy.dash.ai.application.TutorChatService;
import com.ssafy.dash.ai.infrastructure.client.dto.request.AiCounterExampleRequest;
import com.ssafy.dash.ai.infrastructure.client.dto.response.AiCounterExampleResponse;
import com.ssafy.dash.ai.infrastructure.client.dto.request.AiSimulatorRequest;
import com.ssafy.dash.ai.infrastructure.client.dto.response.AiSimulatorResponse;
import com.ssafy.dash.ai.infrastructure.client.dto.response.CodingStyleResponse;
import com.ssafy.dash.ai.infrastructure.client.dto.response.HintChatResponse;
import com.ssafy.dash.ai.infrastructure.client.dto.request.HintChatRequest;
import com.ssafy.dash.ai.domain.CodeAnalysisResult;
import com.ssafy.dash.ai.presentation.dto.LearningDashboardResponse;
import com.ssafy.dash.ai.presentation.dto.request.CodeReviewRequest;
import com.ssafy.dash.ai.presentation.dto.request.HintChatRequestDto;
import com.ssafy.dash.ai.presentation.dto.response.HintChatResponseDto;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

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
        public ResponseEntity<CodeAnalysisResult> analyzeCode(
                        @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
                        @RequestBody CodeReviewRequest request) {
                if (!(principal instanceof CustomOAuth2User customUser)) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
                }
                if (request.algorithmRecordId() == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "algorithmRecordId는 필수입니다.");
                }

                try {
                        CodeAnalysisResult result = codeReviewService.analyzeOnDemand(
                                        request.algorithmRecordId(),
                                        customUser.getUserId(),
                                        Boolean.TRUE.equals(request.force()));
                        return ResponseEntity.ok(result);
                } catch (NoSuchElementException e) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
                } catch (AccessDeniedException e) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
                } catch (IllegalArgumentException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
                }
        }

        @Operation(summary = "분석 결과 조회", description = "저장된 분석 결과를 조회합니다")
        @GetMapping("/review/{algorithmRecordId}")
        public ResponseEntity<CodeAnalysisResult> getAnalysisResult(
                        @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
                        @PathVariable Long algorithmRecordId) {
                if (!(principal instanceof CustomOAuth2User customUser)) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
                }

                try {
                        return codeReviewService.getAnalysisResultAuthorized(algorithmRecordId, customUser.getUserId())
                                        .map(ResponseEntity::ok)
                                        .orElse(ResponseEntity.notFound().build());
                } catch (NoSuchElementException e) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
                } catch (AccessDeniedException e) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
                }
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

        @Operation(summary = "튜터 대화 내역 조회", description = "특정 기록에 대한 튜터 대화 내역을 조회합니다")
        @GetMapping("/tutor/history/{recordId}")
        public ResponseEntity<List<HintChatRequest.ChatMessage>> getTutorHistory(
                        @PathVariable Long recordId,
                        @RequestParam Long userId) {
                return ResponseEntity.ok(tutorChatService.getChatHistory(userId, recordId));
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
                                request.recordId(),
                                request.problemNumber(),
                                request.code(),
                                request.language(),
                                request.platform(),
                                request.problemTitle());
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "코드 시뮬레이션", description = "AI 가상 컴파일러를 통해 코드 실행 결과를 예측합니다")
        @PostMapping("/simulator/run")
        public ResponseEntity<AiSimulatorResponse> runSimulation(@RequestBody AiSimulatorRequest request) {
                return ResponseEntity.ok(debugService.simulate(request.getCode(), request.getLanguage()));
        }
}
