package com.ssafy.dash.algorithm.domain;

import java.util.List;
import java.util.Optional;

public interface AlgorithmRecordRepository {
    void save(AlgorithmRecord record);
    void update(AlgorithmRecord record);
    Optional<AlgorithmRecord> findById(Long id);
    List<AlgorithmRecord> findAll();
    List<AlgorithmRecord> findByUserId(Long userId);
    boolean delete(Long id);
}
