package com.ssafy.dash.oauth.presentation.security;

import com.ssafy.dash.oauth.application.dto.result.OAuthLoginResult;
import com.ssafy.dash.oauth.domain.AuthFlowType;
import com.ssafy.dash.user.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final OAuth2User delegate;
    @Getter
    private final User user;
    @Getter
    private final AuthFlowType flowType;

    public CustomOAuth2User(OAuth2User delegate, OAuthLoginResult loginResult) {
        this.delegate = delegate;
        this.user = loginResult.user();
        this.flowType = loginResult.flowType();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    public Long getUserId() {
        return user.getId();
    }

    public boolean isSignUp() {
        return AuthFlowType.SIGN_UP.equals(flowType);
    }

}
