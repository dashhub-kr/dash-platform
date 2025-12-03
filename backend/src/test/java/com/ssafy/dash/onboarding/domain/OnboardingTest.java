package com.ssafy.dash.onboarding.domain;

import com.ssafy.dash.common.fixtures.FixtureTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OnboardingTest {

    @Test
    @DisplayName("온보딩을 생성하면 기본 상태가 false로 설정된다")
    void create_ShouldInitializeDefaults() {
        var now = FixtureTime.now();

        Onboarding onboarding = Onboarding.create(1L, "repo/name", now);

        assertThat(onboarding.isWebhookConfigured()).isFalse();
        assertThat(onboarding.getCreatedAt()).isEqualTo(now);
        assertThat(onboarding.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("저장소 이름을 변경하면 웹훅 설정이 초기화된다")
    void updateRepository_ShouldResetWebhook() {
        Onboarding onboarding = Onboarding.create(1L, "old", FixtureTime.now());

        var updatedAt = FixtureTime.now().plusHours(1);
        onboarding.updateRepository("new", updatedAt);

        assertThat(onboarding.getRepositoryName()).isEqualTo("new");
        assertThat(onboarding.isWebhookConfigured()).isFalse();
        assertThat(onboarding.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("잘못된 파라미터로 생성하면 IllegalArgumentException이 발생한다")
    void create_ShouldValidateArguments() {
        assertThatThrownBy(() -> Onboarding.create(0L, "", FixtureTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
