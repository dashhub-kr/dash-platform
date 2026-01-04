# 로그인 리다이렉트 바운스 현상 분석 보고서

**작성 일시**: 2026-01-03
**관련 도메인**: `auth`, `onboarding`
**대상**: `OauthRedirect.vue`, `Router Guard`

---

## 🚨 발생 이슈: 대시보드 바운스 (The Dashboard Bounce)

> [!WARNING]
> 사용자가 "시작하기"를 누르면 로딩 화면을 거쳐 대시보드로 이동했다가, 온보딩이 완료되지 않은 경우 다시 온보딩 페이지로 튕겨지는("Bounce") 현상이 발생함.

### 현상 재현 경로
1. 온보딩 미완료 사용자가 `Landing` 페이지에서 `GitHub로 시작하기` 클릭.
2. OAuth 인증 후 `OauthRedirect.vue` (로딩 화면) 진입.
3. `OauthRedirect`에서 `flowType`이 `LOGIN`이므로 대시보드(`/`)로 리다이렉트.
4. `router/index.js`의 Global Guard가 대시보드 진입 시 프로필 상태 확인.
5. 온보딩 미완료 확인 후 `/onboarding`으로 강제 이동.
6. 결과: **로딩 -> 대시보드(잠깐 보임) -> 온보딩**의 불필요한 이동 발생.

---

## 🛠️ 원인 분석

**1. 불충분한 판단 로직 (`OauthRedirect.vue`)**
- 현재 `OauthRedirect`는 백엔드에서 받은 `flowType`만 확인합니다.
- `flowType: LOGIN`은 "인증 성공"만 의미할 뿐, "온보딩 완료"를 보장하지 않습니다.
- 따라서 무조건 대시보드로 보내버리는 것이 문제의 시작입니다.

**2. 책임의 위임 (Delegation of Responsibility)**
- "어디로 갈지"에 대한 판단(Routing Logic)을 `Router Guard`에게 미루고 있습니다.
- Guard는 보안을 위해 존재해야지, UX 흐름을 제어하는 주체가 되면(특히 튕겨내는 방식이면) UX가 저하됩니다.

---

## 💡 해결 방안: 조기 판단 (Earlly Determination)

`OauthRedirect.vue` 단계에서 사용자의 상태를 완벽하게 파악하고, **처음부터 올바른 목적지로 발사**해야 합니다.

### 수정 로직 (OauthRedirect.vue)

```javascript
// 변경 전
if (data.flowType === "SIGN_UP") {
    router.push("/onboarding");
} else {
    window.location.href = "/"; // 무조건 대시보드행
}

// 변경 후
if (isOnboardingNeeded(user)) {
    // 온보딩이 필요한 모든 경우 (신규 가입 OR 기존 가입자 중 미완료자)
    router.replace("/onboarding"); 
} else {
    // 온보딩 완료 사용자만
    router.replace("/dashboard");
}
```

### 기대 효과
- **Zero Bounce**: 사용자는 로딩 화면 직후 자신의 상태에 맞는 페이지로 즉시 이동합니다.
- **Improved UX**: 불필요한 페이지 로드와 깜빡임이 제거됩니다.
