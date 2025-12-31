"""
Dash AI Server - 튜터 서비스 (LangGraph 버전)

LangGraph를 사용한 상태 관리 기반 대화형 튜터 서비스
"""

from app.schemas.tutor import TutorChatRequest, TutorChatResponse
from app.core.graphs import TutorGraph, TutorState
from .base import BaseService


class TutorService(BaseService):
    """대화형 튜터 서비스 (LangGraph)
    
    소크라테스 교수법을 사용하여 학생이 스스로 답을 찾도록 유도합니다.
    LangGraph StateGraph를 사용하여 대화 상태를 관리합니다.
    """
    
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._graph = None
    
    @property
    def graph(self) -> TutorGraph:
        """튜터 그래프 (지연 초기화)"""
        if self._graph is None:
            self._graph = TutorGraph(self.llm, self.prompts)
        return self._graph
    
    def chat(self, request: TutorChatRequest) -> TutorChatResponse:
        """튜터 채팅 (LangGraph 사용)
        
        Args:
            request: 채팅 요청 (메시지, 히스토리, 컨텍스트)
            
        Returns:
            튜터 응답
        """
        # 초기 상태 구성
        initial_state: TutorState = {
            "message": request.message,
            "history": [
                {"role": msg.role, "content": msg.content}
                for msg in (request.history or [])
            ],
            "context": self._extract_context(request.context),
            "problem_number": request.problem_number,
            "code": request.code,
        }
        
        # 그래프 실행
        final_state = self.graph.invoke(initial_state)
        
        # 응답 변환
        return TutorChatResponse(
            reply=final_state.get("reply", ""),
            teaching_style=final_state.get("teaching_style", "socratic"),
            follow_up_questions=final_state.get("follow_up_questions", []),
            concept_explanation=final_state.get("concept_explanation"),
            encouragement=final_state.get("encouragement", "")
        )
    
    def _extract_context(self, context) -> dict:
        """컨텍스트 추출"""
        if context is None:
            return {
                "tier": "Unrated",
                "solved_count": 0,
                "recent_tags": []
            }
        
        return {
            "tier": context.tier,
            "solved_count": context.solved_count,
            "recent_tags": context.recent_tags or []
        }
