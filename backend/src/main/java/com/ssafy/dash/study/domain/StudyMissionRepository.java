package com.ssafy.dash.study.domain;

import java.util.List;
import java.util.Optional;

/**
 * 스터디 미션 Repository 인터페이스
 */
public interface StudyMissionRepository {

    void save(StudyMission mission);

    Optional<StudyMission> findById(Long id);

    List<StudyMission> findByStudyId(Long studyId);

    List<StudyMission> findByStudyIdOrderByWeekDesc(Long studyId);

    void delete(Long id);

    void update(StudyMission mission);
}
