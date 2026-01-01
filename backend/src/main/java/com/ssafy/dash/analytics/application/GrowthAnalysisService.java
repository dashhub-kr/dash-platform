package com.ssafy.dash.analytics.application;

import com.ssafy.dash.analytics.application.dto.response.GrowthTrendDto;
import com.ssafy.dash.analytics.application.dto.response.TagGrowthDto;
import com.ssafy.dash.analytics.domain.UserStatsSnapshot;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserStatsSnapshotMapper;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * 성장 추세 분석 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrowthAnalysisService {

    private final UserStatsSnapshotMapper snapshotMapper;
    private final UserTagStatMapper tagStatMapper;
    private final StatsSnapshotService snapshotService;

    /**
     * 성장 추세 분석
     * 
     * @param userId 사용자 ID
     * @param days   분석 기간 (일)
     */
    public GrowthTrendDto analyzeGrowthTrend(Long userId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate pastDate = today.minusDays(days);

        // 과거 스냅샷 조회
        Optional<UserStatsSnapshot> pastSnapshot = snapshotMapper.findByUserIdAndDate(userId, pastDate);

        // 현재 통계 계산
        List<UserTagStat> currentTags = tagStatMapper.findByUserId(userId);
        int currentTotal = currentTags.stream()
                .mapToInt(UserTagStat::getSolved)
                .max()
                .orElse(0);

        // 스냅샷이 없는 경우 - 첫 분석 또는 데이터 부족
        if (pastSnapshot.isEmpty()) {
            // 현재 스냅샷 생성
            snapshotService.createSnapshot(userId);

            return GrowthTrendDto.builder()
                    .period(days + "일")
                    .totalGrowth(0)
                    .dailyAverage(0.0)
                    .trend("NEW")
                    .trendEmoji("🆕")
                    .topGrowthTags(Collections.emptyList())
                    .recommendation("첫 분석입니다! 앞으로 학습하면 성장 추세를 볼 수 있어요.")
                    .build();
        }

        UserStatsSnapshot past = pastSnapshot.get();
        int growth = currentTotal - past.getTotalSolved();
        double dailyAverage = (double) growth / days;

        // 성장 추세 판정
        String trend;
        String trendEmoji;
        String recommendation;

        if (dailyAverage >= 1.0) {
            trend = "GROWING";
            trendEmoji = "📈";
            recommendation = String.format("훌륭해요! 하루 평균 %.1f문제를 풀고 있습니다.", dailyAverage);
        } else if (dailyAverage >= 0.3) {
            trend = "STABLE";
            trendEmoji = "➡️";
            recommendation = "꾸준히 학습 중입니다. 조금만 더 힘내세요!";
        } else if (dailyAverage > 0) {
            trend = "SLOW";
            trendEmoji = "🐢";
            recommendation = "학습 속도가 느려졌어요. 목표를 세워보세요!";
        } else {
            trend = "DECLINING";
            trendEmoji = "📉";
            recommendation = "최근 학습이 멈췄어요. 다시 시작해볼까요?";
        }

        // 태그별 성장 분석은 히스토리가 쌓여야 가능
        // 현재는 빈 리스트 반환
        List<TagGrowthDto> topGrowthTags = Collections.emptyList();

        return GrowthTrendDto.builder()
                .period(days + "일")
                .totalGrowth(growth)
                .dailyAverage(Math.round(dailyAverage * 100) / 100.0)
                .trend(trend)
                .trendEmoji(trendEmoji)
                .topGrowthTags(topGrowthTags)
                .recommendation(recommendation)
                .build();
    }

}
