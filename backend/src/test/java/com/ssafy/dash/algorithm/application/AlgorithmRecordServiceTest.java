package com.ssafy.dash.algorithm.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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

import com.ssafy.dash.algorithm.application.dto.command.AlgorithmRecordCreateCommand;
import com.ssafy.dash.algorithm.application.dto.command.AlgorithmRecordUpdateCommand;
import com.ssafy.dash.algorithm.application.dto.result.AlgorithmRecordResult;
import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.algorithm.domain.exception.AlgorithmRecordNotFoundException;
import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;

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
        this.user = TestFixtures.createUser();
        this.record = TestFixtures.createAlgorithmRecord(user);
    }

    @Test
    @DisplayName("사용자가 존재할 때 알고리즘 기록을 생성한다")
    void createRecord_Success() {
        AlgorithmRecordCreateCommand command = TestFixtures.createAlgorithmRecordCreateCommand(TestFixtures.TEST_ALGORITHM_CODE);
        given(userRepository.findById(command.getUserId())).willReturn(Optional.of(user));

        AlgorithmRecordResult response = algorithmRecordService.create(command);

        verify(algorithmRecordRepository).save(any(AlgorithmRecord.class));
        assertThat(response.problemNumber()).isEqualTo(command.getProblemNumber());
        assertThat(response.code()).isEqualTo(TestFixtures.TEST_ALGORITHM_CODE);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 기록 생성 시 예외가 발생한다")
    void createRecord_UserMissing_Throws() {
        AlgorithmRecordCreateCommand command = TestFixtures.createAlgorithmRecordCreateCommand(TestFixtures.TEST_ALGORITHM_CODE);
        given(userRepository.findById(command.getUserId())).willReturn(Optional.empty());

        assertThatThrownBy(() -> algorithmRecordService.create(command))
                .isInstanceOf(UserNotFoundException.class);
        verify(algorithmRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("ID로 알고리즘 기록을 조회한다")
    void findById_Success() {
        given(algorithmRecordRepository.findById(TestFixtures.TEST_ALGORITHM_RECORD_ID)).willReturn(Optional.of(record));

        AlgorithmRecordResult response = algorithmRecordService.findById(TestFixtures.TEST_ALGORITHM_RECORD_ID);

        assertThat(response.id()).isEqualTo(TestFixtures.TEST_ALGORITHM_RECORD_ID);
        assertThat(response.title()).isEqualTo(TestFixtures.TEST_ALGORITHM_TITLE);
    }

    @Test
    @DisplayName("없는 ID로 조회 시 예외가 발생한다")
    void findById_NotFound_Throws() {
        given(algorithmRecordRepository.findById(TestFixtures.TEST_ALGORITHM_RECORD_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> algorithmRecordService.findById(TestFixtures.TEST_ALGORITHM_RECORD_ID))
                .isInstanceOf(AlgorithmRecordNotFoundException.class);
    }

    @Test
    @DisplayName("모든 알고리즘 기록을 조회한다")
    void findAll_Success() {
        given(algorithmRecordRepository.findAll()).willReturn(List.of(record));

        List<AlgorithmRecordResult> responses = algorithmRecordService.findAll();

        assertThat(responses)
                .hasSize(1)
                .first()
                .matches(result -> result.id().equals(TestFixtures.TEST_ALGORITHM_RECORD_ID));
    }

    @Test
    @DisplayName("사용자별 알고리즘 기록을 조회한다")
    void findByUserId_Success() {
        given(algorithmRecordRepository.findByUserId(TestFixtures.TEST_USER_ID)).willReturn(List.of(record));

        List<AlgorithmRecordResult> responses = algorithmRecordService.findByUserId(TestFixtures.TEST_USER_ID);

        assertThat(responses)
                .hasSize(1)
                .first()
                .matches(result -> result.userId().equals(TestFixtures.TEST_USER_ID));
    }

    @Test
    @DisplayName("기록을 수정하면 변경 값이 반영된다")
    void updateRecord_Success() {
        AlgorithmRecordUpdateCommand command = TestFixtures.createAlgorithmRecordUpdateCommand("updated code");
        given(algorithmRecordRepository.findById(TestFixtures.TEST_ALGORITHM_RECORD_ID)).willReturn(Optional.of(record));

        AlgorithmRecordResult response = algorithmRecordService.update(TestFixtures.TEST_ALGORITHM_RECORD_ID, command);

        verify(algorithmRecordRepository).update(any(AlgorithmRecord.class));
        assertThat(response.title()).isEqualTo("Updated Title");
        assertThat(response.code()).isEqualTo("updated code");
    }

    @Test
    @DisplayName("없는 기록 수정 시 예외가 발생한다")
    void updateRecord_NotFound_Throws() {
        AlgorithmRecordUpdateCommand command = TestFixtures.createAlgorithmRecordUpdateCommand("updated code");
        given(algorithmRecordRepository.findById(TestFixtures.TEST_ALGORITHM_RECORD_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> algorithmRecordService.update(TestFixtures.TEST_ALGORITHM_RECORD_ID, command))
                .isInstanceOf(AlgorithmRecordNotFoundException.class);
    }

    @Test
    @DisplayName("기록 삭제 시 true를 반환하면 예외 없이 끝난다")
    void deleteRecord_Success() {
        given(algorithmRecordRepository.delete(TestFixtures.TEST_ALGORITHM_RECORD_ID)).willReturn(true);

        algorithmRecordService.delete(TestFixtures.TEST_ALGORITHM_RECORD_ID);

        verify(algorithmRecordRepository).delete(TestFixtures.TEST_ALGORITHM_RECORD_ID);
    }

    @Test
    @DisplayName("삭제할 기록이 없으면 예외가 발생한다")
    void deleteRecord_NotFound_Throws() {
        given(algorithmRecordRepository.delete(TestFixtures.TEST_ALGORITHM_RECORD_ID)).willReturn(false);

        assertThatThrownBy(() -> algorithmRecordService.delete(TestFixtures.TEST_ALGORITHM_RECORD_ID))
                .isInstanceOf(AlgorithmRecordNotFoundException.class);
    }
    
}
