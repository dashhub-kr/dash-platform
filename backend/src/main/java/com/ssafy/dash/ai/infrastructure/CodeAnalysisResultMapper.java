package com.ssafy.dash.ai.infrastructure;

import com.ssafy.dash.ai.domain.CodeAnalysisResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * 코드 분석 결과 MyBatis 매퍼
 */
@Mapper
public interface CodeAnalysisResultMapper {

    void insert(CodeAnalysisResult result);

    Optional<CodeAnalysisResult> findByAlgorithmRecordId(@Param("algorithmRecordId") Long algorithmRecordId);

    void update(CodeAnalysisResult result);

    void deleteByAlgorithmRecordId(@Param("algorithmRecordId") Long algorithmRecordId);
}
