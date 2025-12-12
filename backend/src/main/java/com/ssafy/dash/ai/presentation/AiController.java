package com.ssafy.dash.ai.presentation;

import com.ssafy.dash.ai.application.AiLearningPathService;
import com.ssafy.dash.ai.application.CodeReviewService;
import com.ssafy.dash.ai.application.CodingStyleService;
import com.ssafy.dash.ai.application.HintService;
import com.ssafy.dash.ai.client.dto.CodingStyleResponse;
import com.ssafy.dash.ai.client.dto.HintResponse;
import com.ssafy.dash.ai.client.dto.LearningPathResponse;
import com.ssafy.dash.ai.domain.CodeAnalysisResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI API 컨트롤러
 */
@Tag(name = "AI", description = "AI 기반 코드 분석, 힌트, 학습 경로, 스타일 분석 API")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

        private final CodeReviewService codeReviewService;
        private final HintService hintService;
        private final AiLearningPathService learningPathService;
        private final CodingStyleService codingStyleService;

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
        public ResponseEntity<LearningPathResponse> getLearningPath(@PathVariable Long userId) {
                LearningPathResponse response = learningPathService.generateAiLearningPath(userId);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "코딩 스타일 분석", description = "사용자의 코드 패턴을 분석하여 MBTI 스타일 결과를 생성합니다")
        @GetMapping("/coding-style/{userId}")
        public ResponseEntity<CodingStyleResponse> getCodingStyle(@PathVariable Long userId) {
                CodingStyleResponse response = codingStyleService.analyzeCodingStyle(userId);
                return ResponseEntity.ok(response);
        }

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
}
