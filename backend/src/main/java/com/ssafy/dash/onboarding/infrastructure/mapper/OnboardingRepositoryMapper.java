package com.ssafy.dash.onboarding.infrastructure.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.dash.onboarding.domain.OnboardingRepository;

@Mapper
public interface OnboardingRepositoryMapper {

    OnboardingRepository selectByUserId(Long userId);

    int insert(OnboardingRepository repository);

    int update(OnboardingRepository repository);
    
}
