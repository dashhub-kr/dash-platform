package com.ssafy.dash.onboarding.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.oauth.domain.CustomOAuth2User;
import com.ssafy.dash.oauth.dto.AuthFlowType;
import com.ssafy.dash.oauth.dto.OAuthLoginResult;
import com.ssafy.dash.onboarding.dto.RepositorySetupRequest;
import com.ssafy.dash.onboarding.dto.RepositorySetupResponse;
import com.ssafy.dash.onboarding.service.OnboardingService;

import org.mockito.Mockito;

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
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        OnboardingService onboardingService() {
            return Mockito.mock(OnboardingService.class);
        }
    }

    @Test
    @DisplayName("저장소 설정 성공")
    void setupRepository_success() throws Exception {
        RepositorySetupRequest request = new RepositorySetupRequest("utact/dash-repo");
        RepositorySetupResponse response = new RepositorySetupResponse(TestFixtures.TEST_USER_ID,
                request.getRepositoryName(), false);
        given(onboardingService.setupRepository(eq(TestFixtures.TEST_USER_ID), any(RepositorySetupRequest.class)))
                .willReturn(response);

        OAuth2User delegate = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("id", TestFixtures.TEST_PROVIDER_ID),
                "id");
        OAuthLoginResult loginResult = new OAuthLoginResult(TestFixtures.createUser(), AuthFlowType.SIGN_UP);
        CustomOAuth2User customUser = new CustomOAuth2User(delegate, loginResult);
        AbstractAuthenticationToken authentication = new OAuth2AuthenticationToken(customUser,
                customUser.getAuthorities(), TestFixtures.TEST_PROVIDER);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(post("/api/onboarding/repository")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositoryName").value(request.getRepositoryName()));
    }
    
}
