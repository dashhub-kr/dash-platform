package com.ssafy.dash.analytics.application;

import com.ssafy.dash.analytics.application.dto.response.*;
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
 * 학습 경로 추천 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleBasedLearningPathService {

    private final UserTagStatMapper tagStatMapper;
    private final UserClassStatMapper classStatMapper;
    private final UserRepository userRepository;

    // 티어 이름 매핑
    private static final String[] TIER_NAMES = { "Unrated",
            "Bronze V", "Bronze IV", "Bronze III", "Bronze II", "Bronze I",
            "Silver V", "Silver IV", "Silver III", "Silver II", "Silver I",
            "Gold V", "Gold IV", "Gold III", "Gold II", "Gold I",
            "Platinum V", "Platinum IV", "Platinum III", "Platinum II", "Platinum I",
            "Diamond V", "Diamond IV", "Diamond III", "Diamond II", "Diamond I",
            "Ruby V", "Ruby IV", "Ruby III", "Ruby II", "Ruby I",
            "Master"
    };

    /**
     * 학습 경로 추천
     */
    public LearningPathDto recommendLearningPath(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<UserTagStat> tagStats = tagStatMapper.findByUserId(userId);
        List<UserClassStat> classStats = classStatMapper.findByUserId(userId);

        // 현재 레벨 문자열
        String currentLevel = buildCurrentLevel(user);

        // 다음 목표
        String nextGoal = determineNextGoal(classStats, tagStats);

        // 학습 단계별 추천 생성
        List<RecommendationSetDto> phases = new ArrayList<>();

        // Phase 1: 약점 태그 보완
        addWeaknessPhase(phases, tagStats);

        // Phase 2: 클래스 에센셜 완성
        addClassPhase(phases, classStats);

        // Phase 3: 강점 태그 심화
        addStrengthPhase(phases, tagStats);

        // 거품 탐지
        Integer bubbleIndex = calculateBubbleIndex(user);
        String bubbleWarning = generateBubbleWarning(bubbleIndex, user);

        return LearningPathDto.builder()
                .currentLevel(currentLevel)
                .nextGoal(nextGoal)
                .phases(phases)
                .bubbleIndex(bubbleIndex)
                .bubbleWarning(bubbleWarning)
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
        if (tier == null || tier < 0 || tier >= TIER_NAMES.length) {
            return "Unknown";
        }
        return TIER_NAMES[tier];
    }

    private String determineNextGoal(List<UserClassStat> classStats, List<UserTagStat> tagStats) {
        // 미완성 클래스 찾기
        Optional<UserClassStat> incompleteClass = classStats.stream()
                .filter(c -> c.getEssentialCompletionRate() < 100)
                .min(Comparator.comparing(UserClassStat::getClassNumber));

        if (incompleteClass.isPresent()) {
            UserClassStat target = incompleteClass.get();
            int remaining = target.getEssentials() - target.getEssentialSolved();
            return String.format("Class %d 에센셜 완성 (남은 문제: %d개)", target.getClassNumber(), remaining);
        }

        // 약점 태그 찾기
        Optional<UserTagStat> weakTag = tagStats.stream()
                .filter(t -> t.getSolved() > 0 && t.getSolved() < 5)
                .min(Comparator.comparing(UserTagStat::getSolved));

        if (weakTag.isPresent()) {
            return String.format("%s 태그 보완하기", weakTag.get().getTagKey());
        }

        return "다음 클래스에 도전하기!";
    }

    private void addWeaknessPhase(List<RecommendationSetDto> phases, List<UserTagStat> tagStats) {
        // 약점 태그 (1-4문제)
        List<UserTagStat> weakTags = tagStats.stream()
                .filter(t -> t.getSolved() > 0 && t.getSolved() < 5)
                .sorted(Comparator.comparing(UserTagStat::getSolved))
                .limit(2)
                .collect(Collectors.toList());

        if (weakTags.isEmpty()) {
            return;
        }

        UserTagStat primaryWeak = weakTags.get(0);

        phases.add(RecommendationSetDto.builder()
                .phase(phases.size() + 1)
                .title("약점 보완: " + primaryWeak.getTagKey())
                .category("약점보완")
                .reason(String.format("%s 태그를 보강하면 다양한 문제를 풀 수 있습니다. (%d문제 → %d문제로)",
                        primaryWeak.getTagKey(), primaryWeak.getSolved(), 5))
                .problems(List.of()) // 실제 문제 추천은 Phase 2 확장 시 구현
                .build());
    }

    private void addClassPhase(List<RecommendationSetDto> phases, List<UserClassStat> classStats) {
        // 미완성 클래스
        Optional<UserClassStat> incompleteClass = classStats.stream()
                .filter(c -> c.getEssentialCompletionRate() < 100)
                .min(Comparator.comparing(UserClassStat::getClassNumber));

        if (incompleteClass.isEmpty()) {
            return;
        }

        UserClassStat target = incompleteClass.get();
        int remaining = target.getEssentials() - target.getEssentialSolved();

        phases.add(RecommendationSetDto.builder()
                .phase(phases.size() + 1)
                .title(String.format("Class %d 에센셜 완성", target.getClassNumber()))
                .category("클래스완성")
                .reason(String.format("에센셜 %d문제만 더 풀면 Class %d를 완성할 수 있습니다!",
                        remaining, target.getClassNumber()))
                .problems(List.of()) // 실제 문제 추천은 Phase 2 확장 시 구현
                .build());
    }

    private void addStrengthPhase(List<RecommendationSetDto> phases, List<UserTagStat> tagStats) {
        // 강점 태그 (가장 많이 푼 태그)
        Optional<UserTagStat> strongest = tagStats.stream()
                .max(Comparator.comparing(UserTagStat::getSolved));

        if (strongest.isEmpty() || strongest.get().getSolved() < 30) {
            return;
        }

        UserTagStat tag = strongest.get();

        phases.add(RecommendationSetDto.builder()
                .phase(phases.size() + 1)
                .title("강점 강화: " + tag.getTagKey())
                .category("강점강화")
                .reason(String.format("당신의 최강 태그! %d문제나 풀었습니다. 더 어려운 문제에 도전해보세요.",
                        tag.getSolved()))
                .problems(List.of()) // 실제 문제 추천은 Phase 2 확장 시 구현
                .build());
    }

    /**
     * 거품 지수 계산
     * 
     * @return 티어 - Top100 평균 레벨 (0이면 정상, 양수면 거품)
     */
    private Integer calculateBubbleIndex(User user) {
        Integer tier = user.getSolvedacTier();
        Integer avgTop100 = user.getAvgTop100Level();

        if (tier == null || avgTop100 == null) {
            return null; // 데이터 없음
        }

        return tier - avgTop100;
    }

    /**
     * 거품 경고 메시지 생성
     */
    private String generateBubbleWarning(Integer bubbleIndex, User user) {
        if (bubbleIndex == null || bubbleIndex <= 2) {
            return null; // 정상
        }

        String realTier = getTierName(user.getAvgTop100Level());
        String displayTier = getTierName(user.getSolvedacTier());

        return String.format(
                "현재 티어(%s)에 비해 실제 풀이 수준(%s)이 낮습니다. 기초부터 다시 잡아보세요!",
                displayTier, realTier);
    }
}
