"""
Dash AI Server - 코딩 스타일 서비스
"""

from app.schemas.coding_style import CodingStyleRequest, CodingStyleResponse
from .base import BaseService


class CodingStyleService(BaseService):
    """코딩 스타일 분석 서비스
    
    코드 패턴을 분석하여 MBTI 스타일 결과를 생성합니다.
    """
    
    def analyze(self, request: CodingStyleRequest) -> CodingStyleResponse:
        """코딩 스타일 분석
        
        Args:
            request: 코드 샘플 및 사용자 통계
            
        Returns:
            MBTI 스타일 분석 결과
        """
        # 코드 샘플 포맷팅
        code_samples = self._format_code_samples(request.code_samples)
        
        # 사용자 통계 추출
        stats = request.user_stats
        preferred_tags = ", ".join(stats.preferred_tags[:5]) if stats.preferred_tags else "없음"
        
        # 프롬프트 포맷팅
        prompt = self.prompts.format(
            "coding_style",
            code_samples=code_samples,
            total_solved=stats.total_solved,
            avg_runtime=stats.avg_runtime or "N/A",
            avg_memory=stats.avg_memory or "N/A",
            preferred_tags=preferred_tags,
            tier=stats.tier
        )
        
        # LLM 호출
        return self.llm.generate(
            prompt=prompt,
            response_schema=CodingStyleResponse
        )
    
    def _format_code_samples(self, samples: list) -> str:
        """코드 샘플을 포맷팅된 문자열로 변환"""
        if not samples:
            return "코드 샘플 없음"
        
        formatted = []
        for i, sample in enumerate(samples[:10], 1):  # 최대 10개
            problem_info = f"문제 {sample.problem_number}" if sample.problem_number else f"샘플 {i}"
            runtime_info = f"{sample.runtime_ms}ms" if sample.runtime_ms else "N/A"
            memory_info = f"{sample.memory_kb}KB" if sample.memory_kb else "N/A"
            
            formatted.append(f"""
### {problem_info} ({sample.language})
- 런타임: {runtime_info}, 메모리: {memory_info}
```{sample.language}
{sample.code[:500]}{'...' if len(sample.code) > 500 else ''}
```
""")
        
        return "\n".join(formatted)
