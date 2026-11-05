# DASH: Developers' Algorithm Study Hub

<div align="center">
  <img src="frontend/public/images/icons/icon-512.png" alt="DASH Logo" width="120px" height="120px" />
  <br/>
  <h3>올인원 알고리즘 스터디 플랫폼</h3>
  <br/>

  [![Java Version](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=java)](https://www.java.com/)
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-6DB33F?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
  [![Vue.js](https://img.shields.io/badge/Vue.js-3.3.4-4FC08D?style=flat-square&logo=vue.js)](https://vuejs.org/)
  [![FastAPI](https://img.shields.io/badge/FastAPI-0.112.0-009688?style=flat-square&logo=fastapi)](https://fastapi.tiangolo.com/)
  [![Gemini](https://img.shields.io/badge/AI-Google%20Gemini-8E75B2?style=flat-square&logo=google)](https://deepmind.google/technologies/gemini/)
  [![DashHub](https://img.shields.io/badge/Powered%20by-DashHub-blue?style=flat-square&logo=googlechrome&logoColor=white)](https://chromewebstore.google.com/detail/kimjgflahdmnlhilmojcoaechlgkokhc)
</div>

---

## 📖 Introduction

**DASH**는 알고리즘 스터디의 고질적인 문제를 해결하는 통합 플랫폼입니다.

- 커리큘럼 부재 → 스킬 트리·로드맵
- 형식적인 코드 리뷰 → AI 코드 분석
- 번거로운 수동 기록 → Chrome Extension·GitHub Webhook 자동화

<img width="2880" height="1704" alt="Platform-Main" src="https://github.com/user-attachments/assets/966a6d4d-31b8-46cf-b7df-0abd342b21db" />

> 전체 화면과 기능 설명은 [Showcase](docs/SHOWCASE.md)에서 볼 수 있습니다.

### 💡 Core Value
- **Zero-Manual**: 제출하면 커밋·기록·대시보드 갱신까지 자동
- **Data-Driven**: Solved.ac ID만으로 실력 지표와 취약점 분석
- **AI Coaching**: Gemini 기반 복잡도 분석·코드 리뷰·튜터
- **Gamification**: 스킬 트리, 모의고사, 랜덤 디펜스

---

## 🛠 Tech Stack

| Module | Stack |
|:---|:---|
| Backend | Java 17, Spring Boot 3.5.8, MyBatis, MySQL, Spring Security (OAuth2) |
| Frontend | Vue 3, Vite, Pinia, TailwindCSS, Chart.js, Vue Flow, Monaco Editor |
| AI | FastAPI, Google Gemini (google-genai), Pydantic |

### Key Engineering Decisions
1. **DIP 적용**: Domain 레이어를 인프라 기술과 분리
2. **비동기 처리**: GitHub Push 이벤트를 워커가 백그라운드 처리
3. **크롬 익스텐션**: DOM 파싱 대신 제출 코드를 직접 수집
4. **오답 수집**: 실패한 코드도 학습 데이터로 활용

---

## 📚 Documentation

| 문서 | 내용 |
|:---|:---|
| [Getting Started](docs/GETTING_STARTED.md) | 로컬 실행, 프로젝트 구조 |
| [Showcase](docs/SHOWCASE.md) | 화면, 주요 기능 |
| [System Architecture](docs/SYSTEM_ARCHITECTURE.md) | 시스템 구성도 |
| [Backend Architecture](backend/docs/ARCHITECTURE.md) | 계층 규칙, 코딩 컨벤션 |
| [Auth & Security](docs/AUTH_AND_SECURITY.md) | 인증·보안 전략 |
| [AI Server](ai/README.md) | AI 서버 API, 구조 |
| [Contributing](CONTRIBUTING.md) | 브랜치·커밋 컨벤션 |

---

## 👨‍💻 Contributors

| Role | Name | Responsibility |
|:---:|:---:|:---|
| PM / BE / FE | [유승준](https://github.com/utact) | 기획, 설계, Backend Core, DB, Extension |
| AI / BE / FE | [김용수](https://github.com/canit0221) | AI 모델링·서빙, Backend API, Frontend Core, 시각화 |
| BE / FE | [유소민](https://github.com/SoMin-Yoo) | Extension, UI/UX, QA, 코드 품질 |

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
