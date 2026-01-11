package com.ssafy.dash.chat.domain;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository {

    // ChatRoom
    void saveRoom(ChatRoom room);

    Optional<ChatRoom> findRoomById(Long roomId);

    Optional<ChatRoom> findRoomByStudyId(Long studyId);

    List<ChatRoom> findRoomsByUserId(Long userId);

    void updateRoom(ChatRoom room);

    void deleteRoom(Long roomId);

    // ChatRoomMember
    void saveMember(ChatRoomMember member);

    Optional<ChatRoomMember> findMember(Long roomId, Long userId);

    List<ChatRoomMember> findMembersByRoomId(Long roomId);

    void updateMemberLastReadAt(Long roomId, Long userId);

    void deleteMember(Long roomId, Long userId);

    void deleteMembersByRoomId(Long roomId);

    // ChatRoomMessage
    void saveMessage(ChatRoomMessage message);

    List<ChatRoomMessage> findMessagesByRoomId(Long roomId, int limit, int offset);

    int countUnreadMessages(Long roomId, Long userId);

    Optional<ChatRoomMessage> findLastMessage(Long roomId);
}
