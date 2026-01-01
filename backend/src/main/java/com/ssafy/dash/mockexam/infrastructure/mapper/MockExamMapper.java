package com.ssafy.dash.mockexam.infrastructure.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.dash.mockexam.domain.MockExam;

/**
 * MockExam MyBatis Mapper
 */
@Mapper
public interface MockExamMapper {

    void insert(MockExam mockExam);

    MockExam selectActiveByUserId(Long userId);

    MockExam selectById(Long id);

    void update(MockExam mockExam);

    int delete(Long id);
}
