package com.ssafy.dash.social.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.social.domain.DirectMessage;
import com.ssafy.dash.social.domain.DirectMessageRepository;
import com.ssafy.dash.social.infrastructure.mapper.DirectMessageMapper;

@Repository
public class DirectMessageRepositoryImpl implements DirectMessageRepository {

    private final DirectMessageMapper directMessageMapper;

    public DirectMessageRepositoryImpl(DirectMessageMapper directMessageMapper) {
        this.directMessageMapper = directMessageMapper;
    }

    @Override
    public void save(DirectMessage message) {
        directMessageMapper.save(message);
    }

    @Override
    public Optional<DirectMessage> findById(Long id) {
        return Optional.ofNullable(directMessageMapper.findById(id));
    }

    @Override
    public List<DirectMessage> findBySenderIdAndReceiverId(Long senderId, Long receiverId) {
        return directMessageMapper.findBySenderIdAndReceiverId(senderId, receiverId);
    }

    @Override
    public List<DirectMessage> findConversation(Long userId1, Long userId2) {
        return directMessageMapper.findConversation(userId1, userId2);
    }

    @Override
    public void update(DirectMessage message) {
        directMessageMapper.update(message);
    }

    @Override
    public List<Long> findRecentChatPartners(Long userId) {
        return directMessageMapper.findRecentChatPartners(userId);
    }
}
