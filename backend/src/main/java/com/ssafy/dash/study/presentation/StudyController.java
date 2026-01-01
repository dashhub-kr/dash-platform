package com.ssafy.dash.study.presentation;

import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import com.ssafy.dash.study.application.StudyAnalysisService;
import com.ssafy.dash.study.application.StudyMissionService;
import com.ssafy.dash.study.application.StudyService;
import com.ssafy.dash.study.domain.Study;
import com.ssafy.dash.study.presentation.dto.CreateStudyRequest;
import com.ssafy.dash.study.presentation.dto.request.ApplyStudyRequest;
import com.ssafy.dash.study.presentation.dto.request.CreateMissionRequest;
import com.ssafy.dash.study.presentation.dto.request.AddMissionProblemsRequest;
import com.ssafy.dash.study.presentation.dto.request.UpdateMissionRequest;
import com.ssafy.dash.study.presentation.dto.request.UpdateMissionStatusRequest;
import com.ssafy.dash.study.presentation.dto.response.StudyListResponse;
import com.ssafy.dash.study.presentation.dto.response.StudyStatsResponse;
import com.ssafy.dash.study.application.dto.result.StudyAnalysisResult;
import com.ssafy.dash.study.application.dto.result.TeamFamilyStatResult;
import com.ssafy.dash.study.application.dto.result.MissionWithProgressResult;
import com.ssafy.dash.study.application.dto.result.MemberProgressResult;

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
            Study study = studyService.createStudy(customUser.getUserId(), request.getName(), request.getDescription(),
                    request.getVisibility());
            return ResponseEntity.ok(study);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "스터디 가입 신청", description = "스터디에 가입 신청을 보냅니다.")
    @PostMapping("/{studyId}/apply")
    public ResponseEntity<Void> applyForStudy(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long studyId,
            @RequestBody ApplyStudyRequest request) {
        if (principal instanceof CustomOAuth2User customUser) {
            studyService.applyForStudy(customUser.getUserId(), studyId, request.message());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "가입 신청 승인", description = "스터디장이 가입 신청을 승인합니다.")
    @PostMapping("/applications/{applicationId}/approve")
    public ResponseEntity<Void> approveApplication(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long applicationId) {
        if (principal instanceof CustomOAuth2User customUser) {
            studyService.approveApplication(customUser.getUserId(), applicationId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "스터디 가입 신청 거절", description = "스터디장이 가입 신청을 거절합니다.")
    @PostMapping("/applications/{applicationId}/reject")
    public ResponseEntity<Void> rejectApplication(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long applicationId) {
        if (principal instanceof CustomOAuth2User customUser) {
            studyService.rejectApplication(customUser.getUserId(), applicationId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "가입 신청 목록 조회", description = "스터디장이 대기 중인 가입 신청 목록을 조회합니다.")
    @GetMapping("/{studyId}/applications")
    public ResponseEntity<List<com.ssafy.dash.study.domain.StudyApplication>> getApplications(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long studyId) {
        if (principal instanceof CustomOAuth2User customUser) {
            return ResponseEntity.ok(studyService.getPendingApplications(customUser.getUserId(), studyId));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "내 가입 신청 상태 조회", description = "자신의 현재 대기 중인 가입 신청을 조회합니다.")
    @GetMapping("/applications/me")
    public ResponseEntity<com.ssafy.dash.study.domain.StudyApplication> getMyApplication(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {
        if (principal instanceof CustomOAuth2User customUser) {
            return studyService.getMyPendingApplication(customUser.getUserId())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "가입 신청 취소", description = "자신의 가입 신청을 취소합니다.")
    @DeleteMapping("/applications/{applicationId}")
    public ResponseEntity<Void> cancelApplication(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long applicationId) {
        if (principal instanceof CustomOAuth2User customUser) {
            studyService.cancelApplication(customUser.getUserId(), applicationId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "스터디 탈퇴", description = "현재 소속된 스터디에서 탈퇴합니다.")
    @PostMapping("/leave")
    public ResponseEntity<Void> leaveStudy(
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {
        if (principal instanceof CustomOAuth2User customUser) {
            studyService.leaveStudy(customUser.getUserId());
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
    public ResponseEntity<StudyAnalysisResult> getStudyAnalysis(
            @PathVariable Long studyId) {
        return ResponseEntity.ok(studyAnalysisService.analyzeStudy(studyId));
    }

    @Operation(summary = "팀 패밀리 통계", description = "레이더차트용 팀 패밀리별 통계를 조회합니다.")
    @GetMapping("/{studyId}/family-stats")
    public ResponseEntity<List<TeamFamilyStatResult>> getTeamFamilyStats(
            @PathVariable Long studyId) {
        return ResponseEntity.ok(studyAnalysisService.getTeamFamilyStats(studyId));
    }

    @Operation(summary = "커리큘럼 문제 추천", description = "팀 약점 기반 문제를 추천합니다.")
    @GetMapping("/{studyId}/curriculum")
    public ResponseEntity<List<com.ssafy.dash.problem.presentation.dto.response.ProblemRecommendationResponse>> getCurriculum(
            @PathVariable Long studyId) {
        return ResponseEntity.ok(studyAnalysisService.getCurriculumProblems(studyId));
    }

    // === 주차별 미션 API ===

    @Operation(summary = "미션 목록 조회", description = "스터디의 주차별 미션 목록을 조회합니다.")
    @GetMapping("/{studyId}/missions")
    public ResponseEntity<List<MissionWithProgressResult>> getMissions(
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
    public ResponseEntity<List<MemberProgressResult>> getMissionProgress(
            @PathVariable Long studyId,
            @PathVariable Long missionId) {
        return ResponseEntity.ok(studyMissionService.getMissionProgress(missionId));
    }

    @Operation(summary = "미션 문제 추가", description = "기존 미션에 문제를 추가합니다.")
    @PutMapping("/{studyId}/missions/{missionId}/problems")
    public ResponseEntity<Void> addMissionProblems(
            @PathVariable Long studyId,
            @PathVariable Long missionId,
            @RequestBody AddMissionProblemsRequest request) {
        studyMissionService.addProblemsToMission(missionId, request.problemIds());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "미션 정보 수정", description = "미션의 제목이나 마감일을 수정합니다.")
    @PatchMapping("/{studyId}/missions/{missionId}")
    public ResponseEntity<Void> updateMission(
            @PathVariable Long studyId,
            @PathVariable Long missionId,
            @RequestBody UpdateMissionRequest request) {
        studyMissionService.updateMission(missionId, request.title(), request.deadline());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "미션 문제 삭제", description = "미션에서 특정 문제를 삭제합니다.")
    @DeleteMapping("/{studyId}/missions/{missionId}/problems/{problemId}")
    public ResponseEntity<Void> deleteMissionProblem(
            @PathVariable Long studyId,
            @PathVariable Long missionId,
            @PathVariable Integer problemId) {
        studyMissionService.removeProblemFromMission(missionId, problemId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "미션 삭제", description = "미션을 삭제합니다.")
    @DeleteMapping("/{studyId}/missions/{missionId}")
    public ResponseEntity<Void> deleteMission(
            @PathVariable Long studyId,
            @PathVariable Long missionId) {
        studyMissionService.deleteMission(missionId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "SOS 요청 토글", description = "특정 문제에 대한 SOS 상태를 토글합니다.")
    @PostMapping("/{studyId}/missions/{missionId}/problems/{problemId}/sos")
    public ResponseEntity<Void> toggleSos(
            @PathVariable Long studyId,
            @PathVariable Long missionId,
            @PathVariable Integer problemId,
            @Parameter(hidden = true) @AuthenticationPrincipal OAuth2User principal) {
        if (principal instanceof CustomOAuth2User customUser) {
            studyMissionService.toggleSos(missionId, problemId, customUser.getUserId());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "미션 상태 강제 변경 (완료 처리)", description = "스터디장이 미션을 강제로 완료(종료) 처리합니다.")
    @PatchMapping("/{studyId}/missions/{missionId}/status")
    public ResponseEntity<Void> updateMissionStatus(
            @PathVariable Long studyId,
            @PathVariable Long missionId,
            @RequestBody UpdateMissionStatusRequest request) {
        if ("COMPLETED".equals(request.status())) {
            studyMissionService.completeMission(missionId);
        }
        return ResponseEntity.ok().build();
    }
}
