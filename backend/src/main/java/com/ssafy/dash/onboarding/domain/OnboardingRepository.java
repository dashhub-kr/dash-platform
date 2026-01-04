package com.ssafy.dash.onboarding.domain;

import java.util.Optional;

public interface OnboardingRepository {

    Optional<Onboarding> findByUserId(Long userId);

    Optional<Onboarding> findByRepositoryName(String repositoryName);

    void save(Onboarding repository);

    void deleteByUserId(Long userId);

}
