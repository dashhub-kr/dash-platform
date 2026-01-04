package com.ssafy.dash.notification.presentation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.notification.application.NotificationService;
import com.ssafy.dash.notification.presentation.dto.response.NotificationResponse;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관리 API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 모든 알림을 조회합니다.")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomOAuth2User principal) {
        return ResponseEntity.ok(
                notificationService.getNotifications(principal.getUserId()).stream()
                        .map(NotificationResponse::from)
                        .collect(Collectors.toList()));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    @Operation(summary = "모든 알림 읽음 처리", description = "사용자의 모든 알림을 읽음 상태로 변경합니다.")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal CustomOAuth2User principal) {
        notificationService.markAllAsRead(principal.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/update")
    @Operation(summary = "알림 업데이트", description = "알림의 내용이나 타입을 업데이트합니다.")
    public ResponseEntity<Void> updateNotification(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> payload) {
        String content = payload.get("content");
        String typeStr = payload.get("type");
        com.ssafy.dash.notification.domain.NotificationType type = typeStr != null
                ? com.ssafy.dash.notification.domain.NotificationType.valueOf(typeStr)
                : null;
        notificationService.update(id, content, type);
        return ResponseEntity.ok().build();
    }
}
