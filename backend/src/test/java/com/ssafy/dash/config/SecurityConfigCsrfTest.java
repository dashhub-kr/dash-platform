package com.ssafy.dash.config;

import com.ssafy.dash.common.ratelimit.GlobalRateLimitFilter;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2UserService;
import com.ssafy.dash.oauth.presentation.security.OAuth2AuthenticationFailureHandler;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigCsrfTest.DummyController.class)
@Import({SecurityConfig.class, GlobalRateLimitFilter.class, SecurityConfigCsrfTest.DummyController.class})
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.github.client-id=test",
        "spring.security.oauth2.client.registration.github.client-secret=test"
})
class SecurityConfigCsrfTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Test
    @WithMockUser
    @DisplayName("CSRF 토큰 없는 POST는 거부한다")
    void CSRF_토큰_없는_POST는_거부한다() throws Exception {
        mvc.perform(post("/api/test")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("쿠키로 받은 토큰을 헤더로 보내면 통과한다")
    void 쿠키로_받은_토큰을_헤더로_보내면_통과한다() throws Exception {
        MvcResult result = mvc.perform(get("/api/test")).andExpect(status().isOk()).andReturn();
        Cookie xsrf = result.getResponse().getCookie("XSRF-TOKEN");
        assertThat(xsrf).isNotNull();

        mvc.perform(post("/api/test")
                        .cookie(xsrf)
                        .header("X-XSRF-TOKEN", xsrf.getValue()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GitHub 웹훅은 CSRF 검증에서 제외한다")
    void GitHub_웹훅은_CSRF_검증에서_제외한다() throws Exception {
        mvc.perform(post("/api/webhooks/github")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("비로그인 사용자는 AI API에 접근할 수 없다")
    void 비로그인_사용자는_AI_API에_접근할_수_없다() throws Exception {
        mvc.perform(get("/api/ai/test")).andExpect(status().isUnauthorized());
    }

    @RestController
    static class DummyController {

        @GetMapping("/api/test")
        String get() {
            return "ok";
        }

        @PostMapping("/api/test")
        String post() {
            return "ok";
        }

        @PostMapping("/api/webhooks/github")
        String webhook() {
            return "ok";
        }

        @GetMapping("/api/ai/test")
        String ai() {
            return "ok";
        }
    }
}
