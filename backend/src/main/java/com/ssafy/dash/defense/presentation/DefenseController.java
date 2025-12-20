package com.ssafy.dash.defense.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.defense.application.DefenseService;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/defense")
public class DefenseController {

    private final DefenseService defenseService;

    public DefenseController(DefenseService defenseService) {
        this.defenseService = defenseService;
    }

    @GetMapping("/status")
    public ResponseEntity<DefenseStatusResponse> getStatus(@AuthenticationPrincipal CustomOAuth2User userDetails) {
        var result = defenseService.getDefenseStatus(userDetails.getUserId());
        return ResponseEntity.ok(new DefenseStatusResponse(
            result.defenseType(),
            result.defenseProblemId(),
            result.defenseStartTime(),
            result.silverStreak(),
            result.goldStreak(),
            result.maxSilverStreak(),
            result.maxGoldStreak()
        ));
    }

    @PostMapping("/start")
    public ResponseEntity<Void> startDefense(
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @RequestBody StartDefenseRequest request) {
        defenseService.startDefense(userDetails.getUserId(), request.type());
        return ResponseEntity.ok().build();
    }

    public record DefenseStatusResponse(
        String defenseType,
        Integer defenseProblemId,
        LocalDateTime defenseStartTime,
        Integer silverStreak,
        Integer goldStreak,
        Integer maxSilverStreak,
        Integer maxGoldStreak
    ) {}

    public record StartDefenseRequest(String type) {}
}
