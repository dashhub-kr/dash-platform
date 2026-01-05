package com.ssafy.dash.algorithm.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.StudyStats;

@Mapper
public interface AlgorithmRecordMapper {

    void insert(AlgorithmRecord record);

    AlgorithmRecord selectById(Long id);

    List<AlgorithmRecord> selectAll();

    List<AlgorithmRecord> selectByUserId(Long userId);

    List<AlgorithmRecord> selectByStudyId(Long studyId);

    void save(AlgorithmRecord record);

    void update(AlgorithmRecord record);

    StudyStats countsByStudyId(Long studyId);

    int delete(Long id);

    int countSuccessfulSubmissionByUserIdAndProblemNumber(Long userId, String problemNumber);

    List<String> selectSolvedProblemNumbersByUserId(Long userId);

    AlgorithmRecord selectLatestSuccessfulByUserAndProblem(Long userId, String problemNumber);

    void migrateStudyId(Long oldStudyId, Long newStudyId);

    void migrateUserRecords(Long userId, Long oldStudyId, Long newStudyId);

    List<com.ssafy.dash.dashboard.application.dto.response.HeatmapRawData> selectHeatmapDataByStudyId(Long studyId);
    
    List<com.ssafy.dash.dashboard.application.dto.response.HeatmapRawData> selectHeatmapDataByUserId(Long userId);
}
