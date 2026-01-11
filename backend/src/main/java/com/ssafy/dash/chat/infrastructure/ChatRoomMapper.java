package com.ssafy.dash.chat.infrastructure;

import com.ssafy.dash.chat.domain.ChatRoom;
import com.ssafy.dash.chat.domain.ChatRoomMember;
import com.ssafy.dash.chat.domain.ChatRoomMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ChatRoomMapper {

    // ChatRoom
    void insertRoom(ChatRoom room);

    Optional<ChatRoom> selectRoomById(@Param("roomId") Long roomId);

    Optional<ChatRoom> selectRoomByStudyId(@Param("studyId") Long studyId);

    List<ChatRoom> selectRoomsByUserId(@Param("userId") Long userId);

    void updateRoom(ChatRoom room);

    void deleteRoom(@Param("roomId") Long roomId);

    // ChatRoomMember
    void insertMember(ChatRoomMember member);

    Optional<ChatRoomMember> selectMember(@Param("roomId") Long roomId, @Param("userId") Long userId);

    List<ChatRoomMember> selectMembersByRoomId(@Param("roomId") Long roomId);

    void updateMemberLastReadAt(@Param("roomId") Long roomId, @Param("userId") Long userId);

    void deleteMember(@Param("roomId") Long roomId, @Param("userId") Long userId);

    void deleteMembersByRoomId(@Param("roomId") Long roomId);

    // ChatRoomMessage
    void insertMessage(ChatRoomMessage message);

    List<ChatRoomMessage> selectMessagesByRoomId(@Param("roomId") Long roomId, @Param("limit") int limit,
            @Param("offset") int offset);

    int countUnreadMessages(@Param("roomId") Long roomId, @Param("userId") Long userId);

    Optional<ChatRoomMessage> selectLastMessage(@Param("roomId") Long roomId);
}
