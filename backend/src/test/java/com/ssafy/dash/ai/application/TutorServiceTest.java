package com.ssafy.dash.ai.application;

import com.ssafy.dash.ai.client.AiServerClient;
import com.ssafy.dash.ai.client.dto.TutorChatRequest;
import com.ssafy.dash.ai.client.dto.TutorChatResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TutorService 테스트")
class TutorServiceTest {

    @Mock
    private AiServerClient aiClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTagStatMapper tagStatMapper;

    private TutorService tutorService;

    @BeforeEach
    void setUp() {
        tutorService = new TutorService(aiClient, userRepository, tagStatMapper);
    }

    @Test
    @DisplayName("대화 요청 시 사용자 컨텍스트 포함")
    void chat_shouldIncludeUserContext() {
        // given
        Long userId = 1L;
        String message = "DP가 뭐예요?";

        User user = createUser(userId, 11, 150);

        TutorChatResponse mockResponse = new TutorChatResponse();
        mockResponse.setReply("좋은 질문이에요!");
        mockResponse.setTeachingStyle("socratic");
        mockResponse.setEncouragement("잘하고 있어요!");
        mockResponse.setFollowUpQuestions(List.of("예를 들어볼까요?"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tagStatMapper.findByUserId(userId)).thenReturn(List.of());
        when(aiClient.chat(any(TutorChatRequest.class))).thenReturn(mockResponse);

        // when
        TutorChatResponse result = tutorService.chat(userId, message, null, null, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReply()).isEqualTo("좋은 질문이에요!");
        assertThat(result.getTeachingStyle()).isEqualTo("socratic");

        ArgumentCaptor<TutorChatRequest> captor = ArgumentCaptor.forClass(TutorChatRequest.class);
        verify(aiClient).chat(captor.capture());

        TutorChatRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.getMessage()).isEqualTo(message);
        assertThat(capturedRequest.getContext()).isNotNull();
    }

    @Test
    @DisplayName("대화 히스토리와 함께 요청")
    void chat_shouldIncludeHistory() {
        // given
        Long userId = 1L;
        String message = "더 자세히 알려주세요";
        List<TutorChatRequest.ChatMessage> history = List.of(
                TutorChatRequest.ChatMessage.builder()
                        .role("user")
                        .content("DP가 뭐예요?")
                        .build(),
                TutorChatRequest.ChatMessage.builder()
                        .role("assistant")
                        .content("DP는 Dynamic Programming입니다.")
                        .build());

        TutorChatResponse mockResponse = new TutorChatResponse();
        mockResponse.setReply("물론이죠!");
        mockResponse.setTeachingStyle("direct");
        mockResponse.setEncouragement("열심히 하시네요!");
        mockResponse.setFollowUpQuestions(List.of());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(tagStatMapper.findByUserId(userId)).thenReturn(List.of());
        when(aiClient.chat(any(TutorChatRequest.class))).thenReturn(mockResponse);

        // when
        TutorChatResponse result = tutorService.chat(userId, message, history, null, null);

        // then
        ArgumentCaptor<TutorChatRequest> captor = ArgumentCaptor.forClass(TutorChatRequest.class);
        verify(aiClient).chat(captor.capture());

        TutorChatRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.getHistory()).hasSize(2);
    }

    private User createUser(Long id, Integer tier, Integer rating) {
        User user = new User();
        user.setId(id);
        user.setSolvedacTier(tier);
        user.setSolvedacRating(rating);
        return user;
    }
}
