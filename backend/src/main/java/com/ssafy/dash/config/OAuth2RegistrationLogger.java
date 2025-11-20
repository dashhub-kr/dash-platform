package com.ssafy.dash.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
public class OAuth2RegistrationLogger {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2RegistrationLogger.class);

    @Bean
    public CommandLineRunner logRegistrations(ClientRegistrationRepository repo) {

        return args -> {
            try {
                if (repo == null) {
                    logger.warn("ClientRegistrationRepository bean not found");
                    return;
                }

                // 리포지토리가 Iterable<ClientRegistration>일 수 있음
                if (repo instanceof Iterable) {
                    for (Object o : (Iterable<?>) repo) {
                        if (o instanceof ClientRegistration) {
                            ClientRegistration cr = (ClientRegistration) o;
                                logger.info("OAuth2 client registration loaded: registrationId='{}', clientId='{}'",
                                    cr.getRegistrationId(), mask(cr.getClientId()));
                        }
                    }
                }
                else {
                    // 일반적인 ID에 대해 단일 조회 시도
                    String[] ids = new String[]{"github"};
                    for (String id : ids) {
                        try {
                            ClientRegistration cr = (ClientRegistration) repo.getClass().getMethod("findByRegistrationId", String.class).invoke(repo, id);
                            if (cr != null) logger.info("OAuth2 client registration loaded: id='{}', clientId='{}'", cr.getRegistrationId(), mask(cr.getClientId()));
                        } catch (NoSuchMethodException ignore) {
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to log client registrations", e);
            }
        };
        
    }

    private String mask(String s) {

        if (s == null) return null;
        if (s.length() <= 6) return "****";

        return s.substring(0, 3) + "..." + s.substring(s.length() - 3);

    }
    
}
