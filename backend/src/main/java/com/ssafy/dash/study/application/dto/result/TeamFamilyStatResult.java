package com.ssafy.dash.study.application.dto.result;

/**
 * 팀 패밀리 통계 Result DTO
 */
public class TeamFamilyStatResult {
    private final String familyKey;
    private final String familyName;
    private int solved = 0;

    public TeamFamilyStatResult(String familyKey, String familyName) {
        this.familyKey = familyKey;
        this.familyName = familyName;
    }

    public void addStats(Integer addSolved) {
        this.solved += (addSolved != null ? addSolved : 0);
    }

    public String getFamilyKey() {
        return familyKey;
    }

    public String getFamilyName() {
        return familyName;
    }

    public int getSolved() {
        return solved;
    }

    public int getTotal() {
        return solved; // AlgorithmRadarChart에서 상대적 비교 용도
    }

    public double getCompletionRate() {
        return solved; // 절대값 반환
    }
}
