package com.ssafy.dash.service.security;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.ssafy.dash.dto.CustomOAuth2User;
import com.ssafy.dash.dto.GitHubResponse;
import com.ssafy.dash.dto.GoogleResponse;
import com.ssafy.dash.dto.OAuth2Response;
import com.ssafy.dash.entity.OAuthToken;
import com.ssafy.dash.repository.OAuthTokenRepository;

@Service("customOAuth2SecurityUserService")
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final OAuthTokenRepository tokenRepository;

    public CustomOAuth2UserService(OAuthTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2AccessToken accessToken = userRequest.getAccessToken();

        Map<String, Object> attributes = oauth2User.getAttributes();
        OAuth2Response response;
        if ("github".equalsIgnoreCase(registrationId)) {
            response = new GitHubResponse(attributes);
        } else {
            response = new GoogleResponse(attributes);
        }

        // Persist access token for later use (webhook processing)
        String provider = response.getProvider();
        String providerId = response.getProviderId();
        String providerLogin = null;
        Object loginObj = attributes.get("login");
        if (loginObj != null) providerLogin = loginObj.toString();
        if (provider != null && providerId != null && accessToken != null) {
            OAuthToken t = null;
            if (providerLogin != null) {
                t = tokenRepository.findByProviderAndProviderLogin(provider, providerLogin).orElseGet(OAuthToken::new);
            } else {
                t = tokenRepository.findByProviderAndProviderId(provider, providerId).orElseGet(OAuthToken::new);
            }
            t.setProvider(provider);
            t.setProviderId(providerId);
            t.setProviderLogin(providerLogin);
            t.setAccessToken(accessToken.getTokenValue());
            tokenRepository.save(t);
        }

        return new CustomOAuth2User(response, "ROLE_USER");
    }

}
