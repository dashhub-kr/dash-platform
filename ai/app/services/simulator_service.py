"""
Dash AI Server - Simulator Service

코드를 실행하지 않고 LLM을 통해 실행 결과를 예측하는 시뮬레이터
"""

from pydantic import BaseModel, Field
from app.services.base import BaseService

class SimulatorOutput(BaseModel):
    stdout: str = Field(description="표준 출력 결과")
    stderr: str = Field(description="에러 메시지 (없으면 빈 문자열)")
    time_complexity: str = Field(description="예상 시간 복잡도 (Big-O)")
    space_complexity: str = Field(description="예상 공간 복잡도 (Big-O)")
    analysis: str = Field(description="코드 실행 흐름에 대한 간단한 분석")

class SimulatorService(BaseService):
    """코드 시뮬레이터 서비스"""

    def simulate(self, code: str, language: str = "java") -> dict:
        """코드 실행 결과 예측"""
        
        system_instruction = """당신은 고성능 '가상 컴파일러(Virtual Compiler)'입니다.
사용자가 제출한 코드를 분석하여 실제 실행되었을 때의 결과를 예측해 주세요.
실제 코드를 실행하지는 않지만, 마치 실행된 것처럼 정확한 stdout/stderr를 생성해야 합니다.

다음 규칙을 엄수하세요:
1. 무한 루프, 인덱스 초과 등 런타임 에러가 예상되면 stderr에 상세히 적으세요.
2. 코드에 문법 오류가 있다면 컴파일 에러 메시지를 stderr에 적으세요.
3. 실행 결과(stdout)는 실제 터미널 출력과 동일하게 포맷팅하세요.
4. 시간/공간 복잡도를 분석하여 제공하세요.
"""

        prompt = f"""
Language: {language}
Code:
```
{code}
```
"""

        # GeminiLLM.generate 메서드를 사용하여 구조화된 출력 생성
        # (BaseService.llm은 GeminiLLM 인스턴스임)
        result: SimulatorOutput = self.llm.generate(
            prompt=prompt,
            response_schema=SimulatorOutput,
            system_instruction=system_instruction
        )
        
        return result
