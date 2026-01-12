package com.ssafy.dash.ai.infrastructure;

import com.ssafy.dash.ai.domain.TutorConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TutorConversationMapper {
        void save(TutorConversation conversation);

        List<TutorConversation> findByUserIdAndSessionId(@Param("userId") Long userId,
                        @Param("sessionId") String sessionId);
}
