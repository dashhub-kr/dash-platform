package com.ssafy.dash.config;

import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    static {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(AuthenticationPrincipal.class);
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(OAuth2User.class);
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dash API")
                        .description("Dash Application API Documentation<br><br>" +
                                "<strong>Authentication:</strong> This API uses OAuth2 (GitHub).<br>" +
                                "To test endpoints, you must first <a href='/oauth2/authorization/github' target='_blank'>Log in with GitHub</a> " +
                                "to establish a session in your browser.")
                        .version("v1.0.0"));
    }
    
}
