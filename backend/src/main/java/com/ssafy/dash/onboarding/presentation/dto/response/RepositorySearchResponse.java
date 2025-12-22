package com.ssafy.dash.onboarding.presentation.dto.response;

import com.ssafy.dash.github.domain.RepositoryInfo;

/**
 * 저장소 검색 결과 응답 DTO
 */
public record RepositorySearchResponse(
        String fullName,
        String name,
        String description,
        boolean isPrivate) {
    public static RepositorySearchResponse from(RepositoryInfo info) {
        return new RepositorySearchResponse(
                info.fullName(),
                info.name(),
                info.description(),
                info.isPrivate());
    }
}
