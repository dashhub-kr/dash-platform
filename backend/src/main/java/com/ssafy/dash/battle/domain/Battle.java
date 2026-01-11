package com.ssafy.dash.battle.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Battle {

    private Long id;
    private BattleType type;
    private String detailType;
    private Long creatorId;
    private BattleStatus status;
    private String problemIds; // JSON string
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // Joined fields
    private List<BattleParticipant> participants;

    public enum BattleType {
        MOCK_EXAM, DEFENSE
    }

    public enum BattleStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public static Battle create(Long creatorId, BattleType type, String detailType, String problemIds) {
        Battle battle = new Battle();
        battle.creatorId = creatorId;
        battle.type = type;
        battle.detailType = detailType;
        battle.problemIds = problemIds;
        battle.status = BattleStatus.PENDING;
        battle.createdAt = LocalDateTime.now();
        return battle;
    }

    public void start() {
        this.status = BattleStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = BattleStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = BattleStatus.CANCELLED;
    }
}
