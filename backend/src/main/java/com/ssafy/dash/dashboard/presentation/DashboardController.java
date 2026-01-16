package com.ssafy.dash.dashboard.presentation;

import com.ssafy.dash.algorithm.application.dto.result.AlgorithmRecordResult;
import com.ssafy.dash.dashboard.application.DashboardService;
import com.ssafy.dash.dashboard.application.dto.response.HeatmapItem;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    @GetMapping("/records")
    public ResponseEntity<List<AlgorithmRecordResult>> getDashboardRecords(
            @AuthenticationPrincipal CustomOAuth2User oauthUser,
            @RequestParam(required = false) Long studyId) {

        if (studyId != null) {
            boolean isAdmin = oauthUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            // 세션 캐시 대신 DB에서 최신 유저 정보 조회 (스터디 생성/해체 후에도 정상 동작)
            User freshUser = userRepository.findById(oauthUser.getUserId()).orElse(null);
            Long userStudyId = freshUser != null ? freshUser.getStudyId() : null;

            // Study ID가 같으면 허용, 다르면 관리자만 허용
            if (!isAdmin && (userStudyId == null || !userStudyId.equals(studyId))) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.ok(dashboardService.getAlgorithmRecordsByStudyId(studyId));
        }

        return ResponseEntity.ok(dashboardService.getAlgorithmRecords(oauthUser.getUserId()));
    }

    @GetMapping("/heatmap")
    public ResponseEntity<List<HeatmapItem>> getHeatmap(
            @AuthenticationPrincipal CustomOAuth2User oauthUser,
            @RequestParam(required = false) Long studyId) {

        if (studyId != null) {
            boolean isAdmin = oauthUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            // 세션 캐시 대신 DB에서 최신 유저 정보 조회 (스터디 생성/해체 후에도 정상 동작)
            User freshUser = userRepository.findById(oauthUser.getUserId()).orElse(null);
            Long userStudyId = freshUser != null ? freshUser.getStudyId() : null;

            if (!isAdmin && (userStudyId == null || !userStudyId.equals(studyId))) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.ok(dashboardService.getHeatmap(null, studyId));
        }

        return ResponseEntity.ok(dashboardService.getHeatmap(oauthUser.getUserId(), null));
    }
}
