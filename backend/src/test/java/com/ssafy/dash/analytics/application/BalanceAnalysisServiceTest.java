package com.ssafy.dash.analytics.application;

import com.ssafy.dash.analytics.application.dto.BalanceAnalysisDto;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceAnalysisService 단위 테스트")
class BalanceAnalysisServiceTest {

    @Mock
    private UserTagStatMapper tagStatMapper;

    @InjectMocks
    private BalanceAnalysisService balanceService;

    private static final Long TEST_USER_ID = 1L;

    @Nested
    @DisplayName("analyzeBalance 메서드")
    class AnalyzeBalanceTest {

        @Test
        @DisplayName("상위 3개 태그에 70% 이상 집중되면 SPECIALIZED 타입을 반환한다")
        void specializedWhenConcentrated() {
            // given - 상위 3개 태그가 전체의 90%
            List<UserTagStat> stats = List.of(
                    UserTagStat.create(TEST_USER_ID, "implementation", 300, 50, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "dp", 500, 30, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "greedy", 400, 10, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "bfs", 200, 5, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "dfs", 200, 5, 0, 0));
            given(tagStatMapper.findByUserId(TEST_USER_ID)).willReturn(stats);

            // when
            BalanceAnalysisDto result = balanceService.analyzeBalance(TEST_USER_ID);

            // then
            assertThat(result.getBalanceType()).isEqualTo("SPECIALIZED");
            assertThat(result.getTopThreeConcentration()).isGreaterThan(70);
            assertThat(result.getRecommendation()).contains("다양한 태그");
        }

        @Test
        @DisplayName("상위 3개 태그가 50-70% 사이면 FOCUSED 타입을 반환한다")
        void focusedWhenModeratelyConcentrated() {
            // given - 상위 3개 태그가 전체의 60%
            List<UserTagStat> stats = List.of(
                    UserTagStat.create(TEST_USER_ID, "implementation", 300, 20, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "dp", 500, 20, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "greedy", 400, 20, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "bfs", 200, 20, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "dfs", 200, 20, 0, 0));
            given(tagStatMapper.findByUserId(TEST_USER_ID)).willReturn(stats);

            // when
            BalanceAnalysisDto result = balanceService.analyzeBalance(TEST_USER_ID);

            // then
            assertThat(result.getBalanceType()).isEqualTo("FOCUSED");
            assertThat(result.getTopThreeConcentration()).isBetween(50.0, 70.0);
        }

        @Test
        @DisplayName("상위 3개 태그가 50% 미만이면 BALANCED 타입을 반환한다")
        void balancedWhenDistributed() {
            // given - 고르게 분포된 10개 태그
            List<UserTagStat> stats = List.of(
                    UserTagStat.create(TEST_USER_ID, "tag1", 100, 10, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "tag2", 100, 10, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "tag3", 100, 10, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "tag4", 100, 10, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "tag5", 100, 10, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "tag6", 100, 10, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "tag7", 100, 10, 0, 0));
            given(tagStatMapper.findByUserId(TEST_USER_ID)).willReturn(stats);

            // when
            BalanceAnalysisDto result = balanceService.analyzeBalance(TEST_USER_ID);

            // then
            assertThat(result.getBalanceType()).isEqualTo("BALANCED");
            assertThat(result.getTopThreeConcentration()).isLessThan(50);
            assertThat(result.getBalanceScore()).isGreaterThan(50);
        }

        @Test
        @DisplayName("통계가 없으면 NONE 타입을 반환한다")
        void noneWhenNoStats() {
            // given
            given(tagStatMapper.findByUserId(TEST_USER_ID)).willReturn(Collections.emptyList());

            // when
            BalanceAnalysisDto result = balanceService.analyzeBalance(TEST_USER_ID);

            // then
            assertThat(result.getBalanceType()).isEqualTo("NONE");
            assertThat(result.getBalanceScore()).isEqualTo(0.0);
            assertThat(result.getFocusedTags()).isEmpty();
        }

        @Test
        @DisplayName("집중된 상위 3개 태그 키를 반환한다")
        void returnsFocusedTagKeys() {
            // given
            List<UserTagStat> stats = List.of(
                    UserTagStat.create(TEST_USER_ID, "implementation", 300, 50, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "dp", 500, 30, 0, 0),
                    UserTagStat.create(TEST_USER_ID, "greedy", 400, 20, 0, 0));
            given(tagStatMapper.findByUserId(TEST_USER_ID)).willReturn(stats);

            // when
            BalanceAnalysisDto result = balanceService.analyzeBalance(TEST_USER_ID);

            // then
            assertThat(result.getFocusedTags())
                    .containsExactly("implementation", "dp", "greedy");
        }
    }
}
