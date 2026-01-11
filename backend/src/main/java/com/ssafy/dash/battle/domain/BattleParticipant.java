package com.ssafy.dash.battle.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BattleParticipant {

    private Long id;
    private Long battleId;
    private Long userId;
    private ParticipantStatus status;
    private Integer score;
    private Integer problemsSolved;
    private Long totalTimeSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // Joined fields
    private String userName;
    private String userAvatar;
    private Integer userTier;

    public enum ParticipantStatus {
        INVITED, ACCEPTED, DECLINED, IN_PROGRESS, COMPLETED, QUEUED
    }

    public static BattleParticipant create(Long battleId, Long userId, boolean isCreator) {
        BattleParticipant participant = new BattleParticipant();
        participant.battleId = battleId;
        participant.userId = userId;
        participant.status = isCreator ? ParticipantStatus.ACCEPTED : ParticipantStatus.INVITED;
        participant.score = 0;
        participant.problemsSolved = 0;
        participant.totalTimeSeconds = 0L;
        return participant;
    }

    public void accept() {
        this.status = ParticipantStatus.ACCEPTED;
    }

    public void decline() {
        this.status = ParticipantStatus.DECLINED;
    }

    public void start() {
        this.status = ParticipantStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public void complete(int score, int problemsSolved, long totalTimeSeconds) {
        this.status = ParticipantStatus.COMPLETED;
        this.score = score;
        this.problemsSolved = problemsSolved;
        this.totalTimeSeconds = totalTimeSeconds;
        this.completedAt = LocalDateTime.now();
    }
}
