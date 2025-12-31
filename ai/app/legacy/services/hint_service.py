"""
Dash AI Server - 힌트 서비스
"""

from app.schemas.hint import HintRequest, HintResponse
from .base import BaseService


class HintService(BaseService):
    """힌트 생성 서비스
    
    문제에 대한 레벨별 맞춤 힌트를 생성합니다.
    """
    
    def generate(self, request: HintRequest) -> HintResponse:
        """힌트 생성
        
        Args:
            request: 힌트 요청 (레벨 1-3)
            
        Returns:
            레벨별 맞춤 힌트 응답
        """
        # 레벨별 프롬프트 선택
        template_name = f"hint_level{request.level}"
        
        # 사용자 컨텍스트 추출
        ctx = request.user_context
        tier_name = ctx.tier_name if ctx else "Unrated"
        solved_count = ctx.solved_count if ctx else 0
        weak_tags = ", ".join(ctx.weak_tags) if ctx and ctx.weak_tags else "없음"
        
        # 프롬프트 포맷팅
        prompt = self.prompts.format(
            template_name,
            problem_number=request.problem_number,
            problem_title=request.problem_title or "제목 없음",
            tier_name=tier_name,
            solved_count=solved_count,
            weak_tags=weak_tags
        )
        
        # LLM 호출
        response = self.llm.generate(
            prompt=prompt,
            response_schema=HintResponse
        )
        
        # 레벨 정보 설정
        response.level = request.level
        
        return response
