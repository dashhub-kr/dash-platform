# 성능 최적화

## 1. 문제 진단

대시보드, 스터디 미션, 분석 화면이 느려진 원인은 RDBMS 한계가 아니라 **N+1 쿼리**와 **인메모리 집계**였습니다.

### 1.1. 병목 지점
1.  **대시보드 히트맵 (`DashboardService`)**:
    -   수천 건의 `AlgorithmRecord`를 모두 메모리로 로드하여 Java Stream으로 그룹화.
    -   데이터 증가 시 메모리 부족 및 속도 저하 발생.
2.  **스터디 미션 목록 (`StudyMissionService`)**:
    -   심각한 **N+1 문제**. 미션 수(M) × 멤버 수(N) × 2회 이상의 쿼리가 페이지 로드 시마다 발생.
    -   (예: 미션 10개, 멤버 6명 → 120+ 쿼리)
3.  **데이터베이스 스키마**:
    -   주요 필터링 및 조인 컬럼(`study_id`, `date`, `mission_id` 등)에 인덱스 누락으로 인한 Full Table Scan 발생.

---

## 2. 최적화 전략

### 2.1. 데이터베이스 튜닝 (Indexing)
풀 테이블 스캔을 없애기 위해 인덱스를 추가했습니다.
-   `algorithm_records`: `(study_id, committed_at)` - 히트맵 및 기간별 조회 최적화.
-   `study_mission_submissions`: `(mission_id, user_id)` - 제출 여부 확인 최적화.
-   `problem_tags`: `(tag_key)` - 조인 성능 향상.

### 2.2. 백엔드 리팩토링
1.  **Dashboard Heatmap (SQL Aggregation)**:
    -   Java 인메모리 집계 → **DB 레벨 집계 (`GROUP BY`)** 로 변경.
    -   날짜와 개수만 조회.
2.  **Study Mission (Batch Fetching)**:
    -   스터디 내 모든 미션의 제출 내역을 `IN` 쿼리 한 번으로 조회해 메모리에서 매핑.

---

## 3. 적용 결과

### 3.1. 변경 사항
-   **Schema (`schema.sql`)**: 인덱스 4개 추가.
-   **DashboardService**: `HeatmapRawData` DTO 및 MyBatis XML 수정으로 로직 경량화.
-   **StudyMissionService**: 루프 내 쿼리 호출을 제거하고, `findByMissionIds`를 통한 일괄 조회 구현.

### 3.2. 효과
-   **쿼리 수**: 미션 목록 조회 시 100회 이상 → **3회 내외**.
-   **메모리 효율**: 대시보드 조회 시 불필요한 엔티티 로딩 제거.
-   **코드 단순화**: 스트림 집계 로직을 SQL로 이동.

---

## 4. 의사결정

### 4.1. Analytics 캐싱 전략
-   **현황**: `AnalyticsService`가 실시간 Heavy Join (`algorithm_records` -> `tags`) 수행. `user_tag_stats` (Solved.ac 동기화 데이터) 테이블 존재.
-   **결정**: 실시간 쿼리 대신 미리 계산된 `user_tag_stats` 테이블 사용.
    -   데이터 정합성은 `SolvedacSyncService`를 통해 관리.

### 4.2. NoSQL 도입 여부
-   **결정**: **RDBMS 유지**.
-   **근거**:
    1.  **MySQL 8.0 JSON 지원**: AI 분석 결과 같은 비정형 데이터 처리에 충분.
    2.  **트랜잭션 일관성**: 문제 풀이와 점수 갱신을 원자적으로 처리해야 함.
    3.  **운영 효율성**: 데이터베이스 하나로 관리.

