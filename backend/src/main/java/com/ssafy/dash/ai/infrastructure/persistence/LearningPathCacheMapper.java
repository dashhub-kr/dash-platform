package com.ssafy.dash.ai.infrastructure.persistence;

import com.ssafy.dash.ai.domain.LearningPathCache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * Learning Path 캐시 Mapper
 */
@Mapper
public interface LearningPathCacheMapper {

    /**
     * 사용자 ID로 캐시 조회
     */
    LearningPathCache findByUserId(@Param("userId") Long userId);

    /**
     * 캐시 저장
     */
    void save(LearningPathCache cache);

    /**
     * 캐시 업데이트
     */
    void update(@Param("userId") Long userId,
            @Param("analysisJson") String analysisJson,
            @Param("generatedAt") LocalDate generatedAt);
    /**
     * 캐시 삭제
     */
    void deleteByUserId(@Param("userId") Long userId);
}
