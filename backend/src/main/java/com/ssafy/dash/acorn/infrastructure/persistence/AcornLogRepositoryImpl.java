package com.ssafy.dash.acorn.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.acorn.domain.AcornLog;
import com.ssafy.dash.acorn.domain.AcornLogRepository;
import com.ssafy.dash.acorn.infrastructure.mapper.AcornLogMapper;

@Repository
public class AcornLogRepositoryImpl implements AcornLogRepository {

    private final AcornLogMapper acornLogMapper;

    public AcornLogRepositoryImpl(AcornLogMapper acornLogMapper) {
        this.acornLogMapper = acornLogMapper;
    }

    @Override
    public void save(AcornLog log) {
        acornLogMapper.insert(log);
    }

    @Override
    public List<AcornLog> findByStudyId(Long studyId) {
        return acornLogMapper.selectByStudyId(studyId);
    }
}
