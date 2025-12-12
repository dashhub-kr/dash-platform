package com.ssafy.dash.analytics.application;

import com.ssafy.dash.analytics.domain.UserStatsSnapshot;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.infrastructure.persistence.UserStatsSnapshotMapper;
import com.ssafy.dash.analytics.infrastructure.persistence.UserTagStatMapper;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 통계 스냅샷 서비스
 * - 일별 스냅샷 생성
 * - 성장 추세 분석용 데이터 수집
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsSnapshotService {

    private final UserStatsSnapshotMapper snapshotMapper;
    private final UserTagStatMapper tagStatMapper;
    private final UserRepository userRepository;

    /**
     * 특정 사용자의 스냅샷 생성
     */
    @Transactional
    public void createSnapshot(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 현재 태그 통계에서 총 푼 문제 수 계산
        List<UserTagStat> tagStats = tagStatMapper.findByUserId(userId);
        int totalSolved = tagStats.stream()
                .mapToInt(UserTagStat::getSolved)
                .max()
                .orElse(0);

        // 스냅샷 생성
        UserStatsSnapshot snapshot = UserStatsSnapshot.create(
                userId,
                LocalDate.now(),
                totalSolved,
                user.getSolvedacTier(),
                user.getSolvedacRating(),
                user.getSolvedacClass());

        snapshotMapper.insert(snapshot);
        log.info("Created stats snapshot for user {}: {} problems solved", userId, totalSolved);
    }

    /**
     * 스냅샷 조회 (특정 날짜)
     */
    public UserStatsSnapshot getSnapshot(Long userId, LocalDate date) {
        return snapshotMapper.findByUserIdAndDate(userId, date).orElse(null);
    }

    /**
     * 최근 스냅샷 조회
     */
    public UserStatsSnapshot getLatestSnapshot(Long userId) {
        return snapshotMapper.findLatestByUserId(userId).orElse(null);
    }

    /**
     * 기간별 스냅샷 목록 조회
     */
    public List<UserStatsSnapshot> getSnapshotsBetween(Long userId, LocalDate startDate, LocalDate endDate) {
        return snapshotMapper.findByUserIdBetweenDates(userId, startDate, endDate);
    }

    /**
     * 오래된 스냅샷 정리 (90일 이상)
     */
    @Transactional
    public void cleanupOldSnapshots() {
        LocalDate cutoffDate = LocalDate.now().minusDays(90);
        snapshotMapper.deleteOlderThan(cutoffDate);
        log.info("Cleaned up snapshots older than {}", cutoffDate);
    }

    /**
     * 매일 자정에 모든 활성 사용자의 스냅샷 생성 (스케줄링)
     * 주의: 프로덕션에서는 배치 처리로 대체 권장
     */
    // @Scheduled(cron = "0 0 0 * * *")
    // public void createDailySnapshots() {
    // log.info("Starting daily snapshots creation...");
    // // 모든 사용자 조회 및 스냅샷 생성 로직
    // }
}
