"""
Dash AI Server - LLM 팩토리

설정에 따라 적절한 LLM 인스턴스를 생성하는 팩토리
"""

from typing import Literal
from functools import lru_cache

from .base import BaseLLM
from .gemini import GeminiLLM
from app.config import Settings


class LLMFactory:
    """LLM 인스턴스 팩토리
    
    설정에 따라 Gemini, OpenAI 등 적절한 LLM 구현체를 생성합니다.
    """
    
    @staticmethod
    def create(settings: Settings) -> BaseLLM:
        """설정 기반 LLM 인스턴스 생성
        
        Args:
            settings: 애플리케이션 설정
            
        Returns:
            BaseLLM 구현체
            
        Raises:
            ValueError: 지원하지 않는 provider인 경우
        """
        if settings.llm_provider == "gemini":
            return GeminiLLM(
                api_key=settings.gemini_api_key,
                model=settings.default_model,
                temperature=settings.temperature,
                max_tokens=settings.max_tokens
            )
        elif settings.llm_provider == "openai":
            # TODO: OpenAI 구현체 추가
            raise NotImplementedError("OpenAI provider is not yet implemented")
        else:
            raise ValueError(f"Unsupported LLM provider: {settings.llm_provider}")


@lru_cache
def get_llm(provider: Literal["gemini", "openai"] = "gemini") -> BaseLLM:
    """싱글톤 LLM 인스턴스 반환
    
    FastAPI dependency로 사용됩니다.
    """
    from app.config import get_settings
    settings = get_settings()
    return LLMFactory.create(settings)
