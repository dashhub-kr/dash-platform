package com.ssafy.dash.onboarding.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.oauth.application.dto.result.OAuthLoginResult;
import com.ssafy.dash.oauth.domain.AuthFlowType;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import com.ssafy.dash.onboarding.application.OnboardingService;
import com.ssafy.dash.onboarding.application.dto.command.RepositorySetupCommand;
import com.ssafy.dash.onboarding.application.dto.result.RepositorySetupResult;
import com.ssafy.dash.onboarding.domain.exception.WebhookRegistrationException;
import com.ssafy.dash.onboarding.presentation.dto.request.RepositorySetupRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OnboardingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(OnboardingControllerTest.TestConfig.class)
@DisplayName("OnboardingController 테스트")
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OnboardingService onboardingService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        Mockito.reset(onboardingService);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        OnboardingService onboardingService() {

            return Mockito.mock(OnboardingService.class);
        }
    }

    @Test
    @DisplayName("저장소 설정이 성공하면 웹훅 구성 상태를 반환한다")
    void setupRepository_success() throws Exception {
        RepositorySetupRequest request = new RepositorySetupRequest("utact/dash-repo");
        RepositorySetupResult result = new RepositorySetupResult(TestFixtures.TEST_USER_ID,
                request.getRepositoryName(), true);
        given(onboardingService.setupRepository(any(RepositorySetupCommand.class)))
                .willReturn(result);

        authenticateUser();

        mockMvc.perform(post("/api/onboarding/repository")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositoryName").value(request.getRepositoryName()))
                .andExpect(jsonPath("$.webhookConfigured").value(true));
    }

    @Test
    @DisplayName("웹훅 설정에 실패하면 502를 반환한다")
    void setupRepository_failureWebhook() throws Exception {
        RepositorySetupRequest request = new RepositorySetupRequest("utact/dash-repo");
        authenticateUser();
        given(onboardingService.setupRepository(any(RepositorySetupCommand.class)))
                .willThrow(new WebhookRegistrationException("GitHub 오류"));

        mockMvc.perform(post("/api/onboarding/repository")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway());
    }

    @Test
    @DisplayName("인증 없이 저장소를 설정하면 401을 반환한다")
    void setupRepository_failureUnauthorized() throws Exception {
        RepositorySetupRequest request = new RepositorySetupRequest("utact/dash-repo");

        mockMvc.perform(post("/api/onboarding/repository")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        Mockito.verifyNoInteractions(onboardingService);
    }

    private void authenticateUser() {
        OAuth2User delegate = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("id", TestFixtures.TEST_PROVIDER_ID),
                "id");
        OAuthLoginResult loginResult = new OAuthLoginResult(TestFixtures.createUser(), AuthFlowType.SIGN_UP);
        CustomOAuth2User customUser = new CustomOAuth2User(delegate, loginResult);
        AbstractAuthenticationToken authentication = new OAuth2AuthenticationToken(customUser,
                customUser.getAuthorities(), TestFixtures.TEST_PROVIDER);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
