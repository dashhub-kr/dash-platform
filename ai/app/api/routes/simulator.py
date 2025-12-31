"""
Dash AI Server - Simulator Route
"""

from fastapi import APIRouter, Depends
from pydantic import BaseModel, Field
from app.services.simulator_service import SimulatorService, SimulatorOutput
from app.api.dependencies import get_llm, get_prompts
from app.core.llm import BaseLLM
from app.core.prompts import PromptLoader

router = APIRouter(prefix="/simulator", tags=["Simulator"])

class SimulatorRequest(BaseModel):
    code: str = Field(..., description="소스 코드")
    language: str = Field(default="java", description="프로그래밍 언어")

def get_simulator_service(
    llm: BaseLLM = Depends(get_llm),
    prompts: PromptLoader = Depends(get_prompts)
) -> SimulatorService:
    return SimulatorService(llm=llm, prompts=prompts)

@router.post("/run", response_model=SimulatorOutput)
async def run_simulation(
    request: SimulatorRequest,
    service: SimulatorService = Depends(get_simulator_service)
):
    """코드 시뮬레이션 실행"""
    return service.simulate(request.code, request.language)
