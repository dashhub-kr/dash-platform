# 백엔드 아키텍처 & 코딩 규칙

## 1. 시스템 개요

DASH 백엔드는 Spring Boot 3.x 기반의 단일 애플리케이션이며, **Presentation → Application → Domain ← Infrastructure** 4계층 구조를 따른다. 각 모듈(
Algorithm, Board, User, OAuth, Onboarding, GitHub)은 동일한 규칙을 공유하며, 계층 간 경계를 ArchUnit 테스트(
`backend/src/test/java/com/ssafy/dash/architecture/ArchitectureTest.java`)로 강제한다.

## 2. 계층 규칙

| Layer          | 책임                                                   | 허용 의존성                       | 대표 패키지                                                                                                           |
|----------------|------------------------------------------------------|------------------------------|------------------------------------------------------------------------------------------------------------------|
| Presentation   | REST Endpoint, Request/Response DTO, 인증 컨텍스트 해석      | Application, Application DTO | `..presentation..`, `..presentation.security..`, `..presentation.dto..`                                          |
| Application    | Use Case 조합, 트랜잭션 경계, Domain↔DTO 변환, 외부 Port 호출      | Domain, Application DTO      | `..application..`, `..application.dto..`                                                                         |
| Domain         | 엔티티, 밸류, Repository Port, 비즈니스 예외                    | (없음)                         | `..domain..`                                                                                                     |
| Infrastructure | Repository/Client Adapter, MyBatis Mapper, 외부 API 연동 | Domain(Port 구현), 외부 라이브러리    | `..infrastructure..`, `..infrastructure.persistence..`, `..infrastructure.mapper..`, `..github.infrastructure..` |

핵심 원칙

1. **역의존 금지**: Application/Domain은 Presentation 클래스나 DTO를 참조하지 않는다.
2. **도메인 순수성**: Domain은 Spring/Web/JDBC 의존성이 없고, 시간 값은 호출 측에서 주입한다.
3. **Adapter 격리**: Controller는 인프라 구현체(`..infrastructure..`, `..mapper..`, `..persistence..`)에 직접 접근할 수 없다.
4. **DTO 경계**: Request/Response DTO는 Presentation 전용, Command/Result DTO는 Application/Presentation만 사용한다.

ArchUnit 실행:

```powershell
cd backend
..\mvnw.cmd -pl backend -Dtest=ArchitectureTest test
```

## 3. 모듈 설계 스냅샷

### Algorithm

- `AlgorithmRecordController`가 Multipart 업로드를 받아 `AlgorithmRecordService`로 전달한다.
- 서비스는 `AlgorithmRecordRepository` 포트를 통해 도메인 엔티티를 저장하며, Command/Result DTO를 모두 record로 정의한다.
- 도메인은 `AlgorithmRecord`와 예외(`AlgorithmRecordNotFoundException`)를 통해 부분 업데이트/검증 규칙을 유지한다.
- 인프라는 MyBatis Mapper XML과 `AlgorithmRecordRepositoryImpl`로 구성되어 SQL 세부 정보가 상위에 노출되지 않는다.

### Board

- JSON 기반 CRUD API (`BoardController`)와 `BoardCreate/UpdateRequest` DTO를 갖는다.
- `BoardService`는 작성자 정보를 `UserRepository`로 조회한 뒤 `BoardResult`로 매핑한다.
- 도메인 `Board`는 `applyUpdate`를 통해 제목/내용 검증과 타임스탬프 동기화를 보장한다.
- **확장 필드**: `algorithmRecordId`(풀이 연결), `boardType`(GENERAL/CODE_REVIEW), `likeCount`(추천 수).

### Comment (신규)

- 게시글 종속 REST API (`CommentController`)가 `/api/boards/{boardId}/comments` 경로를 제공한다.
- **대댓글**: `parentId` 필드로 1단계 대댓글 지원. 2단계 이상은 `CommentService`에서 거부한다.
- **라인 댓글**: `lineNumber` 필드로 코드 뷰어 라인별 댓글 지원.
- `CommentService`가 `buildCommentTree()`로 대댓글을 부모 댓글의 `replies`에 포함한 트리 구조로 반환한다.

### Like (신규)

