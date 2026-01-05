package com.ssafy.dash.social.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.social.domain.Friendship;
import com.ssafy.dash.social.domain.FriendshipRepository;
import com.ssafy.dash.social.infrastructure.mapper.FriendshipMapper;

@Repository
public class FriendshipRepositoryImpl implements FriendshipRepository {

    private final FriendshipMapper friendshipMapper;

    public FriendshipRepositoryImpl(FriendshipMapper friendshipMapper) {
        this.friendshipMapper = friendshipMapper;
    }

    @Override
    public void save(Friendship friendship) {
        friendshipMapper.save(friendship);
    }

    @Override
    public Optional<Friendship> findById(Long id) {
        return friendshipMapper.findById(id);
    }

    @Override
    public Optional<Friendship> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId) {
        return friendshipMapper.findByRequesterIdAndReceiverId(requesterId, receiverId);
    }

    @Override
    public List<Friendship> findByUserId(Long userId) {
        return friendshipMapper.findByUserId(userId);
    }

    @Override
    public List<Friendship> findByReceiverIdAndStatus(Long receiverId, Friendship.FriendshipStatus status) {
        return friendshipMapper.findByReceiverIdAndStatus(receiverId, status);
    }

    @Override
    public void update(Friendship friendship) {
        friendshipMapper.update(friendship);
    }

    @Override
    public void delete(Long id) {
        friendshipMapper.delete(id);
    }
}
