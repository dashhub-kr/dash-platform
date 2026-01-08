"""
Dash AI Server - 학습 경로 스키마

스키마의 description이 AI 출력 가이드 역할을 함.
프롬프트는 간단하게, description은 상세하게 작성.
"""

from pydantic import BaseModel, Field
from typing import List, Optional


# === Request ===

class TagStats(BaseModel):
    """태그별 문제 풀이 통계"""
    tag_key: str = Field(..., alias="tagKey",
        description="solved.ac 태그 식별자. 예: 'dp', 'graphs', 'greedy'")
    tag_name: str = Field(default="", alias="tagName",
        description="사용자에게 표시되는 한글 태그명. 예: '다이나믹 프로그래밍'")
    boj_tag_id: Optional[int] = Field(None, alias="bojTagId",
        description="백준 태그 ID (정수). 문제 추천 시 사용")
    solved: int = Field(default=0,
        description="사용자가 해결한 해당 태그 문제 수")
    total: int = Field(default=0,
        description="전체 시도한 해당 태그 문제 수")
    
    @property
    def mastery(self) -> float:
        """숙련도 (0-1)"""
        if self.total == 0:
            return 0.0
        return self.solved / self.total
    
    class Config:
        populate_by_name = True


class ClassStats(BaseModel):
    """solved.ac 클래스별 에센셜 진행 현황"""
    class_number: int = Field(..., alias="classNumber",
        description="solved.ac 클래스 번호 (1-10)")
    essential_solved: int = Field(default=0, alias="essentialSolved",
        description="해결한 에센셜 문제 수")
    essentials: int = Field(default=0,
        description="해당 클래스의 전체 에센셜 문제 수")
    decoration: Optional[str] = Field(None,
        description="클래스 장식 등급 ('bronze', 'silver', 'gold' 또는 None)")
    
    class Config:
        populate_by_name = True


class LearningPathRequest(BaseModel):
    """학습 경로 생성 요청 데이터"""
    current_level: str = Field(..., alias="currentLevel",
        description="현재 사용자 레벨. 예: 'Silver II, Class 3'")
    tier: int = Field(default=0,
        description="숫자형 티어 (1-30). Bronze V=1, Ruby I=30")
    solved_count: int = Field(default=0, alias="solvedCount",
        description="총 해결한 문제 수")
    goal_level: Optional[str] = Field(None, alias="goalLevel",
        description="사용자의 다음 목표. 예: 'Class 4 에센셜 완성'")
    strength_tags: List[TagStats] = Field(default_factory=list, alias="strengthTags",
        description="사용자의 강점 태그 목록 (정답률 높은 상위 5개)")
    weakness_tags: List[TagStats] = Field(default_factory=list, alias="weaknessTags",
        description="사용자의 약점 태그 목록 (정답률 낮은 상위 5개)")
    class_stats: List[ClassStats] = Field(default_factory=list, alias="classStats",
        description="각 클래스별 에센셜 진행 현황")
    
    # 거품 탐지 데이터
    bubble_index: Optional[int] = Field(None, alias="bubbleIndex",
        description="티어 - Top100 평균 레벨. 양수면 거품 (예: 3이면 티어가 실력보다 3단계 높음)")
    avg_top100_level: Optional[int] = Field(None, alias="avgTop100Level",
        description="Top 100 문제 평균 난이도 (1-30). 실제 풀이 실력 지표")
    
    class Config:
        populate_by_name = True


# === Response ===

class LearningPhase(BaseModel):
    """학습 로드맵의 각 Phase (단계)"""
    phase: int = Field(...,
        description="Phase 순서 번호 (1부터 시작, 최대 4개)")
    title: str = Field(...,
        description="Phase 제목. 예: 'DP 기초 다지기'")
    duration_days: int = Field(..., alias="durationDays",
        description="해당 Phase 소요 기간 (일 단위, 7-21일 권장)")
    focus_tags: List[str] = Field(default_factory=list, alias="focusTags",
        description="집중할 알고리즘 태그 (최대 3개). 예: ['다이나믹 프로그래밍']")
    target_problems: int = Field(..., alias="targetProblems",
        description="이 Phase에서 풀어야 할 목표 문제 수 (5-20개)")
    difficulty_range: str = Field(..., alias="difficultyRange",
        description="권장 문제 난이도 범위. 예: 'Silver II ~ Gold IV'")
    goals: List[str] = Field(default_factory=list,
        description="구체적인 학습 목표 (2-3개). 예: ['1차원 DP 완전 이해', '점화식 세우기 연습']")
    milestones: List[str] = Field(default_factory=list,
        description="달성 지표가 될 대표 문제 (2-3개). 예: ['1463 1로 만들기 해결']")

    class Config:
        populate_by_name = True


class LearningPathResponse(BaseModel):
    """AI 생성 학습 경로 응답"""
    
    # 분석 섹션
    analysis_summary: str = Field(..., alias="analysisSummary",
        description="현재 상태 객관적 분석 (2-3문장). 데이터 기반, 감정적 칭찬 지양. 예: 'Silver II, 243문제 해결. 그래프 탐색 강점, DP 약점.'")
    key_strength: str = Field(..., alias="keyStrength",
        description="핵심 강점 1문장 요약. 예: '그래프 탐색 정답률 85%로 동일 티어 대비 우수'")
    primary_weakness: str = Field(..., alias="primaryWeakness",
        description="주요 약점 1문장 요약. 예: 'DP 문제 풀이 경험 부족 (5문제)'")
    
    # 예측 섹션
    estimated_days_to_goal: int = Field(..., alias="estimatedDaysToGoal",
        description="목표 도달 예상 일수 (정수). 현재 풀이 속도 기반 현실적 예측")
    growth_prediction: str = Field(..., alias="growthPrediction",
        description="성장 예측 메시지. 예: '현재 페이스면 18일 후 Gold V 예상'")
    
    # 전략 섹션
    weekly_goal: str = Field(..., alias="weeklyGoal",
        description="이번 주 구체적 목표. 예: 'DP 기초 문제 8개 풀기'")
    recommended_tags: List[str] = Field(default_factory=list, alias="recommendedTags",
        description="이번 주 집중 추천 태그 (최대 3개). 약점 중 가장 시급한 것 우선")
    difficulty_suggestion: str = Field(..., alias="difficultySuggestion",
        description="현재 수준에 맞는 권장 문제 난이도. 예: 'Silver I ~ Gold V'")
    strategic_advice: str = Field(..., alias="strategicAdvice",
        description="다음 단계로 넘어가기 위한 구체적 조언 1가지. 예: 'Gold 안착을 위해 DP 비중을 30%까지 늘리세요'")
    
    # 로드맵 섹션
    phases: List[LearningPhase] = Field(default_factory=list,
        description="2-4주 단위 학습 Phase 목록 (2-4개). 각 Phase는 명확한 목표와 기간 포함")
    next_class_to_clear: int = Field(0, alias="nextClassToClear",
        description="다음으로 클리어할 solved.ac 클래스 번호 (1-10)")
    
    # 동기부여
    motivation_message: str = Field(..., alias="motivationMessage",
        description="강점 기반 격려 메시지. 예: '그래프 탐색 실력이 뛰어나요! 이 감각을 DP에도 적용해보세요.'")
    
    class Config:
        populate_by_name = True
