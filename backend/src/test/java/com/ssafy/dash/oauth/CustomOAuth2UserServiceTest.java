package com.ssafy.dash.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    @DisplayName("GitHub 로그인 시 유저 서비스가 호출된다")
    void loadUser_callsUserService() {
        // given
        String registrationId = "github";
        String userNameAttributeName = "id";
        Map<String, Object> attributes = Map.of(
            "id", 12345678,
            "login", "tester",
            "email", "test@example.com",
            "avatar_url", "http://avatar.url"
        );

        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(registrationId)
                .clientId("client-id")
                .clientSecret("client-secret")
                .clientAuthenticationMethod(org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("read:user")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName(userNameAttributeName)
                .clientName("GitHub")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "token", null, null);

        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);
        OAuth2User oauth2User = new DefaultOAuth2User(
                Collections.emptySet(), attributes, userNameAttributeName);

        given(delegate.loadUser(userRequest)).willReturn(oauth2User);
        given(userService.createOrUpdateOAuthUser(anyString(), anyString(), anyString(), anyString(), anyString()))
            .willReturn(new User());

        // when
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // then
        assertThat(result).isNotNull();
        verify(userService).createOrUpdateOAuthUser(registrationId, "12345678", "tester", "test@example.com", "http://avatar.url");
    }
    
}
