package com.ssafy.dash.ai.application;

import com.ssafy.dash.ai.client.AiServerClient;
import com.ssafy.dash.ai.client.dto.HintRequest;
import com.ssafy.dash.ai.client.dto.HintResponse;
import com.ssafy.dash.analytics.application.UserSkillAnalysisService;
import com.ssafy.dash.analytics.application.dto.TagWeaknessDto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HintService 테스트")
class HintServiceTest {

    @Mock
    private AiServerClient aiClient;

    @Mock
    private UserSkillAnalysisService skillAnalysisService;

    @Mock
    private UserRepository userRepository;

    private HintService hintService;

    @BeforeEach
    void setUp() {
        hintService = new HintService(aiClient, skillAnalysisService, userRepository);
    }

    @Test
    @DisplayName("힌트 생성 시 사용자 컨텍스트 포함")
    void generateHint_shouldIncludeUserContext() {
        // given
        Long userId = 1L;
        String problemNumber = "1000";
        String problemTitle = "A+B";
        int level = 1;

        User user = createUser(userId, 11, 150); // Gold V

        TagWeaknessDto weakness = TagWeaknessDto.builder()
                .tagKey("dp")
                .tagName("다이나믹 프로그래밍")
                .solved(5)
                .build();

        HintResponse mockResponse = new HintResponse();
        mockResponse.setLevel(level);
        mockResponse.setHint("테스트 힌트");
        mockResponse.setEncouragement("잘하고 있어요!");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(skillAnalysisService.getWeaknessTags(userId)).thenReturn(List.of(weakness));
        when(aiClient.generateHint(any(HintRequest.class))).thenReturn(mockResponse);

        // when
        HintResponse result = hintService.generateHint(userId, problemNumber, problemTitle, level);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getHint()).isEqualTo("테스트 힌트");

        ArgumentCaptor<HintRequest> captor = ArgumentCaptor.forClass(HintRequest.class);
        verify(aiClient).generateHint(captor.capture());

        HintRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.getProblemNumber()).isEqualTo(problemNumber);
        assertThat(capturedRequest.getLevel()).isEqualTo(level);
        assertThat(capturedRequest.getUserContext()).isNotNull();
        assertThat(capturedRequest.getUserContext().getTier()).isEqualTo(11);
    }

    @Test
    @DisplayName("사용자 없이도 힌트 생성 가능")
    void generateHint_shouldWorkWithoutUser() {
        // given
        Long userId = 999L;

        HintResponse mockResponse = new HintResponse();
        mockResponse.setLevel(1);
        mockResponse.setHint("기본 힌트");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(skillAnalysisService.getWeaknessTags(userId)).thenReturn(List.of());
        when(aiClient.generateHint(any(HintRequest.class))).thenReturn(mockResponse);

        // when
        HintResponse result = hintService.generateHint(userId, "1000", "A+B", 1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getHint()).isEqualTo("기본 힌트");
    }

    private User createUser(Long id, Integer tier, Integer rating) {
        User user = new User();
        user.setId(id);
        user.setSolvedacTier(tier);
        user.setSolvedacRating(rating);
        return user;
    }
    
}
