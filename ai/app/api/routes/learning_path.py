"""
Dash AI Server - 학습 경로 라우트
"""

from fastapi import APIRouter, Depends

from app.schemas.learning_path import LearningPathRequest, LearningPathResponse
from app.services import LearningPathService
from app.api.dependencies import get_learning_path_service

router = APIRouter(prefix="/learning-path", tags=["Learning Path"])


@router.post("", response_model=LearningPathResponse)
async def generate_learning_path(
    request: LearningPathRequest,
    service: LearningPathService = Depends(get_learning_path_service)
) -> LearningPathResponse:
    """학습 경로 생성 엔드포인트
    
    사용자 분석 데이터 기반 개인화 학습 경로를 생성합니다.
    
    - 전체 평가
    - 핵심 강점/약점
    - 단계별 학습 계획
    - 동기부여 메시지
    """
    return service.generate(request)
