package com.ssafy.dash.study.domain;

import java.util.List;

/**
 * 미션 제출 현황 Repository 인터페이스
 */
public interface StudyMissionSubmissionRepository {

    void save(StudyMissionSubmission submission);

    List<StudyMissionSubmission> findByMissionId(Long missionId);

    StudyMissionSubmission findByMissionIdAndUserIdAndProblemId(Long missionId, Long userId, Integer problemId);

    void markCompleted(Long missionId, Long userId, Integer problemId);

    int countCompletedByMissionIdAndUserId(Long missionId, Long userId);

    List<Integer> findCompletedProblemIds(Long missionId, Long userId);

    void deleteByMissionIdAndProblemId(Long missionId, Integer problemId);

    void updateSosStatus(Long missionId, Long userId, Integer problemId, boolean isSos);

    List<Integer> findSosProblemIds(Long missionId, Long userId);
}
