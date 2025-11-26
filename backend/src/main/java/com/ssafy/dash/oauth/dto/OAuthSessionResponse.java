package com.ssafy.dash.oauth.dto;

import com.ssafy.dash.user.dto.UserResponse;

public class OAuthSessionResponse {

    private final UserResponse user;
    private final AuthFlowType flowType;

    public OAuthSessionResponse(UserResponse user, AuthFlowType flowType) {
        this.user = user;
        this.flowType = flowType;
    }

    public UserResponse getUser() {
        return user;
    }

    public AuthFlowType getFlowType() {
        return flowType;
    }
    
}
