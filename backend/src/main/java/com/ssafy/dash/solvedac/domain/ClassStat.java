package com.ssafy.dash.solvedac.domain;

public record ClassStat(
    int classNumber,
    int total,
    int totalSolved,
    int essentials,
    int essentialSolved,
    String decoration
) {}
