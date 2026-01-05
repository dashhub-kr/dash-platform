package com.ssafy.dash.social.infrastructure.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.dash.social.domain.Friendship;

@Mapper
public interface FriendshipMapper {
    void save(Friendship friendship);
    Optional<Friendship> findById(Long id);
    Optional<Friendship> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);
    List<Friendship> findByUserId(Long userId); // 보낸 요청과 받은 요청 모두 반환
    List<Friendship> findByReceiverIdAndStatus(Long receiverId, Friendship.FriendshipStatus status); // 대기 중인 요청
    void update(Friendship friendship);
    void delete(Long id);
}
