package com.ssafy.dash.oauth.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.oauth.domain.CustomOAuth2User;
import com.ssafy.dash.oauth.dto.OAuthLoginResult;
import com.ssafy.dash.user.service.UserService;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;
    private final OAuthTokenService oauthTokenService;

    @Autowired
    public CustomOAuth2UserService(UserService userService, OAuthTokenService oauthTokenService) {
        this(userService, oauthTokenService, new DefaultOAuth2UserService());
    }

    public CustomOAuth2UserService(UserService userService, OAuthTokenService oauthTokenService,
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate) {
        this.userService = userService;
        this.delegate = delegate;
        this.oauthTokenService = oauthTokenService;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
            
        Map<String, Object> attributes = oauth2User.getAttributes();
        System.out.println("OAuth2 Attributes: " + attributes);
            
        String providerId = String.valueOf(attributes.get("id"));
        String email = (String) attributes.get("email");
        String login = (String) attributes.get("login");
        String avatarUrl = (String) attributes.get("avatar_url");
            
        if (email == null) {
            email = login + "@github.placeholder";
        }
            
        OAuthLoginResult loginResult = userService.createOrUpdateOAuthUser(registrationId, providerId, login, email, avatarUrl);

        oauthTokenService.saveAccessToken(loginResult.getUser().getId(), userRequest.getAccessToken());

        return new CustomOAuth2User(oauth2User, loginResult);
    }

}
