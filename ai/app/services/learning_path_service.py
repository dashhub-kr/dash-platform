"""
Dash AI Server - 학습 경로 서비스
"""

from app.schemas.learning_path import LearningPathRequest, LearningPathResponse
from .base import BaseService


class LearningPathService(BaseService):
    """학습 경로 생성 서비스
    
    사용자 분석 데이터 기반 개인화 학습 경로를 생성합니다.
    """
    
    def generate(self, request: LearningPathRequest) -> LearningPathResponse:
        """학습 경로 생성
        
        Args:
            request: 학습 경로 요청 (태그/클래스 통계 포함)
            
        Returns:
            개인화된 학습 경로
        """
        # 강점/약점 태그 포맷팅
        strength_tags = self._format_tags(request.strength_tags)
        weakness_tags = self._format_tags(request.weakness_tags)
        
        # 클래스 진행률 포맷팅
        class_progress = self._format_class_progress(request.class_stats)
        
        # 프롬프트 포맷팅
        prompt = self.prompts.format(
            "learning_path",
            current_level=request.current_level,
            tier=request.tier,
            solved_count=request.solved_count,
            goal_level=request.goal_level or "다음 티어",
            strength_tags=strength_tags,
            weakness_tags=weakness_tags,
            class_progress=class_progress
        )
        
        # LLM 호출
        return self.llm.generate(
            prompt=prompt,
            response_schema=LearningPathResponse
        )
    
    def _format_tags(self, tags: list) -> str:
        """태그 목록을 포맷팅된 문자열로 변환"""
        if not tags:
            return "- 데이터 없음"
        
        lines = []
        for tag in tags[:5]:  # 상위 5개만
            lines.append(f"- {tag.tag_name or tag.tag_key}: {tag.solved}/{tag.total} 문제")
        return "\n".join(lines)
    
    def _format_class_progress(self, stats: list) -> str:
        """클래스 진행률을 포맷팅된 문자열로 변환"""
        if not stats:
            return "- 클래스 데이터 없음"
        
        lines = []
        for stat in stats:
            decoration = f" ({stat.decoration})" if stat.decoration else ""
            lines.append(
                f"- Class {stat.class_number}: "
                f"{stat.essential_solved}/{stat.essentials} 에센셜{decoration}"
            )
        return "\n".join(lines)
