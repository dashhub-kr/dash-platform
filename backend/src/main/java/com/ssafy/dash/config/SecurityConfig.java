package com.ssafy.dash.config;

import com.ssafy.dash.oauth.presentation.security.CustomOAuth2UserService;
import com.ssafy.dash.oauth.presentation.security.OAuth2AuthenticationFailureHandler;
import com.ssafy.dash.common.ratelimit.GlobalRateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
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
                                .csrf(AbstractHttpConfigurer::disable)
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
                                                                "/api/users/*/solvedac/**", // Solved.ac API endpoints
                                                                "/api/users/*/analysis/**", // Analysis API endpoints
                                                                                            // for
                                                                                            // testing
                                                                "/api/ai/**", // AI endpoints for testing
                                                                "/api/onboarding/solvedac/verify" // Solvedac handle
                                                                                                  // verification
                                                ).permitAll()
                                                .anyRequest().authenticated())
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                                .oauth2Login(oauth2 -> oauth2
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .defaultSuccessUrl(frontendUrl + "/oauth2/redirect", true)
                                                .failureHandler(oAuth2AuthenticationFailureHandler)) // 실패 핸들러 추가
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
                                Arrays.asList("http://localhost:5173", "http://localhost:8080", frontendUrl)); // 로컬 +
                                                                                                               // 운영
                configuration.setAllowedMethods(Collections.singletonList("*"));
                configuration.setAllowedHeaders(Collections.singletonList("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }

}
