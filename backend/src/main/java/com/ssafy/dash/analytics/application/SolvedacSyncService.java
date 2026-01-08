package com.ssafy.dash.analytics.application;

import com.ssafy.dash.analytics.domain.UserClassStat;
import com.ssafy.dash.analytics.domain.UserClassStatRepository;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.domain.UserTagStatRepository;
import com.ssafy.dash.solvedac.domain.SolvedacApiClient;
import com.ssafy.dash.solvedac.domain.ClassStat;
import com.ssafy.dash.solvedac.domain.SolvedacUser;
import com.ssafy.dash.solvedac.domain.TagStat;
import com.ssafy.dash.solvedac.domain.Top100Problem;
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
    private final SolvedacProblemSyncer problemSyncer;

    /**
     * 사용자 Solved.ac 핸들 등록 및 초기 데이터 동기화
     */
    @Transactional
    public void registerSolvedacHandle(Long userId, String handle, String profileImageUrl) {
        log.info("Registering Solved.ac handle for user {}: {}", userId, handle);

        // 1. 사용자 기본 정보 조회 (User entity 먼저 조회)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        SolvedacUser userInfo = solvedacClient.getUserInfo(handle);

        // 2. 소유권 검증 (Bio Check)
        // 형식: "DashHub:{GitHubUsername}"
        String verificationCode = "DashHub:" + user.getUsername();
        String bio = userInfo.bio() != null ? userInfo.bio() : "";

        // 대소문자 무시 체크
        if (!bio.toLowerCase().contains(verificationCode.toLowerCase())) {
            throw new IllegalArgumentException("Solved.ac 소유권 인증 실패: Bio에 '" + verificationCode + "'를 포함해주세요.");
        }

        // 3. User 테이블 업데이트
        user.updateSolvedacProfile(handle, userInfo.tier(),
                userInfo.rating(), userInfo.classLevel(), userInfo.solvedCount());

        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            user.setAvatarUrl(profileImageUrl);
        }

        userRepository.update(user);

        // 4. 클래스 통계 동기화
        syncClassStats(userId, handle);

        // 5. 태그 통계 동기화
        syncTagStats(userId, handle);

        // 7. 전체 푼 문제 동기화 (문제 추천 제외용, 비동기 호출)
        problemSyncer.syncAllSolvedProblems(userId, handle);

        // 8. 학습 경로 캐시 무효화 (데이터 변경됨)
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
        // 1. 태그 통계 (문제 수) 조회
        TagStat tagStats = solvedacClient.getTagStats(handle);

        // 2. 태그 레이팅 조회 (Optional - 숨겨진 API라 실패할 수 있음)
        java.util.Map<String, Integer> ratingMapTemp = new java.util.HashMap<>();
        try {
            List<com.ssafy.dash.solvedac.domain.TagRating> tagRatings = solvedacClient.getTagRatings(handle);
            ratingMapTemp = tagRatings.stream()
                    .collect(java.util.stream.Collectors.toMap(
                            com.ssafy.dash.solvedac.domain.TagRating::tagKey,
                            com.ssafy.dash.solvedac.domain.TagRating::rating,
                            (existing, replacement) -> existing));
            log.info("Fetched {} tag ratings for user {}", tagRatings.size(), handle);
        } catch (Exception e) {
            log.warn("Failed to fetch tag ratings for {} (using 0 for all tags): {}", handle, e.getMessage());
        }
        final java.util.Map<String, Integer> ratingMap = ratingMapTemp;

        // 3. 병합 및 저장
        tagStats.items().forEach(item -> {
            String key = item.tag().key();
            int rating = ratingMap.getOrDefault(key, 0);

            UserTagStat entity = UserTagStat.create(
                    userId,
                    key,
                    item.total(),
                    item.solved(),
                    item.partial(),
                    item.tried(),
                    rating);
            tagStatRepository.save(entity);
        });

        log.info("Synced {} tag stats (with ratings) for user {}", tagStats.count(), userId);
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

    /**
     * Top 100 문제 평균 레벨 계산 (DB 저장 안함, On-the-fly)
     */
    public int fetchTop100AverageLevel(String handle) {
        try {
            List<Top100Problem> top100 = solvedacClient.getTop100(handle);

            if (top100.isEmpty()) {
                return 0;
            }

            // 평균 레벨 계산
            double avgLevel = top100.stream()
                    .mapToInt(Top100Problem::level)
                    .average()
                    .orElse(0.0);

            return (int) Math.round(avgLevel);
        } catch (Exception e) {
            log.warn("Failed to fetch top 100 stats for handle {}: {}", handle, e.getMessage());
            return 0;
        }
    }

}
