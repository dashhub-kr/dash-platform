package com.ssafy.dash.ai.client.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * AI 학습 경로 추천 요청 DTO
 * AI 서버 LearningPathRequest 스키마와 매핑
 */
@Getter
@Builder
public class LearningPathRequest {
    private String currentLevel; // 현재 레벨 (예: "Gold V")
    private int solvedCount; // 총 푼 문제 수
    private String goalLevel; // 목표 레벨 (Optional)
    private List<TagStats> strengthTags; // 강점 태그 목록
    private List<TagStats> weaknessTags; // 약점 태그 목록
    private List<ClassStats> classStats; // 클래스 통계

    @Getter
    @Builder
    public static class TagStats {
        private String tagKey; // 태그 키
        private String tagName; // 태그명
        private Integer bojTagId; // 백준 태그 ID (정수형)
        private int solved; // 푼 문제 수
        private int total; // 전체 문제 수
    }

    @Getter
    @Builder
    public static class ClassStats {
        private int classNumber; // 클래스 번호 (1-10)
        private int essentialSolved; // 에센셜 푼 수
        private int essentials; // 에센셜 전체 수
        private String decoration; // 장식 (bronze/silver/gold)
    }
}
