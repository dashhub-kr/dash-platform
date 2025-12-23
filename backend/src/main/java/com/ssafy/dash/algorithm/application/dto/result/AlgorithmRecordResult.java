package com.ssafy.dash.algorithm.application.dto.result;

import java.time.LocalDateTime;

import com.ssafy.dash.algorithm.domain.AlgorithmRecord;

public record AlgorithmRecordResult(
        Long id,
        Long userId,
        String username,
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
        String tag,
        Integer defenseStreak,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        // Analysis Fields
        Integer score,
        String timeComplexity,
        String spaceComplexity,
        String complexityExplanation,
        String patterns,
        String algorithmIntuition,
        String pitfalls,
        String keyBlocks,
        Boolean refactorProvided,
        String refactorCode,
        String refactorExplanation) {

    public static AlgorithmRecordResult from(AlgorithmRecord record) {
        // NOTE: Analysis result is not directly on AlgorithmRecord entity yet (OneToOne
        // relation needed or fetch join)
        // For now, initializing null. Service layer must populate this.
        return new AlgorithmRecordResult(
                record.getId(),
                record.getUserId(),
                record.getUsername(),
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
                record.getTag(),
                record.getDefenseStreak(),
                record.getCreatedAt(),
                record.getUpdatedAt(),
                record.getScore(),
                record.getTimeComplexity(),
                record.getSpaceComplexity(),
                record.getComplexityExplanation(),
                record.getPatterns(),
                record.getAlgorithmIntuition(),
                record.getPitfalls(),
                record.getKeyBlocks(),
                record.getRefactorProvided(),
                record.getRefactorCode(),
                record.getRefactorExplanation());
    }

}
