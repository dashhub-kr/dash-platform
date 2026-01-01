package com.ssafy.dash.ai.infrastructure.client.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 서버 코드 분석 응답 DTO
 * AI 서버의 StructuredResponse 스키마와 매핑
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeReviewResponse {

    private String summary;
    private ProblemInfo problem;
    private AlgorithmInfo algorithm;
    private List<StructureItem> structure;
    private List<KeyBlock> keyBlocks;
    private TraceExample traceExample;
    private ComplexityInfo complexity;
    private PitfallsInfo pitfalls;
    private RefactorInfo refactor;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProblemInfo {
        private String description;
        private String input;
        private String output;
        private boolean isGuess;
        private String guessReason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AlgorithmInfo {
        private List<String> patterns;
        private String intuition;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StructureItem {
        private String name;
        private String role;
        private String type; // "variable", "function", "class", "constant"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KeyBlock {
        private String code;
        private String explanation;
        private Integer startLine;
        private Integer endLine;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TraceExample {
        private boolean hasExample;
        private String inputExample;
        private List<String> steps;
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ComplexityInfo {
        private String time;
        private String space;
        private String explanation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PitfallsInfo {
        private List<String> items;
        private List<String> improvements;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RefactorInfo {
        private boolean provided;
        private String code;
        private String explanation;
    }
}
