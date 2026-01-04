package com.ssafy.dash.dashboard.presentation;

import com.ssafy.dash.algorithm.application.dto.result.AlgorithmRecordResult;
import com.ssafy.dash.dashboard.application.DashboardService;
import com.ssafy.dash.dashboard.application.dto.response.HeatmapItem;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/records")
    public ResponseEntity<List<AlgorithmRecordResult>> getDashboardRecords(
            @AuthenticationPrincipal CustomOAuth2User oauthUser,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long studyId) {
        
        if (studyId != null) {
            // Check Admin role
             if (!oauthUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                 return ResponseEntity.status(403).build();
             }
             return ResponseEntity.ok(dashboardService.getAlgorithmRecordsByStudyId(studyId));
        }

        return ResponseEntity.ok(dashboardService.getAlgorithmRecords(oauthUser.getUserId()));
    }

    @GetMapping("/heatmap")
    public ResponseEntity<List<HeatmapItem>> getHeatmap(
            @AuthenticationPrincipal CustomOAuth2User oauthUser,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long studyId) {
        
        if (studyId != null) {
             if (!oauthUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                 return ResponseEntity.status(403).build();
             }
             return ResponseEntity.ok(dashboardService.getHeatmap(null, studyId));
        }

        return ResponseEntity.ok(dashboardService.getHeatmap(oauthUser.getUserId(), null));
    }
}
