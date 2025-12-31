"""
Dash AI Server - 커스텀 예외

애플리케이션 전용 예외 클래스 정의
"""


class DashAIException(Exception):
    """Dash AI 서버 기본 예외"""
    
    def __init__(self, message: str, details: dict | None = None):
        self.message = message
        self.details = details or {}
        super().__init__(self.message)


class LLMException(DashAIException):
    """LLM 관련 예외"""
    pass


class PromptNotFoundException(DashAIException):
    """프롬프트 템플릿을 찾을 수 없을 때"""
    pass


class InvalidRequestException(DashAIException):
    """잘못된 요청"""
    pass


class ConfigurationException(DashAIException):
    """설정 오류"""
    pass
