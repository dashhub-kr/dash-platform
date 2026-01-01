package com.ssafy.dash.solvedac.domain;

import java.util.List;

public record TagStat(
    int count,
    List<Item> items
) {
    public record Item(
        TagInfo tag,
        int total,
        int solved,
        int partial,
        int tried
    ) {}

    public record TagInfo(
        String key,
        boolean isMeta,
        int bojTagId
    ) {}
}
