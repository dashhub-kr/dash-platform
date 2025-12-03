package com.ssafy.dash.oauth.application.dto.result;

import com.ssafy.dash.oauth.domain.AuthFlowType;
import com.ssafy.dash.user.domain.User;

public record OAuthLoginResult(User user, AuthFlowType flowType) {

    public boolean isSignUp() {
        return AuthFlowType.SIGN_UP.equals(flowType);
    }
    
}
