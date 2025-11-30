package com.ssafy.dash.oauth.application.dto;

import com.ssafy.dash.oauth.domain.AuthFlowType;
import com.ssafy.dash.user.domain.User;

public class OAuthLoginResult {

    private final User user;
    private final AuthFlowType flowType;

    public OAuthLoginResult(User user, AuthFlowType flowType) {
        this.user = user;
        this.flowType = flowType;
    }

    public User getUser() {
        return user;
    }

    public AuthFlowType getFlowType() {
        return flowType;
    }

    public boolean isSignUp() {
        return AuthFlowType.SIGN_UP.equals(flowType);
    }
    
}
