package com.ssafy.dash.analytics.application;

import com.ssafy.dash.analytics.application.dto.TagStrengthDto;
import com.ssafy.dash.analytics.application.dto.TagWeaknessDto;
import com.ssafy.dash.analytics.application.dto.SkillSummaryDto;
import com.ssafy.dash.analytics.domain.UserClassStat;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserClassStatMapper;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSkillAnalysisService 단위 테스트")
class UserSkillAnalysisServiceTest {

    @Mock
    private UserTagStatMapper tagStatMapper;

    @Mock
    private UserClassStatMapper classStatMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSkillAnalysisService analysisService;

    private static final Long TEST_USER_ID = 1L;

    private List<UserTagStat> createSampleTagStats() {
        return List.of(
                UserTagStat.create(TEST_USER_ID, "implementation", 300, 85, 0, 0),
                UserTagStat.create(TEST_USER_ID, "dp", 500, 45, 0, 0),
                UserTagStat.create(TEST_USER_ID, "greedy", 400, 30, 0, 0),
                UserTagStat.create(TEST_USER_ID, "bfs", 200, 20, 0, 0),
                UserTagStat.create(TEST_USER_ID, "binary_search", 150, 3, 0, 0));
    }

    private User createSampleUser() {
        User user = User.create("testuser", "test@test.com",
                java.time.LocalDateTime.now(), "GITHUB", "12345", null);
        user.updateSolvedacProfile("testhandle", 13, 1234, 3);
        return user;
    }

    @Nested
    @DisplayName("getStrengthTags 메서드")
    class GetStrengthTagsTest {

        @Test
        @DisplayName("강점 태그를 solved 수 기준 내림차순으로 반환한다")
        void returnsTopStrengthsByDescending() {
            // given
            given(tagStatMapper.findByUserId(TEST_USER_ID)).willReturn(createSampleTagStats());

            // when
            List<TagStrengthDto> strengths = analysisService.getStrengthTags(TEST_USER_ID, 3);

            // then
            assertThat(strengths).hasSize(3);
            assertThat(strengths.get(0).getTagKey()).isEqualTo("implementation");
            assertThat(strengths.get(0).getSolved()).isEqualTo(85);
            assertThat(strengths.get(1).getTagKey()).isEqualTo("dp");
            assertThat(strengths.get(2).getTagKey()).isEqualTo("greedy");
        }

        @Test
        @DisplayName("MASTER 레벨 태그에는 🏆 배지가 부여된다")
        void masterTagHasTrophyBadge() {
            // given
            given(tagStatMapper.findByUserId(TEST_USER_ID)).willReturn(createSampleTagStats());

            // when
            List<TagStrengthDto> strengths = analysisService.getStrengthTags(TEST_USER_ID, 1);

            // then
            assertThat(strengths.get(0).getMasteryLevel()).isEqualTo("MASTER");
            assertThat(strengths.get(0).getBadge()).isEqualTo("🏆");
        }

        @Test
        @DisplayName("통계가 없으면 빈 리스트를 반환한다")
        void returnsEmptyWhenNoStats() {
            // given
            given(tagStatMapper.findByUserId(TEST_USER_ID)).willReturn(Collections.emptyList());

            // when
            List<TagStrengthDto> strengths = analysisService.getStrengthTags(TEST_USER_ID, 5);

            // then
            assertThat(strengths).isEmpty();
        }

        @Test
        @DisplayName("상대적 강점이 0-100 범위로 계산된다")
        void relativeStrengthInRange() {
            // given
            given(tagStatMapper.findByUserId(TEST_USER_ID)).willReturn(createSampleTagStats());

            // when
            List<TagStrengthDto> strengths = analysisService.getStrengthTags(TEST_USER_ID, 5);

            // then
            assertThat(strengths.get(0).getRelativeStrength()).isEqualTo(100.0); // max
            assertThat(strengths.get(4).getRelativeStrength()).isLessThan(10.0); // min
        }
    }

    @Nested
    @DisplayName("getWeaknessTags 메서드")
    class GetWeaknessTagsTest {

        @Test
        @DisplayName("1-4문제 푼 태그를 약점으로 반환한다")
        void returnsWeakTags() {
            // given
            given(tagStatMapper.findByUserId(TEST_USER_ID)).willReturn(createSampleTagStats());

            // when
            List<TagWeaknessDto> weaknesses = analysisService.getWeaknessTags(TEST_USER_ID);

            // then
            assertThat(weaknesses).hasSize(1);
            assertThat(weaknesses.get(0).getTagKey()).isEqualTo("binary_search");
            assertThat(weaknesses.get(0).getSolved()).isEqualTo(3);
        }

        @Test
        @DisplayName("약점 태그에 다음 레벨까지 필요한 문제 수가 포함된다")
        void includesNextLevelInfo() {
            // given
            given(tagStatMapper.findByUserId(TEST_USER_ID)).willReturn(createSampleTagStats());

            // when
            List<TagWeaknessDto> weaknesses = analysisService.getWeaknessTags(TEST_USER_ID);

            // then
            assertThat(weaknesses.get(0).getSolvedToNextLevel()).isPositive();
            assertThat(weaknesses.get(0).getRecommendation()).contains("문제만 더 풀면");
        }
    }

    @Nested
    @DisplayName("getRecommendedTags 메서드")
    class GetRecommendedTagsTest {

        @Test
        @DisplayName("경험하지 않은 중요 태그를 추천한다")
        void recommendsUnexperiencedImportantTags() {
            // given - implementation, dp, greedy, bfs, binary_search 경험
            given(tagStatMapper.findByUserId(TEST_USER_ID)).willReturn(createSampleTagStats());

            // when
            List<String> recommended = analysisService.getRecommendedTags(TEST_USER_ID);

            // then
            // 중요 태그 중 미경험: graph_traversal, data_structures, sorting, math, dfs
            assertThat(recommended).doesNotContain("dp", "greedy", "bfs", "binary_search", "implementation");
        }
    }

    @Nested
    @DisplayName("getSkillSummary 메서드")
    class GetSkillSummaryTest {

        @Test
        @DisplayName("종합 스킬 요약을 반환한다")
        void returnsSkillSummary() {
            // given
            User user = createSampleUser();
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
            given(tagStatMapper.findByUserId(TEST_USER_ID)).willReturn(createSampleTagStats());
            given(classStatMapper.findByUserId(TEST_USER_ID)).willReturn(Collections.emptyList());

            // when
            SkillSummaryDto summary = analysisService.getSkillSummary(TEST_USER_ID);

            // then
            assertThat(summary.getTier()).isEqualTo("Gold III");
            assertThat(summary.getClassLevel()).isEqualTo(3);
            assertThat(summary.getTopStrengths()).hasSize(5);
            assertThat(summary.getOverallLevel()).isNotBlank();
        }

        @Test
        @DisplayName("사용자가 없으면 IllegalArgumentException이 발생한다")
        void throwsWhenUserNotFound() {
            // given
            given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> analysisService.getSkillSummary(TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found");
        }
    }
}
