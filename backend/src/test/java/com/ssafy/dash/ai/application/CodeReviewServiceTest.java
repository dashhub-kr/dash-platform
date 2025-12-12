package com.ssafy.dash.ai.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.ai.client.AiServerClient;
import com.ssafy.dash.ai.client.dto.CodeReviewRequest;
import com.ssafy.dash.ai.client.dto.CodeReviewResponse;
import com.ssafy.dash.ai.domain.CodeAnalysisResult;
import com.ssafy.dash.ai.infrastructure.CodeAnalysisResultMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CodeReviewService 테스트")
class CodeReviewServiceTest {

    @Mock
    private AiServerClient aiClient;

    @Mock
    private CodeAnalysisResultMapper mapper;

    private CodeReviewService codeReviewService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        codeReviewService = new CodeReviewService(aiClient, mapper, objectMapper);
    }

    @Test
    @DisplayName("코드 분석 요청 시 AI 서버 호출 및 결과 저장")
    void analyzeAndSave_shouldCallAiServerAndSaveResult() {
        // given
        Long algorithmRecordId = 1L;
        String code = "public class Solution {}";
        String language = "java";
        String problemNumber = "1000";

        CodeReviewResponse mockResponse = createMockResponse();
        when(aiClient.analyzeCode(any(CodeReviewRequest.class))).thenReturn(mockResponse);
        doNothing().when(mapper).deleteByAlgorithmRecordId(algorithmRecordId);
        doNothing().when(mapper).insert(any(CodeAnalysisResult.class));

        // when
        CodeAnalysisResult result = codeReviewService.analyzeAndSave(
                algorithmRecordId, code, language, problemNumber);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAlgorithmRecordId()).isEqualTo(algorithmRecordId);
        assertThat(result.getSummary()).isEqualTo("테스트 요약");
        verify(aiClient, times(1)).analyzeCode(any(CodeReviewRequest.class));
        verify(mapper, times(1)).insert(any(CodeAnalysisResult.class));
    }

    @Test
    @DisplayName("분석 결과 조회 - 결과 존재 시")
    void getAnalysisResult_whenExists_shouldReturnResult() {
        // given
        Long algorithmRecordId = 1L;
        CodeAnalysisResult savedResult = CodeAnalysisResult.builder()
                .algorithmRecordId(algorithmRecordId)
                .summary("저장된 요약")
                .build();
        when(mapper.findByAlgorithmRecordId(algorithmRecordId)).thenReturn(Optional.of(savedResult));

        // when
        Optional<CodeAnalysisResult> result = codeReviewService.getAnalysisResult(algorithmRecordId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getSummary()).isEqualTo("저장된 요약");
    }

    @Test
    @DisplayName("분석 결과 조회 - 결과 없음")
    void getAnalysisResult_whenNotExists_shouldReturnEmpty() {
        // given
        Long algorithmRecordId = 999L;
        when(mapper.findByAlgorithmRecordId(algorithmRecordId)).thenReturn(Optional.empty());

        // when
        Optional<CodeAnalysisResult> result = codeReviewService.getAnalysisResult(algorithmRecordId);

        // then
        assertThat(result).isEmpty();
    }

    private CodeReviewResponse createMockResponse() {
        CodeReviewResponse response = new CodeReviewResponse();
        response.setSummary("테스트 요약");

        CodeReviewResponse.ComplexityInfo complexity = new CodeReviewResponse.ComplexityInfo();
        complexity.setTime("O(n)");
        complexity.setSpace("O(1)");
        response.setComplexity(complexity);

        return response;
    }
}
