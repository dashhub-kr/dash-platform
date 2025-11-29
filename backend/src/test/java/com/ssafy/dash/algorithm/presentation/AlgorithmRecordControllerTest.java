package com.ssafy.dash.algorithm.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.ssafy.dash.algorithm.application.AlgorithmRecordService;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordCreateCommand;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordResult;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordUpdateCommand;
import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.user.domain.User;

@WebMvcTest(AlgorithmRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AlgorithmRecordControllerTest.TestConfig.class)
@DisplayName("AlgorithmRecordController 단위 테스트")
class AlgorithmRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlgorithmRecordService algorithmRecordService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AlgorithmRecordService algorithmRecordService() {
            
            return Mockito.mock(AlgorithmRecordService.class);
        }
    }

    @Test
    @WithMockUser
    @DisplayName("알고리즘 기록 생성 성공")
    void createRecord_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "Main.java", "text/plain", TestFixtures.TEST_ALGORITHM_CODE.getBytes());
        User user = TestFixtures.createUser();
        AlgorithmRecordResult result = TestFixtures.createAlgorithmRecordResult(user);

        given(algorithmRecordService.create(any(AlgorithmRecordCreateCommand.class))).willReturn(result);

        mockMvc.perform(multipart("/api/algorithm-records")
                .file(file)
                .param("problemNumber", TestFixtures.TEST_PROBLEM_NUMBER)
                .param("title", TestFixtures.TEST_ALGORITHM_TITLE)
                .param("language", TestFixtures.TEST_ALGORITHM_LANGUAGE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TestFixtures.TEST_ALGORITHM_RECORD_ID));
    }

    @Test
    @WithMockUser
    @DisplayName("알고리즘 기록 전체 조회 성공")
    void getAllRecords_Success() throws Exception {
        User user = TestFixtures.createUser();
        AlgorithmRecordResult result = TestFixtures.createAlgorithmRecordResult(user);
        given(algorithmRecordService.findAll()).willReturn(List.of(result));

        mockMvc.perform(get("/api/algorithm-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TestFixtures.TEST_ALGORITHM_RECORD_ID));
    }

    @Test
    @WithMockUser
    @DisplayName("알고리즘 기록 단건 조회 성공")
    void getRecordById_Success() throws Exception {
        User user = TestFixtures.createUser();
        AlgorithmRecordResult result = TestFixtures.createAlgorithmRecordResult(user);
        given(algorithmRecordService.findById(TestFixtures.TEST_ALGORITHM_RECORD_ID)).willReturn(result);

        mockMvc.perform(get("/api/algorithm-records/" + TestFixtures.TEST_ALGORITHM_RECORD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestFixtures.TEST_ALGORITHM_RECORD_ID));
    }

    @Test
    @WithMockUser
    @DisplayName("알고리즘 기록 수정 성공")
    void updateRecord_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "Main.java", "text/plain", "updated".getBytes());
        User user = TestFixtures.createUser();
        AlgorithmRecordResult result = TestFixtures.createAlgorithmRecordResult(user);

        given(algorithmRecordService.update(eq(TestFixtures.TEST_ALGORITHM_RECORD_ID), any(AlgorithmRecordUpdateCommand.class))).willReturn(result);

        mockMvc.perform(multipart("/api/algorithm-records/" + TestFixtures.TEST_ALGORITHM_RECORD_ID)
                .file(file)
                .param("title", "Updated")
                .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("알고리즘 기록 삭제 성공")
    void deleteRecord_Success() throws Exception {
        mockMvc.perform(delete("/api/algorithm-records/" + TestFixtures.TEST_ALGORITHM_RECORD_ID))
                .andExpect(status().isNoContent());
    }
    
}
