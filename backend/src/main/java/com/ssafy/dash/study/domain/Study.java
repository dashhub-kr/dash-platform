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

    public Study(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static Study create(String name) {
        return new Study(null, name, LocalDateTime.now());
    }
}
