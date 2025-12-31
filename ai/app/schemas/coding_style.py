"""
Dash AI Server - 코딩 스타일 스키마
"""

from pydantic import BaseModel, Field
from typing import List, Optional

from .common import CodeSample


# === Request ===

class UserStats(BaseModel):
    """사용자 통계"""
    tier: str = Field(default="Unrated", description="티어")
    total_solved: int = Field(default=0, alias="totalSolved", description="총 푼 문제")
    avg_runtime: Optional[float] = Field(None, alias="avgRuntime", description="평균 런타임")
    avg_memory: Optional[float] = Field(None, alias="avgMemory", description="평균 메모리")
    preferred_tags: List[str] = Field(default_factory=list, alias="preferredTags", description="선호 태그")
    
    class Config:
        populate_by_name = True


class CodingStyleRequest(BaseModel):
    """코딩 스타일 분석 요청"""
    code_samples: List[CodeSample] = Field(..., alias="codeSamples", description="코드 샘플들")
    user_stats: UserStats = Field(..., alias="userStats", description="사용자 통계")
    
    class Config:
        populate_by_name = True


# === Response ===

class StyleAxis(BaseModel):
    """스타일 축"""
    axis: str = Field(..., description="축 이름 (E/I, S/N, T/F, J/P)")
    result: str = Field(..., description="결과 (E/I/S/N/T/F/J/P)")
    score: int = Field(default=50, ge=0, le=100, description="점수 (0-100)")
    left_label: str = Field(default="", alias="leftLabel", description="왼쪽 레이블")
    right_label: str = Field(default="", alias="rightLabel", description="오른쪽 레이블")
    description: str = Field(default="", description="설명")
    
    class Config:
        populate_by_name = True


class CodingStyleResponse(BaseModel):
    """코딩 스타일 분석 응답"""
    mbti_code: str = Field(..., alias="mbtiCode", description="MBTI 코드 (4글자)")
    nickname: str = Field(default="", description="별명")
    summary: str = Field(default="", description="요약")
    axes: List[StyleAxis] = Field(default_factory=list, description="4가지 축 분석")
    strengths: List[str] = Field(default_factory=list, description="강점")
    improvements: List[str] = Field(default_factory=list, description="개선점")
    compatible_styles: str = Field(default="", alias="compatibleStyles", description="호환 스타일")
    advice: str = Field(default="", description="조언")
    
    class Config:
        populate_by_name = True
