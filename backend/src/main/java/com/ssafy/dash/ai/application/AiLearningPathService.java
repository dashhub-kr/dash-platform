package com.ssafy.dash.ai.application;

import com.ssafy.dash.ai.client.AiServerClient;
import com.ssafy.dash.ai.client.dto.LearningPathRequest;
import com.ssafy.dash.ai.client.dto.LearningPathResponse;
import com.ssafy.dash.analytics.domain.UserClassStat;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserClassStatMapper;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * AI 기반 개인화 학습 경로 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiLearningPathService {

    private final AiServerClient aiClient;
    private final UserTagStatMapper tagStatMapper;
    private final UserClassStatMapper classStatMapper;
    private final UserRepository userRepository;

    /**
     * AI 개인화 학습 경로 생성
     */
    public LearningPathResponse generateAiLearningPath(Long userId) {
        log.info("Generating AI learning path for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<UserTagStat> tagStats = tagStatMapper.findByUserId(userId);
        List<UserClassStat> classStats = classStatMapper.findByUserId(userId);

        // 분석 데이터 수집
        LearningPathRequest request = collectAnalyticsData(user, tagStats, classStats);

        // AI 서버에 학습 경로 요청
        return aiClient.generateLearningPath(request);
    }

    private LearningPathRequest collectAnalyticsData(User user, List<UserTagStat> tagStats,
            List<UserClassStat> classStats) {
        // 현재 레벨
        String currentLevel = buildCurrentLevel(user);
        String goalLevel = determineNextGoal(classStats);

        // 약점 태그 (상위 5개)
        List<LearningPathRequest.TagStats> weaknessTags = tagStats.stream()
                .filter(t -> t.getSolved() > 0 && t.getSolved() < 10)
                .sorted(Comparator.comparing(UserTagStat::getSolved))
                .limit(5)
                .map(t -> LearningPathRequest.TagStats.builder()
                        .tagKey(t.getTagKey())
                        .tagName(t.getTagKey())
                        .solved(t.getSolved())
                        .total(t.getTotal())
                        .build())
                .toList();

        // 강점 태그 (상위 5개)
        List<LearningPathRequest.TagStats> strengthTags = tagStats.stream()
                .sorted(Comparator.comparing(UserTagStat::getSolved).reversed())
                .limit(5)
                .map(t -> LearningPathRequest.TagStats.builder()
                        .tagKey(t.getTagKey())
                        .tagName(t.getTagKey())
                        .solved(t.getSolved())
                        .total(t.getTotal())
                        .build())
                .toList();

        // 클래스 통계
        List<LearningPathRequest.ClassStats> classStatsDto = classStats.stream()
                .map(c -> LearningPathRequest.ClassStats.builder()
                        .classNumber(c.getClassNumber())
                        .essentialSolved(c.getEssentialSolved())
                        .essentials(c.getEssentials())
                        .decoration(c.getDecoration())
                        .build())
                .toList();

        return LearningPathRequest.builder()
                .currentLevel(currentLevel)
                .goalLevel(goalLevel)
                .solvedCount(user.getSolvedacRating() != null ? user.getSolvedacRating() : 0)
                .weaknessTags(weaknessTags)
                .strengthTags(strengthTags)
                .classStats(classStatsDto)
                .build();
    }

    private String buildCurrentLevel(User user) {
        String tier = getTierName(user.getSolvedacTier());
        Integer classLevel = user.getSolvedacClass();

        if (classLevel != null && classLevel > 0) {
            return String.format("%s, Class %d", tier, classLevel);
        }
        return tier;
    }

    private String getTierName(Integer tier) {
        if (tier == null || tier == 0)
            return "Unrated";
        String[] tiers = { "Bronze", "Silver", "Gold", "Platinum", "Diamond", "Ruby" };
        String[] levels = { "V", "IV", "III", "II", "I" };
        int tierIndex = (tier - 1) / 5;
        int levelIndex = (tier - 1) % 5;
        if (tierIndex >= tiers.length)
            return "Master";
        return tiers[tierIndex] + " " + levels[levelIndex];
    }

    private String determineNextGoal(List<UserClassStat> classStats) {
        return classStats.stream()
                .filter(c -> c.getEssentialCompletionRate() < 100)
                .min(Comparator.comparing(UserClassStat::getClassNumber))
                .map(c -> String.format("Class %d 에센셜 완성 (남은 문제: %d개)",
                        c.getClassNumber(), c.getEssentials() - c.getEssentialSolved()))
                .orElse("다음 클래스에 도전하기!");
    }

}
