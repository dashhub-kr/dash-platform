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
    private LocalDateTime createdAt;
    private Integer memberCount; // 회원수 (조회용)
    private Integer acornCount;

    public Study(Long id, String name, LocalDateTime createdAt, Integer acornCount) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.acornCount = acornCount == null ? 0 : acornCount;
    }

    public static Study create(String name) {
        return new Study(null, name, LocalDateTime.now(), 0);
    }

    public void addAcorns(int amount) {
        if (this.acornCount == null) this.acornCount = 0;
        this.acornCount += amount;
    }

    public void useAcorns(int amount) {
        if (this.acornCount == null) this.acornCount = 0;
        if (this.acornCount < amount) {
            throw new IllegalArgumentException("Not enough acorns");
        }
        this.acornCount -= amount;
    }
}
