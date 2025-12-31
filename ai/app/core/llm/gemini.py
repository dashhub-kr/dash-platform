"""
Dash AI Server - Gemini LLM 구현체

Google Native SDK (google-genai)를 사용한 Gemini 모델 래퍼
"""

import logging
from google import genai
from google.genai import types
from pydantic import BaseModel
from typing import TypeVar

from .base import BaseLLM

logger = logging.getLogger(__name__)

T = TypeVar('T', bound=BaseModel)


class GeminiLLM(BaseLLM):
    """Google Gemini LLM 구현체 (Native SDK 기반)
    
    Google의 공식 google-genai SDK를 사용하여
    BaseLLM 인터페이스를 구현합니다.
    
    Example:
        llm = GeminiLLM(api_key="...", model="gemini-2.5-flash")
        result = llm.generate(prompt, ResponseSchema)
    """
    
    def __init__(
        self, 
        api_key: str, 
        model: str,
        max_tokens: int,
        thinking_level: str | None = None
    ):
        """
        Args:
            api_key: Google API Key
            model: 모델명
            max_tokens: 최대 출력 토큰
            thinking_level: 사고 수준 (None이면 동적, Gemini 3 전용)
        """
        self.model = model
        self.max_tokens = max_tokens
        self.thinking_level = thinking_level
        self.client = genai.Client(api_key=api_key)
    
    def _log_token_usage(self, response, method_name: str = "") -> None:
        """토큰 사용량 로깅"""
        if hasattr(response, 'usage_metadata') and response.usage_metadata:
            usage = response.usage_metadata
            input_tokens = getattr(usage, 'prompt_token_count', 0) or 0
            output_tokens = getattr(usage, 'candidates_token_count', 0) or 0
            total_tokens = getattr(usage, 'total_token_count', 0) or 0
            thinking_tokens = getattr(usage, 'thoughts_token_count', 0) or 0
            
            # thinking tokens이 있으면 함께 표시
            if thinking_tokens > 0:
                logger.info(
                    f"📊 [{method_name}] 토큰 사용량 - "
                    f"입력: {input_tokens}, 출력: {output_tokens}, "
                    f"사고: {thinking_tokens}, 총합: {total_tokens}"
                )
            else:
                logger.info(
                    f"📊 [{method_name}] 토큰 사용량 - "
                    f"입력: {input_tokens}, 출력: {output_tokens}, 총합: {total_tokens}"
                )
    
    def _build_config(
        self, 
        response_schema: type[BaseModel] | None = None,
        system_instruction: str | None = None
    ) -> types.GenerateContentConfig:
        """공통 설정 빌드"""
        config_params = {
            "max_output_tokens": self.max_tokens,
        }
        
        if system_instruction:
            config_params["system_instruction"] = system_instruction
        
        if response_schema:
            config_params["response_mime_type"] = "application/json"
            config_params["response_schema"] = response_schema
        
        # thinking_level이 설정된 경우에만 추가 (Gemini 3 전용)
        if self.thinking_level:
            config_params["thinking_config"] = types.ThinkingConfig(
                thinking_level=self.thinking_level
            )
        
        return types.GenerateContentConfig(**config_params)
    
    def generate(
        self, 
        prompt: str, 
        response_schema: type[T],
        system_instruction: str | None = None
    ) -> T:
        """구조화된 JSON 응답 생성 (Native Structured Output)"""
        logger.info(f"🚀 [generate] 요청 시작 - 스키마: {response_schema.__name__}")
        
        config = self._build_config(response_schema, system_instruction)
        
        response = self.client.models.generate_content(
            model=self.model,
            contents=prompt,
            config=config
        )
        
        self._log_token_usage(response, "generate")
        
        # JSON 파싱 및 Pydantic 모델 변환
        result = response_schema.model_validate_json(response.text)
        return result
    
    def chat(
        self, 
        messages: list[dict],
        response_schema: type[T] | None = None,
        system_instruction: str | None = None
    ) -> str | T:
        """대화형 응답 생성"""
        schema_name = response_schema.__name__ if response_schema else "없음"
        logger.info(f"🚀 [chat] 요청 시작 - 메시지 수: {len(messages)}, 스키마: {schema_name}")
        
        # 메시지를 Native SDK 형식으로 변환
        contents = []
        for msg in messages:
            role = "model" if msg["role"] == "assistant" else "user"
            contents.append(
                types.Content(
                    role=role,
                    parts=[types.Part(text=msg["content"])]
                )
            )
        
        config = self._build_config(response_schema, system_instruction)
        
        response = self.client.models.generate_content(
            model=self.model,
            contents=contents,
            config=config
        )
        
        self._log_token_usage(response, "chat")
        
        if response_schema:
            return response_schema.model_validate_json(response.text)
        else:
            return response.text
    
    def generate_text(self, prompt: str, system_instruction: str | None = None) -> str:
        """단순 텍스트 응답 생성"""
        logger.info(f"🚀 [generate_text] 요청 시작 - 프롬프트 길이: {len(prompt)}자")
        
        config = self._build_config(system_instruction=system_instruction)
        
        response = self.client.models.generate_content(
            model=self.model,
            contents=prompt,
            config=config
        )
        
        self._log_token_usage(response, "generate_text")
        return response.text
