"""
Dash AI Server - 디버그 라우트
"""

from fastapi import APIRouter, Depends

from app.schemas.debug import CounterExampleRequest, CounterExampleResponse
from app.services import DebugService
from app.api.dependencies import get_debug_service

router = APIRouter(prefix="/debug", tags=["Debug"])


@router.post("/counter-example", response_model=CounterExampleResponse, response_model_by_alias=True)
async def generate_counter_example(
    request: CounterExampleRequest,
    service: DebugService = Depends(get_debug_service)
) -> CounterExampleResponse:
    """반례 생성 엔드포인트
    
    제출한 오답 코드에 대해 실패하는 반례(Input)와 설명(Explanation)을 생성합니다.
    """
    return service.generate_counter_example(request)
