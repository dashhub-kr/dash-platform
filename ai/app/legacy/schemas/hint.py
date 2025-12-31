"""
Dash AI Server - 힌트 스키마
"""

from pydantic import BaseModel, Field
from typing import List, Optional

from .common import UserContext


# === Request ===

class HintRequest(BaseModel):
    """힌트 요청"""
    problem_number: str = Field(..., alias="problemNumber", description="문제 번호")
    problem_title: Optional[str] = Field(None, alias="problemTitle", description="문제 제목")
    level: int = Field(default=1, ge=1, le=3, description="힌트 레벨 (1-3)")
    user_context: Optional[UserContext] = Field(None, alias="userContext", description="사용자 컨텍스트")
    
    class Config:
        populate_by_name = True


# === Response ===

class HintResponse(BaseModel):
    """힌트 응답"""
    level: int = Field(..., description="힌트 레벨")
    hint: str = Field(..., description="힌트 내용")
    encouragement: str = Field(default="", description="격려 메시지")
    related_concepts: List[str] = Field(default_factory=list, alias="relatedConcepts", description="관련 개념")
    next_step_suggestion: str = Field(default="", alias="nextStepSuggestion", description="다음 단계 제안")
    
    class Config:
        populate_by_name = True
