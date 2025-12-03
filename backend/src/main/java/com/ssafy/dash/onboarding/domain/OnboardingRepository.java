package com.ssafy.dash.onboarding.domain;

import java.util.Optional;

public interface OnboardingRepository {

    Optional<Onboarding> findByUserId(Long userId);

    void save(Onboarding repository);
    
}
