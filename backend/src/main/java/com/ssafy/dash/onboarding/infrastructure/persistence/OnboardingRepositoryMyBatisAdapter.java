package com.ssafy.dash.onboarding.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.onboarding.domain.OnboardingRepository;
import com.ssafy.dash.onboarding.domain.OnboardingRepositoryPort;
import com.ssafy.dash.onboarding.infrastructure.mapper.OnboardingRepositoryMapper;

@Repository
public class OnboardingRepositoryMyBatisAdapter implements OnboardingRepositoryPort {

    private final OnboardingRepositoryMapper mapper;

    public OnboardingRepositoryMyBatisAdapter(OnboardingRepositoryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<OnboardingRepository> findByUserId(Long userId) {

        return Optional.ofNullable(mapper.selectByUserId(userId));
    }

    @Override
    public void save(OnboardingRepository repository) {
        if (repository.getId() == null) {
            mapper.insert(repository);
        } else {
            mapper.update(repository);
        }
    }
    
}
