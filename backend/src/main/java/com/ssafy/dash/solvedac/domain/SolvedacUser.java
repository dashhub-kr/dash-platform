package com.ssafy.dash.solvedac.domain;

public record SolvedacUser(
    String handle,
    String bio,
    int tier,
    int rating,
    int classLevel,
    int maxStreak,
    String profileImageUrl,
    int solvedCount
) {}
