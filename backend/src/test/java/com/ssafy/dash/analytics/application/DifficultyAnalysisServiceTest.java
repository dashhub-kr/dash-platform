package com.ssafy.dash.analytics.application;

import com.ssafy.dash.analytics.application.dto.ClassProgressDto;
import com.ssafy.dash.analytics.application.dto.DifficultyAnalysisDto;
import com.ssafy.dash.analytics.domain.UserClassStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserClassStatMapper;
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
@DisplayName("DifficultyAnalysisService 단위 테스트")
class DifficultyAnalysisServiceTest {

    @Mock
    private UserClassStatMapper classStatMapper;

    @InjectMocks
    private DifficultyAnalysisService difficultyService;

    private static final Long TEST_USER_ID = 1L;

    private List<UserClassStat> createSampleClassStats() {
        return List.of(
                UserClassStat.create(TEST_USER_ID, 1, 10, 10, 5, 5, "gold"),
                UserClassStat.create(TEST_USER_ID, 2, 20, 15, 8, 6, "silver"),
                UserClassStat.create(TEST_USER_ID, 3, 30, 10, 10, 3, null));
    }

    @Nested
    @DisplayName("analyzeDifficulty 메서드")
    class AnalyzeDifficultyTest {

        @Test
        @DisplayName("가장 완료율 높은 클래스를 strongestClass로 반환한다")
        void returnsStrongestClass() {
            // given
            given(classStatMapper.findByUserId(TEST_USER_ID)).willReturn(createSampleClassStats());

            // when
            DifficultyAnalysisDto result = difficultyService.analyzeDifficulty(TEST_USER_ID);

            // then
            assertThat(result.getStrongestClass()).isEqualTo(1); // 10/10 = 100%
        }

        @Test
        @DisplayName("에센셜 100% 미만인 가장 낮은 클래스를 targetClass로 반환한다")
        void returnsTargetClass() {
            // given
            given(classStatMapper.findByUserId(TEST_USER_ID)).willReturn(createSampleClassStats());

            // when
            DifficultyAnalysisDto result = difficultyService.analyzeDifficulty(TEST_USER_ID);

            // then
            assertThat(result.getTargetClass()).isEqualTo(2); // 6/8 < 100%
        }

        @Test
        @DisplayName("클래스별 진행 상황을 정렬된 순서로 반환한다")
        void returnsClassProgressInOrder() {
            // given
            given(classStatMapper.findByUserId(TEST_USER_ID)).willReturn(createSampleClassStats());

            // when
            DifficultyAnalysisDto result = difficultyService.analyzeDifficulty(TEST_USER_ID);

            // then
            assertThat(result.getClassProgress()).hasSize(3);
            assertThat(result.getClassProgress().get(0).getClassNumber()).isEqualTo(1);
            assertThat(result.getClassProgress().get(1).getClassNumber()).isEqualTo(2);
            assertThat(result.getClassProgress().get(2).getClassNumber()).isEqualTo(3);
        }

        @Test
        @DisplayName("각 클래스에 남은 에센셜 문제 수가 포함된다")
        void includesRemainingEssentials() {
            // given
            given(classStatMapper.findByUserId(TEST_USER_ID)).willReturn(createSampleClassStats());

            // when
            DifficultyAnalysisDto result = difficultyService.analyzeDifficulty(TEST_USER_ID);

            // then
            ClassProgressDto class2 = result.getClassProgress().get(1);
            assertThat(class2.getRemainingEssentials()).isEqualTo(2); // 8-6
        }

        @Test
        @DisplayName("통계가 없으면 Class 1부터 시작하라고 추천한다")
        void recommendsClass1WhenNoStats() {
            // given
            given(classStatMapper.findByUserId(TEST_USER_ID)).willReturn(Collections.emptyList());

            // when
            DifficultyAnalysisDto result = difficultyService.analyzeDifficulty(TEST_USER_ID);

            // then
            assertThat(result.getTargetClass()).isEqualTo(1);
            assertThat(result.getRecommendation()).contains("Class 1");
        }

        @Test
        @DisplayName("에센셜 3개 이하일 때 격려 메시지를 포함한다")
        void encouragementWhenClose() {
            // given - class 2가 2개만 남음
            List<UserClassStat> stats = List.of(
                    UserClassStat.create(TEST_USER_ID, 1, 10, 10, 5, 5, "gold"),
                    UserClassStat.create(TEST_USER_ID, 2, 20, 18, 8, 6, "silver"));
            given(classStatMapper.findByUserId(TEST_USER_ID)).willReturn(stats);

            // when
            DifficultyAnalysisDto result = difficultyService.analyzeDifficulty(TEST_USER_ID);

            // then
            assertThat(result.getRecommendation()).contains("조금만 더");
        }

        @Test
        @DisplayName("모든 에센셜 완료 시 다음 클래스 도전을 추천한다")
        void recommendsNextClassWhenAllComplete() {
            // given
            List<UserClassStat> stats = List.of(
                    UserClassStat.create(TEST_USER_ID, 1, 10, 10, 5, 5, "gold"),
                    UserClassStat.create(TEST_USER_ID, 2, 20, 20, 8, 8, "gold"));
            given(classStatMapper.findByUserId(TEST_USER_ID)).willReturn(stats);

            // when
            DifficultyAnalysisDto result = difficultyService.analyzeDifficulty(TEST_USER_ID);

            // then
            assertThat(result.getTargetClass()).isNull();
            assertThat(result.getRecommendation()).contains("다음 클래스");
        }
    }
}
