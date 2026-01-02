package com.ssafy.dash.study.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    StudyMission selectByStudyIdAndWeek(@Param("studyId") Long studyId, @Param("week") Integer week);

    int delete(Long id);

    void update(StudyMission mission);
}
