package com.ssafy.dash.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.ssafy.dash.service.security.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final CustomOAuth2UserService customOAuth2UserService;

        @Value("${FRONTEND_BASE_URL:http://localhost:5173}")
        private String frontendBaseUrl;

        public SecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
            this.customOAuth2UserService = customOAuth2UserService;
        }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http
                .csrf((csrf) -> csrf.disable());

        http
                .formLogin((login) -> login.disable());

        http
                .httpBasic((basic) -> basic.disable());

        String successUrl = frontendBaseUrl + "/oauth2/redirect";

        http
                .oauth2Login((oauth2) -> oauth2.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .defaultSuccessUrl(successUrl, true)
                        .failureUrl("/login/error"));

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/oauth2/**", "/login/**").permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.addAllowedOrigin(frontendBaseUrl);
                configuration.addAllowedOrigin("http://localhost:5173");
                configuration.addAllowedOrigin("http://localhost:3000");
                configuration.setAllowCredentials(true);
                configuration.addAllowedHeader(CorsConfiguration.ALL);
                configuration.addAllowedMethod(CorsConfiguration.ALL);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

}
