package com.ssafy.dash.analytics.application;

import com.ssafy.dash.analytics.domain.UserClassStat;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserClassStatMapper;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import com.ssafy.dash.external.solvedac.SolvedacApiClient;
import com.ssafy.dash.external.solvedac.dto.ClassStatResponse;
import com.ssafy.dash.external.solvedac.dto.SolvedacUserResponse;
import com.ssafy.dash.external.solvedac.dto.TagStatResponse;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Solved.ac 데이터 동기화 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SolvedacSyncService {

    private final SolvedacApiClient solvedacClient;
    private final UserRepository userRepository;
    private final UserClassStatMapper classStatMapper;
    private final UserTagStatMapper tagStatMapper;

    /**
     * 사용자 Solved.ac 핸들 등록 및 초기 데이터 동기화
     */
    @Transactional
    public void registerSolvedacHandle(Long userId, String handle) {
        log.info("Registering Solved.ac handle for user {}: {}", userId, handle);

        // 1. 사용자 기본 정보 조회
        SolvedacUserResponse userInfo = solvedacClient.getUserInfo(handle);

        // 2. User 테이블 업데이트
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.updateSolvedacProfile(handle, userInfo.getTier(),
                userInfo.getRating(), userInfo.getClassLevel());
        userRepository.save(user);

        // 3. 클래스 통계 동기화
        syncClassStats(userId, handle);

        // 4. 태그 통계 동기화
        syncTagStats(userId, handle);

        log.info("Successfully synced Solved.ac data for user {}", userId);
    }

    /**
     * 클래스별 통계 동기화
     */
    @Transactional
    public void syncClassStats(Long userId, String handle) {
        List<ClassStatResponse> classStats = solvedacClient.getClassStats(handle);

        classStats.forEach(stat -> {
            UserClassStat entity = UserClassStat.create(
                    userId,
                    stat.getClassNumber(),
                    stat.getTotal(),
                    stat.getTotalSolved(),
                    stat.getEssentials(),
                    stat.getEssentialSolved(),
                    stat.getDecoration());
            classStatMapper.upsert(entity);
        });

        log.info("Synced {} class stats for user {}", classStats.size(), userId);
    }

    /**
     * 태그별 통계 동기화
     */
    @Transactional
    public void syncTagStats(Long userId, String handle) {
        TagStatResponse response = solvedacClient.getTagStats(handle);

        response.getItems().forEach(item -> {
            UserTagStat entity = UserTagStat.create(
                    userId,
                    item.getTag().getKey(),
                    item.getTotal(),
                    item.getSolved(),
                    item.getPartial(),
                    item.getTried());
            tagStatMapper.upsert(entity);
        });

        log.info("Synced {} tag stats for user {}", response.getCount(), userId);
    }

    /**
     * 전체 통계 재동기화 (주기적 업데이트용)
     */
    @Transactional
    public void resyncAllStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        String handle = user.getSolvedacHandle();
        if (handle == null) {
            throw new IllegalStateException("User has no Solved.ac handle registered");
        }

        registerSolvedacHandle(userId, handle);
    }
}
