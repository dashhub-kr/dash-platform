"""
Dash AI Server - 코드 리뷰 스키마
"""

from pydantic import BaseModel, Field
from typing import List, Optional, Literal


# === Request ===

class ReviewRequest(BaseModel):
    """코드 리뷰 요청"""
    code: str = Field(..., description="분석할 소스 코드 (전체 코드)")
    language: str = Field(default="java", description="프로그래밍 언어 (java, python, cpp, c, javascript 등)")
    problem_number: Optional[str] = Field(None, alias="problemNumber", description="백준/프로그래머스 등의 문제 번호")
    platform: Optional[str] = Field(None, description="문제 플랫폼 (baekjoon, programmers, swea 등)")
    problem_title: Optional[str] = Field(None, alias="problemTitle", description="문제 제목")
    
    class Config:
        populate_by_name = True


# === Response Components ===

class Algorithm(BaseModel):
    """알고리즘 분석 - 사용된 알고리즘/자료구조 패턴"""
    patterns: List[str] = Field(
        default_factory=list, 
        description="사용된 알고리즘 패턴 리스트 (예: ['DFS', '백트래킹', '그래프 탐색'])"
    )
    intuition: str = Field(
        default="", 
        description="문제 해결의 핵심 아이디어를 2-3문장으로 설명. 왜 이 알고리즘을 선택했는지 포함"
    )


class StructureItem(BaseModel):
    """코드 구조 항목 - 주요 변수 또는 함수"""
    name: str = Field(
        default="", 
        description="변수명 또는 함수 시그니처 (예: 'visited' 또는 'dfs(int node, int depth)')"
    )
    role: str = Field(
        default="", 
        description="해당 항목의 역할을 한국어로 설명 (예: '방문 여부를 저장하는 boolean 배열')"
    )
    type: Literal["variable", "function", "class", "constant"] = Field(
        default="variable",
        description="항목 타입: 'variable'(변수/배열), 'function'(함수/메서드), 'class'(클래스), 'constant'(상수)"
    )
    
    class Config:
        populate_by_name = True


class KeyBlock(BaseModel):
    """핵심 코드 블록 - 알고리즘의 핵심 로직이 담긴 코드 조각"""
    code: str = Field(
        default="", 
        description="핵심 코드 스니펫 (1-5줄). 라인 번호 없이 순수 코드만 포함"
    )
    explanation: str = Field(
        default="", 
        description="이 코드가 왜 중요한지, 무슨 역할을 하는지 한국어로 설명"
    )
    start_line: Optional[int] = Field(
        None, 
        alias="startLine", 
        description="코드 블록 시작 라인 번호 (1-indexed). 프롬프트의 라인 번호 참조"
    )
    end_line: Optional[int] = Field(
        None, 
        alias="endLine", 
        description="코드 블록 끝 라인 번호 (1-indexed). 프롬프트의 라인 번호 참조"
    )
    
    class Config:
        populate_by_name = True


class TraceExample(BaseModel):
    """실행 추적 예시 - 간단한 입력으로 코드 흐름 설명"""
    has_example: bool = Field(default=False, alias="hasExample", description="추적 예시 제공 여부")
    input_example: str = Field(
        default="", 
        alias="inputExample", 
        description="추적에 사용할 간단한 입력 예시 (예: 'N=3, arr=[1,2,3]')"
    )
    steps: List[str] = Field(
        default_factory=list, 
        description="단계별 실행 흐름 설명 리스트 (예: ['1. dfs(0) 호출', '2. visited[0]=true 설정', ...])"
    )
    note: str = Field(default="", description="추가 참고사항이나 특이사항")
    
    class Config:
        populate_by_name = True


class Complexity(BaseModel):
    """복잡도 분석 - 시간/공간 복잡도"""
    time: str = Field(default="-", description="시간 복잡도 (예: 'O(N^2)', 'O(N log N)')")
    space: str = Field(default="-", description="공간 복잡도 (예: 'O(N)', 'O(1)')")
    explanation: str = Field(
        default="", 
        description="복잡도 산출 근거를 한국어로 설명 (예: 'N개 노드를 각각 방문하고, 각 노드에서 M개 간선 확인')"
    )


class Pitfalls(BaseModel):
    """주의사항 및 개선점"""
    items: List[str] = Field(
        default_factory=list, 
        description="코드에서 주의해야 할 점 리스트 (예: ['배열 인덱스 범위 체크 필요', '오버플로우 가능성'])"
    )
    improvements: List[str] = Field(
        default_factory=list, 
        description="코드 개선 제안 리스트 (예: ['변수명을 더 명확하게', 'early return 적용'])"
    )


class Refactor(BaseModel):
    """리팩토링 제안 - 개선된 코드 제공"""
    provided: bool = Field(default=False, description="리팩토링 코드 제공 여부 (개선점이 있을 때만 true)")
    code: str = Field(default="", description="리팩토링된 전체 코드 또는 핵심 부분")
    explanation: str = Field(
        default="", 
        description="리팩토링 내용과 이유를 한국어로 설명 (예: '불필요한 중복 제거 및 가독성 향상')"
    )


# === Main Response ===

class ReviewResponse(BaseModel):
    """코드 리뷰 응답 - 알고리즘 코드 분석 결과"""
    summary: str = Field(
        default="", 
        description="코드가 해결하는 문제와 핵심 접근법을 2-3문장으로 요약"
    )
    algorithm: Algorithm = Field(default_factory=Algorithm, description="알고리즘 패턴 분석")
    structure: List[StructureItem] = Field(
        default_factory=list, 
        description="주요 변수, 함수, 클래스 목록 (3-7개 권장)"
    )
    key_blocks: List[KeyBlock] = Field(
        default_factory=list, 
        alias="keyBlocks", 
        description="핵심 로직이 담긴 코드 블록 (2-5개 권장). 각 블록에 startLine, endLine 필수 포함"
    )
    trace_example: TraceExample = Field(
        default_factory=TraceExample, 
        alias="traceExample", 
        description="간단한 입력으로 실행 흐름 추적 예시"
    )
    complexity: Complexity = Field(default_factory=Complexity, description="시간/공간 복잡도 분석")
    pitfalls: Pitfalls = Field(default_factory=Pitfalls, description="주의사항 및 개선점")
    refactor: Refactor = Field(default_factory=Refactor, description="리팩토링 제안 (선택)")
    
    class Config:
        populate_by_name = True

