"""
Dash AI Server - LangGraph 상태 정의

튜터 워크플로우에서 사용되는 상태 타입 정의
"""

from typing import TypedDict, Optional, List, Literal


class ChatMessage(TypedDict):
    """대화 메시지"""
    role: Literal["user", "assistant"]
    content: str


class UserContext(TypedDict, total=False):
    """사용자 컨텍스트"""
    tier: str
    solved_count: int
    recent_tags: List[str]


class TutorState(TypedDict, total=False):
    """튜터 워크플로우 상태
    
    LangGraph StateGraph에서 노드 간 전달되는 상태 객체
    """
    # 입력
    user_id: int
    message: str
    history: List[ChatMessage]
    context: UserContext
    problem_number: Optional[str]
    code: Optional[str]
    
    # 중간 상태
    intent: str  # "question", "help", "clarify", "feedback"
    teaching_style: str  # "socratic", "direct", "hint"
    needs_clarification: bool
    
    # 출력
    reply: str
    follow_up_questions: List[str]
    concept_explanation: Optional[str]
    encouragement: str
