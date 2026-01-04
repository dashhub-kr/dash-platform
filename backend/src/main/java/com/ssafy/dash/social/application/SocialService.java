package com.ssafy.dash.social.application;

import com.ssafy.dash.notification.application.NotificationService;
import com.ssafy.dash.notification.domain.NotificationType;
import com.ssafy.dash.social.domain.DirectMessage;
import com.ssafy.dash.social.domain.DirectMessageRepository;
import com.ssafy.dash.social.domain.Friendship;
import com.ssafy.dash.social.domain.FriendshipRepository;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SocialService {

    private final FriendshipRepository friendshipRepository;
    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // --- 친구 관계 (Friendship) ---

    public void sendFriendRequest(Long requesterId, Long receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        // 이미 존재하는 관계인지 확인
        friendshipRepository.findByRequesterIdAndReceiverId(requesterId, receiverId).ifPresent(f -> {
            throw new IllegalStateException("Friendship or request already exists");
        });

        Friendship friendship = Friendship.create(requesterId, receiverId);
        friendshipRepository.save(friendship);

        // 수신자에게 알림 전송
        User requester = userRepository.findById(requesterId).orElseThrow();
        notificationService.send(
                receiverId,
                requester.getUsername() + "님이 친구 신청을 보냈습니다.",
                "/social/friends?tab=requests",
                NotificationType.FRIEND_REQUEST,
                friendship.getId()
        );
    }

    public void acceptFriendRequest(Long userId, Long requestId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (!friendship.getReceiverId().equals(userId)) {
            throw new SecurityException("Not authorized to accept this request");
        }

        friendship.accept();
        friendshipRepository.update(friendship);

        // 요청자에게 알림 전송
        User receiver = userRepository.findById(userId).orElseThrow();
        notificationService.send(
                friendship.getRequesterId(),
                receiver.getUsername() + "님이 친구 신청을 수락했습니다.",
                "/social/friends",
                NotificationType.FRIEND_REQUEST,
                friendship.getId()
        );
    }

    public void rejectFriendRequest(Long userId, Long requestId) {
         Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
         
         if (!friendship.getReceiverId().equals(userId)) {
             throw new SecurityException("Not authorized to reject this request");
         }
         
         friendshipRepository.delete(requestId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        // userId가 포함된(요청자 혹은 수신자) 관계를 찾아 삭제
        Friendship friendship = friendshipRepository.findByRequesterIdAndReceiverId(userId, friendId)
                .orElseThrow(() -> new IllegalArgumentException("Friendship not found"));
        
        friendshipRepository.delete(friendship.getId());
    }

    @Transactional(readOnly = true)
    public List<com.ssafy.dash.social.application.dto.result.FriendResult> getFriends(Long userId) {
        return friendshipRepository.findByUserId(userId).stream()
                .filter(f -> f.getStatus() == Friendship.FriendshipStatus.ACCEPTED)
                .map(f -> {
                    Long friendId = f.getRequesterId().equals(userId) ? f.getReceiverId() : f.getRequesterId();
                    User friend = userRepository.findById(friendId).orElseThrow();
                    return com.ssafy.dash.social.application.dto.result.FriendResult.from(f, friend);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<com.ssafy.dash.social.application.dto.result.FriendResult> getReceivedRequests(Long userId) {
        return friendshipRepository.findByReceiverIdAndStatus(userId, Friendship.FriendshipStatus.PENDING).stream()
                .map(f -> {
                    User requester = userRepository.findById(f.getRequesterId()).orElseThrow();
                    return com.ssafy.dash.social.application.dto.result.FriendResult.from(f, requester);
                })
                .collect(Collectors.toList());
    }
    
    // --- 메시징 (Messaging) ---
    
    public void sendMessage(Long senderId, Long receiverId, String content) {
        if (senderId.equals(receiverId)) {
             throw new IllegalArgumentException("Cannot message yourself");
        }
        
        DirectMessage dm = DirectMessage.create(senderId, receiverId, content);
        directMessageRepository.save(dm);
        
        // 수신자에게 알림 전송
        User sender = userRepository.findById(senderId).orElseThrow();
        notificationService.send(
                receiverId,
                sender.getUsername() + "님이 쪽지를 보냈습니다.",
                "/social/messages?partnerId=" + senderId, 
                NotificationType.DIRECT_MESSAGE,
                senderId
        );
    }

    @Transactional(readOnly = true)
    public List<com.ssafy.dash.social.application.dto.result.MessageResult> getConversation(Long userId, Long partnerId) {
        User partner = userRepository.findById(partnerId).orElseThrow();
        User me = userRepository.findById(userId).orElseThrow();
        
        return directMessageRepository.findConversation(userId, partnerId).stream()
                .map(dm -> {
                    User sender = dm.getSenderId().equals(userId) ? me : partner;
                    return com.ssafy.dash.social.application.dto.result.MessageResult.from(dm, sender, userId);
                })
                .collect(Collectors.toList());
    }
    
    public void markMessagesAsRead(Long userId, Long partnerId) {
        List<DirectMessage> messages = directMessageRepository.findConversation(userId, partnerId);
        messages.stream()
            .filter(m -> m.getReceiverId().equals(userId) && !m.isRead())
            .forEach(m -> {
                m.markAsRead();
                directMessageRepository.update(m);
            });
    }

    @Transactional(readOnly = true)
    public List<com.ssafy.dash.user.application.dto.result.UserResult> searchUsers(String keyword, Long currentUserId) {
        // 간단한 최적화: 필터링에 엄밀히 필요하진 않지만 사용자 존재 여부 확인 가능
        return userRepository.findAll().stream() // DB 쿼리로 최적화 필요
                .filter(u -> !u.getId().equals(currentUserId))
                .filter(u -> u.getUsername().contains(keyword) || u.getEmail().contains(keyword))
                .map(com.ssafy.dash.user.application.dto.result.UserResult::from)
                .collect(Collectors.toList());
    }

}
