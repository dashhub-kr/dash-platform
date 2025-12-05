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
        LocalDateTime updatedAt
) {

    public static AlgorithmRecordResult from(AlgorithmRecord record) {
        return new AlgorithmRecordResult(
                record.getId(),
                record.getUserId(),
                record.getProblemNumber(),
                record.getTitle(),
                record.getCode(),
                record.getLanguage(),
                record.getPlatform(),
                record.getDifficulty(),
                record.getRuntimeMs(),
                record.getMemoryKb(),
                record.getRepositoryName(),
                record.getFilePath(),
                record.getCommitSha(),
                record.getCommitMessage(),
                record.getCommittedAt(),
                record.getCreatedAt(),
                record.getUpdatedAt()
            );
    }

}
