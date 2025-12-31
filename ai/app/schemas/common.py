"""
Dash AI Server - 공통 스키마

여러 모듈에서 공유되는 공통 Pydantic 모델
"""

from pydantic import BaseModel, Field
from typing import List, Optional


class UserContext(BaseModel):
    """사용자 컨텍스트 (공통)"""
    tier: int = Field(default=0, description="티어 (1-31)")
    tier_name: str = Field(default="Unrated", alias="tierName", description="티어명")
    solved_count: int = Field(default=0, alias="solvedCount", description="푼 문제 수")
    weak_tags: List[str] = Field(default_factory=list, alias="weakTags", description="약점 태그")
    
    class Config:
        populate_by_name = True


class ChatMessage(BaseModel):
    """대화 메시지"""
    role: str = Field(..., description="역할 (user/assistant)")
    content: str = Field(..., description="메시지 내용")


class CodeSample(BaseModel):
    """코드 샘플"""
    code: str = Field(..., description="코드")
    language: str = Field(default="java", description="언어")
    problem_number: Optional[str] = Field(None, alias="problemNumber", description="문제 번호")
    runtime_ms: Optional[int] = Field(None, alias="runtimeMs", description="실행 시간 (ms)")
    memory_kb: Optional[int] = Field(None, alias="memoryKb", description="메모리 (KB)")
    
    class Config:
        populate_by_name = True

