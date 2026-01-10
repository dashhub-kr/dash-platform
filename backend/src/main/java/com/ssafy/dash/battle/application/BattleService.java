package com.ssafy.dash.battle.application;

import com.ssafy.dash.battle.domain.*;
import com.ssafy.dash.notification.application.NotificationService;
import com.ssafy.dash.notification.domain.NotificationType;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BattleService {

    private final BattleRepository battleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 배틀 생성 (모의고사/디펜스)
     */
    public Battle createBattle(Long creatorId, Battle.BattleType type, String problemIds, List<Long> inviteeIds) {
        Battle battle = Battle.create(creatorId, type, problemIds);
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
                notificationService.notify(inviteeId, NotificationType.BATTLE_INVITE, message, "/social");
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

        battle.start();
        battleRepository.update(battle);

        // 모든 수락한 참가자 시작 상태로 변경
        List<BattleParticipant> participants = battleRepository.findParticipantsByBattleId(battleId);
        for (BattleParticipant p : participants) {
            if (p.getStatus() == BattleParticipant.ParticipantStatus.ACCEPTED) {
                p.start();
                battleRepository.updateParticipant(p);
            }
        }
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
}
