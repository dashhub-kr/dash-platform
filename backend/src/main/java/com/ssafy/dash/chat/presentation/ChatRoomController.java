package com.ssafy.dash.chat.presentation;

import com.ssafy.dash.chat.application.ChatRoomService;
import com.ssafy.dash.chat.application.dto.ChatRoomDetailResult;
import com.ssafy.dash.chat.application.dto.ChatRoomListResult;
import com.ssafy.dash.chat.application.dto.ChatRoomMessageResult;
import com.ssafy.dash.chat.domain.ChatRoom;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * 그룹 채팅방 생성
     */
    @PostMapping
    public ResponseEntity<ChatRoom> createRoom(
            @RequestBody CreateRoomRequest request,
            @AuthenticationPrincipal CustomOAuth2User userPrincipal) {

        ChatRoom room = chatRoomService.createGroupRoom(
                userPrincipal.getUserId(),
                request.name(),
                request.memberIds());

        return ResponseEntity.ok(room);
    }

    /**
     * 내 채팅방 목록
     */
    @GetMapping
    public ResponseEntity<List<ChatRoomListResult>> getChatRooms(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal) {
        return ResponseEntity.ok(chatRoomService.getChatRooms(userPrincipal.getUserId()));
    }

    /**
     * 채팅방 상세
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomDetailResult> getChatRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomOAuth2User userPrincipal) {
        return ResponseEntity.ok(chatRoomService.getChatRoom(roomId, userPrincipal.getUserId()));
    }

    /**
     * 메시지 조회
     */
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatRoomMessageResult>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal CustomOAuth2User userPrincipal) {
        return ResponseEntity.ok(chatRoomService.getMessages(roomId, userPrincipal.getUserId(), page, size));
    }

    /**
     * 메시지 발송
     */
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ChatRoomMessageResult> sendMessage(
            @PathVariable Long roomId,
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal CustomOAuth2User userPrincipal) {
        return ResponseEntity.ok(chatRoomService.sendMessage(roomId, userPrincipal.getUserId(), request.content()));
    }

    /**
     * 멤버 초대
     */
    @PostMapping("/{roomId}/members")
    public ResponseEntity<Void> addMember(
            @PathVariable Long roomId,
            @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal CustomOAuth2User userPrincipal) {

        chatRoomService.addMember(roomId, request.userId(), userPrincipal.getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * 멤버 퇴장 (자신이 나가기)
     */
    @DeleteMapping("/{roomId}/members/me")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomOAuth2User userPrincipal) {

        chatRoomService.removeMember(roomId, userPrincipal.getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * 읽음 처리
     */
    @PostMapping("/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomOAuth2User userPrincipal) {

        chatRoomService.markAsRead(roomId, userPrincipal.getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * 채팅방 삭제 (방장만)
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomOAuth2User userPrincipal) {

        chatRoomService.deleteRoom(roomId, userPrincipal.getUserId());
        return ResponseEntity.ok().build();
    }

    // Request DTOs
    record CreateRoomRequest(String name, List<Long> memberIds) {
    }

    record SendMessageRequest(String content) {
    }

    record AddMemberRequest(Long userId) {
    }
}
