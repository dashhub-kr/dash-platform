package com.ssafy.dash.algorithm.presentation;

import com.ssafy.dash.algorithm.application.AlgorithmRecordService;
import com.ssafy.dash.algorithm.application.dto.command.AlgorithmRecordUpdateCommand;
import com.ssafy.dash.algorithm.application.dto.result.AlgorithmRecordResult;
import com.ssafy.dash.algorithm.domain.exception.AlgorithmRecordNotFoundException;
import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlgorithmRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AlgorithmRecordControllerTest.TestConfig.class)
@DisplayName("AlgorithmRecordController 단위 테스트")
class AlgorithmRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlgorithmRecordService algorithmRecordService;

    private AlgorithmRecordResult recordResult;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AlgorithmRecordService algorithmRecordService() {
            return Mockito.mock(AlgorithmRecordService.class);
        }
    }

    @BeforeEach
    void setUp() {
        User user = TestFixtures.createUser();
        this.recordResult = TestFixtures.createAlgorithmRecordResult(user);
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(algorithmRecordService);
    }

    @Test
    @WithMockUser
    @DisplayName("알고리즘 기록 전체를 조회하면 목록을 반환한다")
    void getAllRecords_Success() throws Exception {
        given(algorithmRecordService.findAll()).willReturn(List.of(recordResult));

        mockMvc.perform(get("/api/algorithm-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TestFixtures.TEST_ALGORITHM_RECORD_ID));
    }

    @Test
    @WithMockUser
    @DisplayName("ID로 알고리즘 기록을 조회하면 단건 결과를 반환한다")
    void getRecordById_Success() throws Exception {
        given(algorithmRecordService.findById(TestFixtures.TEST_ALGORITHM_RECORD_ID)).willReturn(recordResult);

        mockMvc.perform(get("/api/algorithm-records/" + TestFixtures.TEST_ALGORITHM_RECORD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestFixtures.TEST_ALGORITHM_RECORD_ID));
    }

    @Test
    @WithMockUser
    @DisplayName("존재하지 않는 알고리즘 기록을 조회하면 404를 반환한다")
    void getRecordById_Failure_NotFound() throws Exception {
        given(algorithmRecordService.findById(TestFixtures.TEST_ALGORITHM_RECORD_ID))
                .willThrow(new AlgorithmRecordNotFoundException(TestFixtures.TEST_ALGORITHM_RECORD_ID));

        mockMvc.perform(get("/api/algorithm-records/" + TestFixtures.TEST_ALGORITHM_RECORD_ID))
                .andExpect(status().isNotFound());

        verify(algorithmRecordService).findById(TestFixtures.TEST_ALGORITHM_RECORD_ID);
    }

    @Test
    @WithMockUser
    @DisplayName("알고리즘 기록 수정 요청이 성공하면 변경된 결과를 반환한다")
    void updateRecord_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "Main.java", "text/plain", "updated".getBytes());
        given(algorithmRecordService.update(eq(TestFixtures.TEST_ALGORITHM_RECORD_ID),
                any(AlgorithmRecordUpdateCommand.class))).willReturn(recordResult);

        mockMvc.perform(multipart("/api/algorithm-records/" + TestFixtures.TEST_ALGORITHM_RECORD_ID)
                .file(file)
                .param("title", "Updated")
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestFixtures.TEST_ALGORITHM_RECORD_ID));

        verify(algorithmRecordService).update(eq(TestFixtures.TEST_ALGORITHM_RECORD_ID),
                any(AlgorithmRecordUpdateCommand.class));
    }

    @Test
    @WithMockUser
    @DisplayName("알고리즘 기록 삭제에 성공하면 204를 반환한다")
    void deleteRecord_Success() throws Exception {
        mockMvc.perform(delete("/api/algorithm-records/" + TestFixtures.TEST_ALGORITHM_RECORD_ID))
                .andExpect(status().isNoContent());

        verify(algorithmRecordService).delete(TestFixtures.TEST_ALGORITHM_RECORD_ID);
    }

    @Test
    @WithMockUser
    @DisplayName("존재하지 않는 알고리즘 기록을 삭제하면 404를 반환한다")
    void deleteRecord_Failure_NotFound() throws Exception {
        willThrow(new AlgorithmRecordNotFoundException(TestFixtures.TEST_ALGORITHM_RECORD_ID))
                .given(algorithmRecordService).delete(TestFixtures.TEST_ALGORITHM_RECORD_ID);

        mockMvc.perform(delete("/api/algorithm-records/" + TestFixtures.TEST_ALGORITHM_RECORD_ID))
                .andExpect(status().isNotFound());

        verify(algorithmRecordService).delete(TestFixtures.TEST_ALGORITHM_RECORD_ID);
    }

}
