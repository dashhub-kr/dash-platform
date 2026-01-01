package com.ssafy.dash.onboarding.presentation.dto.response;

import com.ssafy.dash.solvedac.domain.dto.SolvedacUserResponse;
import lombok.Builder;
import lombok.Getter;

/**
 * Solved.ac 핸들 검증 응답 DTO
 */
@Getter
@Builder
public class SolvedacVerifyResponse {
    private String handle;
    private Integer tier;
    private String tierName;
    private String profileImageUrl;
    private Integer solvedCount;
    private Integer classLevel;
    private Integer rating;

    private static final String[] TIER_NAMES = { "Unrated",
            "Bronze V", "Bronze IV", "Bronze III", "Bronze II", "Bronze I",
            "Silver V", "Silver IV", "Silver III", "Silver II", "Silver I",
            "Gold V", "Gold IV", "Gold III", "Gold II", "Gold I",
            "Platinum V", "Platinum IV", "Platinum III", "Platinum II", "Platinum I",
            "Diamond V", "Diamond IV", "Diamond III", "Diamond II", "Diamond I",
            "Ruby V", "Ruby IV", "Ruby III", "Ruby II", "Ruby I",
            "Master" };

    public static SolvedacVerifyResponse from(SolvedacUserResponse userResponse) {
        int tier = userResponse.getTier() != null ? userResponse.getTier() : 0;
        String tierName = tier >= 0 && tier < TIER_NAMES.length ? TIER_NAMES[tier] : "Unknown";

        return SolvedacVerifyResponse.builder()
                .handle(userResponse.getHandle())
                .tier(tier)
                .tierName(tierName)
                .profileImageUrl(userResponse.getProfileImageUrl())
                .solvedCount(userResponse.getSolvedCount())
                .classLevel(userResponse.getClassLevel())
                .rating(userResponse.getRating())
                .build();
    }
}
