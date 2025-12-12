package com.ssafy.dash.ai.application;

import com.ssafy.dash.ai.client.AiServerClient;
import com.ssafy.dash.ai.client.dto.HintRequest;
import com.ssafy.dash.ai.client.dto.HintResponse;
import com.ssafy.dash.analytics.application.UserSkillAnalysisService;
import com.ssafy.dash.analytics.application.dto.TagWeaknessDto;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 힌트 생성 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HintService {

    private final AiServerClient aiClient;
    private final UserSkillAnalysisService skillAnalysisService;
    private final UserRepository userRepository;

    /**
     * 사용자 맞춤 힌트 생성
     * 
     * @param userId        사용자 ID
     * @param problemNumber 문제 번호
     * @param problemTitle  문제 제목
     * @param level         힌트 레벨 (1: 유형, 2: 접근법, 3: 상세)
     * @return 생성된 힌트
     */
    public HintResponse generateHint(Long userId, String problemNumber, String problemTitle, int level) {
        log.info("Generating hint for user: {}, problem: {}, level: {}", userId, problemNumber, level);

        // 사용자 컨텍스트 수집
        HintRequest.UserContext userContext = collectUserContext(userId);

        // 힌트 요청 생성
        HintRequest request = HintRequest.builder()
                .problemNumber(problemNumber)
                .problemTitle(problemTitle)
                .level(level)
                .userContext(userContext)
                .build();

        // AI 서버에 힌트 요청
        return aiClient.generateHint(request);
    }

    private HintRequest.UserContext collectUserContext(Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        // 약점 태그 조회 (상위 5개)
        List<String> weakTags = skillAnalysisService.getWeaknessTags(userId)
                .stream()
                .limit(5)
                .map(TagWeaknessDto::getTagKey)
                .toList();

        int tier = user != null ? (user.getSolvedacTier() != null ? user.getSolvedacTier() : 0) : 0;

        return HintRequest.UserContext.builder()
                .weakTags(weakTags)
                .solvedCount(user != null && user.getSolvedacRating() != null ? user.getSolvedacRating() : 0)
                .tier(tier)
                .tierName(getTierName(tier))
                .build();
    }

    private String getTierName(int tier) {
        if (tier == 0)
            return "Unrated";
        String[] tiers = { "Bronze", "Silver", "Gold", "Platinum", "Diamond", "Ruby" };
        String[] levels = { "V", "IV", "III", "II", "I" };
        int tierIndex = (tier - 1) / 5;
        int levelIndex = (tier - 1) % 5;
        if (tierIndex >= tiers.length)
            return "Master";
        return tiers[tierIndex] + " " + levels[levelIndex];
    }
}
