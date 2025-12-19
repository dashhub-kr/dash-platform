package com.ssafy.dash.algorithm.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.algorithm.domain.AlgorithmRecord;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.algorithm.domain.StudyStats;
import com.ssafy.dash.algorithm.infrastructure.mapper.AlgorithmRecordMapper;

@Repository
public class AlgorithmRecordRepositoryImpl implements AlgorithmRecordRepository {

    private final AlgorithmRecordMapper mapper;

    public AlgorithmRecordRepositoryImpl(AlgorithmRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(AlgorithmRecord record) {
        mapper.insert(record);
    }

    @Override
    public void update(AlgorithmRecord record) {
        mapper.update(record);
    }

    @Override
    public Optional<AlgorithmRecord> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public List<AlgorithmRecord> findAll() {
        return mapper.selectAll();
    }

    @Override
    public List<AlgorithmRecord> findByUserId(Long userId) {
        return mapper.selectByUserId(userId);
    }

    @Override
    public List<AlgorithmRecord> findByStudyId(Long studyId) {
        return mapper.selectByStudyId(studyId);
    }

    @Override
    public boolean delete(Long id) {
        return mapper.delete(id) > 0;
    }

    @Override
    public StudyStats countsByStudyId(Long studyId) {
        return mapper.countsByStudyId(studyId);
    }

}
