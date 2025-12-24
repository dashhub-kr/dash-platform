package com.ssafy.dash.ai.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.ai.client.AiServerClient;
import com.ssafy.dash.ai.client.dto.LearningPathRequest;
import com.ssafy.dash.ai.client.dto.LearningPathResponse;
import com.ssafy.dash.ai.domain.LearningPathCache;
import com.ssafy.dash.ai.infrastructure.persistence.LearningPathCacheMapper;
import com.ssafy.dash.ai.presentation.dto.LearningDashboardResponse;
import com.ssafy.dash.analytics.domain.UserClassStat;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserClassStatMapper;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import com.ssafy.dash.problem.domain.Tag;
import com.ssafy.dash.problem.infrastructure.persistence.TagMapper;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        private final TagMapper tagMapper;
        private final UserRepository userRepository;
        private final LearningPathCacheMapper cacheMapper;
        private final ObjectMapper objectMapper;

        /**
         * AI 개인화 학습 경로 생성 (대시보드용 종합 데이터 반환)
         * 하루에 한 번만 새로 생성하고, 이후에는 캐시된 데이터를 반환합니다.
         */
        public LearningDashboardResponse generateAiLearningPath(Long userId) {
                log.info("Generating AI learning path for user: {}", userId);

                // 1. Check cache
                LearningPathCache cache = cacheMapper.findByUserId(userId);
                LocalDate today = LocalDate.now();

                if (cache != null && cache.getGeneratedAt().equals(today)) {
                        log.info("Returning cached learning path for user: {} (generated at: {})", userId,
                                        cache.getGeneratedAt());
                        try {
                                LearningDashboardResponse response = objectMapper.readValue(cache.getAnalysisJson(),
                                                LearningDashboardResponse.class);

                                // 1-1. Patch missing Daily Challenge (for backward compatibility)
                                if (response.getDailyChallenge() == null && response.getClassStats() != null) {
                                        LearningDashboardResponse.DailyChallenge challenge = determineDailyChallengeFromDto(
                                                        response.getClassStats());
                                        response.setDailyChallenge(challenge);

                                        // Update cache with patched data
                                        String updatedJson = objectMapper.writeValueAsString(response);
                                        cacheMapper.update(userId, updatedJson, today);
                                }

                                return response;
                        } catch (Exception e) {
                                log.warn("Failed to parse or patch cached data, regenerating: {}", e.getMessage());
                        }
                }

                // 2. Generate fresh analysis
                LearningDashboardResponse response = generateFreshAnalysis(userId);

                // 3. Save/Update cache
                try {
                        String json = objectMapper.writeValueAsString(response);
                        if (cache == null) {
                                cacheMapper.save(LearningPathCache.builder()
                                                .userId(userId)
                                                .analysisJson(json)
                                                .generatedAt(today)
                                                .build());
                                log.info("Created new learning path cache for user: {}", userId);
                        } else {
                                cacheMapper.update(userId, json, today);
                                log.info("Updated learning path cache for user: {}", userId);
                        }
                } catch (Exception e) {
                        log.error("Failed to save cache: {}", e.getMessage());
                }

                return response;
        }

        /**
         * 실제 AI 분석을 수행하는 내부 메서드
         */
        private LearningDashboardResponse generateFreshAnalysis(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

                List<UserTagStat> tagStats = tagStatMapper.findByUserId(userId);
                List<UserClassStat> classStats = classStatMapper.findByUserId(userId);

                // 분석 데이터 수집
                LearningPathRequest request = collectAnalyticsData(user, tagStats, classStats);

                // AI 서버에 학습 경로 요청
                LearningPathResponse aiResponse = aiClient.generateLearningPath(request);

                // Daily Challenge 생성
                LearningDashboardResponse.DailyChallenge dailyChallenge = determineDailyChallenge(classStats);

                // 종합 응답 생성
                return LearningDashboardResponse.builder()
                                .aiAnalysis(aiResponse)
                                .currentLevel(request.getCurrentLevel())
                                .goalLevel(request.getGoalLevel())
                                .solvedCount(request.getSolvedCount())
                                .weaknessTags(request.getWeaknessTags())
                                .strengthTags(request.getStrengthTags())
                                .classStats(request.getClassStats())
                                .dailyChallenge(dailyChallenge)
                                .build();
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
                                .map(t -> {
                                        Tag tag = tagMapper.findTagByKey(t.getTagKey());
                                        return LearningPathRequest.TagStats.builder()
                                                        .tagKey(t.getTagKey())
                                                        .tagName(tag != null ? tag.getKoreanName() : t.getTagKey())
                                                        .bojTagId(tag != null ? tag.getBojTagId() : null)
                                                        .solved(t.getSolved())
                                                        .total(t.getTotal())
                                                        .build();
                                })
                                .toList();

                // 강점 태그 (상위 5개)
                List<LearningPathRequest.TagStats> strengthTags = tagStats.stream()
                                .sorted(Comparator.comparing(UserTagStat::getSolved).reversed())
                                .limit(5)
                                .map(t -> {
                                        Tag tag = tagMapper.findTagByKey(t.getTagKey());
                                        return LearningPathRequest.TagStats.builder()
                                                        .tagKey(t.getTagKey())
                                                        .tagName(tag != null ? tag.getKoreanName() : t.getTagKey())
                                                        .bojTagId(tag != null ? tag.getBojTagId() : null)
                                                        .solved(t.getSolved())
                                                        .total(t.getTotal())
                                                        .build();
                                })
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

                // solved.ac API에서 가져온 정확한 solvedCount 사용
                int totalSolved = user.getSolvedCount() != null ? user.getSolvedCount() : 0;

                return LearningPathRequest.builder()
                                .currentLevel(currentLevel)
                                .tier(user.getSolvedacTier())
                                .goalLevel(goalLevel)
                                .solvedCount(totalSolved)
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

        private LearningDashboardResponse.DailyChallenge determineDailyChallenge(List<UserClassStat> classStats) {
                return classStats.stream()
                                .filter(c -> c.getEssentialCompletionRate() < 100)
                                .min(Comparator.comparing(UserClassStat::getClassNumber))
                                .map(c -> LearningDashboardResponse.DailyChallenge.builder()
                                                .title(String.format("Class %d 에센셜 완성 (남은 문제: %d개)",
                                                                c.getClassNumber(),
                                                                c.getEssentials() - c.getEssentialSolved()))
                                                .description("다음 단계로 나아가기 위한 도전입니다.")
                                                .targetClass(c.getClassNumber())
                                                .remainingProblems(c.getEssentials() - c.getEssentialSolved())
                                                .link(String.format("https://solved.ac/class/%d", c.getClassNumber()))
                                                .build())
                                .orElse(LearningDashboardResponse.DailyChallenge.builder()
                                                .title("모든 클래스 정복 완료!")
                                                .description("새로운 목표를 찾아보세요.")
                                                .link("https://solved.ac/classes")
                                                .build());
        }

        private LearningDashboardResponse.DailyChallenge determineDailyChallengeFromDto(
                        List<LearningPathRequest.ClassStats> classStats) {
                return classStats.stream()
                                .filter(c -> c.getEssentials() > 0 && c.getEssentialSolved() < c.getEssentials())
                                .min(Comparator.comparing(LearningPathRequest.ClassStats::getClassNumber))
                                .map(c -> LearningDashboardResponse.DailyChallenge.builder()
                                                .title(String.format("Class %d 에센셜 완성 (남은 문제: %d개)",
                                                                c.getClassNumber(),
                                                                c.getEssentials() - c.getEssentialSolved()))
                                                .description("다음 단계로 나아가기 위한 도전입니다.")
                                                .targetClass(c.getClassNumber())
                                                .remainingProblems(c.getEssentials() - c.getEssentialSolved())
                                                .link(String.format("https://solved.ac/class/%d", c.getClassNumber()))
                                                .build())
                                .orElse(LearningDashboardResponse.DailyChallenge.builder()
                                                .title("모든 클래스 정복 완료!")
                                                .description("새로운 목표를 찾아보세요.")
                                                .link("https://solved.ac/classes")
                                                .build());
        }

}
