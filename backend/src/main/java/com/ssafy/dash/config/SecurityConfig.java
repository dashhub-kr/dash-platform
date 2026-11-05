package com.ssafy.dash.config;

import com.ssafy.dash.oauth.presentation.security.CustomOAuth2UserService;
import com.ssafy.dash.oauth.presentation.security.OAuth2AuthenticationFailureHandler;
import com.ssafy.dash.common.ratelimit.GlobalRateLimitFilter;
import com.ssafy.dash.config.csrf.SpaCsrfTokenRequestHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final CustomOAuth2UserService customOAuth2UserService;
        private final GlobalRateLimitFilter globalRateLimitFilter;
        private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

        @Value("${app.frontend.url:http://localhost:5173}")
        private String frontendUrl;

        public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                        GlobalRateLimitFilter globalRateLimitFilter,
                        OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler) {
                this.customOAuth2UserService = customOAuth2UserService;
                this.globalRateLimitFilter = globalRateLimitFilter;
                this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .addFilterBefore(globalRateLimitFilter,
                                                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                // 세션 쿠키 인증이므로 CSRF 토큰 검증 (GitHub 웹훅은 서명으로 검증)
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                                                .ignoringRequestMatchers("/api/webhooks/github"))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/",
                                                                "/login",
                                                                "/error",
                                                                "/favicon.ico",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/oauth2/**",
                                                                "/oauth2/authorization/**",
                                                                "/api/webhooks/github",
                                                                "/actuator/health",
                                                                "/actuator/health/**",
                                                                "/api/onboarding/solvedac/verify"
                                                ).permitAll()
                                                .anyRequest().authenticated())
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                                .oauth2Login(oauth2 -> oauth2
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .defaultSuccessUrl(frontendUrl + "/oauth2/redirect", true)
                                                .failureHandler(oAuth2AuthenticationFailureHandler))
                                .logout(logout -> logout
                                                .logoutUrl("/api/logout")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("JSESSIONID")
                                                .logoutSuccessHandler((request, response, authentication) -> {
                                                        org.springframework.security.core.context.SecurityContextHolder
                                                                        .clearContext();
                                                        response.setStatus(200);
                                                }));

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(
                                Arrays.asList("http://localhost:5173", "http://localhost:8080", frontendUrl));
                configuration.setAllowedMethods(Collections.singletonList("*"));
                configuration.setAllowedHeaders(Collections.singletonList("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }

}
