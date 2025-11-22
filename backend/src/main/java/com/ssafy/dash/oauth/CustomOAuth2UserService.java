package com.ssafy.dash.oauth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.user.service.UserService;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    @Autowired
    public CustomOAuth2UserService(UserService userService) {
        this(userService, new DefaultOAuth2UserService());
    }

    public CustomOAuth2UserService(UserService userService, OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate) {
        this.userService = userService;
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        String providerId = String.valueOf(attributes.get("id"));
        String email = (String) attributes.get("email");
        String login = (String) attributes.get("login");
        String avatarUrl = (String) attributes.get("avatar_url");
        
        if (email == null) {
            email = login + "@github.placeholder";
        }
        
        userService.createOrUpdateOAuthUser(registrationId, providerId, login, email, avatarUrl);

        return oauth2User;
    }

}
