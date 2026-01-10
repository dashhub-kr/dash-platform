package com.ssafy.dash.battle.infrastructure;

import com.ssafy.dash.battle.domain.Battle;
import com.ssafy.dash.battle.domain.BattleParticipant;
import com.ssafy.dash.battle.domain.BattleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BattleRepositoryImpl implements BattleRepository {

    private final BattleMapper battleMapper;

    @Override
    public void save(Battle battle) {
        battleMapper.insertBattle(battle);
    }

    @Override
    public void update(Battle battle) {
        battleMapper.updateBattle(battle);
    }

    @Override
    public Optional<Battle> findById(Long id) {
        return battleMapper.selectById(id);
    }

    @Override
    public List<Battle> findByUserId(Long userId) {
        return battleMapper.selectByUserId(userId);
    }

    @Override
    public List<Battle> findPendingBattlesForUser(Long userId) {
        return battleMapper.selectPendingBattlesForUser(userId);
    }

    @Override
    public void saveParticipant(BattleParticipant participant) {
        battleMapper.insertParticipant(participant);
    }

    @Override
    public void updateParticipant(BattleParticipant participant) {
        battleMapper.updateParticipant(participant);
    }

    @Override
    public List<BattleParticipant> findParticipantsByBattleId(Long battleId) {
        return battleMapper.selectParticipantsByBattleId(battleId);
    }

    @Override
    public Optional<BattleParticipant> findParticipant(Long battleId, Long userId) {
        return battleMapper.selectParticipant(battleId, userId);
    }
}
