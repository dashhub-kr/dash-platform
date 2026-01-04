package com.ssafy.dash.study.domain;

import java.util.List;
import java.util.Optional;

public interface StudyRepository {

    void save(Study study);

    Optional<Study> findById(Long id);

    List<Study> findAll();

    List<Study> searchByKeyword(String keyword);

    void update(Study study);

    boolean delete(Long id);

    void saveApplication(StudyApplication application);

    Optional<StudyApplication> findApplicationById(Long id);

    Optional<StudyApplication> findApplicationByStudyIdAndUserId(Long studyId, Long userId);

    void updateApplicationStatus(StudyApplication application);

    List<StudyApplication> findPendingApplicationsByStudyId(Long studyId);

    Optional<StudyApplication> findPendingApplicationByUserId(Long userId);

    void deleteApplication(Long id);
}
