package com.ssafy.dash.onboarding.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.onboarding.domain.Onboarding;
import com.ssafy.dash.onboarding.domain.OnboardingRepository;
import com.ssafy.dash.onboarding.infrastructure.mapper.OnboardingMapper;

@Repository
public class OnboardingRepositoryImpl implements OnboardingRepository {

    private final OnboardingMapper mapper;

    public OnboardingRepositoryImpl(OnboardingMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<Onboarding> findByUserId(Long userId) {
        return Optional.ofNullable(mapper.selectByUserId(userId));
    }

    @Override
    public Optional<Onboarding> findByRepositoryName(String repositoryName) {
        return Optional.ofNullable(mapper.selectByRepositoryName(repositoryName));
    }

    @Override
    public void save(Onboarding repository) {
        if (repository.getId() == null) {
            mapper.insert(repository);
        } else {
            mapper.update(repository);
        }
    }

    @Override
    public void deleteByUserId(Long userId) {
        mapper.deleteByUserId(userId);
    }

}
