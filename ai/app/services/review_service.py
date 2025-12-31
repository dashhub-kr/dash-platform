"""
Dash AI Server - 코드 리뷰 서비스
"""

from app.schemas.review import ReviewRequest, ReviewResponse
from .base import BaseService


class ReviewService(BaseService):
    """코드 리뷰 서비스
    
    알고리즘 풀이 코드를 분석하여 구조화된 리뷰를 생성합니다.
    """
    
    def _add_line_numbers(self, code: str) -> str:
        """코드에 라인 번호 추가
        
        Args:
            code: 원본 코드
            
        Returns:
            라인 번호가 포함된 코드
        """
        lines = code.split('\n')
        numbered_lines = [f"{i+1}: {line}" for i, line in enumerate(lines)]
        return '\n'.join(numbered_lines)
    
    def analyze(self, request: ReviewRequest) -> ReviewResponse:
        """코드 분석 및 리뷰 생성
        
        Args:
            request: 코드 리뷰 요청
            
        Returns:
            구조화된 코드 리뷰 응답
        """
        # 라인 번호가 포함된 코드 생성
        numbered_code = self._add_line_numbers(request.code)
        
        # 프롬프트 포맷팅
        prompt = self.prompts.format(
            "review",
            language=request.language,
            numbered_code=numbered_code,
            platform=request.platform or "알 수 없음",
            problem_number=request.problem_number or "알 수 없음",
            problem_title=request.problem_title or "알 수 없음"
        )
        
        # LLM 호출 (구조화된 응답)
        response = self.llm.generate(
            prompt=prompt,
            response_schema=ReviewResponse
        )
        
        return response
