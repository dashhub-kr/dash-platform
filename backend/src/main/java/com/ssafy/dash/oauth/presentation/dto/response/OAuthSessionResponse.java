package com.ssafy.dash.oauth.presentation.dto.response;

import com.ssafy.dash.oauth.domain.AuthFlowType;
import com.ssafy.dash.user.presentation.dto.response.UserResponse;

public record OAuthSessionResponse(
        UserResponse user, AuthFlowType flowType
) {

}
