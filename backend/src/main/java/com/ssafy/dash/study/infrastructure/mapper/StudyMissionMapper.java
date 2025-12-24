package com.ssafy.dash.study.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.dash.study.domain.StudyMission;

/**
 * 스터디 미션 MyBatis Mapper
 */
@Mapper
public interface StudyMissionMapper {

    void insert(StudyMission mission);

    StudyMission selectById(Long id);

    List<StudyMission> selectByStudyId(Long studyId);

    List<StudyMission> selectByStudyIdOrderByWeekDesc(Long studyId);

    int delete(Long id);

    void update(StudyMission mission);
}
