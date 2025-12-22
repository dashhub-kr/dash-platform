package com.ssafy.dash.github.domain;

/**
 * GitHub 저장소 정보 DTO
 */
public record RepositoryInfo(
        String fullName, // "owner/repo"
        String name, // "repo"
        String description, // 저장소 설명
        boolean isPrivate, // private 여부
        String defaultBranch // 기본 브랜치
) {
}
