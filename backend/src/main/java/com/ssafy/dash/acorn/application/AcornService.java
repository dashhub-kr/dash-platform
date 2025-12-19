package com.ssafy.dash.acorn.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.acorn.domain.AcornLog;
import com.ssafy.dash.acorn.domain.AcornLogRepository;
import com.ssafy.dash.study.domain.Study;
import com.ssafy.dash.study.domain.StudyRepository;

@Service
public class AcornService {

    private final StudyRepository studyRepository;
    private final AcornLogRepository acornLogRepository;

    public AcornService(StudyRepository studyRepository, AcornLogRepository acornLogRepository) {
        this.studyRepository = studyRepository;
        this.acornLogRepository = acornLogRepository;
    }

    @Transactional
    public void accumulate(Long studyId, Long userId, Integer amount, String reason) {
        if (studyId == null || amount <= 0) return;

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("Study not found"));

        study.addAcorns(amount);
        studyRepository.update(study);

        AcornLog log = AcornLog.create(studyId, userId, amount, reason);
        acornLogRepository.save(log);
    }

    @Transactional
    public void use(Long studyId, Long userId, Integer amount, String reason) {
        if (studyId == null || amount <= 0) return;

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new IllegalArgumentException("Study not found"));

        study.useAcorns(amount);
        studyRepository.update(study);

        AcornLog log = AcornLog.create(studyId, userId, -amount, reason);
        acornLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AcornLog> getLogs(Long studyId) {
        return acornLogRepository.findByStudyId(studyId);
    }
}
