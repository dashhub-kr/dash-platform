# Dash AI Server

알고리즘 코드 분석 및 대화형 튜터링 AI 서버

## 🚀 Features

| 기능 | 엔드포인트 | 설명 |
|------|-----------|------|
| **코드 리뷰** | `POST /review` | 알고리즘 코드 분석 및 리팩토링 제안 |
| **힌트 생성** | `POST /hint` | 레벨별 맞춤 힌트 (1-3) |
| **학습 경로** | `POST /learning-path` | AI 기반 개인화 학습 로드맵 |
| **코딩 스타일** | `POST /coding-style` | MBTI 스타일 코딩 성향 분석 |
| **대화형 튜터** | `POST /tutor/chat` | LangGraph 기반 소크라테스 튜터 |

## 🏗️ Architecture

```
app/
├── main.py              # FastAPI 엔트리포인트 (~50줄)
├── config.py            # pydantic-settings 설정
│
├── api/                 # API 레이어
│   ├── router.py        # 라우터 통합
│   ├── dependencies.py  # 의존성 주입
│   └── routes/          # 5개 라우트
│
├── schemas/             # Pydantic DTOs
│   ├── common.py
│   ├── review.py
│   ├── hint.py
│   ├── learning_path.py
│   ├── coding_style.py
│   └── tutor.py
│
├── services/            # 비즈니스 로직
│   ├── base.py          # 베이스 서비스
│   ├── review_service.py
│   ├── hint_service.py
│   ├── learning_path_service.py
│   ├── coding_style_service.py
│   └── tutor_service.py # LangGraph 통합
│
└── core/                # 핵심 인프라
    ├── llm/             # LLM 추상화 (LangChain)
    ├── prompts/         # 프롬프트 로더 + 7개 템플릿
    └── graphs/          # LangGraph 워크플로우
```

## 🛠️ Tech Stack

- **Framework**: FastAPI
- **LLM**: Google Gemini 2.5 Flash
- **Abstraction**: LangChain
- **State Management**: LangGraph
- **Validation**: Pydantic v2

## 📦 Installation

```bash
# 가상환경 생성
python -m venv .venv

# macOS/Linux
source .venv/bin/activate

# Windows (PowerShell)
.venv\Scripts\Activate.ps1

# 의존성 설치
pip install -r requirements.txt

# 환경변수 설정
# macOS/Linux
cp .env.example .env

# Windows (PowerShell)
Copy-Item .env.example .env

# .env 파일에 GEMINI_API_KEY 설정
```

## 🚀 Running

```bash
# 개발 서버 (새 구조)
uvicorn app.main:app --reload --port 8000
```

## 📡 API Examples

### 코드 리뷰
```bash
curl -X POST http://localhost:8000/review \
  -H "Content-Type: application/json" \
  -d '{"code": "public class Solution {...}", "language": "java"}'
```

### 힌트 생성
```bash
curl -X POST http://localhost:8000/hint \
  -H "Content-Type: application/json" \
  -d '{"problemNumber": "1000", "problemTitle": "A+B", "level": 1}'
```

### 튜터 채팅 (LangGraph)
```bash
curl -X POST http://localhost:8000/tutor/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "DP가 뭐예요?",
    "context": {"tier": "Silver IV", "solvedCount": 50}
  }'
```

## 📄 API Documentation

- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## 📝 Version History

- **v2.0.0**: 클린 아키텍처 리팩토링, LangGraph 통합
- **v1.0.0**: 초기 구현 (모놀리식)

## 📜 License

MIT License
