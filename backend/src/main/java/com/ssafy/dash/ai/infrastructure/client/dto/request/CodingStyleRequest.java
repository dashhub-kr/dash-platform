package com.ssafy.dash.ai.infrastructure.client.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 코딩 스타일 분석 요청 DTO
 */
@Getter
@Builder
public class CodingStyleRequest {
    private List<CodeSample> codeSamples; // 분석할 코드 샘플들
    private UserStats userStats; // 사용자 통계

    @Getter
    @Builder
    public static class CodeSample {
        private String code;
        private String language;
        private String problemNumber;
        private int runtimeMs;
        private int memoryKb;
    }

    @Getter
    @Builder
    public static class UserStats {
        private int totalSolved;
        private double avgRuntime;
        private double avgMemory;
        private List<String> preferredTags;
        private String tier;
    }
}
