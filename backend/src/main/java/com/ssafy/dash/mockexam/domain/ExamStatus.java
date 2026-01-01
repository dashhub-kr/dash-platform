package com.ssafy.dash.mockexam.domain;

/**
 * 모의고사 상태
 */
public enum ExamStatus {
    IN_PROGRESS,  // 진행 중
    COMPLETED,    // 완료
    TIMEOUT,      // 시간 초과
    CANCELLED     // 취소
}
