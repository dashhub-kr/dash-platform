package com.ssafy.dash.ai.client;

import com.ssafy.dash.ai.client.dto.CodeReviewRequest;
import com.ssafy.dash.ai.client.dto.CodeReviewResponse;
import com.ssafy.dash.ai.client.dto.HintRequest;
import com.ssafy.dash.ai.client.dto.HintResponse;

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
}
