package com.ssafy.dash.social.domain;

import java.util.List;
import java.util.Optional;

public interface DirectMessageRepository {
    void save(DirectMessage message);
    Optional<DirectMessage> findById(Long id);
    List<DirectMessage> findBySenderIdAndReceiverId(Long senderId, Long receiverId); // 대화 기록
    List<DirectMessage> findConversation(Long userId1, Long userId2); // 양방향 대화 기록
    void update(DirectMessage message); // 읽음 처리 업데이트
    List<Long> findRecentChatPartners(Long userId);
}
