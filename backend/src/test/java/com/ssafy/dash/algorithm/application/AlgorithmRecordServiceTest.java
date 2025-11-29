package com.ssafy.dash.algorithm.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordCreateCommand;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordResult;
import com.ssafy.dash.algorithm.application.dto.AlgorithmRecordUpdateCommand;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlgorithmRecordService 단위 테스트")
class AlgorithmRecordServiceTest {

    @Mock
    private AlgorithmRecordRepository algorithmRecordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AlgorithmRecordService algorithmRecordService;

    private User user;
    private AlgorithmRecord record;

    @BeforeEach
    void setUp() {
        user = TestFixtures.createUser();
        record = TestFixtures.createAlgorithmRecord(user);
    }

    @Test
    @DisplayName("알고리즘 기록 생성 성공")
    void createRecord_Success() {
        // given
        AlgorithmRecordCreateCommand command = TestFixtures.createAlgorithmRecordCreateCommand(TestFixtures.TEST_ALGORITHM_CODE);
        given(userRepository.findById(command.getUserId())).willReturn(Optional.of(user));
        
        // when
        AlgorithmRecordResult response = algorithmRecordService.create(command);

        // then
        verify(algorithmRecordRepository).save(any(AlgorithmRecord.class));
        assertThat(response.getProblemNumber()).isEqualTo(command.getProblemNumber());
        assertThat(response.getCode()).isEqualTo(TestFixtures.TEST_ALGORITHM_CODE);
    }

    @Test
    @DisplayName("알고리즘 기록 단건 조회 성공")
    void findById_Success() {
        // given
        given(algorithmRecordRepository.findById(TestFixtures.TEST_ALGORITHM_RECORD_ID)).willReturn(Optional.of(record));

        // when
        AlgorithmRecordResult response = algorithmRecordService.findById(TestFixtures.TEST_ALGORITHM_RECORD_ID);

        // then
        assertThat(response.getId()).isEqualTo(TestFixtures.TEST_ALGORITHM_RECORD_ID);
        assertThat(response.getTitle()).isEqualTo(TestFixtures.TEST_ALGORITHM_TITLE);
    }

    @Test
    @DisplayName("알고리즘 기록 전체 조회 성공")
    void findAll_Success() {
        // given
        given(algorithmRecordRepository.findAll()).willReturn(List.of(record));

        // when
        List<AlgorithmRecordResult> responses = algorithmRecordService.findAll();

        // then
        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("알고리즘 기록 수정 성공")
    void updateRecord_Success() {
        // given
        AlgorithmRecordUpdateCommand command = TestFixtures.createAlgorithmRecordUpdateCommand("updated code");
        
        given(algorithmRecordRepository.findById(TestFixtures.TEST_ALGORITHM_RECORD_ID)).willReturn(Optional.of(record));

        // when
        AlgorithmRecordResult response = algorithmRecordService.update(TestFixtures.TEST_ALGORITHM_RECORD_ID, command);

        // then
        verify(algorithmRecordRepository).update(any(AlgorithmRecord.class));
        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.getCode()).isEqualTo("updated code");
    }

    @Test
    @DisplayName("알고리즘 기록 삭제 성공")
    void deleteRecord_Success() {
        // given
        given(algorithmRecordRepository.delete(TestFixtures.TEST_ALGORITHM_RECORD_ID)).willReturn(true);

        // when
        algorithmRecordService.delete(TestFixtures.TEST_ALGORITHM_RECORD_ID);

        // then
        verify(algorithmRecordRepository).delete(TestFixtures.TEST_ALGORITHM_RECORD_ID);
    }
    
}
