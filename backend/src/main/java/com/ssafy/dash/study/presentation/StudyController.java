package com.ssafy.dash.study.presentation;

import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import com.ssafy.dash.study.application.StudyAnalysisService;
import com.ssafy.dash.study.application.StudyMissionService;
import com.ssafy.dash.study.application.StudyService;
import com.ssafy.dash.study.domain.Study;
import com.ssafy.dash.study.presentation.dto.CreateStudyRequest;
import com.ssafy.dash.study.presentation.dto.response.StudyListResponse;
import com.ssafy.dash.study.presentation.dto.response.StudyStatsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Study", description = "스터디 관리 API")
@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final StudyAnalysisService studyAnalysisService;
    private final StudyMissionService studyMissionService;
    private final com.ssafy.dash.acorn.application.AcornService acornService;

    @Operation(summary = "스터디 목록 조회", description = "참여 가능한 스터디 목록을 조회합니다. 평균 티어, 멤버 수, 총 풀이 수 포함.")
    @GetMapping
    public ResponseEntity<List<StudyListResponse>> getStudies() {
        List<StudyListResponse> response = studyService.findAll().stream()
                .map(StudyListResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스터디 상세 조회", description = "특정 스터디의 정보를 조회합니다.")
    @GetMapping("/{studyId}")
    public ResponseEntity<Study> getStudy(@PathVariable Long studyId) {
        return ResponseEntity.of(studyService.findStudyById(studyId));
    }

    @Operation(summary = "스터디 생성", description = "새로운 스터디를 생성하고 자동으로 가입합니다.")
    @PostMapping
    public ResponseEntity<Study> createStudy(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @RequestBody CreateStudyRequest request) {
        if (principal instanceof CustomOAuth2User customUser) {
            Study study = studyService.createStudy(customUser.getUserId(), request.getName());
            return ResponseEntity.ok(study);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "스터디 가입", description = "기존 스터디에 가입합니다.")
    @PostMapping("/{studyId}/join")
    public ResponseEntity<Void> joinStudy(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long studyId) {
        if (principal instanceof CustomOAuth2User customUser) {
            studyService.joinStudy(customUser.getUserId(), studyId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "스터디 문제 풀이 통계 조회", description = "스터디의 티어별 문제 해결 수를 조회합니다.")
    @GetMapping("/{studyId}/stats")
    public ResponseEntity<StudyStatsResponse> getStudyStats(
            @PathVariable Long studyId) {
        return ResponseEntity.ok(StudyStatsResponse.from(studyService.getStudyStats(studyId)));
    }

    @Operation(summary = "도토리 내역 조회", description = "스터디의 도토리 적립/사용 내역을 조회합니다.")
    @GetMapping("/{studyId}/acorns")
    public ResponseEntity<List<com.ssafy.dash.acorn.domain.AcornLog>> getAcornLogs(
            @PathVariable Long studyId) {
        return ResponseEntity.ok(acornService.getLogs(studyId));
    }

    @Operation(summary = "스터디 팀 분석", description = "멤버별 태그 통계와 팀 평균을 조회합니다. 육각차트 표시용.")
    @GetMapping("/{studyId}/analysis")
    public ResponseEntity<StudyAnalysisService.StudyAnalysisResult> getStudyAnalysis(
            @PathVariable Long studyId) {
        return ResponseEntity.ok(studyAnalysisService.analyzeStudy(studyId));
    }

    @Operation(summary = "팀 패밀리 통계", description = "레이더차트용 팀 패밀리별 통계를 조회합니다.")
    @GetMapping("/{studyId}/family-stats")
    public ResponseEntity<List<StudyAnalysisService.TeamFamilyStat>> getTeamFamilyStats(
            @PathVariable Long studyId) {
        return ResponseEntity.ok(studyAnalysisService.getTeamFamilyStats(studyId));
    }

    @Operation(summary = "커리큘럼 문제 추천", description = "팀 약점 기반 문제를 추천합니다.")
    @GetMapping("/{studyId}/curriculum")
    public ResponseEntity<List<com.ssafy.dash.problem.domain.ProblemRecommendationResponse>> getCurriculum(
            @PathVariable Long studyId) {
        return ResponseEntity.ok(studyAnalysisService.getCurriculumProblems(studyId));
    }

    // === 주차별 미션 API ===

    @Operation(summary = "미션 목록 조회", description = "스터디의 주차별 미션 목록을 조회합니다.")
    @GetMapping("/{studyId}/missions")
    public ResponseEntity<List<StudyMissionService.MissionWithProgress>> getMissions(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long studyId) {
        if (principal instanceof CustomOAuth2User customUser) {
            return ResponseEntity.ok(studyMissionService.getMissions(studyId, customUser.getUserId()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "미션 생성 (직접 입력)", description = "문제 번호를 직접 입력하여 미션을 생성합니다.")
    @PostMapping("/{studyId}/missions")
    public ResponseEntity<Void> createMission(
            @PathVariable Long studyId,
            @RequestBody CreateMissionRequest request) {
        studyMissionService.createMissionManual(
                studyId, request.week(), request.title(), request.problemIds(), request.deadline());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "미션 진행 현황", description = "미션별 멤버들의 제출 현황을 조회합니다.")
    @GetMapping("/{studyId}/missions/{missionId}/progress")
    public ResponseEntity<List<StudyMissionService.MemberProgress>> getMissionProgress(
            @PathVariable Long studyId,
            @PathVariable Long missionId) {
        return ResponseEntity.ok(studyMissionService.getMissionProgress(missionId));
    }

    public record CreateMissionRequest(
            Integer week,
            String title,
            List<Integer> problemIds,
            java.time.LocalDate deadline) {
    }
}
