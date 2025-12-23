package com.ssafy.dash.problem.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 스킬 트리 노드 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillTreeNode {
    private String key; // 태그 키 (예: "implementation")
    private String name; // 한글 이름 (예: "구현")
    private String familyKey; // 부모 Family 키 (예: "IMPLEMENTATION")
    private String tier; // 중요도 티어 (S, A, B, C)
    private boolean isCore; // 핵심 태그 여부
    private Integer bojTagId; // 백준 태그 ID (정수형)
    private Integer solved; // 사용자가 푼 문제 수
    private Integer total; // 전체 문제 수
    private Double progressPercent; // 진행률 (0-100)
    private String masteryLevel; // 숙련도 레벨 (BEGINNER, INTERMEDIATE, ADVANCED, MASTER)
    private List<SkillTreeNode> children; // 하위 태그 (Family인 경우)
}
