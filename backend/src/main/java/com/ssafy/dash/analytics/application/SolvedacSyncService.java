package com.ssafy.dash.analytics.application;

import com.ssafy.dash.analytics.domain.UserClassStat;
import com.ssafy.dash.analytics.domain.UserClassStatRepository;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.domain.UserTagStatRepository;
import com.ssafy.dash.solvedac.domain.SolvedacApiClient;
import com.ssafy.dash.solvedac.domain.ClassStat;
import com.ssafy.dash.solvedac.domain.SolvedacUser;
import com.ssafy.dash.solvedac.domain.TagStat;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import com.ssafy.dash.ai.infrastructure.persistence.LearningPathCacheMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolvedacSyncService {

    private final SolvedacApiClient solvedacClient;
    private final UserRepository userRepository;
    private final UserClassStatRepository classStatRepository;
    private final UserTagStatRepository tagStatRepository;
    private final LearningPathCacheMapper cacheMapper;

    /**
     * 사용자 Solved.ac 핸들 등록 및 초기 데이터 동기화
     */
    @Transactional
    public void registerSolvedacHandle(Long userId, String handle, String profileImageUrl) {
        log.info("Registering Solved.ac handle for user {}: {}", userId, handle);

        // 1. 사용자 기본 정보 조회
        SolvedacUser userInfo = solvedacClient.getUserInfo(handle);

        // 2. User 테이블 업데이트
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.updateSolvedacProfile(handle, userInfo.tier(),
                userInfo.rating(), userInfo.classLevel(), userInfo.solvedCount());

        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            user.setAvatarUrl(profileImageUrl);
        }

        userRepository.update(user);

        // 3. 클래스 통계 동기화
        syncClassStats(userId, handle);

        // 4. 태그 통계 동기화
        syncTagStats(userId, handle);

        // 5. 학습 경로 캐시 무효화 (데이터 변경됨)
        cacheMapper.deleteByUserId(userId);
        log.info("Invalidated learning path cache for user {}", userId);

        log.info("Successfully synced Solved.ac data for user {}", userId);
    }

    /**
     * 클래스별 통계 동기화
     */
    @Transactional
    public void syncClassStats(Long userId, String handle) {
        List<ClassStat> classStats = solvedacClient.getClassStats(handle);

        classStats.forEach(stat -> {
            UserClassStat entity = UserClassStat.create(
                    userId,
                    stat.classNumber(),
                    stat.total(),
                    stat.totalSolved(),
                    stat.essentials(),
                    stat.essentialSolved(),
                    stat.decoration());
            classStatRepository.save(entity);
        });

        log.info("Synced {} class stats for user {}", classStats.size(), userId);
    }

    /**
     * 태그별 통계 동기화
     */
    @Transactional
    public void syncTagStats(Long userId, String handle) {
        TagStat response = solvedacClient.getTagStats(handle);

        response.items().forEach(item -> {
            UserTagStat entity = UserTagStat.create(
                    userId,
                    item.tag().key(),
                    item.total(),
                    item.solved(),
                    item.partial(),
                    item.tried());
            tagStatRepository.save(entity);
        });

        log.info("Synced {} tag stats for user {}", response.count(), userId);
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

        registerSolvedacHandle(userId, handle, null);
    }
}
