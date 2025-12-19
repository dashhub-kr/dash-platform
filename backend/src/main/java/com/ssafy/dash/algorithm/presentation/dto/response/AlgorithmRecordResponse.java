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
        String platform,
        String difficulty,
        Integer runtimeMs,
        Integer memoryKb,
        String repositoryName,
        String filePath,
        String commitSha,
        String commitMessage,
        LocalDateTime committedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        // Analysis Fields
        Integer score,
        String timeComplexity,
        String complexityExplanation,
        String patterns
) {

    public static AlgorithmRecordResponse from(AlgorithmRecordResult result) {
        return new AlgorithmRecordResponse(
                result.id(),
                result.userId(),
                result.problemNumber(),
                result.title(),
                result.code(),
                result.language(),
                result.platform(),
                result.difficulty(),
                result.runtimeMs(),
                result.memoryKb(),
                result.repositoryName(),
                result.filePath(),
                result.commitSha(),
                result.commitMessage(),
                result.committedAt(),
                result.createdAt(),
                result.updatedAt(),
                result.score(),
                result.timeComplexity(),
                result.complexityExplanation(),
                result.patterns()
            );
    }

}
