package com.ssafy.dash.battle.application;

import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.battle.domain.*;
import com.ssafy.dash.defense.domain.DefenseProblemBank;
import com.ssafy.dash.mockexam.domain.MockExamProblemBank;
import com.ssafy.dash.mockexam.domain.MockExamType;
import com.ssafy.dash.notification.application.NotificationService;
import com.ssafy.dash.notification.domain.NotificationType;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BattleService {

    private final BattleRepository battleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AlgorithmRecordRepository algorithmRecordRepository;
    private final Random random = new Random();

    /**
     * 배틀 생성 (모의고사/디펜스)
     */
    public Battle createBattle(Long creatorId, Battle.BattleType type, String detailType, String problemIds,
            List<Long> inviteeIds) {
        Battle battle = Battle.create(creatorId, type, detailType, problemIds);
        battleRepository.save(battle);

        // 생성자를 참가자로 추가 (자동 수락)
        BattleParticipant creatorParticipant = BattleParticipant.create(battle.getId(), creatorId, true);
        battleRepository.saveParticipant(creatorParticipant);

        // 초대 대상자들을 참가자로 추가
        for (Long inviteeId : inviteeIds) {
            BattleParticipant invitee = BattleParticipant.create(battle.getId(), inviteeId, false);
            battleRepository.saveParticipant(invitee);

            // 알림 발송
            User creator = userRepository.findById(creatorId).orElse(null);
            if (creator != null) {
                String message = creator.getUsername() + "님이 " +
                        (type == Battle.BattleType.MOCK_EXAM ? "모의고사" : "디펜스") + " 배틀에 도전했어요!";
                notificationService.send(inviteeId, message, "/battle/" + battle.getId(),
                        NotificationType.BATTLE_INVITE, battle.getId());
            }
        }

        return battle;
    }

    /**
     * 배틀 초대 수락
     */
    public void acceptBattle(Long battleId, Long userId) {
        BattleParticipant participant = battleRepository.findParticipant(battleId, userId)
                .orElseThrow(() -> new IllegalArgumentException("참가 정보를 찾을 수 없습니다"));

        participant.accept();
        battleRepository.updateParticipant(participant);

        // 모든 참가자가 수락했는지 확인
        checkAndStartBattle(battleId);
    }

    /**
     * 배틀 초대 거절
     */
    public void declineBattle(Long battleId, Long userId) {
        BattleParticipant participant = battleRepository.findParticipant(battleId, userId)
                .orElseThrow(() -> new IllegalArgumentException("참가 정보를 찾을 수 없습니다"));

        participant.decline();
        battleRepository.updateParticipant(participant);
    }

    /**
     * 배틀 시작 (수동)
     */
    public void startBattle(Long battleId) {
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new IllegalArgumentException("배틀을 찾을 수 없습니다"));

        // 문제 선택 (참가자들이 풀지 않은 문제 중에서)
        List<BattleParticipant> participants = battleRepository.findParticipantsByBattleId(battleId);
        List<Long> participantUserIds = participants.stream()
                .filter(p -> p.getStatus() == BattleParticipant.ParticipantStatus.ACCEPTED)
                .map(BattleParticipant::getUserId)
                .toList();

        List<Integer> selectedProblems = selectProblemsForBattle(battle.getType(), battle.getDetailType(),
                participantUserIds);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String problemIdsJson = objectMapper.writeValueAsString(selectedProblems);
            battle.setProblemIds(problemIdsJson);
        } catch (Exception e) {
            battle.setProblemIds("[]");
        }

        battle.start();
        battleRepository.update(battle);

        // 모든 수락한 참가자 시작 상태로 변경
        for (BattleParticipant p : participants) {
            if (p.getStatus() == BattleParticipant.ParticipantStatus.ACCEPTED) {
                p.start();
                battleRepository.updateParticipant(p);
            }
        }
    }

    /**
     * 배틀용 문제 선택 (참가자들이 모두 풀지 않은 문제 중에서 랜덤 선택)
     */
    private List<Integer> selectProblemsForBattle(Battle.BattleType type, String detailType,
            List<Long> participantUserIds) {
        // 모든 참가자가 푼 문제 수집
        Set<String> allSolvedProblems = new HashSet<>();
        for (Long userId : participantUserIds) {
            allSolvedProblems.addAll(algorithmRecordRepository.findSolvedProblemNumbers(userId));
        }

        List<Integer> problemBank;
        int problemCount;

        if (type == Battle.BattleType.MOCK_EXAM) {
            if (detailType != null) {
                try {
                    MockExamType mockType = MockExamType.valueOf(detailType);
                    problemCount = mockType.getProblemCount();
                    problemBank = switch (mockType) {
                        case IM -> MockExamProblemBank.IM_PROBLEMS;
                        case A -> MockExamProblemBank.A_PROBLEMS;
                        case B -> MockExamProblemBank.B_PROBLEMS;
                        case SAMSUNG -> MockExamProblemBank.SAMSUNG_PROBLEMS;
                        case KAKAO -> MockExamProblemBank.KAKAO_PROBLEMS;
                        default -> DefenseProblemBank.GOLD_PROBLEMS;
                    };
                } catch (IllegalArgumentException e) {
                    // 잘못된 타입인 경우 기본값 처리
                    problemBank = DefenseProblemBank.GOLD_PROBLEMS;
                    problemCount = 2; // 기본 2문제
                }
            } else {
                // MOCK_EXAM인데 detailType이 없으면 기본값 (A형)
                problemBank = MockExamProblemBank.A_PROBLEMS;
                problemCount = 2;
            }
        } else {
            // 디펜스
            if ("GOLD".equalsIgnoreCase(detailType)) {
                problemBank = DefenseProblemBank.GOLD_PROBLEMS;
            } else {
                problemBank = DefenseProblemBank.SILVER_PROBLEMS;
            }
            problemCount = 1; // 디펜스는 1문제
        }

        // 아무도 풀지 않은 문제 필터링
        List<Integer> unsolvedProblems = problemBank.stream()
                .filter(p -> !allSolvedProblems.contains(String.valueOf(p)))
                .collect(Collectors.toList());

        if (unsolvedProblems.size() <= problemCount) {
            return unsolvedProblems;
        }

        // 랜덤 선택
        Collections.shuffle(unsolvedProblems, random);
        return unsolvedProblems.subList(0, problemCount);
    }

    /**
     * 배틀 조회
     */
    @Transactional(readOnly = true)
    public Battle getBattle(Long battleId) {
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new IllegalArgumentException("배틀을 찾을 수 없습니다"));

        battle.setParticipants(battleRepository.findParticipantsByBattleId(battleId));
        return battle;
    }

    /**
     * 내 배틀 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Battle> getMyBattles(Long userId) {
        return battleRepository.findByUserId(userId);
    }

    /**
     * 대기 중인 배틀 초대 조회
     */
    @Transactional(readOnly = true)
    public List<Battle> getPendingBattles(Long userId) {
        return battleRepository.findPendingBattlesForUser(userId);
    }

    private void checkAndStartBattle(Long battleId) {
        List<BattleParticipant> participants = battleRepository.findParticipantsByBattleId(battleId);

        boolean allAccepted = participants.stream()
                .allMatch(p -> p.getStatus() == BattleParticipant.ParticipantStatus.ACCEPTED ||
                        p.getStatus() == BattleParticipant.ParticipantStatus.DECLINED);

        long acceptedCount = participants.stream()
                .filter(p -> p.getStatus() == BattleParticipant.ParticipantStatus.ACCEPTED)
                .count();

        // 최소 2명 이상 수락 시 자동 시작
        if (allAccepted && acceptedCount >= 2) {
            startBattle(battleId);
        }
    }

    /**
     * 결과 제출
     */
    public void submitResult(Long battleId, Long userId, int score, int problemsSolved, long totalTimeSeconds) {
        BattleParticipant participant = battleRepository.findParticipant(battleId, userId)
                .orElseThrow(() -> new IllegalArgumentException("참가 정보를 찾을 수 없습니다"));

        participant.complete(score, problemsSolved, totalTimeSeconds);
        battleRepository.updateParticipant(participant);

        // 모든 참가자가 완료했는지 확인
        checkAndCompleteBattle(battleId);
    }

    private void checkAndCompleteBattle(Long battleId) {
        List<BattleParticipant> participants = battleRepository.findParticipantsByBattleId(battleId);

        boolean allCompleted = participants.stream()
                .filter(p -> p.getStatus() != BattleParticipant.ParticipantStatus.DECLINED)
                .allMatch(p -> p.getStatus() == BattleParticipant.ParticipantStatus.COMPLETED);

        if (allCompleted) {
            Battle battle = battleRepository.findById(battleId)
                    .orElseThrow(() -> new IllegalArgumentException("배틀을 찾을 수 없습니다"));
            battle.complete();
            battleRepository.update(battle);
        }
    }

    /**
     * 사용자가 진행 중인 배틀의 문제인지 확인
     */
    public boolean isActiveBattleProblem(Long userId, Integer problemId) {
        // 사용자가 참가 중인 진행 중(IN_PROGRESS) 배틀 조회
        List<Battle> battles = battleRepository.findByUserId(userId);

        for (Battle battle : battles) {
            if (battle.getStatus() != Battle.BattleStatus.IN_PROGRESS)
                continue;

            // 해당 배틀에서 사용자가 IN_PROGRESS 상태인지 확인
            BattleParticipant participant = battleRepository.findParticipant(battle.getId(), userId).orElse(null);
            if (participant == null || participant.getStatus() != BattleParticipant.ParticipantStatus.IN_PROGRESS)
                continue;

            // 문제 목록에 포함되어 있는지 확인
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Integer> problems = objectMapper.readValue(
                        battle.getProblemIds(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class));
                if (problems.contains(problemId)) {
                    return true;
                }
            } catch (Exception e) {
                // 파싱 실패 시 무시
            }
        }
        return false;
    }

    /**
     * 배틀 문제 풀이 검증 및 점수 업데이트
     */
    public void verifyBattleProblem(Long userId, Integer solvedProblemId) {
        // 사용자가 참가 중인 진행 중(IN_PROGRESS) 배틀 조회
        List<Battle> battles = battleRepository.findByUserId(userId);

        for (Battle battle : battles) {
            if (battle.getStatus() != Battle.BattleStatus.IN_PROGRESS)
                continue;

            // 해당 배틀에서 사용자가 IN_PROGRESS 상태인지 확인 (비관적 락 사용)
            BattleParticipant participant = battleRepository.findParticipantForUpdate(battle.getId(), userId)
                    .orElse(null);
            if (participant == null || participant.getStatus() != BattleParticipant.ParticipantStatus.IN_PROGRESS)
                continue;

            // 문제 목록에 포함되어 있는지 확인
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Integer> problems = objectMapper.readValue(
                        battle.getProblemIds(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class));

                if (problems.contains(solvedProblemId)) {
                    // 이미 해결한 문제인지 확인
                    List<Integer> solvedList = new ArrayList<>();
                    if (participant.getSolvedProblemIds() != null && !participant.getSolvedProblemIds().equals("[]")) {
                        solvedList = objectMapper.readValue(
                                participant.getSolvedProblemIds(),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class));
                    }

                    if (solvedList.contains(solvedProblemId)) {
                        continue; // 이미 점수 획득함
                    }

                    // 실제 정답 제출이 있는지 확인
                    if (!algorithmRecordRepository.existsSuccessfulSubmission(userId,
                            String.valueOf(solvedProblemId))) {
                        continue; // 아직 정답이 아님
                    }

                    // 점수 업데이트 (문제당 100점)
                    participant.setScore((participant.getScore() != null ? participant.getScore() : 0) + 100);
                    participant.setProblemsSolved(
                            (participant.getProblemsSolved() != null ? participant.getProblemsSolved() : 0) + 1);

                    // 해결한 문제 목록 업데이트
                    solvedList.add(solvedProblemId);
                    participant.setSolvedProblemIds(objectMapper.writeValueAsString(solvedList));

                    // 모든 문제를 풀었는지 확인
                    if (participant.getProblemsSolved() >= problems.size()) {
                        long totalTime = java.time.Duration.between(
                                participant.getStartedAt(),
                                java.time.LocalDateTime.now()).getSeconds();
                        participant.complete(participant.getScore(), participant.getProblemsSolved(), totalTime);
                    }

                    battleRepository.updateParticipant(participant);

                    // 배틀 완료 여부 확인
                    checkAndCompleteBattle(battle.getId());
                }
            } catch (Exception e) {
                // 파싱 실패 시 무시
            }
        }
    }
}
