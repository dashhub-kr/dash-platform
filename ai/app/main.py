"""
Dash AI Server - FastAPI 애플리케이션 진입점
"""

import logging
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import get_settings
from app.api import api_router

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format="%(levelname)s:%(name)s:%(message)s"
)


def create_app() -> FastAPI:
    """FastAPI 애플리케이션 팩토리"""
    settings = get_settings()
    
    app = FastAPI(
        title=settings.app_name,
        description="알고리즘 코드 분석 및 튜터링 AI 서버",
        version=settings.app_version,
        docs_url="/docs",
        redoc_url="/redoc",
    )
    
    # 내부 서비스(백엔드)만 호출하므로 브라우저 교차 출처 요청은 허용하지 않는다
    app.add_middleware(
        CORSMiddleware,
        allow_origins=[],
        allow_credentials=False,
        allow_methods=["GET", "POST"],
        allow_headers=["Content-Type"],
    )
    
    # 라우터 등록
    app.include_router(api_router)
    
    return app


# 애플리케이션 인스턴스
app = create_app()


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)


