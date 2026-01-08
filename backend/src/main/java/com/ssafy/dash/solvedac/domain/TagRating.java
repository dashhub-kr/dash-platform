package com.ssafy.dash.solvedac.domain;

public record TagRating(
        String tagKey,
        int rating,
        int solvedCount) {
}
