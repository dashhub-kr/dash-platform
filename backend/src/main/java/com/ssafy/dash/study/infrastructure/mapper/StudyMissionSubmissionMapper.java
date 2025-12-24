package com.ssafy.dash.study.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.dash.study.domain.StudyMissionSubmission;

/**
 * 미션 제출 현황 MyBatis Mapper
 */
@Mapper
public interface StudyMissionSubmissionMapper {

        void insert(StudyMissionSubmission submission);

        List<StudyMissionSubmission> selectByMissionId(Long missionId);

        StudyMissionSubmission selectByMissionIdAndUserIdAndProblemId(
                        @Param("missionId") Long missionId,
                        @Param("userId") Long userId,
                        @Param("problemId") Integer problemId);

        void markCompleted(
                        @Param("missionId") Long missionId,
                        @Param("userId") Long userId,
                        @Param("problemId") Integer problemId);

        int countCompletedByMissionIdAndUserId(
                        @Param("missionId") Long missionId,
                        @Param("userId") Long userId);

        void deleteByMissionIdAndProblemId(
                        @Param("missionId") Long missionId,
                        @Param("problemId") Integer problemId);

        List<Integer> selectCompletedProblemIds(
                        @Param("missionId") Long missionId,
                        @Param("userId") Long userId);

        void updateSosStatus(
                        @Param("missionId") Long missionId,
                        @Param("userId") Long userId,
                        @Param("problemId") Integer problemId,
                        @Param("isSos") boolean isSos);

        List<Integer> selectSosProblemIds(
                        @Param("missionId") Long missionId,
                        @Param("userId") Long userId);
}
