package com.ssafy.dash.defense.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.defense.domain.DefenseProblemBank;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;

import com.ssafy.dash.defense.application.dto.result.DefenseStatusResult;

@Service
@Transactional
public class DefenseService {

    private final UserRepository userRepository;
    private final AlgorithmRecordRepository algorithmRecordRepository;
    private final Random random = new Random();

    public DefenseService(UserRepository userRepository, AlgorithmRecordRepository algorithmRecordRepository) {
        this.userRepository = userRepository;
        this.algorithmRecordRepository = algorithmRecordRepository;
    }

    public void startDefense(Long userId, String type) {
        User user = getUser(userId);

        // 이전 세션이 존재한다면 타임아웃 체크
        checkTimeout(user);

        if (user.getDefenseStartTime() != null) {
            // 이미 진행 중
            throw new IllegalStateException("이미 디펜스가 진행 중입니다.");
        }

        List<Integer> problemBank;
        if ("GOLD".equalsIgnoreCase(type)) {
            problemBank = DefenseProblemBank.GOLD_PROBLEMS;
        } else {
            problemBank = DefenseProblemBank.SILVER_PROBLEMS;
        }

        // 이미 푼 문제 필터링
        List<Integer> unsolvedProblems = filterUnsolvedProblems(userId, problemBank);

        if (unsolvedProblems.isEmpty()) {
            throw new IllegalStateException("풀지 않은 문제가 없습니다. 모든 문제를 완료했습니다!");
        }

        Integer problemId = pickRandomProblem(unsolvedProblems);

        user.setDefenseType(type.toUpperCase());
        user.setDefenseProblemId(problemId);
        user.setDefenseStartTime(LocalDateTime.now());

        userRepository.update(user);
        // 참고: 연승은 실패하거나 타임아웃 될 때까지 유지됨
    }

    public DefenseStatusResult getDefenseStatus(Long userId) {
        User user = getUser(userId);
        checkTimeout(user);

        // 타임아웃 체크는 사용자 상태를 변경할 수 있으므로 저장해야 함.
        // 하지만 getDefenseStatus는 주로 조회용이므로,
        // 상태 변경(타임아웃)이 발생했을 때만 lazy하게 업데이트.

        return new DefenseStatusResult(
                user.getDefenseType(),
                user.getDefenseProblemId(),
                user.getDefenseStartTime(),
                user.getSilverStreak(),
                user.getGoldStreak(),
                user.getMaxSilverStreak(),
                user.getMaxGoldStreak());
    }

    public Integer verifyDefense(Long userId, Integer solvedProblemId) {
        User user = getUser(userId);

        if (user.getDefenseStartTime() == null) {
            return null; // 디펜스 모드가 아님
        }

        checkTimeout(user);
        if (user.getDefenseStartTime() == null) {
            return null; // 방금 타임아웃됨
        }

        if (user.getDefenseProblemId().equals(solvedProblemId)) {
            // 실제 정답 제출이 있는지 확인 (runtime_ms, memory_kb가 있는 기록)
            if (!algorithmRecordRepository.existsSuccessfulSubmission(userId, String.valueOf(solvedProblemId))) {
                return null; // 성공적인 제출 기록이 없음 (오답 또는 컴파일 에러)
            }

            Integer currentStreak = 0;

            // 성공! 풀이 시간 계산 및 저장
            algorithmRecordRepository.findLatestSuccessfulByUserAndProblem(userId, String.valueOf(solvedProblemId))
                    .ifPresent(record -> {
                        LocalDateTime startTime = user.getDefenseStartTime();
                        LocalDateTime endTime = record.getCreatedAt() != null ? record.getCreatedAt()
                                : LocalDateTime.now();
                        long elapsedSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
                        record.setElapsedTimeSeconds(elapsedSeconds);
                        algorithmRecordRepository.update(record);
                    });

            if ("GOLD".equals(user.getDefenseType())) {
                user.setGoldStreak(user.getGoldStreak() + 1);
                currentStreak = user.getGoldStreak();
                if (user.getGoldStreak() > user.getMaxGoldStreak()) {
                    user.setMaxGoldStreak(user.getGoldStreak());
                }
            } else {
                user.setSilverStreak(user.getSilverStreak() + 1);
                currentStreak = user.getSilverStreak();
                if (user.getSilverStreak() > user.getMaxSilverStreak()) {
                    user.setMaxSilverStreak(user.getSilverStreak());
                }
            }

            // 다음 디펜스를 위해 현재 상태 초기화
            user.setDefenseProblemId(null);
            user.setDefenseStartTime(null);
            user.setDefenseType(null);

            userRepository.update(user);
            return currentStreak;
        }
        return null;
    }

    private void checkTimeout(User user) {
        if (user.getDefenseStartTime() != null) {
            if (user.getDefenseStartTime().plusHours(1).isBefore(LocalDateTime.now())) {
                // 시간 초과! 연승 초기화
                user.setSilverStreak(0);
                user.setGoldStreak(0);
                user.setDefenseProblemId(null);
                user.setDefenseStartTime(null);
                user.setDefenseType(null);
                userRepository.update(user);
            }
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Integer pickRandomProblem(List<Integer> problems) {
        return problems.get(random.nextInt(problems.size()));
    }

    private List<Integer> filterUnsolvedProblems(Long userId, List<Integer> problems) {
        // 사용자가 이미 푼 문제 ID 목록 조회
        java.util.Set<String> solvedProblemNumbers = algorithmRecordRepository.findSolvedProblemNumbers(userId);

        return problems.stream()
                .filter(p -> !solvedProblemNumbers.contains(String.valueOf(p)))
                .collect(java.util.stream.Collectors.toList());
    }


}
