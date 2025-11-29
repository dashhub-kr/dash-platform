package com.ssafy.dash.onboarding.domain;

import java.util.Optional;

public interface OnboardingRepositoryPort {

    Optional<OnboardingRepository> findByUserId(Long userId);

    void save(OnboardingRepository repository);
    
}
