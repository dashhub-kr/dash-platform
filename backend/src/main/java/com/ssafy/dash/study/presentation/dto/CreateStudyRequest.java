package com.ssafy.dash.study.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudyRequest {
    private String name;
    private String description;
    private com.ssafy.dash.study.domain.StudyVisibility visibility;
}
