package com.ssafy.dash.onboarding.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.onboarding.domain.OnboardingRepository;
import com.ssafy.dash.onboarding.mapper.OnboardingRepositoryMapper;
import com.ssafy.dash.onboarding.dto.RepositorySetupRequest;
import com.ssafy.dash.onboarding.dto.RepositorySetupResponse;

@Service
public class OnboardingService {

    private final OnboardingRepositoryMapper repositoryMapper;

    public OnboardingService(OnboardingRepositoryMapper repositoryMapper) {
        this.repositoryMapper = repositoryMapper;
    }

    @Transactional
    public RepositorySetupResponse setupRepository(Long userId, RepositorySetupRequest request) {
        String repositoryName = request.getRepositoryName().trim();
        OnboardingRepository repository = repositoryMapper.selectByUserId(userId);
        if (repository == null) {
            repository = new OnboardingRepository();
            repository.setUserId(userId);
            repository.setRepositoryName(repositoryName);
            repository.setWebhookConfigured(false);
            repositoryMapper.insert(repository);
        } else {
            repository.setRepositoryName(repositoryName);
            repository.setWebhookConfigured(false);
            repositoryMapper.update(repository);
        }

        return new RepositorySetupResponse(userId, repository.getRepositoryName(), repository.isWebhookConfigured());
    }
    
}
