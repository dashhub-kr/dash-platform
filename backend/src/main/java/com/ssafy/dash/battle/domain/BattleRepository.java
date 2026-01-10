package com.ssafy.dash.battle.domain;

import java.util.List;
import java.util.Optional;

public interface BattleRepository {
    void save(Battle battle);

    void update(Battle battle);

    Optional<Battle> findById(Long id);

    List<Battle> findByUserId(Long userId);

    List<Battle> findPendingBattlesForUser(Long userId);

    void saveParticipant(BattleParticipant participant);

    void updateParticipant(BattleParticipant participant);

    List<BattleParticipant> findParticipantsByBattleId(Long battleId);

    Optional<BattleParticipant> findParticipant(Long battleId, Long userId);
}
