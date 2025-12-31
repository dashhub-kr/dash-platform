"""
Dash AI Server - API 의존성

FastAPI Depends로 사용되는 의존성 함수들
"""

from functools import lru_cache

from app.config import get_settings
from app.core.llm import GeminiLLM, BaseLLM
from app.core.prompts import PromptLoader
from app.services import (
    ReviewService,
    LearningPathService,
    CodingStyleService,
    DebugService
)
from app.services.tutor_chat_service import TutorChatService


@lru_cache
def get_llm() -> BaseLLM:
    """LLM 인스턴스 (싱글톤)"""
    settings = get_settings()
    return GeminiLLM(
        api_key=settings.gemini_api_key,
        model=settings.default_model,
        max_tokens=settings.max_tokens,
        thinking_level=settings.thinking_level
    )


@lru_cache
def get_prompts() -> PromptLoader:
    """프롬프트 로더 (싱글톤)"""
    return PromptLoader()


def get_review_service() -> ReviewService:
    """코드 리뷰 서비스"""
    return ReviewService(llm=get_llm(), prompts=get_prompts())


def get_tutor_chat_service() -> TutorChatService:
    """AI 튜터 대화 서비스"""
    return TutorChatService(llm=get_llm(), prompts=get_prompts())


def get_learning_path_service() -> LearningPathService:
    """학습 경로 서비스"""
    return LearningPathService(llm=get_llm(), prompts=get_prompts())


def get_coding_style_service() -> CodingStyleService:
    """코딩 스타일 서비스"""
    return CodingStyleService(llm=get_llm(), prompts=get_prompts())


def get_debug_service() -> DebugService:
    """디버그 서비스"""
    return DebugService(llm=get_llm(), prompts=get_prompts())


