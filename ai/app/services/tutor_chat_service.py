"""
Dash AI Server - AI 튜터 대화 서비스

맞은 문제/틀린 문제 모두 지원하는 대화형 AI 튜터
LangGraph 미사용, 단순 멀티턴 API 호출
"""

from app.schemas.tutor_chat import TutorChatRequest, TutorChatResponse
from .base import BaseService


class TutorChatService(BaseService):
    """AI 튜터 대화 서비스
    
    맞은 문제: 코드 리뷰, 최적화 조언
    틀린 문제: 디버깅 힌트, 문제점 분석
    """
    
    def chat(self, request: TutorChatRequest) -> TutorChatResponse:
        """AI 튜터 대화
        
        Args:
            request: 튜터 대화 요청 (메시지, 히스토리, solveStatus)
            
        Returns:
            튜터 대화 응답
        """
        # 사용자 컨텍스트 추출
        ctx = request.user_context
        tier_name = ctx.tier_name if ctx else "Unrated"
        solved_count = ctx.solved_count if ctx else 0
        weak_tags = ", ".join(ctx.weak_tags) if ctx and ctx.weak_tags else "없음"
        
        # 코드 섹션 구성
        code_section = ""
        if request.code:
            lang = request.language or "java"
            code_section = f"## 학생의 코드\n```{lang}\n{request.code[:2000]}\n```"
        
        # 풀이 상태에 따른 컨텍스트
        if request.solve_status == "solved":
            status_context = "✅ 맞은 문제입니다. 코드 리뷰와 최적화 조언을 제공하세요."
        else:
            wrong_reason = request.wrong_reason or "알 수 없음"
            status_context = f"❌ 틀린 문제입니다. 결과: {wrong_reason}. 디버깅 힌트를 제공하세요."
        
        # 시스템 프롬프트 구성
        system_prompt = self.prompts.format(
            "tutor_chat",
            problem_number=request.problem_number,
            problem_title=request.problem_title or "제목 없음",
            code_section=code_section,
            status_context=status_context,
            tier_name=tier_name,
            solved_count=solved_count,
            weak_tags=weak_tags
        )
        
        # 대화 히스토리 구성
        messages = []
        for msg in (request.history or [])[-10:]:  # 최근 10턴
            messages.append({"role": msg.role, "content": msg.content})
        messages.append({"role": "user", "content": request.message})
        
        # LLM 호출 (멀티턴)
        response = self.llm.chat(
            messages=messages,
            response_schema=TutorChatResponse,
            system_instruction=system_prompt
        )
        
        return response

