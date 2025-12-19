package com.ssafy.dash.analytics.application;

import com.ssafy.dash.analytics.application.dto.GrowthTrendDto;
import com.ssafy.dash.analytics.domain.UserStatsSnapshot;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserStatsSnapshotMapper;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GrowthAnalysisService 단위 테스트")
class GrowthAnalysisServiceTest {

    @Mock
    private UserStatsSnapshotMapper snapshotMapper;

    @Mock
    private UserTagStatMapper tagStatMapper;

    @Mock
    private StatsSnapshotService snapshotService;

    @InjectMocks
    private GrowthAnalysisService growthService;

    private static final Long TEST_USER_ID = 1L;

    private List<UserTagStat> createCurrentStats(int maxSolved) {
        return List.of(
                UserTagStat.create(TEST_USER_ID, "implementation", 300, maxSolved, 0, 0));
    }

    private UserStatsSnapshot createPastSnapshot(int totalSolved) {
        UserStatsSnapshot snapshot = new UserStatsSnapshot();
        snapshot.setId(1L);
        snapshot.setUserId(TEST_USER_ID);
        snapshot.setSnapshotDate(LocalDate.now().minusDays(30));
        snapshot.setTotalSolved(totalSolved);
        snapshot.setSolvedacTier(13);
        snapshot.setSolvedacRating(1234);
        snapshot.setSolvedacClass(3);
        return snapshot;
    }

    @Nested
    @DisplayName("analyzeGrowthTrend 메서드")
    class AnalyzeGrowthTrendTest {

        @Test
        @DisplayName("과거 스냅샷이 없으면 NEW 트렌드를 반환하고 현재 스냅샷을 생성한다")
        void returnsNewWhenNoSnapshot() {
            // given
            given(snapshotMapper.findByUserIdAndDate(eq(TEST_USER_ID), any()))
                    .willReturn(Optional.empty());
            given(tagStatMapper.findByUserId(TEST_USER_ID))
                    .willReturn(createCurrentStats(100));

            // when
            GrowthTrendDto result = growthService.analyzeGrowthTrend(TEST_USER_ID, 30);

            // then
            assertThat(result.getTrend()).isEqualTo("NEW");
            assertThat(result.getTrendEmoji()).isEqualTo("🆕");
            assertThat(result.getTotalGrowth()).isEqualTo(0);
            verify(snapshotService).createSnapshot(TEST_USER_ID);
        }

        @Test
        @DisplayName("일평균 1.0 이상이면 GROWING 트렌드를 반환한다")
        void growingWhenHighDailyAverage() {
            // given - 30일 동안 50개 증가 (일평균 1.67)
            given(snapshotMapper.findByUserIdAndDate(eq(TEST_USER_ID), any()))
                    .willReturn(Optional.of(createPastSnapshot(50)));
            given(tagStatMapper.findByUserId(TEST_USER_ID))
                    .willReturn(createCurrentStats(100));

            // when
            GrowthTrendDto result = growthService.analyzeGrowthTrend(TEST_USER_ID, 30);

            // then
            assertThat(result.getTrend()).isEqualTo("GROWING");
            assertThat(result.getTrendEmoji()).isEqualTo("📈");
            assertThat(result.getTotalGrowth()).isEqualTo(50);
            assertThat(result.getDailyAverage()).isGreaterThanOrEqualTo(1.0);
        }

        @Test
        @DisplayName("일평균 0.3-1.0 사이면 STABLE 트렌드를 반환한다")
        void stableWhenModerateDailyAverage() {
            // given - 30일 동안 15개 증가 (일평균 0.5)
            given(snapshotMapper.findByUserIdAndDate(eq(TEST_USER_ID), any()))
                    .willReturn(Optional.of(createPastSnapshot(85)));
            given(tagStatMapper.findByUserId(TEST_USER_ID))
                    .willReturn(createCurrentStats(100));

            // when
            GrowthTrendDto result = growthService.analyzeGrowthTrend(TEST_USER_ID, 30);

            // then
            assertThat(result.getTrend()).isEqualTo("STABLE");
            assertThat(result.getTrendEmoji()).isEqualTo("➡️");
        }

        @Test
        @DisplayName("일평균 0-0.3 사이면 SLOW 트렌드를 반환한다")
        void slowWhenLowDailyAverage() {
            // given - 30일 동안 5개 증가 (일평균 0.17)
            given(snapshotMapper.findByUserIdAndDate(eq(TEST_USER_ID), any()))
                    .willReturn(Optional.of(createPastSnapshot(95)));
            given(tagStatMapper.findByUserId(TEST_USER_ID))
                    .willReturn(createCurrentStats(100));

            // when
            GrowthTrendDto result = growthService.analyzeGrowthTrend(TEST_USER_ID, 30);

            // then
            assertThat(result.getTrend()).isEqualTo("SLOW");
            assertThat(result.getTrendEmoji()).isEqualTo("🐢");
        }

        @Test
        @DisplayName("성장이 없거나 감소하면 DECLINING 트렌드를 반환한다")
        void decliningWhenNoGrowth() {
            // given - 30일 동안 0개 증가
            given(snapshotMapper.findByUserIdAndDate(eq(TEST_USER_ID), any()))
                    .willReturn(Optional.of(createPastSnapshot(100)));
            given(tagStatMapper.findByUserId(TEST_USER_ID))
                    .willReturn(createCurrentStats(100));

            // when
            GrowthTrendDto result = growthService.analyzeGrowthTrend(TEST_USER_ID, 30);

            // then
            assertThat(result.getTrend()).isEqualTo("DECLINING");
            assertThat(result.getTrendEmoji()).isEqualTo("📉");
            assertThat(result.getTotalGrowth()).isEqualTo(0);
        }

        @Test
        @DisplayName("분석 기간이 응답에 포함된다")
        void includesPeriodInResponse() {
            // given
            given(snapshotMapper.findByUserIdAndDate(eq(TEST_USER_ID), any()))
                    .willReturn(Optional.of(createPastSnapshot(50)));
            given(tagStatMapper.findByUserId(TEST_USER_ID))
                    .willReturn(createCurrentStats(100));

            // when
            GrowthTrendDto result = growthService.analyzeGrowthTrend(TEST_USER_ID, 7);

            // then
            assertThat(result.getPeriod()).isEqualTo("7일");
        }

        @Test
        @DisplayName("트렌드에 맞는 추천 메시지가 포함된다")
        void includesRecommendation() {
            // given
            given(snapshotMapper.findByUserIdAndDate(eq(TEST_USER_ID), any()))
                    .willReturn(Optional.of(createPastSnapshot(50)));
            given(tagStatMapper.findByUserId(TEST_USER_ID))
                    .willReturn(createCurrentStats(100));

            // when
            GrowthTrendDto result = growthService.analyzeGrowthTrend(TEST_USER_ID, 30);

            // then
            assertThat(result.getRecommendation()).isNotBlank();
        }
    }

}
