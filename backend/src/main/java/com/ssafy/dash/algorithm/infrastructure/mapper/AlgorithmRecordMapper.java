package com.ssafy.dash.algorithm.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.dash.algorithm.domain.AlgorithmRecord;

@Mapper
public interface AlgorithmRecordMapper {

    void insert(AlgorithmRecord record);
    
    AlgorithmRecord selectById(Long id);
    
    List<AlgorithmRecord> selectAll();
    
    List<AlgorithmRecord> selectByUserId(Long userId);
    
    void update(AlgorithmRecord record);
    
    int delete(Long id);

}
