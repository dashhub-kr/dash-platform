package com.ssafy.dash.study.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 스터디 주차별 미션
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudyMission {

    private Long id;
    private Long studyId;
    private Integer week;
    private String title;
    private String problemIds; // JSON 배열 "[1234, 5678]"
    private String sourceType; // "AI_RECOMMENDED" 또는 "MANUAL"
    private LocalDate deadline;
    private MissionStatus status;
    private LocalDateTime createdAt;

    public static StudyMission create(Long studyId, Integer week, String title,
            String problemIds, String sourceType, LocalDate deadline) {
        return new StudyMission(
                null, studyId, week, title, problemIds, sourceType, deadline, MissionStatus.IN_PROGRESS,
                LocalDateTime.now());
    }

    public enum MissionStatus {
        IN_PROGRESS,
        COMPLETED
    }
}
