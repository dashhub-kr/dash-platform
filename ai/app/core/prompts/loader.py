"""
Dash AI Server - 프롬프트 로더

프롬프트 템플릿 파일을 로드하고 관리하는 유틸리티
"""

from pathlib import Path
from functools import lru_cache
from typing import Callable


class PromptLoader:
    """프롬프트 템플릿 로더
    
    templates/ 디렉토리에서 프롬프트 파일을 로드합니다.
    캐싱을 통해 반복 로드를 방지합니다.
    
    Example:
        loader = PromptLoader()
        prompt = loader.get("review")
        formatted = loader.format("hint", level=1, problem="...")
    """
    
    def __init__(self, templates_dir: str | Path | None = None):
        """
        Args:
            templates_dir: 템플릿 디렉토리 경로 (기본: app/core/prompts/templates)
        """
        if templates_dir is None:
            # 기본 경로 설정
            self.templates_dir = Path(__file__).parent / "templates"
        else:
            self.templates_dir = Path(templates_dir)
        
        self._cache: dict[str, str] = {}
    
    def get(self, name: str) -> str:
        """프롬프트 템플릿 로드
        
        Args:
            name: 템플릿 이름 (확장자 제외)
            
        Returns:
            프롬프트 템플릿 문자열
            
        Raises:
            FileNotFoundError: 템플릿 파일이 없는 경우
        """
        if name in self._cache:
            return self._cache[name]
        
        # .txt 또는 .jinja2 확장자 시도
        for ext in [".txt", ".jinja2", ".md"]:
            file_path = self.templates_dir / f"{name}{ext}"
            if file_path.exists():
                content = file_path.read_text(encoding="utf-8")
                self._cache[name] = content
                return content
        
        raise FileNotFoundError(f"Prompt template not found: {name}")
    
    def format(self, name: str, **kwargs) -> str:
        """프롬프트 포맷팅
        
        Args:
            name: 템플릿 이름
            **kwargs: 포맷팅 변수
            
        Returns:
            포맷팅된 프롬프트
        """
        template = self.get(name)
        return template.format(**kwargs)
    
    def list_templates(self) -> list[str]:
        """사용 가능한 템플릿 목록 반환"""
        templates = []
        for ext in ["*.txt", "*.jinja2", "*.md"]:
            templates.extend([
                f.stem for f in self.templates_dir.glob(ext)
            ])
        return sorted(set(templates))
    
    def clear_cache(self):
        """캐시 초기화"""
        self._cache.clear()


@lru_cache
def get_prompt_loader() -> PromptLoader:
    """싱글톤 프롬프트 로더 반환"""
    return PromptLoader()
