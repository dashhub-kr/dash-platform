# 성능 최적화 통합 보고서 (Performance Optimization Report)
**작성자**: utact
**작성일**: 2026-01-05
**버전**: 1.1 (Architecture Decision Added)

---

## 1. 개요 및 문제 진단 (Overview & Diagnosis)

대시보드, 스터디 미션, 분석 모듈에서 시스템 지연 현상이 감지되었습니다. 분석 결과, 이는 RDBMS의 한계가 아닌 **비효율적인 데이터 접근 패턴(N+1 쿼리)** 및 **인메모리 집계 방식**이 주원인으로 파악되었습니다.

### 1.1. 주요 병목 지점
1.  **대시보드 히트맵 (`DashboardService`)**:
    -   수천 건의 `AlgorithmRecord`를 모두 메모리로 로드하여 Java Stream으로 그룹화.
    -   데이터 증가 시 메모리 부족 및 속도 저하 발생.
2.  **스터디 미션 목록 (`StudyMissionService`)**:
    -   심각한 **N+1 문제**. 미션 수(M) × 멤버 수(N) × 2회 이상의 쿼리가 페이지 로드 시마다 발생.
    -   (예: 미션 10개, 멤버 6명 → 120+ 쿼리)
3.  **데이터베이스 스키마**:
    -   주요 필터링 및 조인 컬럼(`study_id`, `date`, `mission_id` 등)에 인덱스 누락으로 인한 Full Table Scan 발생.

---

## 2. 최적화 전략 (Optimization Strategy)

### 2.1. 데이터베이스 튜닝 (Indexing)
주요 조회 경로의 풀 테이블 스캔을 제거하기 위해 커버링 인덱스를 추가합니다.
-   `algorithm_records`: `(study_id, committed_at)` - 히트맵 및 기간별 조회 최적화.
-   `study_mission_submissions`: `(mission_id, user_id)` - 제출 여부 확인 최적화.
-   `problem_tags`: `(tag_key)` - 조인 성능 향상.

### 2.2. 백엔드 리팩토링
1.  **Dashboard Heatmap (SQL Aggregation)**:
    -   Java 인메모리 집계 → **DB 레벨 집계 (`GROUP BY`)** 로 변경.
    -   필요한 데이터(날짜, 카운트)만 조회하도록 최적화.
2.  **Study Mission (Batch Fetching)**:
    -   **배치 페칭(Batch Fetching)** 도입.
    -   스터디 내 모든 미션의 제출 내역을 **단 한 번의 `IN` 쿼리**로 조회하여 메모리 매핑.

---

## 3. 적용 내용 및 결과 (Implementation & Verification)

### 3.1. 적용된 변경 사항
-   **Schema (`schema.sql`)**: 4개의 성능 인덱스 추가 완료.
-   **DashboardService**: `HeatmapRawData` DTO 및 MyBatis XML 수정으로 로직 경량화.
-   **StudyMissionService**: 루프 내 쿼리 호출을 제거하고, `findByMissionIds`를 통한 일괄 조회 구현.

### 3.2. 검증 결과
-   **쿼리 수 감소**: 미션 목록 조회 시 100+회 → **3회 내외**로 감소.
-   **메모리 효율**: 대시보드 조회 시 불필요한 엔티티 로딩 제거.
-   **코드 단순화**: 복잡한 스트림 집계 로직을 SQL로 위임하여 유지보수성 향상.

---

## 4. 아키텍처 의사결정 (Architecture Decision)

### 4.1. Analytics 캐싱 전략
-   **현황**: `AnalyticsService`가 실시간 Heavy Join (`algorithm_records` -> `tags`) 수행. `user_tag_stats` (Solved.ac 동기화 데이터) 테이블 존재.
-   **결정**: **Long-term Optimization** 채택.
    -   실시간 쿼리 대신 **Pre-calculated Table (`user_tag_stats`)** 을 사용하여 조회 성능 극대화.
    -   데이터 정합성은 `SolvedacSyncService`를 통해 관리.

### 4.2. NoSQL 도입 여부
-   **Opinion**: **RDBMS 유지**.
-   **근거**:
    1.  **MySQL 8.0 JSON 지원**: AI 분석 결과 등 비정형 데이터 처리에 충분한 성능 제공.
    2.  **트랜잭션 일관성**: 문제 풀이와 점수 갱신의 원자성(Atomicity) 보장이 중요. 분산 트랜잭션 불필요.
    3.  **운영 효율성**: 단일 데이터베이스로 관리 복잡도 최소화.

