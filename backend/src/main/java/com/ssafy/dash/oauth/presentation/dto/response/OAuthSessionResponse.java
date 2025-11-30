package com.ssafy.dash.oauth.presentation.dto.response;

import com.ssafy.dash.oauth.domain.AuthFlowType;
import com.ssafy.dash.user.presentation.dto.response.UserResponse;

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
