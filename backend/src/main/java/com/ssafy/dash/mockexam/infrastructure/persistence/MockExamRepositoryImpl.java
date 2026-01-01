package com.ssafy.dash.mockexam.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.mockexam.domain.MockExam;
import com.ssafy.dash.mockexam.domain.MockExamRepository;
import com.ssafy.dash.mockexam.infrastructure.mapper.MockExamMapper;

/**
 * MockExam Repository 구현체
 */
@Repository
public class MockExamRepositoryImpl implements MockExamRepository {

    private final MockExamMapper mockExamMapper;

    public MockExamRepositoryImpl(MockExamMapper mockExamMapper) {
        this.mockExamMapper = mockExamMapper;
    }

    @Override
    public void save(MockExam mockExam) {
        mockExamMapper.insert(mockExam);
    }

    @Override
    public Optional<MockExam> findActiveByUserId(Long userId) {
        return Optional.ofNullable(mockExamMapper.selectActiveByUserId(userId));
    }

    @Override
    public Optional<MockExam> findById(Long id) {
        return Optional.ofNullable(mockExamMapper.selectById(id));
    }

    @Override
    public void update(MockExam mockExam) {
        mockExamMapper.update(mockExam);
    }

    @Override
    public void delete(Long id) {
        mockExamMapper.delete(id);
    }
}