- 게시글/댓글 추천 기능을 `LikeController`가 제공한다.
- 도메인: `BoardLike`, `CommentLike` (userId + targetId 복합 유니크).
- `LikeService`가 추천/취소 시 해당 엔티티의 `likeCount` 캐시를 동기화한다.
- 엔드포인트: `POST/DELETE /api/boards/{id}/like`, `POST/DELETE /api/comments/{id}/like`.

### User

- `UserController`는 인증 없는 CRUD와 `/me` 엔드포인트를 제공한다.
- `UserService`는 Command/Result record와 `UserNotFoundException`으로 실패 경로를 명확히 한다.
- 도메인 `User`는 팩토리(`create`)와 행위(`updateProfile`) 메서드로 표현한다.

### OAuth

- `OAuthSessionController`와 `presentation.security` 패키지가 OAuth2 로그인 이벤트를 Application 포트로 전달한다.
- `OAuthUserService`는 회원 가입/로그인을 구분하고, `OAuthTokenService`는 `UserOAuthTokenRepository` 포트로 액세스 토큰을 저장한다.
- 도메인 `UserOAuthToken`이 만료 판단과 토큰 업데이트 로직을 소유한다.

### Onboarding

- `OnboardingController`는 인증된 사용자의 저장소 설정 요청만 허용하며 실패 시 401/502를 반환한다.
- `OnboardingService`는 GitHub Webhook 보장과 OAuth 토큰 검증을 하나의 Use Case로 묶는다.
- 도메인 `Onboarding`은 저장소 변경 시 웹훅 플래그를 초기화한다.

### GitHub & Common

- `github.infrastructure` 패키지가 GitHub API/Webhook 어댑터를 캡슐화한다.
- `com.ssafy.dash.common`에는 `TestFixtures`, `FixtureTime`, `GlobalExceptionHandler` 등이 포함된다.

### Analytics (신규)

Solved.ac 연동 및 사용자 스킬 분석을 담당하는 도메인이다.

- **Presentation**: `SolvedacStatsController`, `AnalysisController`가 통계 동기화 및 분석 API를 제공한다.
- **Application**: 7개 서비스로 구성된다:
    - `SolvedacSyncService`: Solved.ac API 호출 및 데이터 동기화.
    - `UserSkillAnalysisService`: 강점/약점 태그 분석, 종합 요약.
    - `BalanceAnalysisService`: 학습 밸런스(집중도) 분석.
    - `DifficultyAnalysisService`: 클래스별 난이도 진행 분석.
    - `LearningPathService`: 3단계 맞춤 학습 경로 추천.
    - `GrowthAnalysisService`: 성장 추세 분석.
    - `StatsSnapshotService`: 일별 통계 스냅샷 관리.
- **Domain**: `UserTagStat`, `UserClassStat`, `UserStatsSnapshot` 엔티티.
    - 엔티티 내 비즈니스 로직: `getMasteryLevel()`, `getCompletionRate()` 등 계산 메서드 포함.
- **Infrastructure**: `UserTagStatMapper`, `UserClassStatMapper`, `UserStatsSnapshotMapper` MyBatis 매퍼.

### External (신규)

외부 API 연동을 담당하는 공용 패키지이다.

- **solvedac**: `SolvedacApiClient` 인터페이스와 `SolvedacApiClientImpl` 구현체.
    - Spring `RestClient`를 사용한 HTTP 통신.
    - `SolvedacUserResponse`, `TagStatResponse`, `ClassStatResponse` DTO.
    - `SolvedacApiException` 예외 처리.

## 4. DTO & 데이터 경계

| 종류         | 위치                            | 용도                              | 생성 위치  |
| ------------ | ------------------------------- | --------------------------------- | ---------- |
| Request DTO  | `..presentation.dto.request..`  | HTTP 입력 역직렬화, Command 변환  | Controller |
| Response DTO | `..presentation.dto.response..` | HTTP 응답 직렬화, Result 변환     | Controller |
| Command DTO  | `..application.dto.command..`   | Use Case 입력, 불변 record        | Service    |
| Result DTO   | `..application.dto.result..`    | Domain → Presentation 데이터 전달 | Service    |

규칙

- Request DTO는 Setter 기반이 허용되지만 도메인/인프라에서 사용하면 안 된다.
- Command/Result는 record를 기본값으로 사용하며, 팩토리 메서드(`from`)로 엔티티 ↔ DTO 변환을 수행한다.
- 도메인 엔티티는 외부 DTO를 참조하지 않는다.

## 5. 코딩 스타일 & 공통 규칙

