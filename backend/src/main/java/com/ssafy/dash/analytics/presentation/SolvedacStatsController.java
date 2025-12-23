package com.ssafy.dash.analytics.presentation;

import com.ssafy.dash.analytics.application.SolvedacSyncService;
import com.ssafy.dash.analytics.application.dto.RegisterHandleRequest;
import com.ssafy.dash.analytics.domain.UserClassStat;
import com.ssafy.dash.analytics.domain.UserClassStatRepository;
import com.ssafy.dash.analytics.domain.UserTagStat;
import com.ssafy.dash.analytics.domain.UserTagStatRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Solved.ac 사용자 통계 API 컨트롤러
 */
@Tag(name = "Solved.ac Statistics", description = "Solved.ac 사용자 통계 API")
@RestController
@RequestMapping("/api/users/{userId}/solvedac")
@RequiredArgsConstructor
public class SolvedacStatsController {

    private final SolvedacSyncService syncService;
    private final UserClassStatRepository classStatRepository;
    private final UserTagStatRepository tagStatRepository;

    /**
     * Solved.ac 핸들 등록 및 초기 데이터 동기화
     */
    @Operation(summary = "Solved.ac 핸들 등록", description = "사용자의 Solved.ac 핸들을 등록하고 통계 데이터를 동기화합니다")
    @PostMapping
    public ResponseEntity<Map<String, String>> registerHandle(
            @PathVariable Long userId,
            @RequestBody @Valid RegisterHandleRequest request) {
        syncService.registerSolvedacHandle(userId, request.getHandle(), request.getProfileImageUrl());
        return ResponseEntity.ok(Map.of(
                "message", "Solved.ac 핸들이 성공적으로 등록되었습니다",
                "handle", request.getHandle()));
    }

    /**
     * 클래스별 통계 조회
     */
    @Operation(summary = "클래스별 통계 조회", description = "사용자의 Solved.ac 클래스별 문제 풀이 통계를 조회합니다")
    @GetMapping("/stats/classes")
    public ResponseEntity<List<UserClassStat>> getClassStats(@PathVariable Long userId) {
        List<UserClassStat> stats = classStatRepository.findByUserId(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 태그별 통계 조회
     */
    @Operation(summary = "태그별 통계 조회", description = "사용자의 Solved.ac 태그별 문제 풀이 통계를 조회합니다")
    @GetMapping("/stats/tags")
    public ResponseEntity<List<UserTagStat>> getTagStats(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        List<UserTagStat> stats = tagStatRepository.findByUserId(userId);
        return ResponseEntity.ok(limit != null && stats.size() > limit
                ? stats.subList(0, limit)
                : stats);
    }

    /**
     * 통계 재동기화
     */
    @Operation(summary = "통계 재동기화", description = "사용자의 Solved.ac 통계를 최신 데이터로 재동기화합니다")
    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> resyncStats(@PathVariable Long userId) {
        syncService.resyncAllStats(userId);
        return ResponseEntity.ok(Map.of("message", "통계가 성공적으로 동기화되었습니다"));
    }
}
