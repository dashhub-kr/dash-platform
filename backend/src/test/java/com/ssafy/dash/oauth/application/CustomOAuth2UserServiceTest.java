package com.ssafy.dash.oauth.application;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.oauth.infrastructure.security.CustomOAuth2User;
import com.ssafy.dash.oauth.domain.AuthFlowType;
import com.ssafy.dash.oauth.application.dto.OAuthLoginResult;
import com.ssafy.dash.user.application.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomOAuth2UserService 단위 테스트")
class CustomOAuth2UserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private OAuthTokenService oauthTokenService;

    @Mock
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    @DisplayName("GitHub 로그인 시 유저 서비스 호출 성공")
    void loadUser_callsUserService() {
        // given
        String registrationId = TestFixtures.TEST_PROVIDER;
        String userNameAttributeName = "id";
        long providerNumericId = Long.parseLong(TestFixtures.TEST_PROVIDER_ID);
        Map<String, Object> attributes = Map.of(
            "id", providerNumericId,
            "login", TestFixtures.TEST_USERNAME,
            "email", TestFixtures.TEST_EMAIL,
            "avatar_url", TestFixtures.TEST_AVATAR_URL
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
            .willReturn(new OAuthLoginResult(TestFixtures.createUser(), AuthFlowType.LOGIN));

        // when
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // then
        assertThat(result).isInstanceOfSatisfying(CustomOAuth2User.class, customUser -> {
            assertThat(customUser.getFlowType()).isEqualTo(AuthFlowType.LOGIN);
            assertThat(customUser.isSignUp()).isFalse();
        });
        verify(userService).createOrUpdateOAuthUser(registrationId, TestFixtures.TEST_PROVIDER_ID, TestFixtures.TEST_USERNAME, TestFixtures.TEST_EMAIL, TestFixtures.TEST_AVATAR_URL);
        verify(oauthTokenService).saveAccessToken(TestFixtures.TEST_USER_ID, accessToken);
    }

}
