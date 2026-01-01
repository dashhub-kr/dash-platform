package com.ssafy.dash.ai.infrastructure.client.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 코딩 스타일 분석 응답 DTO (MBTI 스타일)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodingStyleResponse {
    private String mbtiCode; // 예: "INTP" (코딩 스타일)
    private String nickname; // 예: "논리적 설계자"
    private String summary; // 종합 설명
    private List<StyleAxis> axes; // 4개 축 분석
    private List<String> strengths; // 강점
    private List<String> improvements; // 개선점
    private String compatibleStyles; // 잘 맞는 스타일
    private String advice; // 조언

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StyleAxis {
        private String axis; // "E/I", "S/N", "T/F", "J/P"
        private String result; // "I", "N", etc.
        private int score; // 0-100
        private String leftLabel; // "외향적 코딩"
        private String rightLabel; // "내향적 코딩"
        private String description; // 설명
    }
}
