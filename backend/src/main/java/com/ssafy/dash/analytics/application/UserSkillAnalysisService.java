package com.ssafy.dash.analytics.application;

import com.ssafy.dash.analytics.application.dto.SkillSummaryDto;
import com.ssafy.dash.analytics.application.dto.TagStrengthDto;
import com.ssafy.dash.analytics.application.dto.TagWeaknessDto;
import com.ssafy.dash.analytics.domain.UserClassStat;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserClassStatMapper;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 사용자 스킬 분석 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSkillAnalysisService {

    private final UserTagStatMapper tagStatMapper;
    private final UserClassStatMapper classStatMapper;
    private final UserRepository userRepository;

    // 태그 키 -> 한글 이름 매핑
    private static final Map<String, String> TAG_NAMES = Map.ofEntries(
            Map.entry("implementation", "구현"),
            Map.entry("math", "수학"),
            Map.entry("dp", "다이나믹 프로그래밍"),
            Map.entry("data_structures", "자료 구조"),
            Map.entry("graphs", "그래프 이론"),
            Map.entry("greedy", "그리디 알고리즘"),
            Map.entry("string", "문자열"),
            Map.entry("bruteforcing", "브루트포스"),
            Map.entry("sorting", "정렬"),
            Map.entry("bfs", "너비 우선 탐색"),
            Map.entry("dfs", "깊이 우선 탐색"),
            Map.entry("binary_search", "이분 탐색"),
            Map.entry("simulation", "시뮬레이션"),
            Map.entry("number_theory", "정수론"),
            Map.entry("graph_traversal", "그래프 탐색"),
            Map.entry("geometry", "기하학"),
            Map.entry("trees", "트리"),
            Map.entry("backtracking", "백트래킹"),
            Map.entry("recursion", "재귀"),
            Map.entry("combinatorics", "조합론"));

    // 중요한 필수 태그 목록
    private static final List<String> IMPORTANT_TAGS = Arrays.asList(
            "dp", "greedy", "graph_traversal", "data_structures",
            "implementation", "bfs", "dfs", "binary_search",
            "sorting", "math");

    /**
     * 강점 태그 TOP N
     */
    public List<TagStrengthDto> getStrengthTags(Long userId, int limit) {
        List<UserTagStat> allTags = tagStatMapper.findByUserId(userId);

        if (allTags.isEmpty()) {
            return Collections.emptyList();
        }

        int maxSolved = allTags.stream()
                .mapToInt(UserTagStat::getSolved)
                .max()
                .orElse(1);

        return allTags.stream()
                .sorted(Comparator.comparing(UserTagStat::getSolved).reversed())
                .limit(limit)
                .map(tag -> TagStrengthDto.builder()
                        .tagKey(tag.getTagKey())
                        .tagName(getTagName(tag.getTagKey()))
                        .solved(tag.getSolved())
                        .masteryLevel(tag.getMasteryLevel())
                        .relativeStrength(calculateRelativeStrength(tag.getSolved(), maxSolved))
                        .badge(tag.getMasteryBadge())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 약점 태그 (1-5문제 푼 태그)
     */
    public List<TagWeaknessDto> getWeaknessTags(Long userId) {
        List<UserTagStat> allTags = tagStatMapper.findByUserId(userId);

        return allTags.stream()
                .filter(tag -> tag.getSolved() > 0 && tag.getSolved() < 5)
                .sorted(Comparator.comparing(UserTagStat::getSolved))
                .limit(5)
                .map(tag -> TagWeaknessDto.builder()
                        .tagKey(tag.getTagKey())
                        .tagName(getTagName(tag.getTagKey()))
                        .solved(tag.getSolved())
                        .nextLevel(tag.getNextLevel())
                        .solvedToNextLevel(tag.getSolvedToNextLevel())
                        .recommendation(generateRecommendation(tag))
                        .suggestedProblems(Collections.emptyList()) // Phase 2에서 구현
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 미경험 중요 태그 추천
     */
    public List<String> getRecommendedTags(Long userId) {
        List<UserTagStat> userTags = tagStatMapper.findByUserId(userId);
        Set<String> experiencedTags = userTags.stream()
                .filter(tag -> tag.getSolved() > 0)
                .map(UserTagStat::getTagKey)
                .collect(Collectors.toSet());

        return IMPORTANT_TAGS.stream()
                .filter(tag -> !experiencedTags.contains(tag))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 종합 스킬 요약
     */
    public SkillSummaryDto getSkillSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<TagStrengthDto> strengths = getStrengthTags(userId, 5);
        List<TagWeaknessDto> weaknesses = getWeaknessTags(userId);
        List<String> recommendedTags = getRecommendedTags(userId);
        List<UserClassStat> classStats = classStatMapper.findByUserId(userId);

        // 총 푼 문제 수 계산
        List<UserTagStat> allTags = tagStatMapper.findByUserId(userId);
        int totalSolved = allTags.stream()
                .mapToInt(UserTagStat::getSolved)
                .max()
                .orElse(0);

        // 다음 목표 설정
        String nextGoal = determineNextGoal(classStats, weaknesses);
        String overallLevel = determineOverallLevel(strengths, weaknesses);

        return SkillSummaryDto.builder()
                .tier(getTierName(user.getSolvedacTier()))
                .tierNumber(user.getSolvedacTier())
                .classLevel(user.getSolvedacClass())
                .totalSolved(totalSolved)
                .rating(user.getSolvedacRating())
                .topStrengths(strengths)
                .weaknesses(weaknesses)
                .recommendedTags(recommendedTags)
                .overallLevel(overallLevel)
                .nextGoal(nextGoal)
                .build();
    }

    // === Helper Methods ===

    private String getTagName(String tagKey) {
        return TAG_NAMES.getOrDefault(tagKey, tagKey);
    }

    private Double calculateRelativeStrength(int solved, int maxSolved) {
        if (maxSolved == 0)
            return 0.0;
        return Math.round((double) solved / maxSolved * 1000) / 10.0;
    }

    private String generateRecommendation(UserTagStat tag) {
        int toNext = tag.getSolvedToNextLevel();
        String nextLevel = tag.getNextLevel();
        return String.format("%d문제만 더 풀면 %s 레벨!", toNext, nextLevel);
    }

    private String getTierName(Integer tier) {
        if (tier == null)
            return "Unknown";

        String[] tiers = { "Unrated",
                "Bronze V", "Bronze IV", "Bronze III", "Bronze II", "Bronze I",
                "Silver V", "Silver IV", "Silver III", "Silver II", "Silver I",
                "Gold V", "Gold IV", "Gold III", "Gold II", "Gold I",
                "Platinum V", "Platinum IV", "Platinum III", "Platinum II", "Platinum I",
                "Diamond V", "Diamond IV", "Diamond III", "Diamond II", "Diamond I",
                "Ruby V", "Ruby IV", "Ruby III", "Ruby II", "Ruby I",
                "Master"
        };

        return tier < tiers.length ? tiers[tier] : "Unknown";
    }

    private String determineNextGoal(List<UserClassStat> classStats, List<TagWeaknessDto> weaknesses) {
        // 미완성 클래스 찾기
        Optional<UserClassStat> incompleteClass = classStats.stream()
                .filter(c -> c.getEssentialCompletionRate() < 100)
                .min(Comparator.comparing(UserClassStat::getClassNumber));

        if (incompleteClass.isPresent()) {
            return String.format("Class %d 에센셜 완성하기", incompleteClass.get().getClassNumber());
        }

        if (!weaknesses.isEmpty()) {
            return String.format("%s 태그 보완하기", weaknesses.get(0).getTagName());
        }

        return "다음 클래스 도전하기!";
    }

    private String determineOverallLevel(List<TagStrengthDto> strengths, List<TagWeaknessDto> weaknesses) {
        if (strengths.isEmpty())
            return "시작 단계";

        long masterCount = strengths.stream()
                .filter(s -> "MASTER".equals(s.getMasteryLevel()) || "EXPERT".equals(s.getMasteryLevel()))
                .count();

        if (masterCount >= 3 && weaknesses.size() <= 2) {
            return "균형잡힌 실력자 🎯";
        } else if (masterCount >= 2) {
            return "특화된 전문가 ⭐";
        } else if (strengths.size() >= 3) {
            return "성장하는 학습자 📈";
        } else {
            return "시작하는 도전자 🌱";
        }
    }
}
