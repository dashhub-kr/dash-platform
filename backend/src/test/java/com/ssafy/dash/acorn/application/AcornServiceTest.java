package com.ssafy.dash.acorn.application;

import com.ssafy.dash.acorn.domain.AcornLog;
import com.ssafy.dash.acorn.domain.AcornLogRepository;
import com.ssafy.dash.study.domain.Study;
import com.ssafy.dash.study.domain.StudyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AcornServiceTest {

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private AcornLogRepository acornLogRepository;

    @InjectMocks
    private AcornService acornService;

    @Test
    @DisplayName("도토리를 적립하면 스터디 정보가 업데이트되고 로그가 저장된다")
    void accumulateAcorns() {
        // given
        Long studyId = 1L;
        Long userId = 1L;
        Integer amount = 10;
        String reason = "Problem solved";
        Study study = Study.create("Test Study");
        
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));

        // when
        acornService.accumulate(studyId, userId, amount, reason);

        // then
        verify(studyRepository).update(study);
        verify(acornLogRepository).save(any(AcornLog.class));
    }
}