### 5.1 임포트 순서

1. `java.*`, `javax.*`
2. `org.*`
3. 외부 라이브러리 `com.*`
4. 사내 패키지 `com.ssafy.dash.*`
5. 빈 줄 뒤 static import (알파벳 순)

Spotless 또는 IDE `Optimize Imports` 설정으로 동일한 순서를 유지한다.

### 5.2 DTO & Record 정책

- DTO/응답 타입은 record를 기본으로 사용한다.
- 도메인 엔티티는 명시적인 팩토리(`create`, `issued`)와 행위(`update`, `mark...`) 메서드로 표현하고, Lombok 사용 시에도 비즈니스 로직을 메서드로 캡슐화한다.

### 5.3 Fixture & 시간

- 고정 테스트 데이터는 `com.ssafy.dash.common.TestFixtures` 또는 모듈별 fixture record에서 가져온다.
- 시간 값은 `FixtureTime.now()`를 사용해 결정적 값을 유지한다.

### 5.4 예외 & 로깅

- 도메인/애플리케이션 예외는 `..domain.exception` 또는 `..application.exception`에 위치한다.
- `GlobalExceptionHandler`가 HTTP 상태를 매핑하므로 Controller에서는 예외를 그대로 전파한다.
- 서비스/인프라 레이어의 로깅은 `Slf4j`와 의미 있는 메시지(식별자, 외부 API 상태)만 남긴다.

### 5.5 네이밍

- 클래스: PascalCase, 메서드/변수: camelCase, 상수: UPPER_SNAKE_CASE.
- 패키지는 `module.layer.feature` 순서를 따른다(`algorithm.application.dto`).

## 6. 교차 관심사

- **보안**: `presentation.security.CustomOAuth2UserService`가 GitHub OAuth 로그인 결과를 Application 계층에 위임한다.
- **에러 처리**: `GlobalExceptionHandler`가 `UserNotFoundException`, `AlgorithmRecordNotFoundException` 등을 HTTP 404/502 등으로
  변환한다.
- **헬스 체크**: Spring Boot Actuator(`/actuator/health`, `/actuator/health/dash`)가 DB/디스크 기본 지표와 `githubWebhook`,
  `oauthTokenStore` 커스텀 인디케이터를 노출한다. 운영 환경에서는 `management.endpoint.health.show-details=when_authorized`
  설정으로 인증된 사용자만 세부 정보를 조회할 수 있다.
- **아키텍처 검증**: ArchUnit 테스트를 CI에 포함해 레이어/DTO 규칙 위반을 즉시 탐지한다.
- **로깅**: `src/main/resources/logback-spring.xml`에서 콘솔(INFO) + 롤링 파일(WARN 이상) 출력을 정의한다. `com.ssafy.dash` 패키지
  이하에서는 SLF4J를 사용하고, 도메인/서비스 로그 레벨은 기본적으로 INFO, 인프라/외부 연동 실패는 WARN/ERROR로 표준화한다. 로그 파일
  위치는 `${LOG_PATH}` 환경 변수로 오버라이드 가능하다 (기본 `logs/`).
- **GitHub Push 파이프라인(Planning)**: `/api/webhooks/github`는 GitHub 서명 검증 후 push payload에서 커밋/파일 메타만 추출하고 즉시
  응답한다. 추출된 이벤트는 큐/테이블에 저장되며, 백그라운드 워커가 대상 커밋의 README는 건너뛰고 자바 풀이 파일만 GitHub API로 가져와
  전처리한다. 전처리 결과(커밋 메타, 파일 경로, 소스, 문제 식별자)는 DB에 저장되고, 이후 AI 서버에 전달되어 요약/분석을 만든 뒤 다시 동일
  엔트리에 저장된다. 이렇게 웹훅 처리·전처리·AI 연동·대시보드 갱신을 분리해 재시도와 장애 복구가 용이하도록 설계한다.

## 7. 문서 & 운영 팁

- 아키텍처나 스타일을 변경할 때는 본 문서를 우선 수정하고, 테스트 지침은 `docs/TESTING_GUIDE.md`에 정리한다.
- 신규 모듈은 레이어별 패키지와 DTO 종류를 먼저 정의한 뒤 구현한다.
- 인프라 교체 시 ArchUnit과 모듈 테스트를 실행해 계약이 깨지지 않는지 확인한다.
