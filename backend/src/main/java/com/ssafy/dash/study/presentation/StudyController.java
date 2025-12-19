package com.ssafy.dash.study.presentation;

import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;
import com.ssafy.dash.study.application.StudyService;
import com.ssafy.dash.study.domain.Study;
import com.ssafy.dash.study.presentation.dto.CreateStudyRequest;
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
    private final com.ssafy.dash.acorn.application.AcornService acornService;

    @Operation(summary = "스터디 목록 조회", description = "참여 가능한 스터디 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<Study>> getStudies() {
        return ResponseEntity.ok(studyService.findAll());
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
}
