# API Rate Limiting (API 요청 제한)

## 1. 개요
DASH는 외부 서비스(Solved.ac, GitHub)와의 연동 안정성을 보장하고, 비정상적인 트래픽으로부터 시스템을 보호하기 위해 API 레벨의 Rate Limiting을 적용하고 있습니다.

## 2. 전략 (Strategy)

### 2.1 Token Bucket 알고리즘
- **라이브러리**: [Bucket4j](https://github.com/bucket4j/bucket4j) 8.x
- **구현 방식**: Spring AOP (`@RateLimit`)
- **버킷 저장소**: In-Memory (ConcurrentHashMap)
    - *참고: 향후 분산 서버 환경 전환 시 Redis 기반으로 변경 예정.*

### 2.2 식별 기준 (Key Resolver)
1. **인증된 사용자**: `User ID` (SecurityContextHolder 내 인증 정보)
2. **익명 사용자 (DoS 방어)**: `IP Address`
    - `GlobalRateLimitFilter`: 모든 요청에 대해 기본 IP 제한 적용.
    - `@RateLimit`: 비로그인 상태의 특정 API(예: 로그인 전) 사용 시에도 IP 기준으로 버킷 생성.

## 3. 적용 현황 (Policies)

### 3.1 Global DoS Protection (Filter Layer)
- **Target**: 모든 HTTP 요청
- **Limit**: IP당 100회 / 1분
- **목적**: 무차별적인 접속 시도(DoS), 스캐닝, 브루트포스 공격 방어.

### 3.2 Business Logic Protection (AOP Layer)
| API | Method | Limit | Refill | 설명 |
| :--- | :--- | :--- | :--- | :--- |
| **Solved.ac 핸들 등록** | `POST /api/users/{id}/solvedac` | 5회 | 5회 / 1분 | 소유권 검증 및 초기 데이터 동기화 (외부 API 호출 비용 높음) |
| **통계 재동기화** | `POST /api/users/{id}/solvedac/sync` | 3회 | 3회 / 1분 | 전체 데이터 갱신 (가장 무거운 작업) |

## 4. 에러 응답 (Error Handling)

제한 초과 시 `429 Too Many Requests` 상태 코드와 함께 에러 메시지가 반환됩니다.

```json
{
  "errorCode": "TOO_MANY_REQUESTS",
  "message": "요청 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요.",
  "path": "/api/users/1/solvedac/sync",
  "timestamp": "2026-01-04T17:30:00"
}
```

## 5. 사용법 (Usage)

`@RateLimit` 어노테이션을 컨트롤러 메서드에 적용합니다.

```java
@RateLimit(capacity = 10, refillTokens = 10, refillDurationSeconds = 60)
@GetMapping("/search")
public ResponseEntity<?> search(...) { ... }
```
