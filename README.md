# 프로젝트: DASH

SSAFY 14기 1학기 관통 프로젝트를 위한 DASH 저장소입니다.  
현재 **서비스 개발 단계**에 있어 상세한 소개는 추후 업데이트 예정입니다.

## 🛠 기술 스택

### ■ 개발환경

- **Java**: 17 (LTS)
- **Build Tool**: Maven
- **Framework**: Spring Boot 3.5.8

### ■ 데이터베이스

- **Database**: MySQL 8.0+
- **Persistence Framework**: MyBatis 3.0.5
- **In-Memory DB (Test)**: H2 Database

### ■ 보안 및 인증

- **Security**: Spring Security
- **Authentication**: OAuth 2.0 (GitHub)

### ■ 문서화

- **API Docs**: SpringDoc OpenAPI (Swagger UI) 2.8.5

### ■ 테스트

- **Framework**: JUnit 5
- **Mocking**: Mockito
- **Assertion**: AssertJ

## 📚 더 알아보기

- [백엔드 아키텍처 & 코딩 규칙](backend/docs/ARCHITECTURE.md)
- [테스트 가이드](backend/docs/TESTING_GUIDE.md)
- [기여 가이드](CONTRIBUTING.md)

## ✅ 헬스 체크

- `GET /actuator/health`: 기본 애플리케이션 상태
- `GET /actuator/health/dash`: GitHub 웹훅 설정, OAuth 토큰 저장소 등 DASH 전용 지표
