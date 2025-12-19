package com.ssafy.dash.problem.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TagFamily {
    private Long id;
    private String familyKey;
    private String name;
    private Integer orderIndex;

    private TagFamily(String familyKey, String name, Integer orderIndex) {
        this.familyKey = familyKey;
        this.name = name;
        this.orderIndex = orderIndex;
    }

    public static TagFamily create(String familyKey, String name, Integer orderIndex) {
        return new TagFamily(familyKey, name, orderIndex);
    }
}
