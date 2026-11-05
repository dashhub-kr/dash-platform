# API Rate Limiting

외부 서비스(Solved.ac, GitHub) 호출 비용과 비정상 트래픽을 제한하기 위해 Token Bucket 방식의 요청 제한을 적용합니다.

## 1. 구성
- **라이브러리**: [Bucket4j](https://github.com/bucket4j/bucket4j) 8.x
- **버킷 저장소**: Caffeine 캐시 (최대 10만 개, 일정 시간 미사용 시 만료)
- **식별 기준**: 로그인 사용자는 사용자 ID, 비로그인 요청은 IP

## 2. 클라이언트 IP 판별
클라이언트가 조작할 수 있는 `X-Forwarded-For`는 쓰지 않습니다.

1. nginx: Cloudflare IP 대역에서 온 요청만 `CF-Connecting-IP`를 실제 IP로 인정하고, `X-Real-IP`를 덮어씀
2. 백엔드(`ClientIpResolver`): 내부망(nginx)에서 온 요청일 때만 `X-Real-IP`를 사용하고, 그 외에는 접속 IP 사용

## 3. 정책

| 대상 | 제한 | 위치 |
|:---|:---|:---|
| 모든 요청 | IP당 100회/분 | `GlobalRateLimitFilter` |
| Solved.ac 핸들 등록 `POST /api/users/{id}/solvedac` | 5회/분 | `@RateLimit` |
| 통계 재동기화 `POST /api/users/{id}/solvedac/sync` | 3회/분 | `@RateLimit` |

## 4. 초과 시 응답
`429 Too Many Requests`

```json
{
  "errorCode": "TOO_MANY_REQUESTS",
  "message": "요청 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요."
}
```

## 5. 사용법

```java
@RateLimit(capacity = 10, refillTokens = 10, refillDurationSeconds = 60)
@GetMapping("/search")
public ResponseEntity<?> search(...) { ... }
```

버킷은 서버 메모리에 있으므로, 서버를 여러 대로 늘리면 Redis 같은 공유 저장소로 옮겨야 합니다.
