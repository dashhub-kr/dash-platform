package com.ssafy.dash.acorn.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AcornLog {
    
    private Long id;
    private Long studyId;
    private Long userId;
    private Integer amount;
    private String reason;
    private LocalDateTime createdAt;
    
    // Optional: for joining
    private String username;

    public AcornLog(Long studyId, Long userId, Integer amount, String reason) {
        this.studyId = studyId;
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    public static AcornLog create(Long studyId, Long userId, Integer amount, String reason) {
        return new AcornLog(studyId, userId, amount, reason);
    }
}
