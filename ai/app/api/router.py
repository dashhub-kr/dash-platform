"""
Dash AI Server - API 라우터 통합

모든 라우터를 하나의 APIRouter에 통합
"""

from fastapi import APIRouter

from .routes import (
    health_router,
    review_router,
    learning_path_router,
    coding_style_router,
    tutor_router,
    debug_router,
    simulator_router,
)

# 메인 API 라우터
api_router = APIRouter()

# 라우터 등록
api_router.include_router(health_router)
api_router.include_router(review_router)
api_router.include_router(learning_path_router)
api_router.include_router(coding_style_router)
api_router.include_router(tutor_router)
api_router.include_router(debug_router)
api_router.include_router(simulator_router)

