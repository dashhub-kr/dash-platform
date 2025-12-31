"""
Dash AI Server - LLM 추상화 인터페이스

모든 LLM 구현체가 따라야 하는 추상 베이스 클래스
"""

from abc import ABC, abstractmethod
from pydantic import BaseModel
from typing import TypeVar, Generic

T = TypeVar('T', bound=BaseModel)


class BaseLLM(ABC):
    """LLM 추상 인터페이스
    
    모든 LLM 구현체(Gemini, OpenAI 등)는 이 인터페이스를 구현해야 합니다.
    이를 통해 모델 교체가 용이해지고 테스트 시 Mock 주입이 가능합니다.
    """
    
    @abstractmethod
    def generate(
        self, 
        prompt: str, 
        response_schema: type[T],
        system_instruction: str | None = None
    ) -> T:
        """구조화된 JSON 응답 생성
        
        Args:
            prompt: 사용자 프롬프트
            response_schema: Pydantic 모델 (응답 스키마)
            system_instruction: 시스템 명령어 (optional)
            
        Returns:
            response_schema 타입의 인스턴스
        """
        pass
    
    @abstractmethod
    def chat(
        self, 
        messages: list[dict],
        response_schema: type[T] | None = None,
        system_instruction: str | None = None
    ) -> str | T:
        """대화형 응답 생성
        
        Args:
            messages: 대화 히스토리 [{"role": "user"|"assistant", "content": "..."}]
            response_schema: Pydantic 모델 (optional, 구조화 응답 시)
            system_instruction: 시스템 명령어 (optional)
            
        Returns:
            문자열 또는 response_schema 타입의 인스턴스
        """
        pass
    
    @abstractmethod
    def generate_text(self, prompt: str, system_instruction: str | None = None) -> str:
        """단순 텍스트 응답 생성
        
        Args:
            prompt: 사용자 프롬프트
            system_instruction: 시스템 명령어 (optional)
            
        Returns:
            생성된 텍스트
        """
        pass
