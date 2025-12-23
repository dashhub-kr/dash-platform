package com.ssafy.dash.problem.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 그래프 스킬트리 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillGraphResponse {

    private List<Node> nodes;
    private List<Edge> edges;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Node {
        private String id; // tagKey
        private String label; // 한글 이름
        private String tier; // S, A, B
        private Long familyId; // 패밀리 ID
        private Integer solved; // 사용자 푼 문제 수
        private String masteryLevel; // 마스터리 레벨
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Edge {
        private String source; // 선수 태그 (from)
        private String target; // 현재 태그 (to)
        private Integer strength; // 1=권장, 2=필수
    }
}
