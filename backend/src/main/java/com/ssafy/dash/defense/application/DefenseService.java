package com.ssafy.dash.defense.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.dash.defense.domain.DefenseProblemBank;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;

@Service
@Transactional
public class DefenseService {

    private final UserRepository userRepository;
    private final Random random = new Random();

    public DefenseService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void startDefense(Long userId, String type) {
        User user = getUser(userId);
        
        // 이전 세션이 존재한다면 타임아웃 체크
        checkTimeout(user);

        if (user.getDefenseStartTime() != null) {
            // 이미 진행 중
           throw new IllegalStateException("이미 디펜스가 진행 중입니다.");
        }

        Integer problemId;
        if ("GOLD".equalsIgnoreCase(type)) {
            problemId = pickRandomProblem(DefenseProblemBank.GOLD_PROBLEMS);
        } else {
            problemId = pickRandomProblem(DefenseProblemBank.SILVER_PROBLEMS);
        }

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
            user.getMaxGoldStreak()
        );
    }

    public void verifyDefense(Long userId, Integer solvedProblemId) {
        User user = getUser(userId);
        
        if (user.getDefenseStartTime() == null) {
            return; // 디펜스 모드가 아님
        }

        checkTimeout(user);
        if (user.getDefenseStartTime() == null) {
            return; // 방금 타임아웃됨
        }

        if (user.getDefenseProblemId().equals(solvedProblemId)) {
            // 성공!
            if ("GOLD".equals(user.getDefenseType())) {
                user.setGoldStreak(user.getGoldStreak() + 1);
                if (user.getGoldStreak() > user.getMaxGoldStreak()) {
                    user.setMaxGoldStreak(user.getGoldStreak());
                }
            } else {
                user.setSilverStreak(user.getSilverStreak() + 1);
                if (user.getSilverStreak() > user.getMaxSilverStreak()) {
                    user.setMaxSilverStreak(user.getSilverStreak());
                }
            }
            
            // 다음 디펜스를 위해 현재 상태 초기화
            user.setDefenseProblemId(null);
            user.setDefenseStartTime(null);
            user.setDefenseType(null);
            
            userRepository.update(user);
        }
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

    public record DefenseStatusResult(
        String defenseType,
        Integer defenseProblemId,
        LocalDateTime defenseStartTime,
        Integer silverStreak,
        Integer goldStreak,
        Integer maxSilverStreak,
        Integer maxGoldStreak
    ) {}
}
