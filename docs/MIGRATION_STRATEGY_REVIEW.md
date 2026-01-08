# Learning Path 구현 및 DB 마이그레이션 전략 검토

**작성일**: 2026-01-08
**작성자**: Antigravity (AI Assistant)
**상태**: 기능 구현 완료 / 배포 전략 검토 중

---

## 1. 현재 작업 현황 (Implemented Features)

**"거품 탐지 및 학습 경로 개선"** 기능을 위해 다음 작업들이 완료되었습니다.

### 1.1 Backend 기능 구현
- **Top 100 데이터 수집**: `SolvedacApiClient`를 통해 사용자 상위 100개 문제 데이터 수집.
- **평균 레벨 계산**: `SolvedacSyncService`에서 Top 100 문제의 평균 난이도를 계산하여 `User` 엔티티에 저장.
- **거품 탐지 로직**: `RuleBasedLearningPathService`에서 `(UserTier - AvgTop100Level)` 공식을 통해 거품 지수 산출.
- **AI 프롬프트 개선**: `AiLearningPathService` 및 Python AI 서버에서 거품 지수를 포함하여 프롬프트 생성.

### 1.2 DB 스키마 변경 사항
- `users` 테이블에 **`avg_top100_level` (INT)** 컬럼 추가 필요.
- MyBatis `UserMapper.xml`에 해당 컬럼의 UPDATE 쿼리 추가 완료.

### 1.3 Flyway 도입 (진행 중)
- `pom.xml`에 Flyway 의존성 추가.
- `application.properties`에 Flyway 설정 활성화 (`enabled=true`, `baseline-on-migrate=true`).
- `db/migration/V1__add_avg_top100_level_column.sql` 마이그레이션 파일 생성.

---

## 2. 배포 및 마이그레이션 전략 (고민 사항)

현재 로컬 개발 환경과 운영(배포) 환경의 DB 관리 방식 차이로 인해 **안전한 마이그레이션 전략** 결정이 필요합니다.

### 2.1 현재 인프라 구성
- **로컬 (Local)**:
  - `application-local.properties` 사용.
  - `schema.sql` (및 `schema-local.sql`)이 매번 실행되어 DB를 초기화(`DROP` & `CREATE`)함.
- **운영 (Prod / Docker)**:
  - `application.properties` 사용.
  - `schema.sql`이 `init.mode=always`로 설정되어 있으나, `IF NOT EXISTS` 구문으로 되어 있어 기존 데이터는 유지됨.

### 2.2 잠재적 문제점 (Risks)
1.  **중복 실행 충돌**:
    - `schema.sql`에도 `avg_top100_level` 컬럼 정의가 포함됨.
    - Flyway `V1__...sql` 파일도 동일한 컬럼 추가(`ALTER TABLE`)를 수행함.
    - 배포 시 두 스크립트가 모두 실행되면 **"Duplicate column name"** 에러가 발생할 수 있음.

2.  **초기화 순서**:
    - Flyway가 먼저 실행되는지, Hibernate/ScriptUtils(`schema.sql`)가 먼저 실행되는지에 따라 동작이 달라질 수 있음. (보통 Flyway가 먼저 실행됨)

### 2.3 해결을 위한 결정 사항 (Decision Points)

다음 중 하나의 전략을 선택해야 합니다.

#### ✅ 옵션 A: Hybrid 방식 (권장 - 안전 장치 추가)
기존 `schema.sql` 방식을 유지하되, Flyway 스크립트에 안전 장치를 둠.
- **설정**: 운영 환경에서 `schema.sql`과 Flyway 모두 활성화.
- **조치**: Flyway SQL 파일에 **`IF NOT EXISTS` (또는 프로시저)** 로직을 추가하여, 컬럼이 이미 있으면 건너뛰도록 작성.
- **장점**: 기존 배포 파이프라인 변경 최소화. 로컬/운영 동작의 괴리 적음.

#### 옵션 B: 완전한 역할 분리 (Strict)
운영 환경에서는 `schema.sql`을 아예 끄고 Flyway만 사용.
- **설정**: `application.properties` (운영용)에서 `spring.sql.init.mode=never`로 변경.
- **조치**: `V1` 이전에 초기 스키마를 생성하는 `V0__init.sql` (Baseline)이 필요하거나, `baseline-on-migrate`를 믿고 V1부터 적용.
- **단점**: 기존에 `schema.sql` 관리에 익숙했던 워크플로우 변경 필요.

---

## 3. 추천 조치 사항 (Next Actions)

협업 환경과 현재 설정을 고려할 때 **옵션 A** 기반으로 안정성을 확보하는 것을 추천합니다.

1.  **Flyway 스크립트 보강**: `V1__add_avg_top100_level_column.sql`을 수정하여 **컬럼 존재 여부를 확인하고 추가**하는 안전한 SQL로 변경 (MySQL 버전에 맞는 문법 사용).
2.  **Flyway 재시도 설정**: Docker 컨테이너 실행 시점 차이를 극복하기 위해 `connect-retries` 설정 추가.
3.  **로컬 설정 분리**: 로컬에서는 Flyway가 구동되지 않거나, 구동되더라도 충돌하지 않도록 확인 (`application-local.properties`에서 `flyway.enabled=false` 명시 권장).

---

이 문서는 `/Users/yongsu/ssafy/dash/docs/MIGRATION_STRATEGY_REVIEW.md`에 저장되었습니다.
