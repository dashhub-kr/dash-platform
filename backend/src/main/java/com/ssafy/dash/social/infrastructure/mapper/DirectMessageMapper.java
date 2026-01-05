package com.ssafy.dash.social.infrastructure.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.ssafy.dash.social.domain.DirectMessage;

@Mapper
public interface DirectMessageMapper {
    void save(DirectMessage message);
    DirectMessage findById(Long id);
    List<DirectMessage> findBySenderIdAndReceiverId(Long senderId, Long receiverId); // 대화 기록
    List<DirectMessage> findConversation(Long userId1, Long userId2); // 양방향 대화 기록
    void update(DirectMessage message); // 읽음 처리 업데이트
    List<Long> findRecentChatPartners(Long userId);
}
