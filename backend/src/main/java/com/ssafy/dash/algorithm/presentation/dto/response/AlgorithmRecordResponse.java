package com.ssafy.dash.algorithm.presentation.dto.response;

import java.time.LocalDateTime;

import com.ssafy.dash.algorithm.application.dto.result.AlgorithmRecordResult;

public record AlgorithmRecordResponse(
        Long id,
        Long userId,
        String problemNumber,
        String title,
        String code,
        String language,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AlgorithmRecordResponse from(AlgorithmRecordResult result) {
        return new AlgorithmRecordResponse(result.id(), result.userId(), result.problemNumber(), result.title(), result.code(), result.language(), result.createdAt(), result.updatedAt());
    }

}
