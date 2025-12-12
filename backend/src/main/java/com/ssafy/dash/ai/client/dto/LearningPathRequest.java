package com.ssafy.dash.ai.client.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * AI 학습 경로 추천 요청 DTO
 */
@Getter
@Builder
public class LearningPathRequest {
    private String currentLevel; // "Gold V, Class 4"
    private String nextGoal; // "Class 5 에센셜 완성"
    private List<TagInfo> weaknessTags; // 약점 태그 목록
    private List<TagInfo> strengthTags; // 강점 태그 목록
    private List<ClassInfo> classProgress; // 클래스 진행도
    private int solvedCount; // 총 푼 문제 수
    private String balanceType; // BALANCED, FOCUSED, SPECIALIZED
    private String growthTrend; // GROWING, STABLE, SLOW, DECLINING

    @Getter
    @Builder
    public static class TagInfo {
        private String tagKey;
        private int solved;
        private int total;
    }

    @Getter
    @Builder
    public static class ClassInfo {
        private int classNumber;
        private int essentialSolved;
        private int essentials;
        private double completionRate;
    }
}
