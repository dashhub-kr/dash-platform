package com.ssafy.dash.acorn.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.dash.acorn.domain.AcornLog;

@Mapper
public interface AcornLogMapper {
    void insert(AcornLog log);
    List<AcornLog> selectByStudyId(Long studyId);
    void deleteByStudyId(Long studyId);
}
