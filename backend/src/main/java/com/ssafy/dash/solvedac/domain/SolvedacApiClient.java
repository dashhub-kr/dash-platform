package com.ssafy.dash.solvedac.domain;

import java.util.List;

/**
 * Solved.ac API 클라이언트 인터페이스
 */
public interface SolvedacApiClient {
    /**
     * 사용자 기본 정보 조회
     * GET /user/show?handle={handle}
     */
    SolvedacUser getUserInfo(String handle);

    /**
     * 클래스별 문제 풀이 통계 조회
     * GET /user/class_stats?handle={handle}
     */
    List<ClassStat> getClassStats(String handle);

    /**
     * 태그별 문제 풀이 통계 조회
     * GET /user/problem_tag_stats?handle={handle}
     */
    TagStat getTagStats(String handle);


}
