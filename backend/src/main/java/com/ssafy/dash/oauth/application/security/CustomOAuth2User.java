package com.ssafy.dash.oauth.application.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ssafy.dash.oauth.domain.AuthFlowType;
import com.ssafy.dash.oauth.application.OAuthLoginResult;
import com.ssafy.dash.user.domain.User;

public class CustomOAuth2User implements OAuth2User, Serializable {

    private static final long serialVersionUID = 1L;

    private final OAuth2User delegate;
    private final User user;
    private final AuthFlowType flowType;

    public CustomOAuth2User(OAuth2User delegate, OAuthLoginResult loginResult) {
        this.delegate = delegate;
        this.user = loginResult.getUser();
        this.flowType = loginResult.getFlowType();
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
    
    public User getUser() {
        return user;
    }
    
    public Long getUserId() {
        return user.getId();
    }

    public AuthFlowType getFlowType() {
        return flowType;
    }

    public boolean isSignUp() {
        return AuthFlowType.SIGN_UP.equals(flowType);
    }
    
}
