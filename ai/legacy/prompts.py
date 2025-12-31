"""
이 모듈은 AI 모델에 전달될 프롬프트 템플릿과 로딩 로직을 관리합니다.
"""

from langchain_core.prompts import PromptTemplate
from langchain_core.output_parsers import PydanticOutputParser
from schemas import StructuredResponse

def load_base_prompt(filepath: str = "prompt.txt") -> str:
    """프롬프트 파일 내용을 로드합니다. 파일이 없으면 기본 프롬프트를 반환합니다."""
    try:
        with open(filepath, "r", encoding="utf-8") as f:
            return f.read()
    except FileNotFoundError:
        print(f"[WARNING] {filepath} 파일을 찾을 수 없습니다. 기본 프롬프트를 사용합니다.")
        return """당신은 다른 사람이 작성한 코드를 이해하기 쉽게 코드에 대한 요약, 설명을 제공하는 AI입니다.
필수 구조는 별도 전달되는 스키마가 보장하므로, 각 필드 내용만 한국어로 간결하게 채워주세요."""

def get_code_analysis_prompt_template() -> PromptTemplate:
    """코드 분석을 위한 프롬프트 템플릿을 생성하여 반환합니다."""
    
    # 1. Output Parser (포맷 지시사항 생성을 위해 필요)
    parser = PydanticOutputParser(pydantic_object=StructuredResponse)
    
    # 2. Base Prompt (System Instruction)
    base_prompt = load_base_prompt()
    
    # 3. Create Template
    prompt_template = PromptTemplate(
        template="{system_instruction}\n\n{format_instructions}\n\n분석할 코드:\n{query}",
        input_variables=["query"],
        partial_variables={
            "system_instruction": base_prompt,
            "format_instructions": parser.get_format_instructions()
        }
    )
    
    return prompt_template
