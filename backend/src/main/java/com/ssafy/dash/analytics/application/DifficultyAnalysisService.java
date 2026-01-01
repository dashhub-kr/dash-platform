package com.ssafy.dash.analytics.application;

import com.ssafy.dash.analytics.application.dto.response.ClassProgressDto;
import com.ssafy.dash.analytics.application.dto.response.DifficultyAnalysisDto;
import com.ssafy.dash.analytics.domain.UserClassStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserClassStatMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 난이도(클래스) 분포 분석 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DifficultyAnalysisService {

    private final UserClassStatMapper classStatMapper;

    /**
     * 난이도 분포 분석
     * - 클래스별 완료율
     * - 가장 강한 클래스 / 목표 클래스 식별
     */
    public DifficultyAnalysisDto analyzeDifficulty(Long userId) {
        List<UserClassStat> classStats = classStatMapper.findByUserId(userId);

        if (classStats.isEmpty()) {
            return DifficultyAnalysisDto.builder()
                    .strongestClass(null)
                    .targetClass(1)
                    .classProgress(List.of())
                    .recommendation("Class 1부터 시작해보세요!")
                    .build();
        }

        // 가장 완료율 높은 클래스
        Optional<UserClassStat> strongest = classStats.stream()
                .filter(c -> c.getTotalSolved() > 0)
                .max(Comparator.comparing(UserClassStat::getCompletionRate));

        // 다음 목표 클래스 (에센셜 100% 미만인 가장 낮은 클래스)
        Optional<UserClassStat> target = classStats.stream()
                .filter(c -> c.getEssentialCompletionRate() < 100)
                .min(Comparator.comparing(UserClassStat::getClassNumber));

        // 클래스별 진행 현황
        List<ClassProgressDto> progress = classStats.stream()
                .sorted(Comparator.comparing(UserClassStat::getClassNumber))
                .map(this::toProgressDto)
                .collect(Collectors.toList());

        // 추천 메시지 생성
        String recommendation = generateRecommendation(target.orElse(null), strongest.orElse(null));

        return DifficultyAnalysisDto.builder()
                .strongestClass(strongest.map(UserClassStat::getClassNumber).orElse(null))
                .targetClass(target.map(UserClassStat::getClassNumber).orElse(null))
                .classProgress(progress)
                .recommendation(recommendation)
                .build();
    }

    private ClassProgressDto toProgressDto(UserClassStat stat) {
        int remainingEssentials = stat.getEssentials() - stat.getEssentialSolved();

        return ClassProgressDto.builder()
                .classNumber(stat.getClassNumber())
                .completionRate(stat.getCompletionRate())
                .essentialCompletionRate(stat.getEssentialCompletionRate())
                .decoration(stat.getDecoration())
                .totalProblems(stat.getTotal())
                .solvedProblems(stat.getTotalSolved())
                .remainingEssentials(Math.max(0, remainingEssentials))
                .build();
    }

    private String generateRecommendation(UserClassStat target, UserClassStat strongest) {
        if (target == null) {
            return "모든 클래스 에센셜을 완료했습니다! 다음 클래스에 도전하세요.";
        }

        int remaining = target.getEssentials() - target.getEssentialSolved();
        if (remaining <= 3) {
            return String.format("Class %d 에센셜 %d문제만 더 풀면 완성! 조금만 더 힘내세요!",
                    target.getClassNumber(), remaining);
        }

        return String.format("Class %d 에센셜 문제부터 푸는 것을 추천합니다. (남은 문제: %d개)",
                target.getClassNumber(), remaining);
    }
}
