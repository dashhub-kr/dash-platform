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
        Long elapsedTimeSeconds,
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
        String refactorExplanation,
        String fullResponse,
        // Counter Example Fields
        String counterExampleInput,
        String counterExampleExpected,
        String counterExamplePredicted,
        String counterExampleReason) {

    public static AlgorithmRecordResult from(AlgorithmRecord record) {
        // 분석 결과는 엔티티에 없으므로 null로 두고 서비스 계층에서 채운다
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
                record.getElapsedTimeSeconds(),
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
                record.getRefactorExplanation(),
                record.getFullResponse(),
                record.getCounterExampleInput(),
                record.getCounterExampleExpected(),
                record.getCounterExamplePredicted(),
                record.getCounterExampleReason());
    }

}
