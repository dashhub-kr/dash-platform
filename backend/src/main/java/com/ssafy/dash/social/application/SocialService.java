package com.ssafy.dash.social.application;

import com.ssafy.dash.notification.application.NotificationService;
import com.ssafy.dash.notification.domain.NotificationType;
import com.ssafy.dash.social.domain.DirectMessage;
import com.ssafy.dash.social.domain.DirectMessageRepository;
import com.ssafy.dash.social.domain.Friendship;
import com.ssafy.dash.social.domain.FriendshipRepository;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.common.encrypt.AesEncryptor;
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
    private final com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository algorithmRecordRepository;
    private final NotificationService notificationService;
    private final AesEncryptor aesEncryptor;

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
                friendship.getId());
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
                NotificationType.FRIEND_ACCEPTED,
                friendship.getId());
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
                    User friend = userRepository.findByIdIncludingDeleted(friendId).orElseThrow();
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

        // 수신자가 탈퇴한 회원인지 확인
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (receiver.isDeleted()) {
            throw new IllegalStateException("탈퇴한 회원에게는 메시지를 보낼 수 없습니다.");
        }

        String encryptedContent = aesEncryptor.encrypt(content);
        DirectMessage dm = DirectMessage.create(senderId, receiverId, encryptedContent);
        directMessageRepository.save(dm);

        // 수신자에게 알림 전송
        User sender = userRepository.findById(senderId).orElseThrow();
        notificationService.send(
                receiverId,
                sender.getUsername() + "님이 쪽지를 보냈습니다.",
                "/social/messages?partnerId=" + senderId,
                NotificationType.DIRECT_MESSAGE,
                senderId);
    }

    @Transactional(readOnly = true)
    public List<com.ssafy.dash.social.application.dto.result.MessageResult> getConversation(Long userId,
            Long partnerId) {
        User partner = userRepository.findByIdIncludingDeleted(partnerId).orElseThrow();
        User me = userRepository.findById(userId).orElseThrow();

        return directMessageRepository.findConversation(userId, partnerId).stream()
                .map(dm -> {
                    User sender = dm.getSenderId().equals(userId) ? me : partner;
                    String decryptedContent = aesEncryptor.decrypt(dm.getContent());

                    return new com.ssafy.dash.social.application.dto.result.MessageResult(
                            dm.getId(),
                            sender.getId(),
                            sender.getUsername(),
                            sender.getAvatarUrl(),
                            sender.getEquippedDecorationClass(),
                            decryptedContent,
                            dm.isRead(),
                            dm.getCreatedAt(),
                            sender.getId().equals(userId));
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
        List<User> users = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .filter(u -> u.getUsername().contains(keyword) || u.getEmail().contains(keyword))
                .collect(Collectors.toList());

        return users.stream().map(u -> {
            String status = "NONE";
            var friendshipOpt = friendshipRepository.findByRequesterIdAndReceiverId(currentUserId, u.getId());
            if (friendshipOpt.isPresent()) {
                status = friendshipOpt.get().getStatus().name();
            }
            return com.ssafy.dash.user.application.dto.result.UserResult.from(u, status);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<com.ssafy.dash.social.application.dto.result.ConversationResult> getConversations(Long userId) {
        List<Long> partnerIds = directMessageRepository.findRecentChatPartners(userId);

        return partnerIds.stream().map(partnerId -> {
            User partner = userRepository.findById(partnerId).orElse(null);
            if (partner == null)
                return null;

            List<DirectMessage> messages = directMessageRepository.findConversation(userId, partnerId);
            DirectMessage lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);

            String lastMessagePreview = lastMessage != null ? aesEncryptor.decrypt(lastMessage.getContent()) : null;
            int unreadCount = (int) messages.stream()
                    .filter(m -> m.getReceiverId().equals(userId) && !m.isRead())
                    .count();

            return com.ssafy.dash.social.application.dto.result.ConversationResult.of(
                    partner.getId(),
                    partner.getUsername(),
                    partner.getAvatarUrl(),
                    partner.getEquippedDecorationClass(),
                    lastMessagePreview,
                    lastMessage != null ? lastMessage.getCreatedAt() : null,
                    unreadCount);
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }

    // --- 친구 피드 (Feed) ---
    @Transactional(readOnly = true)
    public List<com.ssafy.dash.social.application.dto.result.FeedResult> getFriendFeed(
            Long userId, int page, int size) {

        // 친구 목록 조회
        List<Friendship> friendships = friendshipRepository.findByUserId(userId);
        List<Long> friendIds = friendships.stream()
                .filter(f -> f.getStatus() == Friendship.FriendshipStatus.ACCEPTED)
                .map(f -> f.getRequesterId().equals(userId) ? f.getReceiverId() : f.getRequesterId())
                .distinct()
                .collect(Collectors.toList());

        if (friendIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        int offset = page * size;
        List<com.ssafy.dash.algorithm.domain.AlgorithmRecord> records = algorithmRecordRepository.findFeed(friendIds,
                offset, size);

        return records.stream()
                .map(record -> {
                    // Mapper에서 username, avatarUrl 등을 조회해왔으므로 안전하게 사용 가능
                    // 다만 AlgorithmRecord 엔티티에는 해당 필드가 없으므로, MyBatis ResultMap에서
                    // AlgorithmRecord 객체가 아닌 DTO로 바로 매핑하거나,
                    // AlgorithmRecord를 확장한 DTO를 쓰거나 해야 함.
                    // 현재 Mapper는 AlgorithmRecord를 반환하고 있는데,
                    // AlgorithmRecordMapper.xml의 selectFriendFeed는 r.*, u.username 등을 조회함.
                    // 하지만 AlgorithmRecord 엔티티에는 username 필드가 없음.
                    // 따라서 AlgorithmRecord에 @Transient로 필드를 추가하거나, 별도 DTO를 써야 함.

                    // 기존 로직에서는 User를 다시 조회해서 FeedResult를 만들었음.
                    // 여기서도 User 조회 최적화가 필요하지만, 일단 repository가 AlgorithmRecord를 반환하므로
                    // N+1 문제가 '부분적으로' 해결됨 (AlgorithmRecord 조회는 1방).
                    // 하지만 FeedResult로 변환 과정에서 User 정보가 필요함.

                    // 개선: `AlgorithmRecordRepository.findFeed`가 `FeedResult`를 반환하도록 하거나,
                    // `AlgorithmRecord`를 조회한 뒤, User 들을 배치 조회하여 매핑해야 함.

                    // 하지만 기왕 최적화 하는거, Mapper에서 Join한 데이터를 활용해야 함.
                    // 현재 AlgorithmRecordMapper.xml 에서는 u.username 등을 조회하고 있음.
                    // 하지만 자바 코드에서는 AlgorithmRecord를 반환 타입으로 쓰고 있어서 이 추가 정보들이 유실될 수 있음 (MyBatis가 매핑할
                    // 곳이 없어서).

                    // **중요**: 현재 단계에서 가장 빠르고 안전한 수정은,
                    // 1. findFeed로 Record ID 목록 페이징 처리해서 가져옴. (혹은 Record 자체)
                    // 2. 가져온 Record들의 UserID를 수집.
                    // 3. User들을 IN 쿼리로 한방에 조회.
                    // 4. 조립.

                    // 하지만 이미 XML에서 JOIN을 걸었으니,
                    // AlgorithmRecord 엔티티에 username, avatarUrl, tier 등을 담을 수 있는 Transient 필드가 있다면
                    // 좋겠지만
                    // Entity를 수정하는건 사이드이펙트가 있을 수 있음.

                    // 따라서, 일단은 User 조회를 별도로 하더라도, Record 조회 자체를 페이징 처리하는 것이 핵심임.
                    // 이전 코드: 유저별 전체 레코드 조회 -> 메모리 머지 -> 페이징
                    // 개선 코드: 친구들 전체 레코드 중 최신순 페이징 조회 -> 해당 레코드들의 유저 정보 조회 (최대 20명)

                    User user = userRepository.findById(record.getUserId()).orElseThrow();

                    return com.ssafy.dash.social.application.dto.result.FeedResult.solved(
                            record.getId(),
                            user.getId(),
                            user.getUsername(),
                            user.getAvatarUrl(),
                            user.getEquippedDecorationClass(),
                            user.getSolvedacTier(),
                            Long.parseLong(record.getProblemNumber()),
                            record.getTitle(),
                            record.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

}
