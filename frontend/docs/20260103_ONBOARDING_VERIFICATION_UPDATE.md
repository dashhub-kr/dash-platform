# 온보딩 검증 프로세스 강화 제안 (Onboarding Verification Update)

**문서 번호**: 20260103_ONBOARDING_VERIFICATION_UPDATE
**작성자**: Antigravity
**작성일**: 2026-01-03
**상태**: 제안됨 (Proposed)

## 1. 배경 및 목적
현재 Dash 온보딩 과정은 사용자의 자발적인 확인 버튼 클릭에 의존하고 있어, 실제 익스텐션 설치 여부나 저장소 설정 여부를 확실하게 보장하지 못합니다. 이로 인해 온보딩을 완료했음에도 기능이 정상 작동하지 않는 "이탈(Churn)" 가능성이 존재합니다.

따라서, **DashHub 익스텐션과 Dash 웹 간의 통신 브릿지(`dash-bridge.js`)를 활용하여 강력한 기술적 검증(Strict Verification)**을 도입하고자 합니다.

## 2. 주요 변경 사항

### 2.1 익스텐션 설치 검증 (Step 4)
- **기존 방식**: 사용자가 "설치 완료" 버튼을 클릭하면 다음 단계로 이동.
- **변경 방식**: 
  - "다음" 버튼을 비활성화(Disabled) 상태로 시작.
  - 웹페이지가 로드될 때 및 주기적으로 `DashHub-dash-ready` 이벤트를 감지.
  - 익스텐션이 주입하는 `#DashHub-dash-data` 요소의 `data-extension-installed="true"` 속성 확인.
  - **이중 검증 (Dual Verification)**: 설치 여부뿐만 아니라 **저장소 연결(`data-hook`)** 여부까지 확인.
  - 설치는 되었으나 저장소가 연결되지 않은 경우, 사용자에게 명확한 안내 메시지 표시 및 진행 차단.
  - 모든 검증(설치 + 연결) 성공 시에만 "다음" 버튼 활성화 및 "설치 및 연결 완료" 상태 표시.

### 2.2 저장소 설정 검증 (Step 5)
- **기존 방식**: `repo` 데이터(저장소 이름)만 확인하거나, 없으면 수동 검색 유도.
- **변경 방식**:
  - `repo` (단순 이름)보다 **`DashHub_hook` (연동된 전체 경로, `username/repo`)** 데이터를 우선적으로 검증.
  - `DashHub_hook` 데이터가 존재해야만 "저장소 설정 완료"로 간주.
  - 플레이스홀더: 수동 입력 시 혼란을 방지하기 위해 `username/repository` 형식을 유지하며, 실제 입력값도 이 형식을 권장.

### 2.3 이벤트 및 데이터 명세 수정 (Event Naming Mismatch Fix)
- **문제점**: Frontend는 `baekjoonhub-dash-ready`를 리스닝하고 있으나, Extension(`dash-bridge.js`)은 `DashHub-dash-ready`를 발송함.
- **조치**: Frontend 코드를 수정하여 `DashHub-dash-ready` 이벤트를 리스닝하도록 통일.

## 3. 구현 계획
1. **Frontend**: `OnboardingStep4Extension.vue` 및 `OnboardingStep5Repo.vue` 내 이벤트 리스너 이름 변경.
2. **Frontend**: `OnboardingStep4Extension.vue`에 **엄격한 이중 검증(설치 + 저장소 연결)** 로직 추가.
3. **Frontend**: `OnboardingStep5Repo.vue`에서 `data-hook` 속성 기반의 엄격한 검증 로직 추가 (Step 4에서 넘어왔으므로 사실상 확인용).

## 4. 기대 효과
- **사용자 경험**: 온보딩 단계에서 설정 오류를 조기에 발견하여, 실제 서비스 이용 시 발생할 수 있는 문제를 예방.
- **데이터 무결성**: 연동되지 않은 상태로 온보딩이 완료되는 케이스 제거.
