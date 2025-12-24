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
import com.ssafy.dash.mockexam.domain.MockExamProblemBank;
import com.ssafy.dash.mockexam.domain.MockExamType;
import com.ssafy.dash.user.domain.User;
import com.ssafy.dash.user.domain.UserRepository;
import com.ssafy.dash.user.domain.exception.UserNotFoundException;

@Service
@Transactional
public class MockExamService {

    private final UserRepository userRepository;
    private final AlgorithmRecordRepository algorithmRecordRepository;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    public MockExamService(UserRepository userRepository,
            AlgorithmRecordRepository algorithmRecordRepository,
            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.algorithmRecordRepository = algorithmRecordRepository;
        this.objectMapper = objectMapper;
    }

    public void startExam(Long userId, MockExamType examType) {
        User user = getUser(userId);

        // 이전 세션 타임아웃 체크
        checkTimeout(user);

        if (user.getExamStartTime() != null) {
            throw new IllegalStateException("이미 시험이 진행 중입니다.");
        }

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

        user.setExamType(examType.name());
        user.setExamProblems(toJson(selectedProblems));
        user.setExamStartTime(LocalDateTime.now());
        user.setExamSolvedCount(0);

        userRepository.update(user);
    }

    public ExamStatusResult getExamStatus(Long userId) {
        User user = getUser(userId);
        checkTimeout(user);

        List<Integer> problems = parseProblems(user.getExamProblems());
        MockExamType examType = user.getExamType() != null
                ? MockExamType.valueOf(user.getExamType())
                : null;

        // Check which problems are solved
        List<Integer> solvedProblems = problems.stream()
                .filter(p -> algorithmRecordRepository.existsSuccessfulSubmission(userId, String.valueOf(p)))
                .collect(Collectors.toList());

        return new ExamStatusResult(
                user.getExamType(),
                examType != null ? examType.getDisplayName() : null,
                examType != null ? examType.getCategory() : null,
                problems,
                solvedProblems,
                user.getExamStartTime(),
                examType != null ? examType.getTimeLimitHours() : 0,
                solvedProblems.size(),
                examType != null ? examType.getProblemCount() : 0);
    }

    public void verifyExam(Long userId, Integer solvedProblemId) {
        User user = getUser(userId);

        if (user.getExamStartTime() == null) {
            return; // 시험 모드가 아님
        }

        checkTimeout(user);
        if (user.getExamStartTime() == null) {
            return; // 방금 타임아웃됨
        }

        List<Integer> problems = parseProblems(user.getExamProblems());
        if (problems.contains(solvedProblemId)) {
            // 이 문제에 대한 성공적인 제출이 있는지 확인
            if (!algorithmRecordRepository.existsSuccessfulSubmission(userId, String.valueOf(solvedProblemId))) {
                return; // 아직 정답이 아님
            }

            // 풀이 시간 계산 및 저장
            algorithmRecordRepository.findLatestSuccessfulByUserAndProblem(userId, String.valueOf(solvedProblemId))
                    .ifPresent(record -> {
                        LocalDateTime startTime = user.getExamStartTime();
                        LocalDateTime endTime = record.getCreatedAt() != null ? record.getCreatedAt()
                                : LocalDateTime.now();
                        long elapsedSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
                        record.setElapsedTimeSeconds(elapsedSeconds);
                        algorithmRecordRepository.update(record);
                    });

            // 푼 문제 수 증가
            int newSolvedCount = (user.getExamSolvedCount() != null ? user.getExamSolvedCount() : 0) + 1;
            user.setExamSolvedCount(newSolvedCount);

            MockExamType examType = MockExamType.valueOf(user.getExamType());

            // 모든 문제를 풀었거나 시험이 완료된 경우
            if (newSolvedCount >= examType.getProblemCount()) {
                // 시험 완료 - 상태 초기화
                clearExamState(user);
            }

            userRepository.update(user);
        }
    }

    public void cancelExam(Long userId) {
        User user = getUser(userId);
        if (user.getExamStartTime() != null) {
            clearExamState(user);
            userRepository.update(user);
        }
    }

    private void checkTimeout(User user) {
        if (user.getExamStartTime() != null && user.getExamType() != null) {
            MockExamType examType = MockExamType.valueOf(user.getExamType());
            LocalDateTime deadline = user.getExamStartTime().plusHours(examType.getTimeLimitHours());

            if (deadline.isBefore(LocalDateTime.now())) {
                // 시간 초과
                clearExamState(user);
                userRepository.update(user);
            }
        }
    }

    private void clearExamState(User user) {
        user.setExamType(null);
        user.setExamProblems(null);
        user.setExamStartTime(null);
        user.setExamSolvedCount(null);
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

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
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

    public record ExamStatusResult(
            String examType,
            String displayName,
            String category,
            List<Integer> problems,
            List<Integer> solvedProblems,
            LocalDateTime startTime,
            int timeLimitHours,
            int solvedCount,
            int totalCount) {
    }
}
