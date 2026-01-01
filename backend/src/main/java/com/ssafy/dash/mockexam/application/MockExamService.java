package com.ssafy.dash.mockexam.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.algorithm.domain.AlgorithmRecordRepository;
import com.ssafy.dash.mockexam.domain.MockExam;
import com.ssafy.dash.mockexam.domain.MockExamProblemBank;
import com.ssafy.dash.mockexam.domain.MockExamRepository;
import com.ssafy.dash.mockexam.domain.MockExamType;

import com.ssafy.dash.mockexam.application.dto.result.ExamStatusResult;

@Service
@Transactional
public class MockExamService {

    private final MockExamRepository mockExamRepository;
    private final AlgorithmRecordRepository algorithmRecordRepository;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    public MockExamService(MockExamRepository mockExamRepository,
            AlgorithmRecordRepository algorithmRecordRepository,
            ObjectMapper objectMapper) {
        this.mockExamRepository = mockExamRepository;
        this.algorithmRecordRepository = algorithmRecordRepository;
        this.objectMapper = objectMapper;
    }

    public void startExam(Long userId, MockExamType examType) {
        // 이전 세션 타임아웃 체크
        checkAndTimeoutActiveExam(userId);

        // 이미 진행 중인 시험이 있는지 확인
        mockExamRepository.findActiveByUserId(userId).ifPresent(exam -> {
            throw new IllegalStateException("이미 시험이 진행 중입니다.");
        });

        // 문제 은행에서 해당 타입 문제 가져오기
        List<Integer> problemBank = getProblemBank(examType);

        // 이미 푼 문제 필터링
        List<Integer> unsolvedProblems = filterUnsolvedProblems(userId, problemBank);

        if (unsolvedProblems.size() < examType.getProblemCount()) {
            throw new IllegalStateException("풀지 않은 문제가 부족합니다. (필요: " + examType.getProblemCount() +
                    ", 남은 문제: " + unsolvedProblems.size() + ")");
        }

        // 랜덤으로 N개 선택
        List<Integer> selectedProblems = pickRandomProblems(unsolvedProblems, examType.getProblemCount());

        // MockExam 엔티티 생성 및 저장
        MockExam mockExam = MockExam.create(userId, examType, toJson(selectedProblems), LocalDateTime.now());
        mockExamRepository.save(mockExam);
    }

    public ExamStatusResult getExamStatus(Long userId) {
        checkAndTimeoutActiveExam(userId);

        MockExam mockExam = mockExamRepository.findActiveByUserId(userId).orElse(null);
        
        if (mockExam == null) {
            return new ExamStatusResult(null, null, null, List.of(), List.of(), null, 0, 0, 0);
        }

        List<Integer> problems = parseProblems(mockExam.getProblems());
        MockExamType examType = mockExam.getExamTypeEnum();

        // Check which problems are solved
        List<Integer> solvedProblems = problems.stream()
                .filter(p -> algorithmRecordRepository.existsSuccessfulSubmission(userId, String.valueOf(p)))
                .collect(Collectors.toList());

        return new ExamStatusResult(
                mockExam.getExamType(),
                examType != null ? examType.getDisplayName() : null,
                examType != null ? examType.getCategory() : null,
                problems,
                solvedProblems,
                mockExam.getStartTime(),
                examType != null ? examType.getTimeLimitHours() : 0,
                solvedProblems.size(),
                examType != null ? examType.getProblemCount() : 0);
    }

    public void verifyExam(Long userId, Integer solvedProblemId) {
        MockExam mockExam = mockExamRepository.findActiveByUserId(userId).orElse(null);
        
        if (mockExam == null) {
            return; // 시험 모드가 아님
        }

        // 타임아웃 체크
        MockExamType examType = mockExam.getExamTypeEnum();
        if (examType != null && mockExam.isTimeout(examType.getTimeLimitHours())) {
            mockExam.timeout();
            mockExamRepository.update(mockExam);
            return;
        }

        List<Integer> problems = parseProblems(mockExam.getProblems());
        if (problems.contains(solvedProblemId)) {
            // 이 문제에 대한 성공적인 제출이 있는지 확인
            if (!algorithmRecordRepository.existsSuccessfulSubmission(userId, String.valueOf(solvedProblemId))) {
                return; // 아직 정답이 아님
            }

            // 풀이 시간 계산 및 저장
            algorithmRecordRepository.findLatestSuccessfulByUserAndProblem(userId, String.valueOf(solvedProblemId))
                    .ifPresent(record -> {
                        LocalDateTime startTime = mockExam.getStartTime();
                        LocalDateTime endTime = record.getCreatedAt() != null ? record.getCreatedAt()
                                : LocalDateTime.now();
                        long elapsedSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
                        record.setElapsedTimeSeconds(elapsedSeconds);
                        algorithmRecordRepository.update(record);
                    });

            // 푼 문제 수 증가
            mockExam.markProblemSolved();

            // 모든 문제를 풀었는지 확인
            if (mockExam.getSolvedCount() >= examType.getProblemCount()) {
                mockExam.complete();
            }

            mockExamRepository.update(mockExam);
        }
    }

    public void cancelExam(Long userId) {
        mockExamRepository.findActiveByUserId(userId).ifPresent(mockExam -> {
            mockExam.cancel();
            mockExamRepository.update(mockExam);
        });
    }

    /**
     * 활성화된 시험의 타임아웃 여부를 확인하고 처리
     */
    private void checkAndTimeoutActiveExam(Long userId) {
        mockExamRepository.findActiveByUserId(userId).ifPresent(mockExam -> {
            MockExamType examType = mockExam.getExamTypeEnum();
            if (examType != null && mockExam.isTimeout(examType.getTimeLimitHours())) {
                mockExam.timeout();
                mockExamRepository.update(mockExam);
            }
        });
    }

    /**
     * 사용자가 현재 진행 중인 모의고사의 문제인지 확인
     */
    public boolean isActiveProblem(Long userId, Integer problemId) {
        return mockExamRepository.findActiveByUserId(userId)
                .map(mockExam -> {
                    List<Integer> problems = parseProblems(mockExam.getProblems());
                    return problems.contains(problemId);
                })
                .orElse(false);
    }

    private List<Integer> getProblemBank(MockExamType examType) {
        return switch (examType) {
            case IM -> MockExamProblemBank.IM_PROBLEMS;
            case A -> MockExamProblemBank.A_PROBLEMS;
            case B -> MockExamProblemBank.B_PROBLEMS;
            case SAMSUNG -> MockExamProblemBank.SAMSUNG_PROBLEMS;
            case KAKAO -> MockExamProblemBank.KAKAO_PROBLEMS;
        };
    }

    private List<Integer> filterUnsolvedProblems(Long userId, List<Integer> problems) {
        // 사용자가 이미 푼 문제 ID 목록 조회
        Set<String> solvedProblemNumbers = algorithmRecordRepository.findSolvedProblemNumbers(userId);

        return problems.stream()
                .filter(p -> !solvedProblemNumbers.contains(String.valueOf(p)))
                .collect(Collectors.toList());
    }

    private List<Integer> pickRandomProblems(List<Integer> problems, int count) {
        List<Integer> shuffled = new ArrayList<>(problems);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    private String toJson(List<Integer> problems) {
        try {
            return objectMapper.writeValueAsString(problems);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("문제 목록 직렬화 실패", e);
        }
    }

    private List<Integer> parseProblems(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Integer>>() {
            });
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

}
