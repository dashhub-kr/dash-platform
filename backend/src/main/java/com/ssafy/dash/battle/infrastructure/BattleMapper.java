package com.ssafy.dash.battle.infrastructure;

import com.ssafy.dash.battle.domain.Battle;
import com.ssafy.dash.battle.domain.BattleParticipant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BattleMapper {
    void insertBattle(Battle battle);

    void updateBattle(Battle battle);

    Optional<Battle> selectById(Long id);

    List<Battle> selectByUserId(Long userId);

    List<Battle> selectPendingBattlesForUser(Long userId);

    void saveParticipant(BattleParticipant participant);

    void updateParticipant(BattleParticipant participant);

    List<BattleParticipant> findParticipantsByBattleId(Long battleId);

    Optional<BattleParticipant> findParticipant(@Param("battleId") Long battleId, @Param("userId") Long userId);

    Optional<BattleParticipant> findParticipantForUpdate(@Param("battleId") Long battleId,
            @Param("userId") Long userId);
}
