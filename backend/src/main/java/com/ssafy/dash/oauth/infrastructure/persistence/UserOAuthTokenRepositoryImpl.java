package com.ssafy.dash.oauth.infrastructure.persistence;

import com.ssafy.dash.oauth.domain.UserOAuthToken;
import com.ssafy.dash.oauth.domain.UserOAuthTokenRepository;
import com.ssafy.dash.oauth.infrastructure.mapper.UserOAuthTokenMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserOAuthTokenRepositoryImpl implements UserOAuthTokenRepository {

    private final UserOAuthTokenMapper userOAuthTokenMapper;

    public UserOAuthTokenRepositoryImpl(UserOAuthTokenMapper userOAuthTokenMapper) {
        this.userOAuthTokenMapper = userOAuthTokenMapper;
    }

    @Override
    public Optional<UserOAuthToken> findByUserId(Long userId) {
        return Optional.ofNullable(userOAuthTokenMapper.selectByUserId(userId));
    }

    @Override
    public void save(UserOAuthToken token) {
        if (userOAuthTokenMapper.selectByUserId(token.getUserId()) == null) {
            userOAuthTokenMapper.insert(token);
        } else {
            userOAuthTokenMapper.update(token);
        }
    }

}
