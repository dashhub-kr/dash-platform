package com.ssafy.dash.oauth.domain;

import java.util.Optional;

public interface UserOAuthTokenRepository {

    Optional<UserOAuthToken> findByUserId(Long userId);

    void save(UserOAuthToken token);
    
}
