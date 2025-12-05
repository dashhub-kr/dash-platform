package com.ssafy.dash.oauth.presentation.security;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.oauth.application.OAuthTokenService;
import com.ssafy.dash.oauth.application.OAuthUserService;
import com.ssafy.dash.oauth.application.dto.result.OAuthLoginResult;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final OAuthUserService oauthUserService;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;
    private final OAuthTokenService oauthTokenService;

    @Autowired
    public CustomOAuth2UserService(OAuthUserService oauthUserService, OAuthTokenService oauthTokenService) {
        this(oauthUserService, oauthTokenService, new DefaultOAuth2UserService());
    }

    public CustomOAuth2UserService(OAuthUserService oauthUserService, OAuthTokenService oauthTokenService,
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate) {
        this.oauthUserService = oauthUserService;
        this.delegate = delegate;
        this.oauthTokenService = oauthTokenService;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();
        log.debug("OAuth2 Attributes: {}", attributes);

        String providerId = String.valueOf(attributes.get("id"));
        String email = (String) attributes.get("email");
        String login = (String) attributes.get("login");
        String avatarUrl = (String) attributes.get("avatar_url");

        if (email == null) {
            email = login + "@github.placeholder";
        }

        OAuthLoginResult loginResult = oauthUserService.createOrUpdateOAuthUser(registrationId, providerId, login, email, avatarUrl);

        try {
            OAuth2AccessToken accessToken = userRequest.getAccessToken();
            boolean hasToken = accessToken != null && accessToken.getTokenValue() != null;
            if (hasToken && log.isInfoEnabled()) {
                log.info("GitHub token details userId={} issuedAtUTC={} expiresAtUTC={} scopes={} tokenType={} tokenSnippet={}",
                        loginResult.user().getId(),
                        accessToken.getIssuedAt(),
                        accessToken.getExpiresAt(),
                        accessToken.getScopes(),
                        accessToken.getTokenType() != null ? accessToken.getTokenType().getValue() : null,
                        mask(accessToken.getTokenValue()));
            }
            log.debug("Saving access token for userId={} tokenPresent={}", loginResult.user().getId(), hasToken);
            oauthTokenService.saveAccessToken(loginResult.user().getId(), accessToken);
            log.debug("Access token save attempted for userId={}", loginResult.user().getId());
        } catch (Exception ex) {
            log.error("Failed to save access token for userId={}: {}", loginResult.user().getId(), ex.getMessage(), ex);
        }

        return new CustomOAuth2User(oauth2User, loginResult);
    }

    private String mask(String tokenValue) {
        if (tokenValue == null) {
            return "null";
        }
        return tokenValue.length() > 8 ? tokenValue.substring(0, 6) + "..." : "***";
    }

}
