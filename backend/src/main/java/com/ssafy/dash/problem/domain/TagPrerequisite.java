package com.ssafy.dash.problem.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 태그 선수 관계 (그래프 스킬트리용)
 */
@Getter
@Setter
@NoArgsConstructor
public class TagPrerequisite {
    private Long id;
    private String tagKey; // 현재 태그
    private String prerequisiteTagKey; // 선수 태그
    private Integer strength; // 1=권장, 2=필수
    private LocalDateTime createdAt;

    public static TagPrerequisite create(String tagKey, String prerequisiteTagKey, Integer strength) {
        TagPrerequisite tp = new TagPrerequisite();
        tp.tagKey = tagKey;
        tp.prerequisiteTagKey = prerequisiteTagKey;
        tp.strength = strength != null ? strength : 1;
        return tp;
    }
}
