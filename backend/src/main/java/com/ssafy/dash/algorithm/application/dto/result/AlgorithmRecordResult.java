package com.ssafy.dash.algorithm.application.dto.result;

import java.time.LocalDateTime;

import com.ssafy.dash.algorithm.domain.AlgorithmRecord;

public record AlgorithmRecordResult(
        Long id,
        Long userId,
        String problemNumber,
        String title,
        String code,
        String language,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AlgorithmRecordResult from(AlgorithmRecord record) {
        return new AlgorithmRecordResult(record.getId(), record.getUserId(), record.getProblemNumber(), record.getTitle(), record.getCode(), record.getLanguage(), record.getCreatedAt(), record.getUpdatedAt());
    }

}
