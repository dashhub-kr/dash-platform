package com.ssafy.dash.mockexam.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 모의고사 세션 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
public class MockExam {

    private Long id;
    private Long userId;
    private String examType; // MockExamType enum의 name()
    private String problems; // JSON 배열 "[1234, 5678, 9012]"
    private LocalDateTime startTime;
    private Integer solvedCount;
    private String status; // ExamStatus enum의 name()
    private LocalDateTime createdAt;

    private MockExam(Long userId, MockExamType examType, String problems, LocalDateTime startTime) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.examType = Objects.requireNonNull(examType, "examType must not be null").name();
        this.problems = problems;
        this.startTime = Objects.requireNonNull(startTime, "startTime must not be null");
        this.solvedCount = 0;
        this.status = ExamStatus.IN_PROGRESS.name();
        this.createdAt = LocalDateTime.now();
    }

    public static MockExam create(Long userId, MockExamType examType, String problems, LocalDateTime startTime) {
        return new MockExam(userId, examType, problems, startTime);
    }

    /**
     * 문제 풀이 완료 처리
     */
    public void markProblemSolved() {
        if (this.solvedCount == null) {
            this.solvedCount = 0;
        }
        this.solvedCount++;
    }

    /**
     * 시험 완료 처리
     */
    public void complete() {
        this.status = ExamStatus.COMPLETED.name();
    }

    /**
     * 시험 취소 처리
     */
    public void cancel() {
        this.status = ExamStatus.CANCELLED.name();
    }

    /**
     * 시간 초과 처리
     */
    public void timeout() {
        this.status = ExamStatus.TIMEOUT.name();
    }

    /**
     * 시험이 진행 중인지 확인
     */
    public boolean isInProgress() {
        return ExamStatus.IN_PROGRESS.name().equals(this.status);
    }

    /**
     * 제한 시간 초과 여부 확인
     */
    public boolean isTimeout(int timeLimitHours) {
        if (this.startTime == null) {
            return false;
        }
        LocalDateTime deadline = this.startTime.plusHours(timeLimitHours);
        return deadline.isBefore(LocalDateTime.now());
    }

    /**
     * MockExamType enum 반환
     */
    public MockExamType getExamTypeEnum() {
        return this.examType != null ? MockExamType.valueOf(this.examType) : null;
    }

    /**
     * ExamStatus enum 반환
     */
    public ExamStatus getStatusEnum() {
        return this.status != null ? ExamStatus.valueOf(this.status) : null;
    }
}
