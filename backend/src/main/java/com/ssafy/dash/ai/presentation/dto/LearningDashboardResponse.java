package com.ssafy.dash.ai.presentation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ssafy.dash.ai.client.dto.LearningPathResponse;
import com.ssafy.dash.ai.client.dto.LearningPathRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 프론트엔드 대시보드용 종합 응답 DTO
 * AI 분석 결과(AiServer) + 통계 데이터(DB)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LearningDashboardResponse {

    // AI 서버 분석 결과
    private LearningPathResponse aiAnalysis;

    // 대시보드 시각화를 위한 통계 데이터 (from DB)
    private String currentLevel;
    private String goalLevel;
    private int solvedCount;
    private List<LearningPathRequest.TagStats> weaknessTags;
    private List<LearningPathRequest.TagStats> strengthTags;
    private List<LearningPathRequest.ClassStats> classStats;
}
