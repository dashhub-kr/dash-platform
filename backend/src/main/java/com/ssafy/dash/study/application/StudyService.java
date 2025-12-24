package com.ssafy.dash.study.application;

import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.algorithm.domain.StudyStats;
import com.ssafy.dash.study.application.dto.result.StudyStatsResult;
import com.ssafy.dash.study.domain.Study;
import com.ssafy.dash.study.domain.StudyRepository;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final UserRepository userRepository;
    private final AlgorithmRecordRepository algorithmRecordRepository;

    @Transactional(readOnly = true)
    public List<Study> findAll() {
        return studyRepository.findAll();
    }

    @Transactional
    public Study createStudy(Long userId, String name) {
        Study study = Study.create(name, userId);
        studyRepository.save(study);
        // MyBatis가 insert 후 객체에 ID를 설정한다고 가정

        joinStudy(userId, study.getId());

        return study;
    }

    @Transactional
    public void joinStudy(Long userId, Long studyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // existsById가 없으므로 findById를 사용하여 확인
        if (studyRepository.findById(studyId).isEmpty()) {
            throw new IllegalArgumentException("Study not found");
        }

        user.updateStudy(studyId);
        userRepository.update(user);
    }

    @Transactional(readOnly = true)
    public StudyStatsResult getStudyStats(Long studyId) {
        StudyStats stats = algorithmRecordRepository.countsByStudyId(studyId);
        return new StudyStatsResult(stats.bronze(), stats.silver(), stats.gold(), stats.platinum());
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Study> findStudyById(Long studyId) {
        return studyRepository.findById(studyId);
    }

}
