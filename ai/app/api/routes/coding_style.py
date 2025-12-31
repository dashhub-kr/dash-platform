"""
Dash AI Server - 코딩 스타일 라우트
"""

from fastapi import APIRouter, Depends

from app.schemas.coding_style import CodingStyleRequest, CodingStyleResponse
from app.services import CodingStyleService
from app.api.dependencies import get_coding_style_service

router = APIRouter(prefix="/coding-style", tags=["Coding Style"])


@router.post("", response_model=CodingStyleResponse)
async def analyze_coding_style(
    request: CodingStyleRequest,
    service: CodingStyleService = Depends(get_coding_style_service)
) -> CodingStyleResponse:
    """코딩 스타일 분석 엔드포인트
    
    코드 패턴을 분석하여 MBTI 스타일 결과를 생성합니다.
    
    - 4가지 축 분석 (E/I, S/N, T/F, J/P)
    - MBTI 코드 및 별명
    - 강점/개선점
    - 호환 스타일 및 조언
    """
    return service.analyze(request)
