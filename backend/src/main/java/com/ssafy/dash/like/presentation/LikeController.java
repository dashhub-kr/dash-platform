package com.ssafy.dash.like.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.like.application.LikeService;
import com.ssafy.dash.like.presentation.dto.response.LikeResponse;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    // ===== Comment Like =====

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<LikeResponse> likeComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {

        Long userId = extractUserId(principal);
        int likeCount = likeService.likeComment(commentId, userId);

        return ResponseEntity.ok(new LikeResponse(likeCount));
    }

    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<LikeResponse> unlikeComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {

        Long userId = extractUserId(principal);
        int likeCount = likeService.unlikeComment(commentId, userId);

        return ResponseEntity.ok(new LikeResponse(likeCount));
    }

    private Long extractUserId(OAuth2User principal) {
        if (principal instanceof CustomOAuth2User customUser) {
            return customUser.getUserId();
        }
        return 1L;
    }

}
