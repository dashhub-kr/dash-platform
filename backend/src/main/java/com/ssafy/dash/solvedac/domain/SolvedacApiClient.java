package com.ssafy.dash.solvedac.domain;

import com.ssafy.dash.solvedac.domain.dto.ClassStatResponse;
import com.ssafy.dash.solvedac.domain.dto.SolvedacUserResponse;
import com.ssafy.dash.solvedac.domain.dto.TagStatResponse;
import com.ssafy.dash.solvedac.domain.dto.Top100Response;
import java.util.List;

/**
 * Solved.ac API 클라이언트 인터페이스
 */
public interface SolvedacApiClient {
    /**
     * 사용자 기본 정보 조회
     * GET /user/show?handle={handle}
     */
    SolvedacUserResponse getUserInfo(String handle);

    /**
     * 클래스별 문제 풀이 통계 조회
     * GET /user/class_stats?handle={handle}
     */
    List<ClassStatResponse> getClassStats(String handle);

    /**
     * 태그별 문제 풀이 통계 조회
     * GET /user/problem_tag_stats?handle={handle}
     */
    TagStatResponse getTagStats(String handle);

    /**
     * 상위 100개 문제 조회
     * GET /user/top_100?handle={handle}
     */
    Top100Response getTop100Problems(String handle);
}
