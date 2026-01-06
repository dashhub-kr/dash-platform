package com.ssafy.dash.acorn.domain;

import java.util.List;

public interface AcornLogRepository {
    void save(AcornLog log);
    List<AcornLog> findByStudyId(Long studyId);
    void deleteByStudyId(Long studyId);
}
