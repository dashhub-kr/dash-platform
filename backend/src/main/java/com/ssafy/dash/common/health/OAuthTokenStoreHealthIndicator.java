package com.ssafy.dash.common.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.ssafy.dash.oauth.domain.UserOAuthTokenRepository;

@Component("oauthTokenStore")
public class OAuthTokenStoreHealthIndicator implements HealthIndicator {

    private static final long PROBE_USER_ID = -1L;

    private final UserOAuthTokenRepository tokenRepository;

    public OAuthTokenStoreHealthIndicator(UserOAuthTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public Health health() {
        try {
            tokenRepository.findByUserId(PROBE_USER_ID);
            return Health.up()
                    .withDetail("probeUserId", PROBE_USER_ID)
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .build();
        }
    }
    
}
