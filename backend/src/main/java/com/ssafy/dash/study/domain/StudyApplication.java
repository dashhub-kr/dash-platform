package com.ssafy.dash.study.domain;

import com.ssafy.dash.user.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class StudyApplication {
    
    private Long id;
    private Long studyId;
    private Long userId; // 신청자 ID
    private String message; // 가입 신청 메시지
    private ApplicationStatus status; // PENDING, APPROVED, REJECTED
    private LocalDateTime createdAt;
    
    // 조인을 위한 필드 (MyBatis ResultMap용)
    private User applicant;

    public enum ApplicationStatus {
        PENDING, APPROVED, REJECTED
    }

    public StudyApplication(Long studyId, Long userId, String message) {
        this.studyId = studyId;
        this.userId = userId;
        this.message = message;
        this.status = ApplicationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public static StudyApplication create(Long studyId, Long userId, String message) {
        return new StudyApplication(studyId, userId, message);
    }
    
    public void approve() {
        this.status = ApplicationStatus.APPROVED;
    }
    
    public void reject() {
        this.status = ApplicationStatus.REJECTED;
    }
}
