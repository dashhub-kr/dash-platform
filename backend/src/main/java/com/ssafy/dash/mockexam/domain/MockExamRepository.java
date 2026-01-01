package com.ssafy.dash.mockexam.domain;

import java.util.Optional;

/**
 * MockExam Repository 인터페이스
 */
public interface MockExamRepository {

    void save(MockExam mockExam);

    Optional<MockExam> findActiveByUserId(Long userId);

    Optional<MockExam> findById(Long id);

    void update(MockExam mockExam);

    void delete(Long id);
}
