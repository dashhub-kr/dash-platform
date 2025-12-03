package com.ssafy.dash.oauth.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.oauth.application.dto.result.OAuthLoginResult;
import com.ssafy.dash.oauth.domain.AuthFlowType;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;

@Service
public class OAuthUserService {

    private final UserRepository userRepository;

    public OAuthUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public OAuthLoginResult createOrUpdateOAuthUser(String provider, String providerId, String login, String email, String avatarUrl) {
        User user = userRepository.findByProviderAndProviderId(provider, providerId).orElse(null);
        AuthFlowType flowType;

        if (user == null) {
            user = User.create(login, email, LocalDateTime.now(), provider, providerId, avatarUrl);
            userRepository.save(user);
            flowType = AuthFlowType.SIGN_UP;
        } else {
            user.updateProfile(login, email, avatarUrl);
            userRepository.update(user);
            flowType = AuthFlowType.LOGIN;
        }

        return new OAuthLoginResult(user, flowType);
    }
    
}
