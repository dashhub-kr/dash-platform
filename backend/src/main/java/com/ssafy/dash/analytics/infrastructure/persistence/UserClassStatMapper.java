package com.ssafy.dash.analytics.infrastructure.persistence;

import com.ssafy.dash.analytics.domain.UserClassStat;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 사용자 클래스 통계 Mapper
 */
@Mapper
public interface UserClassStatMapper {
    /**
     * 클래스 통계 저장 또는 업데이트 (UPSERT)
     */
    void upsert(UserClassStat stat);

    /**
     * 사용자의 모든 클래스 통계 조회
     */
    List<UserClassStat> findByUserId(Long userId);

    /**
     * 특정 클래스 통계 조회
     */
    UserClassStat findByUserIdAndClass(Long userId, Integer classNumber);

    /**
     * 사용자의 모든 클래스 통계 삭제
     */
    void deleteByUserId(Long userId);
}
