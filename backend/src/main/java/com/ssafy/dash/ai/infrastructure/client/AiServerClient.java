package com.ssafy.dash.ai.infrastructure.client;

import com.ssafy.dash.ai.infrastructure.client.dto.request.*;
import com.ssafy.dash.ai.infrastructure.client.dto.response.*;

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

    /**
     * 반례 생성 요청
     * 
     * @param request 반례 요청 정보 (문제, 코드)
     * @return 반례 응답
     */
    AiCounterExampleResponse generateCounterExample(AiCounterExampleRequest request);

    /**
     * 코드 시뮬레이션 (가상 컴파일러)
     */
    AiSimulatorResponse simulate(AiSimulatorRequest request);

    /**
     * 힌트 대화
     * 
     * @param request 힌트 대화 요청 (메시지, 히스토리, 컨텍스트)
     * @return 힌트 대화 응답
     */
    HintChatResponse hintChat(HintChatRequest request);
}
