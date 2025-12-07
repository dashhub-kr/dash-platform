# DASH 프로젝트

SSAFY 14기 1학기 관통 프로젝트 – DASH 저장소입니다.  
현재 **서비스 개발 단계**로, 상세 소개는 추후 업데이트됩니다.

---

## 목차

1. [기술 스택](#기술-스택)
2. [백엔드 개발/실행](#백엔드-개발실행)
3. [프론트엔드 개발/실행](#프론트엔드-개발실행)
4. [폴더 구조](#폴더-구조)
5. [커밋/PR/테스트 가이드](#커밋pr테스트-가이드)
6. [문서 & 참고](#문서--참고)
7. [헬스 체크](#헬스-체크)

---

## 기술 스택

### 백엔드

- **Java 17 (LTS)**, **Spring Boot 3.5**
- **MySQL 8.0+**, **MyBatis**
- **Spring Security**, **OAuth 2.0 (GitHub)**
- **JUnit 5**, **Mockito**, **AssertJ**
- **OpenAPI(Swagger UI)**

### 프론트엔드

- **Vue 3**, **Vite**, **Tailwind CSS**
- **JavaScript (ES2020+)**

---

## 백엔드 개발/실행

```bash
cd backend
./mvnw spring-boot:run
```

> MySQL 실행 필요. DB 설정은 `backend/src/main/resources/application.properties` 참고

### 테스트/패키징

```bash
./mvnw test                 # 전체 테스트
./mvnw -Dtest=클래스명 test # 특정 테스트만
./mvnw package              # 패키징
```

---

## 프론트엔드 개발/실행

```bash
cd frontend
npm install        # 최초 1회
npm run dev        # 개발 서버 (http://localhost:5173)
npm run build      # 프로덕션 빌드
npm run preview    # 빌드 결과 미리보기
```

---

## 폴더 구조

```
├── backend/   # Spring Boot 백엔드
│   ├── src/main/java/...
│   ├── src/main/resources/mapper/...
│   └── docs/  # 아키텍처/테스트 가이드 등
├── frontend/  # Vue3 + Vite 프론트엔드
│   ├── src/
│   │   ├── main.js         # 진입점
│   │   ├── App.vue         # 루트 컴포넌트
│   │   ├── router/         # 라우터
│   │   ├── views/          # 페이지 단위 뷰
│   │   ├── components/     # 공통 컴포넌트
│   │   ├── composables/    # 커스텀 훅
│   │   └── styles.css      # Tailwind 확장
│   ├── public/             # 정적 리소스
│   └── docs/               # 프론트 리팩터링/페이지 문서
├── AGENTS.md, README.md, ...
```

---

## 커밋/PR/테스트 가이드

- **커밋**: `feat|fix|docs|style|refactor|test|chore` + 선택적 범위, 예) `refactor(user): 도메인 강화`
- **PR**: 이슈 링크, 변경 요약, 테스트 결과(스크린샷/테스트 로그) 첨부 권장
- **백엔드 테스트**: JUnit5 + Mockito + AssertJ, 상세 가이드는 [`backend/docs/TESTING_GUIDE.md`](backend/docs/TESTING_GUIDE.md)
- **프론트엔드 테스트**: (Vitest/Cypress 미도입, 추후 필요시 추가)

---

## 문서 & 참고

- [백엔드 아키텍처 & 코딩 규칙](backend/docs/ARCHITECTURE.md)
- [테스트 가이드](backend/docs/TESTING_GUIDE.md)
- [기여 가이드](CONTRIBUTING.md)

---

## 헬스 체크

- `GET /actuator/health`: 기본 애플리케이션 상태
- `GET /actuator/health/dash`: GitHub 웹훅, OAuth 토큰 등 DASH 전용 지표
