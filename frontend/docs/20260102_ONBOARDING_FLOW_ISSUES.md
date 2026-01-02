# 온보딩 프로세스 이슈 및 해결 보고서

**작성 일시**: 2026-01-02  
**관련 도메인**: `onboarding`  
**대상**: `Global Security Guard`, `Onboarding Flow`

---

## 🛡️ Global Security Guard ("Global Guard") 개요

Global Security Guard(`router/index.js`)는 애플리케이션의 보안과 무결성을 유지하기 위한 핵심 방어체계입니다. 사용자가 필수 온보딩 과정을 건너뛰고 비정상적으로 기능에 접근하는 것을 방지하기 위해 설계되었습니다.

### 주요 역할
1.  **미인증 차단**: 로그인하지 않은 사용자의 Protected Route 접근 차단
2.  **온보딩 강제**: 필수 데이터(Solved.ac 연동, 스터디 가입, 저장소 연결)가 누락된 사용자를 대시보드 진입 전 차단하고 온보딩 페이지로 회귀시킴
3.  **역진입 방지**: 온보딩을 완료한 사용자가 다시 온보딩 페이지에 진입해 데이터를 꼬이게 하는 것을 방지

---

## 🚨 발생 이슈 및 해결 과정

### 1. 🧊 Repository Step Frozen Issue (저장소 단계 멈춤 현상)

> [!WARNING]
> 사용자가 저장소를 성공적으로 연결했음에도 불구하고, 대시보드로 넘어가지 못하고 온보딩 페이지에 갇히는 치명적인 사용자 경험(UX) 이슈가 발생했습니다.

**원인 분석**:
- **Race Condition (경쟁 상태)**: 사용자가 "완료" 버튼을 누르는 시점과 백엔드 DB가 업데이트되는 시점, 그리고 프론트엔드가 이를 감지하는 시점 간에 미세한 시차 발생.
- **Replication Lag**: `finishOnboarding` 호출 직후 `await refresh()`를 했으나, 백엔드 로직이 완료되기 전이나 DB 복제 지연 등으로 인해 `refresh()`가 여전히 "Repository 연결 안 됨" 상태를 반환.
- **Guard Blocking**: Global Guard는 `user.repositoryName`이 없으면 얄짤없이 온보딩 페이지로 리다이렉트 시킴.

**해결책: Optimistic UI Update (낙관적 업데이트)**
- 백엔드의 응답을 기다리지 않고, 사용자가 연결한 저장소 이름(`repoName`)을 프론트엔드 상태에 **강제로 주입(Injection)**하여 Guard를 통과시킴.
- 이는 사용자가 성공했다는 "사실"을 우선시하여 UX를 매끄럽게 만듦.

```javascript
// OnboardingView.vue 수정안
const finishOnboarding = async (repoName) => {
  await refresh(); // 정공법 시도
  
  // Guard Bypass: 백엔드 반영이 늦더라도 일단 통과시킴
  if (user.value && !user.value.repositoryName && repoName) {
    user.value.repositoryName = repoName;
  }
  
  router.replace('/dashboard');
};
```

---

### 2. ♾️ Infinite Study Loop (스터디 무한 루프)

> [!IMPORTANT]
> 이미 스터디에 가입된 사용자가 새로고침 등을 통해 온보딩에 재진입할 경우, "스터디 찾기/생성" 단계에 갇혀 오도가도 못하는 상황 발생.

**원인 분석**:
- **단순 증가 로직**: `nextStep` 함수가 현재 사용자의 상태(`studyId` 유무)를 체크하지 않고 기계적으로 다음 단계(`Step 2: Study`)로 넘김.
- **Auto-Skip 부재**: `OnboardingStudy.vue` 컴포넌트가 마운트될 때 자신이 이미 완료된 상태인지 체크하는 로직이 미흡했음.

**해결책: Smart Skip Logic**
1.  **View 레벨**: "AI 분석" 단계에서 "다음"을 누를 때, 이미 `studyId`가 있다면 "스터디 단계"를 건너뛰고 바로 "저장소 단계"로 점프.
2.  **Component 레벨**: `OnboardingStudy.vue`가 마운트되자마자 `studyId`가 있으면 즉시 `allow` 시그널을 보내 자동 통과.

---

### 3. 🚧 Guard Strictness (가드 엄격성 불일치)

> [!CAUTION]
> 사용자가 모든 과정을 완료했음에도 Global Guard가 이를 인정하지 않고 차단하는 문제 발생.

**원인 분석**:
- **필드명 불일치**: 
  - `OnboardingView` 로직: `solvedacId` OR `solvedacHandle`을 체크 (유연함)
  - `Router Guard`: 오직 `solvedacId`만 체크 (엄격함)
- 일부 사용자 데이터에서 `solvedacId`는 `null`이고 `solvedacHandle`만 존재하는 경우가 있어, 실제로는 완료했으나 가드는 미완료로 판별.

**해결책: Policy Relaxation (정책 완화)**
- Global Guard의 검증 로직을 View 로직과 동일하게 완화하여 `solvedacHandle`만 있어도 통과되도록 수정.

```javascript
// router/index.js 수정
const isOnboardingComplete = user.value && 
                             (user.value.solvedacId || user.value.solvedacHandle) && // 유연한 체크
                             user.value.studyId && 
                             user.value.repositoryName;
```

---

## 📌 결론

Global Security Guard는 필수적인 방어막이지만, 너무 엄격하거나 현실적인 딜레이(Latency)를 고려하지 않으면 사용자에게 **"벽"**처럼 느껴질 수 있습니다.

이번 조치는 보안을 해치지 않는 선에서 **"낙관적 업데이트(Optimistic Update)"**와 **"지능형 스킵(Smart Skip)"**을 도입하여, 시스템의 견고함과 사용자 경험 사이의 균형을 맞춘 사례입니다.
