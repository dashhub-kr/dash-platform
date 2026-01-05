package com.ssafy.dash.social.presentation;

import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import com.ssafy.dash.social.application.SocialService;
import com.ssafy.dash.social.application.dto.result.FriendResult;
import com.ssafy.dash.social.application.dto.result.MessageResult;
import com.ssafy.dash.user.application.dto.result.UserResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialController {

    private final SocialService socialService;

    @GetMapping("/friends")
    public ResponseEntity<List<FriendResult>> getFriends(@AuthenticationPrincipal CustomOAuth2User userPrincipal) {
        return ResponseEntity.ok(socialService.getFriends(userPrincipal.getUserId()));
    }

    @GetMapping("/friends/requests")
    public ResponseEntity<List<FriendResult>> getFriendRequests(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal) {
        return ResponseEntity.ok(socialService.getReceivedRequests(userPrincipal.getUserId()));
    }

    @PostMapping("/friends/request")
    public ResponseEntity<Void> sendFriendRequest(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal,
            @RequestBody Map<String, Long> payload) {
        Long receiverId = payload.get("receiverId");
        socialService.sendFriendRequest(userPrincipal.getUserId(), receiverId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/friends/accept/{requestId}")
    public ResponseEntity<Void> acceptFriendRequest(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal,
            @PathVariable Long requestId) {
        socialService.acceptFriendRequest(userPrincipal.getUserId(), requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/friends/reject/{requestId}")
    public ResponseEntity<Void> rejectFriendRequest(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal,
            @PathVariable Long requestId) {
        socialService.rejectFriendRequest(userPrincipal.getUserId(), requestId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/friends/{friendId}")
    public ResponseEntity<Void> deleteFriend(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal,
            @PathVariable Long friendId) {
        socialService.deleteFriend(userPrincipal.getUserId(), friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserResult>> searchUsers(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal,
            @RequestParam String query) {
        return ResponseEntity.ok(socialService.searchUsers(query, userPrincipal.getUserId()));
    }

    // --- 메시징 (Messaging) ---

    @GetMapping("/messages/{partnerId}")
    public ResponseEntity<List<MessageResult>> getConversation(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal,
            @PathVariable Long partnerId) {
        // 읽음 처리도 함께 수행
        socialService.markMessagesAsRead(userPrincipal.getUserId(), partnerId);
        return ResponseEntity.ok(socialService.getConversation(userPrincipal.getUserId(), partnerId));
    }

    @PostMapping("/messages")
    public ResponseEntity<Void> sendMessage(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal,
            @RequestBody Map<String, Object> payload) {
        Long receiverId = ((Number) payload.get("receiverId")).longValue();
        String content = (String) payload.get("content");
        socialService.sendMessage(userPrincipal.getUserId(), receiverId, content);
        return ResponseEntity.ok().build();
    }
}
