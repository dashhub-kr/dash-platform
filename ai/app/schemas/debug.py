"""
Dash AI Server - 디버그 스키마
"""

from pydantic import BaseModel, Field
from typing import Optional

# === Request ===

class CounterExampleRequest(BaseModel):
    """반례 생성 요청"""
    problem_number: str = Field(..., alias="problemNumber", description="문제 번호")
    code: str = Field(..., description="사용자 코드 (오답)")
    language: str = Field(default="java", description="프로그래밍 언어")
    platform: Optional[str] = Field(None, description="문제 플랫폼")
    problem_title: Optional[str] = Field(None, alias="problemTitle", description="문제 제목")
    problem_description: Optional[str] = Field(None, alias="problemDescription", description="문제 설명 (옵션)")
    
    class Config:
        populate_by_name = True


# === Response ===

class CounterExampleResponse(BaseModel):
    """반례 생성 응답"""
    input_example: str = Field(..., alias="input", description="반례 입력값 (복사 붙여넣기 용)")
    expected_output: str = Field(..., alias="expected", description="기대되는 정답 출력")
    predicted_output: str = Field(..., alias="predicted", description="사용자 코드의 예상 오답 출력")
    explanation: str = Field(..., alias="reason", description="실패 원인 설명 (마크다운 지원)")
    
    class Config:
        populate_by_name = True
