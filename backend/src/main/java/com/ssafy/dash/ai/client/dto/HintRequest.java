package com.ssafy.dash.ai.client.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 힌트 생성 요청 DTO
 */
@Getter
@Builder
public class HintRequest {
    private String problemNumber;
    private String problemTitle;
    private int level; // 1: 알고리즘 유형, 2: 접근법, 3: 상세 가이드
    private UserContext userContext;

    @Getter
    @Builder
    public static class UserContext {
        private List<String> weakTags;
        private int solvedCount;
        private int tier;
        private String tierName;
    }
}
