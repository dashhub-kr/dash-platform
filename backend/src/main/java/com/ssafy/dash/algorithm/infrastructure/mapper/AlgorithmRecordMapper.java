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

}
