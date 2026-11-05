# Getting Started

> [README로 돌아가기](../README.md)

## Prerequisites
- Java JDK 17+
- Node.js 18+
- Python 3.10+
- MySQL 8.0+

## 1. Backend
```bash
cd backend
# 환경 변수 설정 (DB, OAuth, AI Server URL 등)
./mvnw clean install
./mvnw spring-boot:run
```

환경 변수는 `backend/src/main/resources/application.properties`를 참고하세요.
암호화 키 `DASH_ENCRYPT_SECRET_KEY`는 32자 이상이어야 합니다.

```bash
openssl rand -base64 48
```

## 2. AI Server
```bash
cd ai
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
# .env에 GEMINI_API_KEY 설정
uvicorn app.main:app --reload
```

## 3. Frontend
```bash
cd frontend
npm install
npm run dev
```

## Project Structure

| 경로 | 설명 |
|:---|:---|
| `backend/` | Spring Boot |
| `backend/.../acorn` | 재화(도토리) |
| `backend/.../ai` | AI 서버 연동 |
| `backend/.../algorithm` | 풀이 기록 |
| `backend/.../analytics` | 통계·분석 |
| `backend/.../board` | 커뮤니티 |
| `backend/.../defense` | 랜덤 디펜스 |
| `backend/.../github` | GitHub Webhook |
| `backend/.../mockexam` | 모의고사 |
| `backend/.../problem` | 문제·태그 |
| `backend/.../study` | 스터디 |
| `frontend/src/api` | Axios API |
| `frontend/src/components` | 공통 컴포넌트 |
| `frontend/src/views` | 페이지 |
| `ai/app/api` | 라우트 |
| `ai/app/core` | 프롬프트·LLM 설정 |
| `ai/app/services` | 비즈니스 로직 |

> `backend/...`는 `backend/src/main/java/com/ssafy/dash`의 줄임입니다.
