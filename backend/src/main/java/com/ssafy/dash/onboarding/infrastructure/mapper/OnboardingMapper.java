package com.ssafy.dash.onboarding.infrastructure.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.dash.onboarding.domain.Onboarding;

@Mapper
public interface OnboardingMapper {

    Onboarding selectByUserId(Long userId);

    Onboarding selectByRepositoryName(String repositoryName);

    int insert(Onboarding repository);

    int update(Onboarding repository);

    int deleteByUserId(Long userId);

}
