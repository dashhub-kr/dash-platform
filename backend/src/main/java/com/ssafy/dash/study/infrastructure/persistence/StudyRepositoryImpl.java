package com.ssafy.dash.study.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.study.domain.Study;
import com.ssafy.dash.study.domain.StudyApplication;
import com.ssafy.dash.study.domain.StudyRepository;
import com.ssafy.dash.study.infrastructure.mapper.StudyMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StudyRepositoryImpl implements StudyRepository {

    private final StudyMapper studyMapper;

    @Override
    public void save(Study study) {
        studyMapper.save(study);
    }

    @Override
    public Optional<Study> findById(Long id) {
        return studyMapper.findById(id);
    }

    @Override
    public List<Study> findAll() {
        return studyMapper.findAll();
    }

    @Override
    public void update(Study study) {
        studyMapper.update(study);
    }

    @Override
    public boolean delete(Long id) {
        return studyMapper.delete(id) > 0;
    }

    @Override
    public void saveApplication(StudyApplication application) {
        studyMapper.saveApplication(application);
    }

    @Override
    public Optional<StudyApplication> findApplicationById(Long id) {
        return Optional.ofNullable(studyMapper.findApplicationById(id));
    }

    @Override
    public Optional<StudyApplication> findApplicationByStudyIdAndUserId(Long studyId, Long userId) {
        return Optional.ofNullable(studyMapper.findApplicationByStudyIdAndUserId(studyId, userId));
    }

    @Override
    public void updateApplicationStatus(StudyApplication application) {
        studyMapper.updateApplicationStatus(application.getId(), application.getStatus());
    }

    @Override
    public java.util.List<StudyApplication> findPendingApplicationsByStudyId(Long studyId) {
        return studyMapper.findPendingApplicationsByStudyId(studyId);
    }

    @Override
    public Optional<StudyApplication> findPendingApplicationByUserId(Long userId) {
        return Optional.ofNullable(studyMapper.findPendingApplicationByUserId(userId));
    }
}
