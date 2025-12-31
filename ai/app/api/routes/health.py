"""
Dash AI Server - 헬스 체크 라우트
"""

from fastapi import APIRouter

router = APIRouter(tags=["Health"])


@router.get("/")
async def root():
    """루트 엔드포인트"""
    return {"status": "ok", "service": "Dash AI Server", "version": "2.0.0"}


@router.get("/health")
async def health_check():
    """헬스 체크"""
    return {"status": "healthy"}
