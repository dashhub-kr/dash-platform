package com.ssafy.dash.algorithm.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AlgorithmRecordRepository {

    void save(AlgorithmRecord record);

    void update(AlgorithmRecord record);

    Optional<AlgorithmRecord> findById(Long id);

    List<AlgorithmRecord> findAll();

    List<AlgorithmRecord> findByUserId(Long userId);

    List<AlgorithmRecord> findByStudyId(Long studyId);

    boolean delete(Long id);

    StudyStats countsByStudyId(Long studyId);

    boolean existsSuccessfulSubmission(Long userId, String problemNumber);

    Set<String> findSolvedProblemNumbers(Long userId);

    Optional<AlgorithmRecord> findLatestSuccessfulByUserAndProblem(Long userId, String problemNumber);

    void migrateStudyId(Long oldStudyId, Long newStudyId);

    void migrateUserRecords(Long userId, Long oldStudyId, Long newStudyId);

    List<com.ssafy.dash.dashboard.application.dto.response.HeatmapRawData> findHeatmapDataByStudyId(Long studyId);

    List<com.ssafy.dash.dashboard.application.dto.response.HeatmapRawData> findHeatmapDataByUserId(Long userId);
}
