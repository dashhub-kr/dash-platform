"""
Dash AI Server - 코드 리뷰 라우트
"""

from fastapi import APIRouter, Depends

from app.schemas.review import ReviewRequest, ReviewResponse
from app.services import ReviewService
from app.api.dependencies import get_review_service

router = APIRouter(prefix="/review", tags=["Code Review"])


@router.post("", response_model=ReviewResponse)
async def review_code(
    request: ReviewRequest,
    service: ReviewService = Depends(get_review_service)
) -> ReviewResponse:
    """코드 분석 엔드포인트
    
    알고리즘 풀이 코드를 AI로 분석합니다.
    
    - 요약 및 문제 추론
    - 알고리즘 패턴 분석
    - 복잡도 분석
    - 주의사항 및 개선점
    - 리팩토링 제안
    """
    return service.analyze(request)


