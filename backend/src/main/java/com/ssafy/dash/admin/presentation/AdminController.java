package com.ssafy.dash.admin.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.acorn.application.AcornService;
import com.ssafy.dash.admin.presentation.dto.request.GiftAcornRequest;
import com.ssafy.dash.admin.presentation.dto.request.GiftLogRequest;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import com.ssafy.dash.user.application.UserService;
import com.ssafy.dash.notification.application.NotificationService;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;
import com.ssafy.dash.notification.domain.NotificationType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin", description = "관리자 전용 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AcornService acornService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Operation(summary = "도토리 선물", description = "특정 스터디에 도토리를 지급합니다. (관리자 전용)")
    @PostMapping("/acorns/gift")
    public ResponseEntity<Void> giftAcorns(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @RequestBody GiftAcornRequest request) {

        if (principal instanceof CustomOAuth2User customUser) {
            var user = userService.findById(customUser.getUserId());

            if (!"ROLE_ADMIN".equals(user.role())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            acornService.accumulate(request.studyId(), user.id(), request.amount(), request.reason());
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "로그 선물", description = "특정 유저에게 로그를 지급합니다. (관리자 전용)")
    @PostMapping("/logs/gift")
    @Transactional
    public ResponseEntity<Void> giftLogs(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @RequestBody GiftLogRequest request) {

        if (principal instanceof CustomOAuth2User customUser) {
            var admin = userService.findById(customUser.getUserId());

            if (!"ROLE_ADMIN".equals(admin.role())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 유저 조회
            var targetUser = userRepository.findById(request.userId())
                    .orElseThrow(() -> new UserNotFoundException(request.userId()));

            // 1. 로그 지급
            targetUser.addLogs(request.amount());
            userRepository.update(targetUser);

            // 2. 알림 전송
            notificationService.send(
                    targetUser.getId(),
                    String.format("관리자로부터 %d 로그를 선물받았습니다! (사유: %s)", request.amount(), request.reason()),
                    "/shop",
                    NotificationType.SYSTEM);

            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "회원 차단", description = "특정 회원을 차단합니다. (관리자 전용)")
    @PostMapping("/users/{userId}/block")
    public ResponseEntity<Void> blockUser(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long userId) {

        if (principal instanceof CustomOAuth2User customUser) {
            var user = userService.findById(customUser.getUserId());
            if (!"ROLE_ADMIN".equals(user.role())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            userService.blockUser(userId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
