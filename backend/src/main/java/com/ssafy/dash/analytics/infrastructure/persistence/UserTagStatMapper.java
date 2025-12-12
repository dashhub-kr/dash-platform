package com.ssafy.dash.analytics.infrastructure.persistence;

import com.ssafy.dash.analytics.domain.UserTagStat;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 사용자 태그 통계 Mapper
 */
@Mapper
public interface UserTagStatMapper {
    /**
     * 태그 통계 저장 또는 업데이트 (UPSERT)
     */
    void upsert(UserTagStat stat);

    /**
     * 사용자의 모든 태그 통계 조회
     */
    List<UserTagStat> findByUserId(Long userId);

    /**
     * 특정 태그 통계 조회
     */
    UserTagStat findByUserIdAndTag(Long userId, String tagKey);

    /**
     * 사용자의 모든 태그 통계 삭제
     */
    void deleteByUserId(Long userId);
}
