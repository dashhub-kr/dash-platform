"""
이 모듈은 프로젝트 전체에서 사용되는 데이터 스키마(Pydantic 모델)를 정의합니다.
"""

from pydantic import BaseModel, Field
from typing import List

class Problem(BaseModel):
    """코드 문제에 대한 정보를 담는 Pydantic 모델."""
    description: str = Field(description="문제 설명")
    input: str = Field(description="입력 예시")
    output: str = Field(description="출력 예시")
    isGuess: bool = Field(description="문제 추론 여부")
    guessReason: str = Field(description="문제 추론 시 이유")


class Algorithm(BaseModel):
    """알고리즘 분석 정보를 담는 Pydantic 모델."""
    patterns: List[str] = Field(description="사용된 디자인 패턴 또는 주요 기법")
    intuition: str = Field(description="알고리즘의 핵심 아이디어 또는 직관")


class StructureItem(BaseModel):
    """코드 구조의 각 항목을 나타내는 Pydantic 모델."""
    name: str = Field(description="항목 이름 (예: 함수, 클래스)")
    role: str = Field(description="항목의 역할 또는 기능")


class KeyBlock(BaseModel):
    """코드의 주요 블록과 설명을 담는 Pydantic 모델."""
    code: str = Field(description="주요 코드 블록")
    explanation: str = Field(description="해당 코드 블록에 대한 설명")


class TraceExample(BaseModel):
    """코드 실행 추적 예시를 담는 Pydantic 모델."""
    hasExample: bool = Field(description="추적 예시 존재 여부")
    inputExample: str = Field(description="입력 예시")
    steps: List[str] = Field(description="각 단계별 설명")
    note: str = Field(description="특이사항 또는 추가 설명")


class Complexity(BaseModel):
    """시간 및 공간 복잡도 분석을 담는 Pydantic 모델."""
    time: str = Field(description="시간 복잡도")
    space: str = Field(description="공간 복잡도")
    explanation: str = Field(description="복잡도에 대한 설명")


class Pitfalls(BaseModel):
    """흔히 저지를 수 있는 실수와 개선 방안을 담는 Pydantic 모델."""
    items: List[str] = Field(description="주의해야 할 함정 또는 실수")
    improvements: List[str] = Field(description="개선 방안")


class Refactor(BaseModel):
    """리팩토링 제안을 담는 Pydantic 모델."""
    provided: bool = Field(description="리팩토링 코드 제공 여부")
    code: str = Field(description="리팩토링된 코드")
    explanation: str = Field(description="리팩토링에 대한 설명")


class StructuredResponse(BaseModel):
    """코드 설명 서비스의 최종 구조화된 응답 스키마."""
    summary: str = Field(description="전체 코드에 대한 요약 설명")
    problem: Problem = Field(description="문제 정의 및 입출력 정보")
    algorithm: Algorithm = Field(description="알고리즘 및 디자인 패턴 분석")
    structure: List[StructureItem] = Field(description="코드 구조 분석")
    keyBlocks: List[KeyBlock] = Field(description="핵심 코드 블록 설명")
    traceExample: TraceExample = Field(description="실행 추적 예시")
    complexity: Complexity = Field(description="복잡도 분석")
    pitfalls: Pitfalls = Field(description="주의사항 및 개선점")
    refactor: Refactor = Field(description="리팩토링 제안")
