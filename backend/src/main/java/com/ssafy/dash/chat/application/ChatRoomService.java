package com.ssafy.dash.chat.application;

import com.ssafy.dash.chat.application.dto.ChatRoomDetailResult;
import com.ssafy.dash.chat.application.dto.ChatRoomListResult;
import com.ssafy.dash.chat.application.dto.ChatRoomMessageResult;
import com.ssafy.dash.chat.domain.*;
import com.ssafy.dash.notification.application.NotificationService;
import com.ssafy.dash.notification.domain.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final NotificationService notificationService;

    /**
     * 그룹 채팅방 생성
     */
    public ChatRoom createGroupRoom(Long creatorId, String name, List<Long> memberIds) {
        ChatRoom room = ChatRoom.createGroupRoom(name, creatorId);
        chatRoomRepository.saveRoom(room);

        // 생성자를 멤버로 추가
        chatRoomRepository.saveMember(ChatRoomMember.create(room.getId(), creatorId));

        // 추가 멤버들 초대
        for (Long memberId : memberIds) {
            if (!memberId.equals(creatorId)) {
                chatRoomRepository.saveMember(ChatRoomMember.create(room.getId(), memberId));
                // 알림 발송
                notificationService.send(memberId, "새 그룹 채팅방에 초대되었습니다: " + name,
                        "/chat/rooms/" + room.getId(), NotificationType.CHAT_ROOM_INVITE, room.getId());
            }
        }

        return room;
    }

    /**
     * 스터디 채팅방 생성 (스터디 생성 시 자동 호출)
     */
    public ChatRoom createStudyRoom(Long studyId, String studyName, Long creatorId) {
        // 이미 스터디 채팅방이 있는지 확인
        if (chatRoomRepository.findRoomByStudyId(studyId).isPresent()) {
            return chatRoomRepository.findRoomByStudyId(studyId).get();
        }

        ChatRoom room = ChatRoom.createStudyRoom(studyName + " 채팅방", studyId, creatorId);
        chatRoomRepository.saveRoom(room);

        // 생성자를 멤버로 추가
        chatRoomRepository.saveMember(ChatRoomMember.create(room.getId(), creatorId));

        return room;
    }

    /**
     * 스터디 채팅방에 멤버 추가 (스터디 가입 승인 시 호출)
     */
    public void addStudyMember(Long studyId, Long userId) {
        chatRoomRepository.findRoomByStudyId(studyId).ifPresent(room -> {
            if (chatRoomRepository.findMember(room.getId(), userId).isEmpty()) {
                chatRoomRepository.saveMember(ChatRoomMember.create(room.getId(), userId));
            }
        });
    }

    /**
     * 스터디 채팅방에서 멤버 제거 (스터디 탈퇴 시 호출)
     */
    public void removeStudyMember(Long studyId, Long userId) {
        chatRoomRepository.findRoomByStudyId(studyId).ifPresent(room -> {
            chatRoomRepository.deleteMember(room.getId(), userId);
        });
    }

    /**
     * 내 채팅방 목록
     */
    @Transactional(readOnly = true)
    public List<ChatRoomListResult> getChatRooms(Long userId) {
        return chatRoomRepository.findRoomsByUserId(userId).stream()
                .map(room -> new ChatRoomListResult(
                        room.getId(),
                        room.getName(),
                        room.getType().name(),
                        room.getStudyId(),
                        room.getMemberCount(),
                        room.getLastMessage(),
                        room.getLastMessageAt(),
                        room.getUnreadCount()))
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 상세 조회
     */
    @Transactional(readOnly = true)
    public ChatRoomDetailResult getChatRoom(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // 멤버인지 확인
        if (chatRoomRepository.findMember(roomId, userId).isEmpty()) {
            throw new IllegalArgumentException("채팅방 멤버가 아닙니다.");
        }

        List<ChatRoomMember> members = chatRoomRepository.findMembersByRoomId(roomId);

        return new ChatRoomDetailResult(
                room.getId(),
                room.getName(),
                room.getType().name(),
                room.getStudyId(),
                room.getCreatorId(),
                room.getCreatedAt(),
                members.stream()
                        .map(m -> new ChatRoomDetailResult.MemberInfo(
                                m.getUserId(), m.getUsername(), m.getAvatarUrl()))
                        .collect(Collectors.toList()));
    }

    /**
     * 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<ChatRoomMessageResult> getMessages(Long roomId, Long userId, int page, int size) {
        // 멤버인지 확인
        if (chatRoomRepository.findMember(roomId, userId).isEmpty()) {
            throw new IllegalArgumentException("채팅방 멤버가 아닙니다.");
        }

        return chatRoomRepository.findMessagesByRoomId(roomId, size, page * size).stream()
                .map(m -> new ChatRoomMessageResult(
                        m.getId(),
                        m.getSenderId(),
                        m.getSenderUsername(),
                        m.getSenderAvatarUrl(),
                        m.getContent(),
                        m.getCreatedAt(),
                        m.getSenderId().equals(userId)))
                .collect(Collectors.toList());
    }

    /**
     * 메시지 발송
     */
    public ChatRoomMessageResult sendMessage(Long roomId, Long senderId, String content) {
        // 멤버인지 확인
        ChatRoomMember member = chatRoomRepository.findMember(roomId, senderId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 멤버가 아닙니다."));

        ChatRoomMessage message = ChatRoomMessage.create(roomId, senderId, content);
        chatRoomRepository.saveMessage(message);

        // 읽음 처리
        chatRoomRepository.updateMemberLastReadAt(roomId, senderId);

        return new ChatRoomMessageResult(
                message.getId(),
                message.getSenderId(),
                member.getUsername(),
                member.getAvatarUrl(),
                message.getContent(),
                message.getCreatedAt(),
                true);
    }

    /**
     * 멤버 초대 (GROUP 타입만)
     */
    public void addMember(Long roomId, Long userId, Long inviterId) {
        ChatRoom room = chatRoomRepository.findRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        if (room.getType() == ChatRoom.ChatRoomType.STUDY) {
            throw new IllegalArgumentException("스터디 채팅방은 직접 멤버를 추가할 수 없습니다.");
        }

        // 초대자가 멤버인지 확인
        if (chatRoomRepository.findMember(roomId, inviterId).isEmpty()) {
            throw new IllegalArgumentException("채팅방 멤버만 초대할 수 있습니다.");
        }

        // 이미 멤버인지 확인
        if (chatRoomRepository.findMember(roomId, userId).isPresent()) {
            throw new IllegalArgumentException("이미 채팅방 멤버입니다.");
        }

        chatRoomRepository.saveMember(ChatRoomMember.create(roomId, userId));
        notificationService.send(userId, "채팅방에 초대되었습니다: " + room.getName(),
                "/chat/rooms/" + roomId, NotificationType.CHAT_ROOM_INVITE, roomId);
    }

    /**
     * 멤버 퇴장
     */
    public void removeMember(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // 스터디 채팅방은 스터디 탈퇴를 통해서만 제거
        if (room.getType() == ChatRoom.ChatRoomType.STUDY) {
            throw new IllegalArgumentException("스터디 채팅방은 스터디 탈퇴를 통해서만 나갈 수 있습니다.");
        }

        chatRoomRepository.deleteMember(roomId, userId);
    }

    /**
     * 읽음 처리
     */
    public void markAsRead(Long roomId, Long userId) {
        if (chatRoomRepository.findMember(roomId, userId).isEmpty()) {
            throw new IllegalArgumentException("채팅방 멤버가 아닙니다.");
        }
        chatRoomRepository.updateMemberLastReadAt(roomId, userId);
    }

    /**
     * 채팅방 삭제 (방장만)
     */
    public void deleteRoom(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        if (!room.getCreatorId().equals(userId)) {
            throw new IllegalArgumentException("채팅방 생성자만 삭제할 수 있습니다.");
        }

        if (room.getType() == ChatRoom.ChatRoomType.STUDY) {
            throw new IllegalArgumentException("스터디 채팅방은 스터디 삭제 시 자동으로 삭제됩니다.");
        }

        chatRoomRepository.deleteRoom(roomId);
    }
}
