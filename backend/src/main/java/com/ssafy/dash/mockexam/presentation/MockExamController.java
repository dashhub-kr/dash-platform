package com.ssafy.dash.mockexam.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.dash.mockexam.application.MockExamService;
import com.ssafy.dash.mockexam.domain.MockExamType;
import com.ssafy.dash.mockexam.presentation.dto.request.StartExamRequest;
import com.ssafy.dash.mockexam.presentation.dto.response.ExamStatusResponse;
import com.ssafy.dash.mockexam.presentation.dto.response.ExamTypeInfoResponse;
import com.ssafy.dash.oauth.presentation.security.CustomOAuth2User;

@RestController
@RequestMapping("/api/mockexam")
public class MockExamController {

    private final MockExamService mockExamService;

    public MockExamController(MockExamService mockExamService) {
        this.mockExamService = mockExamService;
    }

    @GetMapping("/status")
    public ResponseEntity<ExamStatusResponse> getStatus(@AuthenticationPrincipal CustomOAuth2User userDetails) {
        var result = mockExamService.getExamStatus(userDetails.getUserId());
        return ResponseEntity.ok(new ExamStatusResponse(
                result.examType(),
                result.displayName(),
                result.category(),
                result.problems(),
                result.solvedProblems(),
                result.startTime(),
                result.timeLimitHours(),
                result.solvedCount(),
                result.totalCount()));
    }

    @PostMapping("/start")
    public ResponseEntity<Void> startExam(
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @RequestBody StartExamRequest request) {
        MockExamType examType = MockExamType.valueOf(request.examType().toUpperCase());
        mockExamService.startExam(userDetails.getUserId(), examType);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelExam(@AuthenticationPrincipal CustomOAuth2User userDetails) {
        mockExamService.cancelExam(userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/types")
    public ResponseEntity<List<ExamTypeInfoResponse>> getExamTypes() {
        List<ExamTypeInfoResponse> types = List.of(
                new ExamTypeInfoResponse("IM", "IM 모의고사", "모의고사", 1, 2, "IM 등급 대비"),
                new ExamTypeInfoResponse("A", "A형 모의고사", "모의고사", 2, 2, "A형 대비 (1문제=A, 2문제=A+)"),
                new ExamTypeInfoResponse("B", "B형 모의고사", "모의고사", 1, 4, "B형 대비"),
                new ExamTypeInfoResponse("SAMSUNG", "삼성 코딩테스트", "코딩테스트", 3, 2, "삼성 기출 문제"),
                new ExamTypeInfoResponse("KAKAO", "카카오 코딩테스트", "코딩테스트", 3, 2, "카카오 문제"));
        return ResponseEntity.ok(types);
    }
}
