"""
Dash AI Server - AI 튜터 라우트
"""

from fastapi import APIRouter, Depends

from app.schemas.tutor_chat import TutorChatRequest, TutorChatResponse
from app.services.tutor_chat_service import TutorChatService
from app.api.dependencies import get_tutor_chat_service

router = APIRouter(prefix="/tutor", tags=["Tutor"])


@router.post("/chat", response_model=TutorChatResponse)
async def tutor_chat(
    request: TutorChatRequest,
    service: TutorChatService = Depends(get_tutor_chat_service)
) -> TutorChatResponse:
    """AI 튜터 대화 엔드포인트
    
    맞은 문제와 틀린 문제 모두에 대해 대화형 튜터링을 제공합니다.
    - 맞은 문제: 코드 리뷰, 최적화 조언
    - 틀린 문제: 디버깅 힌트, 문제점 분석
    """
    return service.chat(request)


