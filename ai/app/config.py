"""
Dash AI Server - 설정 관리

pydantic-settings를 사용한 타입 안전 설정 관리
"""

from pydantic_settings import BaseSettings
from pydantic import Field
from functools import lru_cache
from typing import Literal


class Settings(BaseSettings):
    """애플리케이션 설정"""
    
    # API Keys
    gemini_api_key: str = Field(..., description="Google Gemini API Key")
    openai_api_key: str | None = Field(None, description="OpenAI API Key (optional)")
    
    # LLM Configuration
    default_model: str = Field("gemini-3-flash-preview", description="기본 LLM 모델")
    llm_provider: Literal["gemini", "openai"] = Field("gemini", description="LLM 제공자")
    max_tokens: int = Field(16384, description="최대 토큰 수")
    thinking_level: Literal["minimal", "low", "medium", "high"] | None = Field("high", description="Gemini 3 사고 수준")
    
    # Application Configuration
    app_name: str = Field("Dash AI Server", description="애플리케이션 이름")
    app_version: str = Field("2.0.0", description="애플리케이션 버전")
    debug: bool = Field(False, description="디버그 모드")
    log_level: str = Field("INFO", description="로그 레벨")
    
    # Prompt Configuration
    prompts_dir: str = Field("app/core/prompts/templates", description="프롬프트 템플릿 디렉토리")
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = False
        extra = "ignore"  # 추가 환경변수 무시


@lru_cache
def get_settings() -> Settings:
    """싱글톤 설정 인스턴스 반환"""
    return Settings()
