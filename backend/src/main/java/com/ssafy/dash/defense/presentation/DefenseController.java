package com.ssafy.dash.defense.presentation;

import com.ssafy.dash.defense.application.DefenseService;
import com.ssafy.dash.defense.presentation.dto.request.StartDefenseRequest;
import com.ssafy.dash.defense.presentation.dto.response.DefenseStatusResponse;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // Inner records removed
}
