package com.ssafy.dash.user.application;

import com.ssafy.dash.onboarding.domain.OnboardingRepository;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(UserCleanupScheduler.class);

    private final UserRepository userRepository;
    private final OnboardingRepository onboardingRepository;

    public UserCleanupScheduler(UserRepository userRepository, OnboardingRepository onboardingRepository) {
        this.userRepository = userRepository;
        this.onboardingRepository = onboardingRepository;
    }

    /**
     * 매일 자정에 실행, 탈퇴한 지 30일 지난 계정 익명화 및 레포지토리 정보 삭제
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void anonymizeExpiredUsers() {
        // 30일 전
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        log.info("Starting user anonymization for users deleted before {}", threshold);

        List<User> users = userRepository.findUsersForAnonymization(threshold);
        log.info("Found {} users to anonymize.", users.size());

        for (User user : users) {
            try {
                log.info("Anonymizing user id={}", user.getId());
                userRepository.anonymize(user.getId());
                // 익명화와 동시에 레포지토리 연동 정보 삭제 (재가입 시 충돌 방지)
                onboardingRepository.deleteByUserId(user.getId());
            } catch (Exception e) {
                log.error("Failed to anonymize user id={}", user.getId(), e);
            }
        }

        log.info("Completed user anonymization.");
    }
}
