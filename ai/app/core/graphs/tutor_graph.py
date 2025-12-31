"""
Dash AI Server - LangGraph 튜터 워크플로우

상태 관리 기반 대화형 튜터 그래프 정의
"""

from langgraph.graph import StateGraph, START, END

from .states import TutorState
from .nodes import create_analyze_intent_node, create_generate_response_node
from app.core.llm import BaseLLM
from app.core.prompts import PromptLoader


def create_tutor_graph(llm: BaseLLM, prompts: PromptLoader):
    """튜터 워크플로우 그래프 생성
    
    Args:
        llm: LLM 인스턴스
        prompts: 프롬프트 로더
        
    Returns:
        컴파일된 LangGraph 앱
    
    Graph Structure:
    ```
    START → analyze_intent → generate_response → END
                  ↓
           [needs_clarification]
                  ↓
              clarify → generate_response → END
    ```
    """
    # 노드 함수 생성
    analyze_intent = create_analyze_intent_node(llm)
    generate_response = create_generate_response_node(llm, prompts)
    
    # 그래프 생성
    workflow = StateGraph(TutorState)
    
    # 노드 추가
    workflow.add_node("analyze_intent", analyze_intent)
    workflow.add_node("generate_response", generate_response)
    
    # 간단한 선형 흐름 (추후 분기 추가 가능)
    workflow.add_edge(START, "analyze_intent")
    workflow.add_edge("analyze_intent", "generate_response")
    workflow.add_edge("generate_response", END)
    
    # 그래프 컴파일
    return workflow.compile()


class TutorGraph:
    """튜터 그래프 래퍼 클래스
    
    서비스에서 사용하기 편리하도록 래핑합니다.
    """
    
    def __init__(self, llm: BaseLLM, prompts: PromptLoader):
        self.graph = create_tutor_graph(llm, prompts)
    
    def invoke(self, state: TutorState) -> TutorState:
        """그래프 실행
        
        Args:
            state: 초기 상태
            
        Returns:
            최종 상태 (응답 포함)
        """
        return self.graph.invoke(state)
    
    async def ainvoke(self, state: TutorState) -> TutorState:
        """비동기 그래프 실행"""
        return await self.graph.ainvoke(state)
