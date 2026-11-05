# Dash AI Server

알고리즘 코드 분석과 대화형 튜터링을 제공하는 FastAPI 서버입니다. 백엔드에서만 호출합니다.

> [README로 돌아가기](../README.md)

## Endpoints

| 기능 | 엔드포인트 | 설명 |
|------|-----------|------|
| 코드 리뷰 | `POST /review` | 복잡도 분석, 리팩토링 제안 |
| 튜터 채팅 | `POST /tutor/chat` | 대화형 튜터 |
| 학습 경로 | `POST /learning-path` | 개인화 학습 로드맵 |
| 코딩 스타일 | `POST /coding-style` | 코드 성향 분석 |
| 반례 생성 | `POST /debug/counter-example` | 오답 코드 반례 |
| 시뮬레이터 | `POST /simulator/run` | 코드 실행 결과 예측 |
| 헬스 체크 | `GET /health` | |

요청·응답 스키마는 서버 실행 후 `http://localhost:8000/docs`에서 확인할 수 있습니다.

## Structure

```
app/
├── main.py         # 엔트리포인트
├── config.py       # 설정 (pydantic-settings)
├── api/routes/     # 라우트
├── schemas/        # 요청·응답 모델
├── services/       # 비즈니스 로직
└── core/
    ├── llm/        # LLM 클라이언트
    └── prompts/    # 프롬프트 템플릿
```

## Tech Stack
- FastAPI, Pydantic v2
- Google Gemini (`google-genai`)

## Running

```bash
python -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r requirements.txt
echo "GEMINI_API_KEY=..." > .env
uvicorn app.main:app --reload --port 8000
```

## Example

```bash
curl -X POST http://localhost:8000/review \
  -H "Content-Type: application/json" \
  -d '{"code": "public class Main {...}", "language": "java", "problemNumber": "1000"}'
```
