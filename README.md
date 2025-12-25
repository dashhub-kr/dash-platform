# 🚀 DASH - Developer's Algorithm Study Hub

![Vue.js](https://img.shields.io/badge/Vue.js-3.x-4FC08D?logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot)
![FastAPI](https://img.shields.io/badge/FastAPI-0.100+-009688?logo=fastapi)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)

**알고리즘 학습을 위한 올인원 플랫폼**  
스터디 관리, AI 코드 분석, 랜덤 디펜스, 모의고사 등 개발자 성장을 위한 다양한 기능을 제공합니다.

> SSAFY 14기 자율 프로젝트

---

## ✨ 주요 기능

### 📊 개인 학습
- **대시보드**: 풀이 현황, Streak, 티어 등 학습 통계 한눈에 확인
- **스킬 트리**: 알고리즘 태그별 숙련도 시각화
- **학습 로드맵**: AI 기반 맞춤형 4단계 학습 경로 추천

### 🎯 훈련 모드
- **랜덤 디펜스**: 티어 기반 랜덤 문제 제한시간 내 풀이 도전
- **모의고사**: 삼성, 카카오 등 실제 코딩테스트 환경 시뮬레이션

### 👥 스터디
- **미션 관리**: 주차별 문제 배정 및 진행률 트래킹
- **팀 분석**: 팀원별 실력 분석 및 AI 커리큘럼 추천
- **도토리 시스템**: 스터디 활동 보상 재화

### 🤖 AI 서비스
- **코드 리뷰**: 복잡도 분석, 패턴 인식, 개선점 제안
- **AI 튜터**: 소크라테스식 1:1 문답 튜터링
- **반례 생성**: 틀린 코드에 대한 반례 자동 생성
- **학습 경로**: 개인화된 단계별 커리큘럼

### 💬 커뮤니티
- **코드 리뷰 게시판**: 라인별 댓글로 상세한 코드 피드백

---

## 🛠 기술 스택

| 영역 | 기술 |
|:---|:---|
| **Frontend** | Vue 3, Vite, Tailwind CSS, Pinia, Chart.js |
| **Backend** | Spring Boot 3.5, Java 17, MyBatis, Spring Security |
| **AI Service** | FastAPI, Python 3.9+, Google Gemini |
| **Database** | MySQL 8.0 |
| **External** | GitHub API (OAuth, Webhook), Solved.ac API |

---

## 🚀 시작하기

### 백엔드
```bash
cd backend
./mvnw spring-boot:run
```
> MySQL 실행 필요. 설정: `backend/src/main/resources/application.properties`

### 프론트엔드
```bash
cd frontend
npm install
npm run dev    # http://localhost:5173
```

### AI 서비스
```bash
cd ai
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```
> `.env` 파일에 `GEMINI_API_KEY` 설정 필요

---

## 📁 프로젝트 구조

```
├── backend/           # Spring Boot API 서버
│   ├── src/main/java/com/ssafy/dash/
│   │   ├── algorithm/     # 알고리즘 기록
│   │   ├── ai/            # AI 연동
│   │   ├── study/         # 스터디 관리
│   │   ├── board/         # 게시판
│   │   └── oauth/         # GitHub OAuth
│   └── docs/              # 백엔드 문서
├── frontend/          # Vue 3 클라이언트
│   ├── src/views/         # 페이지 컴포넌트
│   ├── src/components/    # 공통 컴포넌트
│   └── DESIGN_SYSTEM.md   # 디자인 시스템
├── ai/                # FastAPI AI 서버
│   └── app/
│       ├── services/      # AI 비즈니스 로직
│       └── schemas/       # Pydantic 스키마
└── docs/              # 프로젝트 문서
```

---

## 👥 팀원

| 이름 | 역할 |
|:---|:---|
| 김용수 | AI, BE, FE|
| 유승준 | PM, BE ,FE|

---

## 📄 라이선스

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
