package com.ssafy.dash.oauth.presentation.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

/**
 * 컨트롤러에서 로그인 사용자 식별과 본인 확인을 처리하는 헬퍼
 */
public final class AuthenticatedUsers {

    private AuthenticatedUsers() {
    }

    public static Long requireUserId(OAuth2User principal) {
        if (!(principal instanceof CustomOAuth2User customUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        return customUser.getUserId();
    }

    public static Long requireSelf(OAuth2User principal, Long userId) {
        Long currentUserId = requireUserId(principal);
        if (!Objects.equals(currentUserId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 정보만 요청할 수 있습니다.");
        }

        return currentUserId;
    }

}
