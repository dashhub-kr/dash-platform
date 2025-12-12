package com.ssafy.dash.ai.infrastructure;

import com.ssafy.dash.ai.domain.TutorConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 튜터 대화 MyBatis 매퍼
 */
@Mapper
public interface TutorConversationMapper {

    /**
     * 대화 메시지 저장
     */
    void insert(TutorConversation conversation);

    /**
     * 세션의 최근 대화 조회 (AI 전송용)
     * 
     * @param userId    사용자 ID
     * @param sessionId 세션 ID
     * @param limit     조회 개수
     * @return 최근 대화 목록 (오래된 순)
     */
    List<TutorConversation> findRecentBySession(
            @Param("userId") Long userId,
            @Param("sessionId") String sessionId,
            @Param("limit") int limit);

    /**
     * 사용자의 세션 목록 조회 (최근 30일)
     */
    List<String> findSessionsByUser(@Param("userId") Long userId);

    /**
     * 세션의 마지막 활동 시간 조회
     */
    java.time.LocalDateTime findLastActivityBySession(
            @Param("userId") Long userId,
            @Param("sessionId") String sessionId);

    /**
     * 90일 이상 된 대화 삭제
     */
    int deleteOldConversations(@Param("daysOld") int daysOld);
}
