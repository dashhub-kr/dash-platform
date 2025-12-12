package com.ssafy.dash.ai.client;

import com.ssafy.dash.ai.client.dto.*;

/**
 * AI 서버 통신 클라이언트 인터페이스
 */
public interface AiServerClient {

    /**
     * 코드 분석 요청
     * 
     * @param request 분석할 코드 정보
     * @return 분석 결과
     */
    CodeReviewResponse analyzeCode(CodeReviewRequest request);

    /**
     * 힌트 생성 요청
     * 
     * @param request 힌트 요청 정보 (문제, 레벨, 사용자 컨텍스트)
     * @return 레벨별 힌트
     */
    HintResponse generateHint(HintRequest request);

    /**
     * AI 개인화 학습 경로 생성
     * 
     * @param request 분석 데이터 기반 학습 경로 요청
     * @return AI 추천 학습 경로
     */
    LearningPathResponse generateLearningPath(LearningPathRequest request);

    /**
     * 코딩 스타일 분석 (MBTI 스타일)
     * 
     * @param request 코드 샘플 및 통계
     * @return MBTI 스타일 분석 결과
     */
    CodingStyleResponse analyzeCodingStyle(CodingStyleRequest request);
}
