"""
Dash AI Server - AI 튜터 대화 스키마

맞은 문제/틀린 문제 모두 지원하는 대화형 AI 튜터
"""

from pydantic import BaseModel, Field
from typing import List, Optional, Literal

from .common import ChatMessage, UserContext


# === Request ===

class TutorChatRequest(BaseModel):
    """AI 튜터 대화 요청"""
    message: str = Field(..., description="사용자 메시지")
    problem_number: str = Field(..., alias="problemNumber", description="문제 번호")
    problem_title: Optional[str] = Field(None, alias="problemTitle", description="문제 제목")
    code: Optional[str] = Field(None, description="사용자 코드")
    language: Optional[str] = Field(default="java", description="코드 언어")
    solve_status: Literal["solved", "wrong"] = Field(..., alias="solveStatus", description="풀이 상태")
    wrong_reason: Optional[str] = Field(None, alias="wrongReason", description="틀린 이유 (시간초과, 틀렸습니다 등)")
    history: List[ChatMessage] = Field(default_factory=list, description="전체 대화 히스토리")
    user_context: Optional[UserContext] = Field(None, alias="userContext", description="사용자 컨텍스트")
    
    class Config:
        populate_by_name = True


# === Response ===

class TutorChatResponse(BaseModel):
    """AI 튜터 대화 응답"""
    reply: str = Field(..., description="AI 응답")
    teaching_style: str = Field(default="socratic", alias="teachingStyle", description="교수법")
    follow_up_questions: List[str] = Field(default_factory=list, alias="followUpQuestions", description="추천 후속 질문")
    
    class Config:
        populate_by_name = True




