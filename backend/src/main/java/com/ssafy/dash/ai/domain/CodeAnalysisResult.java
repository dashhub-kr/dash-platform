package com.ssafy.dash.ai.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 코드 분석 결과 엔티티
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeAnalysisResult {

    private Long id;
    private Long algorithmRecordId;

    // 요약
    private String summary;

    // 복잡도
    private String timeComplexity;
    private String spaceComplexity;
    private String complexityExplanation;

    // 알고리즘 패턴 (JSON)
    private String patterns;
    private String algorithmIntuition;

    // 개선점 (JSON)
    private String pitfalls;
    private String improvements;

    // 핵심 블록 (JSON)
    private String keyBlocks;

    // 리팩토링 제안
    private boolean refactorProvided;
    private String refactorCode;
    private String refactorExplanation;

    // 점수 (0-100)
    private Integer score;

    // Full JSON Response (for UI rendering)
    private String fullResponse;

    private LocalDateTime analyzedAt;
}
