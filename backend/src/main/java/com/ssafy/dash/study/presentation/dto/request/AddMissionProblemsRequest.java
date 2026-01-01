package com.ssafy.dash.study.presentation.dto.request;

import java.util.List;

/**
 * 미션 문제 추가 요청 DTO
 */
public record AddMissionProblemsRequest(List<Integer> problemIds) {
}
