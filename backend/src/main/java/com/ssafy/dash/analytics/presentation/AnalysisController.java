package com.ssafy.dash.analytics.presentation;

import com.ssafy.dash.analytics.application.AnalyticsService;
import com.ssafy.dash.analytics.application.BalanceAnalysisService;
import com.ssafy.dash.analytics.application.DifficultyAnalysisService;
import com.ssafy.dash.analytics.application.GrowthAnalysisService;
import com.ssafy.dash.analytics.application.RuleBasedLearningPathService;
import com.ssafy.dash.analytics.application.StatsSnapshotService;
import com.ssafy.dash.analytics.application.UserSkillAnalysisService;
import com.ssafy.dash.analytics.application.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 스킬 분석 API
 */
@Tag(name = "User Skill Analysis", description = "사용자 알고리즘 실력 분석 API")
@RestController
@RequestMapping("/api/users/{userId}/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final UserSkillAnalysisService analysisService;
    private final BalanceAnalysisService balanceService;
    private final DifficultyAnalysisService difficultyService;
    private final RuleBasedLearningPathService learningPathService;
    private final GrowthAnalysisService growthService;
    private final StatsSnapshotService snapshotService;
    private final AnalyticsService analyticsService;

    // === Phase 1: 기본 분석 ===

    @Operation(summary = "레이더 차트 데이터", description = "사용자의 패밀리별 점수 분포를 반환합니다.")
    @GetMapping("/radar")
    public ResponseEntity<List<FamilyScoreDto>> getRadar(@PathVariable Long userId) {
        return ResponseEntity.ok(analyticsService.getUserFamilyScores(userId));
    }

    @Operation(summary = "코어 태그 커버리지", description = "코어 태그(S/A 티어) 달성 현황을 반환합니다.")
    @GetMapping("/coverage")
    public ResponseEntity<TagCoverageDto> getCoverage(@PathVariable Long userId) {
        return ResponseEntity.ok(analyticsService.getUserCoreCoverage(userId));
    }

    @Operation(summary = "종합 스킬 요약", description = "사용자의 종합적인 알고리즘 실력 분석 결과를 반환합니다.")
    @GetMapping("/summary")
    public ResponseEntity<SkillSummaryDto> getSummary(@PathVariable Long userId) {
        SkillSummaryDto summary = analysisService.getSkillSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "강점 태그 목록", description = "사용자가 가장 잘하는 태그 TOP N을 반환합니다.")
    @GetMapping("/strengths")
    public ResponseEntity<List<TagStrengthDto>> getStrengths(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        List<TagStrengthDto> strengths = analysisService.getStrengthTags(userId, limit);
        return ResponseEntity.ok(strengths);
    }

    @Operation(summary = "약점 태그 목록", description = "보완이 필요한 약점 태그와 추천 정보를 반환합니다.")
    @GetMapping("/weaknesses")
    public ResponseEntity<List<TagWeaknessDto>> getWeaknesses(@PathVariable Long userId) {
        List<TagWeaknessDto> weaknesses = analysisService.getWeaknessTags(userId);
        return ResponseEntity.ok(weaknesses);
    }

    @Operation(summary = "추천 학습 태그", description = "아직 경험하지 않은 중요 태그를 추천합니다.")
    @GetMapping("/recommended-tags")
    public ResponseEntity<List<String>> getRecommendedTags(@PathVariable Long userId) {
        List<String> recommendedTags = analysisService.getRecommendedTags(userId);
        return ResponseEntity.ok(recommendedTags);
    }

    // === Phase 2: 비교/추천 분석 ===

    @Operation(summary = "학습 밸런스 분석", description = "태그별 학습 균형도를 분석합니다.")
    @GetMapping("/balance")
    public ResponseEntity<BalanceAnalysisDto> getBalance(@PathVariable Long userId) {
        BalanceAnalysisDto balance = balanceService.analyzeBalance(userId);
        return ResponseEntity.ok(balance);
    }

    @Operation(summary = "난이도 분포 분석", description = "클래스별 문제 풀이 현황과 진행 상태를 분석합니다.")
    @GetMapping("/difficulty")
    public ResponseEntity<DifficultyAnalysisDto> getDifficulty(@PathVariable Long userId) {
        DifficultyAnalysisDto difficulty = difficultyService.analyzeDifficulty(userId);
        return ResponseEntity.ok(difficulty);
    }

    @Operation(summary = "학습 경로 추천", description = "개인 맞춤형 학습 경로와 추천 문제를 제공합니다.")
    @GetMapping("/learning-path")
    public ResponseEntity<LearningPathDto> getLearningPath(@PathVariable Long userId) {
        LearningPathDto learningPath = learningPathService.recommendLearningPath(userId);
        return ResponseEntity.ok(learningPath);
    }

    // === Phase 3: 고급 분석 ===

    @Operation(summary = "성장 추세 분석", description = "지정 기간 동안의 문제 풀이 성장 추세를 분석합니다.")
    @GetMapping("/growth")
    public ResponseEntity<GrowthTrendDto> getGrowth(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        GrowthTrendDto growth = growthService.analyzeGrowthTrend(userId, days);
        return ResponseEntity.ok(growth);
    }

    @Operation(summary = "스냅샷 생성", description = "현재 통계의 스냅샷을 생성합니다 (성장 추세 분석용).")
    @PostMapping("/snapshot")
    public ResponseEntity<String> createSnapshot(@PathVariable Long userId) {
        snapshotService.createSnapshot(userId);
        return ResponseEntity.ok("스냅샷이 생성되었습니다.");
    }
}
