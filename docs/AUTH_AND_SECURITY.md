# 인증 및 보안

## 1. 인증 (GitHub OAuth)
- **프로토콜**: OAuth 2.0
- **스코프**: `read:user`, `user:email`, `repo`, `admin:repo_hook` (풀이 저장소 커밋·웹훅 등록)
- **세션**: Spring Security 세션(JSESSIONID). 선택 근거는 [ADR](./DECISION_LOG_AUTH_STRATEGY.md) 참고

## 2. 요청 보호
| 항목 | 방식 |
|:---|:---|
| CSRF | `XSRF-TOKEN` 쿠키 → `X-XSRF-TOKEN` 헤더 검증. GitHub 웹훅은 HMAC 서명으로 대신 검증 |
| 세션 쿠키 | `HttpOnly`, `SameSite=Lax`, 운영 환경 `Secure` |
| 권한 | 수정·삭제는 작성자·스터디장·관리자만 허용 |
| 요청 제한 | [Rate Limiting](./RATE_LIMITING.md) 참고 |
| XSS | `v-html`로 그리는 HTML은 DOMPurify로 정제 |

## 3. 데이터 암호화
OAuth 토큰과 DM 본문은 DB에 AES-256-GCM으로 암호화해 저장합니다.
- **키**: 환경 변수 `DASH_ENCRYPT_SECRET_KEY`(32자 이상). 없으면 서버가 시작되지 않음
- **형식**: `v2:` + Base64(IV + 암호문 + 인증 태그)
- **복호화 실패**: 토큰은 없는 것으로 보고 재로그인 유도, 메시지는 대체 문구 표시

## 4. Solved.ac 소유권 검증
타인의 Solved.ac 계정을 등록하지 못하도록, 이미 인증된 GitHub 계정으로 소유권을 확인합니다.

1. 사용자가 Solved.ac 핸들 입력
2. 서버가 해당 프로필 Bio에 `DashHub:{GitHubUsername}`이 있는지 확인
3. 있으면 연동, 없으면 거부

검증 코드를 서버에 저장하지 않아도 되고, 공격자가 피해자의 Bio를 바꿀 수 없으므로 안전합니다.

```java
// SolvedacSyncService.registerSolvedacHandle
String verificationCode = "DashHub:" + user.getUsername();
if (!bio.toLowerCase().contains(verificationCode.toLowerCase())) {
    throw new IllegalArgumentException("Solved.ac 소유권 인증 실패: Bio에 '" + verificationCode + "'를 포함해주세요.");
}
```
