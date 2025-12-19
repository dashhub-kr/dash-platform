package com.ssafy.dash.analytics.presentation;

import com.ssafy.dash.analytics.application.dto.FamilyScoreDto;
import com.ssafy.dash.analytics.application.dto.TagCoverageDto;
import com.ssafy.dash.analytics.application.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Algorithm Analytics", description = "Methods for analyzing user algorithm statistics")
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get User Family Scores", description = "Returns radar chart data for user.")
    @GetMapping("/users/{userId}/radar")
    public ResponseEntity<List<FamilyScoreDto>> getRadarChartData(@PathVariable Long userId) {
        return ResponseEntity.ok(analyticsService.getUserFamilyScores(userId));
    }

    @Operation(summary = "Get User Core Tag Coverage", description = "Returns statistics on core tag coverage.")
    @GetMapping("/users/{userId}/coverage")
    public ResponseEntity<TagCoverageDto> getCoreTagCoverage(@PathVariable Long userId) {
        return ResponseEntity.ok(analyticsService.getUserCoreCoverage(userId));
    }
}
