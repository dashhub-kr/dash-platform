package com.ssafy.dash.study.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.study.domain.StudyMission;
import com.ssafy.dash.study.domain.StudyMissionRepository;
import com.ssafy.dash.study.infrastructure.mapper.StudyMissionMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StudyMissionRepositoryImpl implements StudyMissionRepository {

    private final StudyMissionMapper mapper;

    @Override
    public void save(StudyMission mission) {
        mapper.insert(mission);
    }

    @Override
    public Optional<StudyMission> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public List<StudyMission> findByStudyId(Long studyId) {
        return mapper.selectByStudyId(studyId);
    }

    @Override
    public List<StudyMission> findByStudyIdOrderByWeekDesc(Long studyId) {
        return mapper.selectByStudyIdOrderByWeekDesc(studyId);
    }

    @Override
    public Optional<StudyMission> findByStudyIdAndWeek(Long studyId, Integer week) {
        return Optional.ofNullable(mapper.selectByStudyIdAndWeek(studyId, week));
    }

    @Override
    public void delete(Long id) {
        mapper.delete(id);
    }

    @Override
    public void update(StudyMission mission) {
        mapper.update(mission);
    }
}
