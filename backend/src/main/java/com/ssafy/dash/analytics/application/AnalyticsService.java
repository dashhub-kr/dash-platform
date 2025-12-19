package com.ssafy.dash.analytics.application;

import com.ssafy.dash.analytics.application.dto.FamilyScoreDto;
import com.ssafy.dash.analytics.application.dto.TagCoverageDto;
import com.ssafy.dash.analytics.infrastructure.persistence.AnalyticsMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 알고리즘 통계 및 분석 서비스
 * 유저의 문제 풀이 기록을 바탕으로 레이더 차트 데이터(패밀리별 점수) 및 코어 태그 커버리지를 계산합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsMapper analyticsMapper;

    /**
     * 유저의 패밀리별 점수 통계를 조회합니다.
     * (레이더 차트용 데이터: rawScore, distinctTags, solvedProblems)
     */
    @Transactional(readOnly = true)
    public List<FamilyScoreDto> getUserFamilyScores(Long userId) {
        return analyticsMapper.findFamilyScoresByUserId(userId);
    }

    /**
     * 유저의 코어 태그(S/A 티어) 커버리지 통계를 조회합니다.
     * (전체 코어 태그 중 몇 %를 경험했는지)
     */
    @Transactional(readOnly = true)
    public TagCoverageDto getUserCoreCoverage(Long userId) {
        return analyticsMapper.findCoreCoverageByUserId(userId);
    }

    /**
     * (추후 구현 예정)
     * 유저의 약점 패밀리 식별 로직
     * 예: 평균 점수보다 60% 미만인 패밀리를 약점으로 식별
     */
    public List<Long> identifyWeakFamilyIds(Long userId) {
        List<FamilyScoreDto> scores = getUserFamilyScores(userId);
        if (scores.isEmpty())
            return List.of();

        double totalScore = scores.stream().mapToDouble(FamilyScoreDto::getRawScore).sum();
        double avgScore = totalScore / scores.size();

        // 간단한 약점 판별 예시: 평균 점수의 60% 미만인 경우
        return scores.stream()
                .filter(s -> s.getRawScore() < avgScore * 0.6)
                .map(FamilyScoreDto::getFamilyId)
                .toList();
    }
}
