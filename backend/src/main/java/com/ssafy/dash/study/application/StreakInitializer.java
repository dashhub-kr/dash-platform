package com.ssafy.dash.study.application;

import com.ssafy.dash.study.domain.Study;
import com.ssafy.dash.study.domain.StudyRepository;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 서버 시작 시 기존 스터디의 streak 초기화 (1회성)
 * streak과 streakUpdatedAt이 NULL인 스터디만 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StreakInitializer {

    private final StudyRepository studyRepository;
    private final AlgorithmRecordRepository algorithmRecordRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeStreaks() {
        log.info("Streak 초기화 시작...");

        List<Study> studies = studyRepository.findAll();
        int initialized = 0;

        for (Study study : studies) {
            // 데이터 불일치 복구를 위해 모든 스터디에 대해 강제 재계산 수행
            // if (study.getStreakUpdatedAt() != null) {
            // continue;
            // }

            // 활동 날짜 조회 및 streak 계산
            int streak = calculateStreakFromHistory(study.getId());

            // 오늘 또는 어제 활동이 있으면 해당 날짜로 설정
            LocalDate updatedAt = determineLastActivityDate(study.getId());

            if (updatedAt != null) {
                study.setStreak(streak);
                study.setStreakUpdatedAt(updatedAt);
                studyRepository.update(study);
                initialized++;
                log.debug("Study {} 초기화: streak={}, updatedAt={}", study.getId(), streak, updatedAt);
            }
        }

        log.info("Streak 초기화 완료: {}개 스터디 처리", initialized);
    }

    private int calculateStreakFromHistory(Long studyId) {
        List<String> activityDates = algorithmRecordRepository.findActivityDatesByStudyId(studyId);
        if (activityDates == null || activityDates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        Set<LocalDate> activitySet = activityDates.stream()
                .map(LocalDate::parse)
                .collect(Collectors.toSet());

        int streak = 0;
        LocalDate checkDate = today;

        // 오늘부터 시작해서 연속으로 활동이 있는 날을 카운트
        while (activitySet.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        // 오늘 활동이 없으면 어제부터 체크
        if (streak == 0) {
            checkDate = today.minusDays(1);
            while (activitySet.contains(checkDate)) {
                streak++;
                checkDate = checkDate.minusDays(1);
            }
        }

        return streak;
    }

    private LocalDate determineLastActivityDate(Long studyId) {
        List<String> activityDates = algorithmRecordRepository.findActivityDatesByStudyId(studyId);
        if (activityDates == null || activityDates.isEmpty()) {
            return null;
        }

        // 가장 최근 활동 날짜 반환 (이미 DESC 정렬되어 있음)
        return LocalDate.parse(activityDates.get(0));
    }
}
