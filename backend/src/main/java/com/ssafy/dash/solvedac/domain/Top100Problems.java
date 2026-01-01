package com.ssafy.dash.solvedac.domain;

import java.util.List;

public record Top100Problems(
    int count,
    List<Problem> items
) {
    public record Problem(
        String problemId,
        String titleKo,
        int level,
        List<Tag> tags
    ) {}

    public record Tag(
        String key,
        String bojTagId,
        List<DisplayName> displayNames
    ) {}

    public record DisplayName(
        String language,
        String name
    ) {}
}
