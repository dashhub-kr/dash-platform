package com.ssafy.dash.battle.presentation;

import com.ssafy.dash.battle.application.BattleService;
import com.ssafy.dash.battle.domain.Battle;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/battle")
@RequiredArgsConstructor
public class BattleController {

    private final BattleService battleService;

    /**
     * 배틀 생성
     */
    @PostMapping
    public ResponseEntity<Battle> createBattle(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal,
            @RequestBody Map<String, Object> payload) {

        String type = (String) payload.get("type");
        String detailType = (String) payload.get("detailType");
        Object problemIdsObj = payload.get("problemIds");
        String problemIds = problemIdsObj != null ? (String) problemIdsObj : "[]";
        @SuppressWarnings("unchecked")
        List<Number> inviteeIds = (List<Number>) payload.get("inviteeIds");

        if (type == null || inviteeIds == null || inviteeIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Battle.BattleType battleType = Battle.BattleType.valueOf(type);
        List<Long> invitees = inviteeIds.stream().map(Number::longValue).toList();

        Battle battle = battleService.createBattle(
                userPrincipal.getUserId(), battleType, detailType, problemIds, invitees);

        return ResponseEntity.ok(battle);
    }

    /**
     * 배틀 조회
     */
    @GetMapping("/{battleId}")
    public ResponseEntity<Battle> getBattle(@PathVariable Long battleId) {
        return ResponseEntity.ok(battleService.getBattle(battleId));
    }

    /**
     * 내 배틀 목록
     */
    @GetMapping("/my")
    public ResponseEntity<List<Battle>> getMyBattles(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal) {
        return ResponseEntity.ok(battleService.getMyBattles(userPrincipal.getUserId()));
    }

    /**
     * 대기 중인 배틀 초대
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Battle>> getPendingBattles(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal) {
        return ResponseEntity.ok(battleService.getPendingBattles(userPrincipal.getUserId()));
    }

    /**
     * 배틀 초대 수락
     */
    @PostMapping("/{battleId}/accept")
    public ResponseEntity<Void> acceptBattle(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal,
            @PathVariable Long battleId) {
        battleService.acceptBattle(battleId, userPrincipal.getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * 배틀 초대 거절
     */
    @PostMapping("/{battleId}/decline")
    public ResponseEntity<Void> declineBattle(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal,
            @PathVariable Long battleId) {
        battleService.declineBattle(battleId, userPrincipal.getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * 배틀 시작
     */
    @PostMapping("/{battleId}/start")
    public ResponseEntity<Void> startBattle(@PathVariable Long battleId) {
        battleService.startBattle(battleId);
        return ResponseEntity.ok().build();
    }

    /**
     * 배틀 상태 조회 (폴링용)
     */
    @GetMapping("/{battleId}/status")
    public ResponseEntity<Battle> getBattleStatus(@PathVariable Long battleId) {
        return ResponseEntity.ok(battleService.getBattle(battleId));
    }

    /**
     * 결과 제출
     */
    @PostMapping("/{battleId}/submit")
    public ResponseEntity<Void> submitResult(
            @AuthenticationPrincipal CustomOAuth2User userPrincipal,
            @PathVariable Long battleId,
            @RequestBody Map<String, Object> payload) {

        int score = ((Number) payload.getOrDefault("score", 0)).intValue();
        int problemsSolved = ((Number) payload.getOrDefault("problemsSolved", 0)).intValue();
        long totalTimeSeconds = ((Number) payload.getOrDefault("totalTimeSeconds", 0L)).longValue();

        battleService.submitResult(battleId, userPrincipal.getUserId(), score, problemsSolved, totalTimeSeconds);
        return ResponseEntity.ok().build();
    }
}
