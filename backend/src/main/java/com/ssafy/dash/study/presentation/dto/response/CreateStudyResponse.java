package com.ssafy.dash.study.presentation.dto.response;

import com.ssafy.dash.study.domain.Study;
import com.ssafy.dash.study.domain.StudyVisibility;

public record CreateStudyResponse(
        Long id,
        String name,
        String description,
        StudyVisibility visibility,
        Study.StudyType studyType) {
    public static CreateStudyResponse from(Study study) {
        return new CreateStudyResponse(
                study.getId(),
                study.getName(),
                study.getDescription(),
                study.getVisibility(),
                study.getStudyType());
    }
}
