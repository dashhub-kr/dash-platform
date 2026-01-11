package com.ssafy.dash.chat.infrastructure;

import com.ssafy.dash.chat.domain.ChatRoom;
import com.ssafy.dash.chat.domain.ChatRoomMember;
import com.ssafy.dash.chat.domain.ChatRoomMessage;
import com.ssafy.dash.chat.domain.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepository {

    private final ChatRoomMapper chatRoomMapper;

    @Override
    public void saveRoom(ChatRoom room) {
        chatRoomMapper.insertRoom(room);
    }

    @Override
    public Optional<ChatRoom> findRoomById(Long roomId) {
        return chatRoomMapper.selectRoomById(roomId);
    }

    @Override
    public Optional<ChatRoom> findRoomByStudyId(Long studyId) {
        return chatRoomMapper.selectRoomByStudyId(studyId);
    }

    @Override
    public List<ChatRoom> findRoomsByUserId(Long userId) {
        return chatRoomMapper.selectRoomsByUserId(userId);
    }

    @Override
    public void updateRoom(ChatRoom room) {
        chatRoomMapper.updateRoom(room);
    }

    @Override
    public void deleteRoom(Long roomId) {
        chatRoomMapper.deleteRoom(roomId);
    }

    @Override
    public void saveMember(ChatRoomMember member) {
        chatRoomMapper.insertMember(member);
    }

    @Override
    public Optional<ChatRoomMember> findMember(Long roomId, Long userId) {
        return chatRoomMapper.selectMember(roomId, userId);
    }

    @Override
    public List<ChatRoomMember> findMembersByRoomId(Long roomId) {
        return chatRoomMapper.selectMembersByRoomId(roomId);
    }

    @Override
    public void updateMemberLastReadAt(Long roomId, Long userId) {
        chatRoomMapper.updateMemberLastReadAt(roomId, userId);
    }

    @Override
    public void deleteMember(Long roomId, Long userId) {
        chatRoomMapper.deleteMember(roomId, userId);
    }

    @Override
    public void deleteMembersByRoomId(Long roomId) {
        chatRoomMapper.deleteMembersByRoomId(roomId);
    }

    @Override
    public void saveMessage(ChatRoomMessage message) {
        chatRoomMapper.insertMessage(message);
    }

    @Override
    public List<ChatRoomMessage> findMessagesByRoomId(Long roomId, int limit, int offset) {
        return chatRoomMapper.selectMessagesByRoomId(roomId, limit, offset);
    }

    @Override
    public int countUnreadMessages(Long roomId, Long userId) {
        return chatRoomMapper.countUnreadMessages(roomId, userId);
    }

    @Override
    public Optional<ChatRoomMessage> findLastMessage(Long roomId) {
        return chatRoomMapper.selectLastMessage(roomId);
    }
}
