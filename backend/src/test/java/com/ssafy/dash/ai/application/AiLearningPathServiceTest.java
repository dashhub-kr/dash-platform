package com.ssafy.dash.ai.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.ai.client.AiServerClient;
import com.ssafy.dash.ai.client.dto.LearningPathRequest;
import com.ssafy.dash.ai.client.dto.LearningPathResponse;
import com.ssafy.dash.ai.infrastructure.persistence.LearningPathCacheMapper;
import com.ssafy.dash.analytics.domain.UserClassStat;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserClassStatMapper;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiLearningPathService 테스트")
class AiLearningPathServiceTest {

    @Mock
    private AiServerClient aiClient;

    @Mock
    private UserTagStatMapper tagStatMapper;

    @Mock
    private UserClassStatMapper classStatMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LearningPathCacheMapper cacheMapper;

    @Mock
    private ObjectMapper objectMapper;

    private AiLearningPathService learningPathService;

    @BeforeEach
    void setUp() {
        learningPathService = new AiLearningPathService(
                aiClient, tagStatMapper, classStatMapper, userRepository, cacheMapper, objectMapper);
    }

    @Test
    @DisplayName("학습 경로 생성 시 분석 데이터 포함")
    void generateAiLearningPath_shouldIncludeAnalyticsData() {
        // given
        Long userId = 1L;
        User user = createUser(userId, 11, 150, 3); // Gold V, Class 3

        UserTagStat tagStat = new UserTagStat();
        tagStat.setUserId(userId);
        tagStat.setTagKey("dp");
        tagStat.setSolved(5);
        tagStat.setTotal(50);

        UserClassStat classStat = new UserClassStat();
        classStat.setUserId(userId);
        classStat.setClassNumber(3);
        classStat.setEssentialSolved(8);
        classStat.setEssentials(10);

        LearningPathResponse mockResponse = createMockResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tagStatMapper.findByUserId(userId)).thenReturn(List.of(tagStat));
        when(classStatMapper.findByUserId(userId)).thenReturn(List.of(classStat));
        when(aiClient.generateLearningPath(any(LearningPathRequest.class))).thenReturn(mockResponse);

        // when
        var result = learningPathService.generateAiLearningPath(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAiAnalysis()).isNotNull();
        assertThat(result.getAiAnalysis().getOverallAssessment()).isEqualTo("현재 상태 평가");

        ArgumentCaptor<LearningPathRequest> captor = ArgumentCaptor.forClass(LearningPathRequest.class);
        verify(aiClient).generateLearningPath(captor.capture());

        LearningPathRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.getCurrentLevel()).contains("Gold");
        assertThat(capturedRequest.getSolvedCount()).isEqualTo(150);
    }

    @Test
    @DisplayName("사용자 없으면 예외 발생")
    void generateAiLearningPath_shouldThrowWhenUserNotFound() {
        // given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> learningPathService.generateAiLearningPath(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    private User createUser(Long id, Integer tier, Integer rating, Integer classLevel) {
        User user = new User();
        user.setId(id);
        user.setSolvedacTier(tier);
        user.setSolvedacRating(rating);
        user.setSolvedacClass(classLevel);
        return user;
    }

    private LearningPathResponse createMockResponse() {
        LearningPathResponse response = new LearningPathResponse();
        response.setOverallAssessment("현재 상태 평가");
        response.setKeyStrength("강점");
        response.setPrimaryWeakness("약점");
        response.setPersonalizedAdvice("조언");
        response.setPhases(List.of());
        response.setMotivationalMessage("화이팅!");
        return response;
    }
}
