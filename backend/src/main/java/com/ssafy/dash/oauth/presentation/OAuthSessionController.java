package com.ssafy.dash.oauth.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.oauth.presentation.dto.response.OAuthSessionResponse;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import com.ssafy.dash.user.application.UserService;
import com.ssafy.dash.user.presentation.dto.response.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/oauth")
public class OAuthSessionController {

    private final UserService userService;

    public OAuthSessionController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/session")
    @Operation(summary = "현재 OAuth 세션 정보 조회", description = "로그인한 사용자의 정보와 로그인 흐름 타입을 반환합니다.")
    public ResponseEntity<OAuthSessionResponse> session(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {
        if (principal instanceof CustomOAuth2User customUser) {
            UserResponse userResponse = UserResponse.from(userService.findById(customUser.getUserId()));
            OAuthSessionResponse response = new OAuthSessionResponse(userResponse, customUser.getFlowType());
            
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
}
