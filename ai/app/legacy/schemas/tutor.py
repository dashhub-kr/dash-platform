"""
Dash AI Server - 튜터 스키마
"""

from pydantic import BaseModel, Field
from typing import List, Optional

from .common import ChatMessage, UserContext


# === Request ===

class TutorContext(BaseModel):
    """튜터 컨텍스트"""
    tier: str = Field(default="Unrated", description="티어")
    solved_count: int = Field(default=0, alias="solvedCount", description="푼 문제 수")
    recent_tags: List[str] = Field(default_factory=list, alias="recentTags", description="최근 관심 태그")
    
    class Config:
        populate_by_name = True


class TutorChatRequest(BaseModel):
    """튜터 채팅 요청"""
    message: str = Field(..., description="사용자 메시지")
    history: List[ChatMessage] = Field(default_factory=list, description="대화 히스토리")
    context: Optional[TutorContext] = Field(None, description="사용자 컨텍스트")
    problem_number: Optional[str] = Field(None, alias="problemNumber", description="관련 문제 번호")
    code: Optional[str] = Field(None, description="관련 코드")
    
    class Config:
        populate_by_name = True


# === Response ===

class TutorChatResponse(BaseModel):
    """튜터 채팅 응답"""
    reply: str = Field(..., description="튜터 응답")
    teaching_style: str = Field(default="socratic", alias="teachingStyle", description="교수법")
    follow_up_questions: List[str] = Field(default_factory=list, alias="followUpQuestions", description="후속 질문")
    concept_explanation: Optional[str] = Field(None, alias="conceptExplanation", description="개념 설명")
    encouragement: str = Field(default="", description="격려 메시지")
    
    class Config:
        populate_by_name = True
