"""
Dash AI Server - 베이스 서비스

모든 서비스의 공통 기능을 제공하는 베이스 클래스
"""

from abc import ABC
from app.core.llm import BaseLLM
from app.core.prompts import PromptLoader


class BaseService(ABC):
    """베이스 서비스 클래스
    
    모든 AI 서비스가 상속받는 추상 베이스 클래스입니다.
    LLM과 프롬프트 로더를 의존성으로 주입받습니다.
    """
    
    def __init__(self, llm: BaseLLM, prompts: PromptLoader):
        """
        Args:
            llm: LLM 인스턴스
            prompts: 프롬프트 로더
        """
        self.llm = llm
        self.prompts = prompts
