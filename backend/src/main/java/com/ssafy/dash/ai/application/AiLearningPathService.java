package com.ssafy.dash.ai.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.ai.infrastructure.client.AiServerClient;
import com.ssafy.dash.ai.infrastructure.client.dto.request.LearningPathRequest;
import com.ssafy.dash.ai.infrastructure.client.dto.response.LearningPathResponse;
import com.ssafy.dash.ai.domain.LearningPathCache;
import com.ssafy.dash.ai.infrastructure.persistence.LearningPathCacheMapper;
import com.ssafy.dash.ai.presentation.dto.LearningDashboardResponse;
import com.ssafy.dash.analytics.domain.UserClassStat;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserClassStatMapper;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import com.ssafy.dash.problem.domain.Tag;
import com.ssafy.dash.problem.domain.CoreTagsConfig;
import com.ssafy.dash.problem.infrastructure.persistence.TagMapper;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
        private final com.ssafy.dash.analytics.application.SolvedacSyncService solvedacSyncService;
        private final com.ssafy.dash.problem.application.TagService tagService;

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
                                        cacheMapper.update(userId, updatedJson, today);
                                }

                                // 4. Force Korean Names (Display Name) - Apply to cache as well
                                translateTagsToKorean(response);

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

                // 4. Force Korean Names (Display Name)
                translateTagsToKorean(response);

                return response;
        }

        private void translateTagsToKorean(LearningDashboardResponse response) {
                if (response == null || response.getAiAnalysis() == null)
                        return;

                LearningPathResponse analysis = response.getAiAnalysis();

                // 1. Recommended Tags
                if (analysis.getRecommendedTags() != null) {
                        List<String> translated = analysis.getRecommendedTags().stream()
                                        .map(tag -> tagService.getKoreanName(tag))
                                        .toList();
                        analysis.setRecommendedTags(translated);
                }

                // 2. Phases Focus Tags
                if (analysis.getPhases() != null) {
                        for (LearningPathResponse.LearningPhase phase : analysis.getPhases()) {
                                if (phase.getFocusTags() != null) {
                                        List<String> translated = phase.getFocusTags().stream()
                                                        .map(tag -> tagService.getKoreanName(tag))
                                                        .toList();
                                        phase.setFocusTags(translated);
                                }
                        }
                }
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

                // 사용자 티어 기반 레이팅 (비교 기준)
                int userTierRating = user.getSolvedacTier() != null ? user.getSolvedacTier() * 100 : 0;

                // 분석 대상 태그 (Core + Basic) 전체 조회
                List<Tag> candidateTags = tagMapper.findCandidateTags();
                java.util.Map<String, UserTagStat> statMap = tagStats.stream()
                                .collect(java.util.stream.Collectors.toMap(UserTagStat::getTagKey,
                                                java.util.function.Function.identity()));

                // 모든 후보 태그에 대해 통계 확보 (없으면 기본값 0)
                List<UserTagStat> fullStats = candidateTags.stream()
                                .map(tag -> statMap.getOrDefault(tag.getTagKey(),
                                                UserTagStat.builder()
                                                                .userId(user.getId())
                                                                .tagKey(tag.getTagKey())
                                                                .total(0)
                                                                .solved(0)
                                                                .rating(0)
                                                                .isBasic(tag.getIsBasic())
                                                                .build()))
                                .toList();

                // 약점 태그:
                // 1. 유저 수준에 맞는 태그 (Bronze: Basic, Silver+: Core)
                // 2. 레이팅이 낮은 순 (0점 포함 -> 미학습 분야가 최우선 약점)
                List<LearningPathRequest.TagStats> weaknessTags = fullStats.stream()
                                .filter(t -> isAppropriateForUser(t, user.getSolvedacTier()))
                                // rating > 0 필터 제거: 아예 안 푼(0점) 태그도 약점으로 추천해야 함
                                .sorted(Comparator.comparing(UserTagStat::getRating)
                                                .thenComparing(UserTagStat::getSolved)) // 레이팅 같으면 푼 문제 적은 순
                                .limit(5)
                                .map(t -> {
                                        Tag tag = tagMapper.findTagByKey(t.getTagKey());
                                        return LearningPathRequest.TagStats.builder()
                                                        .tagKey(t.getTagKey())
                                                        .tagName(tag != null ? tag.getKoreanName() : t.getTagKey())
                                                        .bojTagId(tag != null ? tag.getBojTagId() : null)
                                                        .solved(t.getSolved())
                                                        .total(t.getTotal())
                                                        .rating(t.getRating())
                                                        .build();
                                })
                                .toList();

                // 강점 태그: 레이팅이 높은 순 (진짜 잘하는 분야)
                // 강점은 무조건 레이팅이 있어야 함 (0점은 강점이 아님)
                List<LearningPathRequest.TagStats> strengthTags = fullStats.stream()
                                .filter(t -> t.getRating() > 0)
                                .sorted(Comparator.comparing(UserTagStat::getRating).reversed())
                                .limit(5)
                                .map(t -> {
                                        Tag tag = tagMapper.findTagByKey(t.getTagKey());
                                        return LearningPathRequest.TagStats.builder()
                                                        .tagKey(t.getTagKey())
                                                        .tagName(tag != null ? tag.getKoreanName() : t.getTagKey())
                                                        .bojTagId(tag != null ? tag.getBojTagId() : null)
                                                        .solved(t.getSolved())
                                                        .total(t.getTotal())
                                                        .rating(t.getRating())
                                                        .build();
                                })
                                .toList();

                // 클래스 통계 (Forward Progression 적용)
                // 가장 높은 완성된 클래스 번호 찾기
                int maxClearedClass = classStats.stream()
                                .filter(c -> c.getEssentialCompletionRate() >= 100)
                                .mapToInt(UserClassStat::getClassNumber)
                                .max()
                                .orElse(0);

                // AI에게는 이미 완성한 클래스보다 높은 것만 전달
                List<LearningPathRequest.ClassStats> classStatsDto = classStats.stream()
                                .filter(c -> c.getClassNumber() > maxClearedClass) // Forward Progression!
                                .map(c -> LearningPathRequest.ClassStats.builder()
                                                .classNumber(c.getClassNumber())
                                                .essentialSolved(c.getEssentialSolved())
                                                .essentials(c.getEssentials())
                                                .decoration(c.getDecoration())
                                                .build())
                                .toList();

                // solved.ac API에서 가져온 정확한 solvedCount 사용
                int totalSolved = user.getSolvedCount() != null ? user.getSolvedCount() : 0;

                // Top 100 레벨 실시간 계산
                int avgTop100 = solvedacSyncService.fetchTop100AverageLevel(user.getSolvedacHandle());

                return LearningPathRequest.builder()
                                .currentLevel(currentLevel)
                                .tier(user.getSolvedacTier())
                                .goalLevel(goalLevel)
                                .solvedCount(totalSolved)
                                .weaknessTags(weaknessTags)
                                .strengthTags(strengthTags)
                                .classStats(classStatsDto)
                                .bubbleIndex(calculateBubbleIndex(user, avgTop100))
                                .avgTop100Level(avgTop100)
                                .build();
        }

        private Integer calculateBubbleIndex(User user, int avgTop100) {
                Integer tier = user.getSolvedacTier();
                if (tier == null || avgTop100 == 0)
                        return null;
                return tier - avgTop100;
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
                // 1. 가장 높은 '완성(100%)' 클래스 번호를 찾음
                int maxClearedClass = classStats.stream()
                                .filter(c -> c.getEssentialCompletionRate() >= 100)
                                .mapToInt(UserClassStat::getClassNumber)
                                .max()
                                .orElse(0); // 완성한 게 없으면 0

                // 2. 그보다 높은 클래스 중에서 가장 낮은 '미완성' 클래스를 찾음
                return classStats.stream()
                                .filter(c -> c.getClassNumber() > maxClearedClass) // 이미 깬 레벨보다 높은 것만
                                .filter(c -> c.getEssentialCompletionRate() < 100)
                                .min(Comparator.comparing(UserClassStat::getClassNumber))
                                .map(c -> String.format("Class %d 에센셜 완성 (남은 문제: %d개)",
                                                c.getClassNumber(), c.getEssentials() - c.getEssentialSolved()))
                                .orElse("다음 클래스에 도전하기!");
        }

        private LearningDashboardResponse.DailyChallenge determineDailyChallenge(List<UserClassStat> classStats) {
                // 1. 가장 높은 '완성(100%)' 클래스 번호를 찾음
                int maxClearedClass = classStats.stream()
                                .filter(c -> c.getEssentialCompletionRate() >= 100)
                                .mapToInt(UserClassStat::getClassNumber)
                                .max()
                                .orElse(0);

                // 2. 그보다 높은 클래스 중에서 가장 낮은 '미완성' 클래스를 찾음
                return classStats.stream()
                                .filter(c -> c.getClassNumber() > maxClearedClass)
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
                // 1. 가장 높은 '완성(100%)' 클래스 번호를 찾음
                int maxClearedClass = classStats.stream()
                                .filter(c -> c.getEssentials() > 0 && c.getEssentialSolved() >= c.getEssentials())
                                .mapToInt(LearningPathRequest.ClassStats::getClassNumber)
                                .max()
                                .orElse(0);

                // 2. 그보다 높은 클래스 중에서 가장 낮은 '미완성' 클래스를 찾음
                return classStats.stream()
                                .filter(c -> c.getClassNumber() > maxClearedClass)
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

        /**
         * 유저 티어에 따라 태그가 적절한지 판단
         * Bronze 이하(Tier < 6)인 경우 기초 태그만 허용 (DB isBasic 필드 사용)
         */
        private boolean isAppropriateForUser(UserTagStat stat, Integer userTier) {
                // 티어 정보가 없거나 Bronze 이하인 경우
                if (userTier == null || userTier < 6) {
                        return Boolean.TRUE.equals(stat.getIsBasic());
                }
                // Silver 이상은 모든 Core Tag 허용
                return true;
        }

}
