"""
Dash AI Server - 디버그 서비스
"""

from app.schemas.debug import CounterExampleRequest, CounterExampleResponse
from .base import BaseService


class DebugService(BaseService):
    """디버그 서비스
    
    코드 디버깅 도우미 기능을 제공합니다.
    - 반례 생성
    """
    
    def generate_counter_example(self, request: CounterExampleRequest) -> CounterExampleResponse:
        """반례 생성
        
        오답 코드를 분석하여 실패하는 입력 케이스를 생성합니다.
        """
        # 프롬프트 로드
        prompt = self.prompts.get("counter_example")
        
        # 프롬프트 포맷팅
        formatted_prompt = prompt.format(
            platform=request.platform or "알 수 없음",
            problem_number=request.problem_number,
            problem_title=request.problem_title or "",
            language=request.language,
            code=request.code,
        )
        
        # LLM 호출
        response = self.llm.generate(
            prompt=formatted_prompt,
            response_schema=CounterExampleResponse
        )
        
        return response
