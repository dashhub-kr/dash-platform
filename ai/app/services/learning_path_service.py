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
        
        # 거품 분석 포맷팅
        bubble_analysis = self._format_bubble_analysis(request.bubble_index, request.avg_top100_level)
        
        # 프롬프트 포맷팅
        prompt = self.prompts.format(
            "learning_path",
            current_level=request.current_level,
            tier=request.tier,
            solved_count=request.solved_count,
            goal_level=request.goal_level or "다음 티어",
            strength_tags=strength_tags,
            weakness_tags=weakness_tags,
            class_progress=class_progress,
            bubble_analysis=bubble_analysis
        )
        
        # LLM 호출
        return self.llm.generate(
            prompt=prompt,
            response_schema=LearningPathResponse
        )
    
    def _format_tags(self, tags: list) -> str:
        """태그 목록을 포맷팅된 문자열로 변환 (레이팅 포함)"""
        if not tags:
            return "- 데이터 없음"
        
        lines = []
        for tag in tags[:5]:  # 상위 5개만
            rating_info = f" (레이팅: {tag.rating})" if hasattr(tag, 'rating') and tag.rating else ""
            lines.append(f"- {tag.tag_name or tag.tag_key}: {tag.solved}/{tag.total} 문제{rating_info}")
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
    
    def _format_bubble_analysis(self, bubble_index: int | None, avg_top100_level: int | None) -> str:
        """거품 분석 데이터를 포맷팅"""
        if bubble_index is None or avg_top100_level is None:
            return "- 분석 데이터 없음"
        
        tier_names = ["Unrated"] + [
            f"{t} {l}" for t in ["Bronze", "Silver", "Gold", "Platinum", "Diamond", "Ruby"]
            for l in ["V", "IV", "III", "II", "I"]
        ] + ["Master"]
        
        real_tier = tier_names[avg_top100_level] if 0 <= avg_top100_level < len(tier_names) else "Unknown"
        
        if bubble_index <= 2:
            return f"- 실제 실력: {real_tier} (정상 범위)"
        else:
            return f"- 실제 실력: {real_tier} (⚠️ 거품 경고: 현재 티어보다 {bubble_index}단계 낮음)"
