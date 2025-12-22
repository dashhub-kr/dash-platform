package com.ssafy.dash.ai.application;

import com.ssafy.dash.ai.client.AiServerClient;
import com.ssafy.dash.ai.client.dto.HintChatRequest;
import com.ssafy.dash.ai.client.dto.HintChatResponse;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
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
@DisplayName("AI 튜터 대화 서비스 테스트")
class TutorChatServiceTest {

    @Mock
    private AiServerClient aiClient;

    @Mock
    private UserSkillAnalysisService skillAnalysisService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AlgorithmRecordRepository algorithmRecordRepository;

    @Mock
    private com.ssafy.dash.acorn.application.AcornService acornService;

    private TutorChatService tutorChatService;

    @BeforeEach
    void setUp() {
        tutorChatService = new TutorChatService(aiClient, skillAnalysisService, userRepository,
                algorithmRecordRepository, acornService);
    }

    @Test
    @DisplayName("AI 튜터 대화 시 사용자 컨텍스트 포함 및 도토리 차감")
    void hintChatWithRecord_shouldIncludeUserContext() {
        // given
        Long userId = 1L;
        Long recordId = 10L;
        Long studyId = 100L;
        String message = "이 문제 너무 어려워요";
        String problemNumber = "1000";
        String problemTitle = "A+B";
        String code = "print(a+b)";
        String language = "python";
        String solveStatus = "wrong";
        String wrongReason = "시간초과";

        User user = createUser(userId, 11, 150); // Gold V
        user.updateStudy(studyId);

        AlgorithmRecord record = mock(AlgorithmRecord.class);
        when(record.getProblemNumber()).thenReturn(problemNumber);
        when(record.getTitle()).thenReturn(problemTitle);
        when(record.getCode()).thenReturn(code);
        when(record.getLanguage()).thenReturn(language);

        TagWeaknessDto weakness = TagWeaknessDto.builder()
                .tagKey("dp")
                .tagName("다이나믹 프로그래밍")
                .solved(5)
                .build();

        HintChatResponse mockResponse = HintChatResponse.builder()
                .reply("이 문제는 DP로 풀어보세요.")
                .teachingStyle("socratic")
                .build();

        when(algorithmRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(skillAnalysisService.getWeaknessTags(userId)).thenReturn(List.of(weakness));
        when(aiClient.hintChat(any(HintChatRequest.class))).thenReturn(mockResponse);

        // when
        HintChatResponse result = tutorChatService.hintChatWithRecord(
                userId, recordId, message, solveStatus, wrongReason, List.of());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReply()).isEqualTo("이 문제는 DP로 풀어보세요.");

        // Verify Acorn Usage (3 acorns)
        verify(acornService).use(eq(studyId), eq(userId), eq(3), anyString());

        ArgumentCaptor<HintChatRequest> captor = ArgumentCaptor.forClass(HintChatRequest.class);
        verify(aiClient).hintChat(captor.capture());

        HintChatRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.getProblemNumber()).isEqualTo(problemNumber);
        assertThat(capturedRequest.getSolveStatus()).isEqualTo(solveStatus);
        assertThat(capturedRequest.getUserContext()).isNotNull();
        assertThat(capturedRequest.getUserContext().getTierName()).isEqualTo("Gold V");
    }

    private User createUser(Long id, Integer tier, Integer rating) {
        User user = new User();
        user.setId(id);
        user.setSolvedacTier(tier);
        user.setSolvedacRating(rating);
        return user;
    }
}
