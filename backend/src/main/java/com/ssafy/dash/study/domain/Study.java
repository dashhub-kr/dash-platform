package com.ssafy.dash.study.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Study {

    private Long id;
    private String name;
    private Long creatorId;
    private LocalDateTime createdAt;
    private Integer memberCount; // 회원수 (조회용)
    private Integer acornCount;
    private Double averageTier; // 평균 티어 (조회용)
    private Integer totalSolved; // 총 문제 풀이 수 (조회용)
    private String mvpUsername; // MVP 사용자명 (조회용)

    public Study(Long id, String name, Long creatorId, LocalDateTime createdAt, Integer acornCount) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.acornCount = acornCount == null ? 0 : acornCount;
    }

    public static Study create(String name, Long creatorId) {
        return new Study(null, name, creatorId, LocalDateTime.now(), 0);
    }

    public void addAcorns(int amount) {
        if (this.acornCount == null)
            this.acornCount = 0;
        this.acornCount += amount;
    }

    public void useAcorns(int amount) {
        if (this.acornCount == null)
            this.acornCount = 0;
        if (this.acornCount < amount) {
            throw new IllegalArgumentException("Not enough acorns");
        }
        this.acornCount -= amount;
    }
}
