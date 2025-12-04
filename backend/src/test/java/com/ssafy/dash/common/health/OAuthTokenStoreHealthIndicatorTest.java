package com.ssafy.dash.common.health;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.springframework.boot.actuate.health.Health;

import com.ssafy.dash.oauth.domain.UserOAuthTokenRepository;

@DisplayName("OAuthTokenStoreHealthIndicator 테스트")
class OAuthTokenStoreHealthIndicatorTest {

    private final UserOAuthTokenRepository repository = Mockito.mock(UserOAuthTokenRepository.class);
    private final OAuthTokenStoreHealthIndicator indicator = new OAuthTokenStoreHealthIndicator(repository);

    @Test
    @DisplayName("저장소 접근에 성공하면 UP 상태를 반환한다")
    void healthUpWhenRepositoryAccessible() {
        when(repository.findByUserId(anyLong())).thenReturn(Optional.empty());

        Health health = indicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("UP");
    }

    @Test
    @DisplayName("저장소 접근 중 예외가 나면 DOWN 상태를 반환한다")
    void healthDownWhenRepositoryThrows() {
        when(repository.findByUserId(anyLong())).thenThrow(new IllegalStateException("db down"));

        Health health = indicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("DOWN");
    }
    
}
