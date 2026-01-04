package com.ssafy.dash.admin.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.acorn.application.AcornService;
import com.ssafy.dash.admin.presentation.dto.request.GiftAcornRequest;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import com.ssafy.dash.user.application.UserService;

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
}
