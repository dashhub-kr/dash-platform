"""
Dash AI Server - LangGraph 노드 정의

튜터 워크플로우의 각 노드(처리 단계) 구현
네이티브 LangChain 멀티턴 메시지 지원
"""

from typing import Dict, Any
from langchain_core.messages import HumanMessage, AIMessage, SystemMessage

from .states import TutorState
from app.core.llm import BaseLLM
from app.core.prompts import PromptLoader


def create_analyze_intent_node(llm: BaseLLM):
    """의도 분석 노드 팩토리"""
    
    def analyze_intent(state: TutorState) -> Dict[str, Any]:
        """사용자 의도 분석
        
        사용자의 메시지를 분석하여 의도를 파악합니다:
        - question: 개념에 대한 질문
        - help: 문제 풀이 도움 요청
        - clarify: 이전 설명에 대한 추가 질문
        - feedback: 코드에 대한 피드백 요청
        """
        message = state.get("message", "")
        code = state.get("code")
        problem = state.get("problem_number")
        
        # 간단한 규칙 기반 의도 분류
        if code:
            intent = "feedback"
        elif problem:
            intent = "help"
        elif any(kw in message for kw in ["뭐", "무엇", "어떻게", "왜", "?", "설명"]):
            intent = "question"
        else:
            intent = "clarify"
        
        # 교수법 결정
        if intent == "question":
            teaching_style = "socratic"  # 질문으로 유도
        elif intent == "feedback":
            teaching_style = "direct"  # 직접적 피드백
        else:
            teaching_style = "hint"  # 힌트 제공
        
        return {
            "intent": intent,
            "teaching_style": teaching_style,
            "needs_clarification": False
        }
    
    return analyze_intent


def create_generate_response_node(llm: BaseLLM, prompts: PromptLoader):
    """응답 생성 노드 팩토리 (네이티브 멀티턴 지원)"""
    
    def generate_response(state: TutorState) -> Dict[str, Any]:
        """튜터 응답 생성 (LangChain 네이티브 멀티턴)
        
        이전 대화 히스토리를 LangChain 메시지 객체로 변환하여
        모델이 대화 맥락을 정확히 이해할 수 있도록 합니다.
        """
        from app.schemas.tutor import TutorChatResponse
        
        # 컨텍스트 추출
        ctx = state.get("context", {})
        tier = ctx.get("tier", "Unrated")
        solved_count = ctx.get("solved_count", 0)
        recent_tags = ", ".join(ctx.get("recent_tags", [])[:5]) or "없음"
        
        # 문제/코드 섹션
        problem_section = ""
        if state.get("problem_number"):
            problem_section = f"## 관련 문제\n- 문제 번호: {state['problem_number']}"
        
        code_section = ""
        if state.get("code"):
            code_section = f"## 학생의 코드\n```\n{state['code'][:500]}\n```"
        
        # 시스템 프롬프트 구성
        system_prompt = prompts.get("tutor_system")
        system_prompt = system_prompt.format(
            tier=tier,
            solved_count=solved_count,
            recent_tags=recent_tags,
            problem_section=problem_section,
            code_section=code_section
        )
        
        # 대화 히스토리를 LangChain 메시지로 변환
        messages = _build_messages(
            history=state.get("history", []),
            current_message=state.get("message", "")
        )
        
        # LLM 호출 (네이티브 멀티턴)
        response = llm.chat(
            messages=messages,
            response_schema=TutorChatResponse,
            system_instruction=system_prompt
        )
        
        return {
            "reply": response.reply,
            "follow_up_questions": response.follow_up_questions,
            "concept_explanation": response.concept_explanation,
            "encouragement": response.encouragement,
            "teaching_style": response.teaching_style
        }
    
    return generate_response


def _build_messages(history: list, current_message: str) -> list[dict]:
    """대화 히스토리를 LangChain 메시지 형식으로 변환
    
    Args:
        history: 이전 대화 히스토리 [{"role": "user"|"assistant", "content": "..."}]
        current_message: 현재 사용자 메시지
        
    Returns:
        LangChain chat() 메서드용 메시지 리스트
    """
    messages = []
    
    # 이전 대화 추가 (최근 10턴)
    for msg in history[-10:]:
        role = msg.get("role", "user")
        content = msg.get("content", "")
        messages.append({"role": role, "content": content})
    
    # 현재 메시지 추가
    messages.append({"role": "user", "content": current_message})
    
    return messages
