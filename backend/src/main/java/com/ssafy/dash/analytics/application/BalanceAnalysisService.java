package com.ssafy.dash.analytics.application;

import com.ssafy.dash.analytics.application.dto.BalanceAnalysisDto;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 학습 밸런스 분석 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceAnalysisService {

    private final UserTagStatMapper tagStatMapper;

    /**
     * 학습 밸런스 분석
     * - 상위 3개 태그에 얼마나 집중되어 있는지 분석
     * - BALANCED / FOCUSED / SPECIALIZED 판정
     */
    public BalanceAnalysisDto analyzeBalance(Long userId) {
        List<UserTagStat> allTags = tagStatMapper.findByUserId(userId);

        if (allTags.isEmpty()) {
            return BalanceAnalysisDto.builder()
                    .balanceScore(0.0)
                    .balanceType("NONE")
                    .topThreeConcentration(0.0)
                    .focusedTags(List.of())
                    .recommendation("태그별 문제를 풀어보세요!")
                    .build();
        }

        // 총 푼 문제 수
        int totalSolved = allTags.stream()
                .mapToInt(UserTagStat::getSolved)
                .sum();

        if (totalSolved == 0) {
            return BalanceAnalysisDto.builder()
                    .balanceScore(0.0)
                    .balanceType("NONE")
                    .topThreeConcentration(0.0)
                    .focusedTags(List.of())
                    .recommendation("문제를 풀어보세요!")
                    .build();
        }

        // 상위 3개 태그
        List<UserTagStat> topThree = allTags.stream()
                .sorted(Comparator.comparing(UserTagStat::getSolved).reversed())
                .limit(3)
                .collect(Collectors.toList());

        // 상위 3개 태그의 합
        int topThreeSolved = topThree.stream()
                .mapToInt(UserTagStat::getSolved)
                .sum();

        // 집중도 계산 (%)
        double concentration = (double) topThreeSolved / totalSolved * 100;

        // 밸런스 점수 (집중도가 낮을수록 높음)
        double balanceScore = Math.max(0, 100 - concentration);

        // 밸런스 타입 결정
        String balanceType;
        String recommendation;

        if (concentration > 70) {
            balanceType = "SPECIALIZED";
            recommendation = "특정 태그에 특화되어 있어요! 다양한 태그의 문제도 풀어보세요.";
        } else if (concentration > 50) {
            balanceType = "FOCUSED";
            recommendation = "균형잡힌 학습 중입니다. 약점 태그를 보완해보세요.";
        } else {
            balanceType = "BALANCED";
            recommendation = "훌륭한 밸런스입니다! 깊이를 더해보세요.";
        }

        // 집중된 태그 이름들
        List<String> focusedTags = topThree.stream()
                .map(UserTagStat::getTagKey)
                .collect(Collectors.toList());

        return BalanceAnalysisDto.builder()
                .balanceScore(Math.round(balanceScore * 10) / 10.0)
                .balanceType(balanceType)
                .topThreeConcentration(Math.round(concentration * 10) / 10.0)
                .focusedTags(focusedTags)
                .recommendation(recommendation)
                .build();
    }
}
