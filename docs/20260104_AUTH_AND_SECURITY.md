# 인증 및 보안 전략 (Authentication & Security Strategy)

## 1. 개요
이 문서는 DASH 서비스의 인증 및 보안 전략, 특히 GitHub OAuth와 외부 서비스(Solved.ac) 검증 간의 연동 방식에 대해 기술합니다.

## 2. 인증 흐름 (GitHub OAuth)
DASH는 GitHub를 주요 자격 증명 제공자(Identity Provider)로 사용합니다.
- **프로토콜**: OAuth 2.0
- **스코프**: `user:email`, `read:user`, `repo` (저장소 자동화를 위함)
- **세션**: Spring Security 기반의 Stateful 세션 (JSESSIONID) 사용.

## 3. Solved.ac 소유권 검증 상세

### 3.1. 문제점: 신원 도용 위험
Solved.ac 프로필은 공개되어 있습니다. 별도의 검증 메커니즘이 없다면, 악의적인 사용자가 타인의 고티어 Solved.ac 계정을 자신의 것인 양 등록할 수 있습니다.
- **기존 방식 (Stateful)**: 서버가 랜덤 코드 생성 -> `(userId, code)` 저장 -> 사용자가 Bio에 입력 -> 서버가 확인.
- **단점**: 임시 상태 관리, 만료 로직 처리, 미인증 시도에 대한 불필요한 DB 쓰기 발생.

### 3.2. 해결책: 연동된 신원을 활용한 무상태(Stateless) 검증
DASH는 이미 인증된 GitHub 신원을 활용하여 **무상태 검증(Stateless Verification)** 메커니즘을 구현했습니다.

#### 메커니즘
1. **전제 조건**: 사용자는 GitHub로 로그인된 상태입니다 (신원 보증).
2. **사용자 액션**: Solved.ac 핸들(ID)을 입력합니다.
3. **검증 로직**:
    - 시스템이 동적 검증 코드를 조합합니다: `DashHub:{GitHubUsername}`
    - 시스템이 해당 Solved.ac 프로필의 Bio(자기소개)를 조회합니다.
    - **확인**: Bio 내용에 `DashHub:{GitHubUsername}` 문자열이 포함되어 있는가?
4. **결과**:
    - **일치**: 소유권 확인됨. 계정 연동 성공.
    - **불일치**: 소유권 거부.

### 3.3. 아키텍처 채택 근거
- **무상태성(Statelessness)**: 서버가 랜덤 검증 코드를 생성, 저장, 삭제할 필요가 없습니다. 사용자의 고유한 신원(GitHub ID)에서 유니크한 시크릿이 자연스럽게 유도됩니다.
- **보안성(Security)**: `GitHubUsername`은 유니크하며 사용자가 소유하고 있습니다. 공격자가 피해자의 GitHub ID를 자신의 Solved.ac Bio에 넣도록 강제할 수 없으므로 안전합니다.
- **사용자 경험(UX)**: "이 코드를 복사해서 붙여넣으세요"라는 단순한 지침으로 충분하며, 복잡한 UUID보다 인지가 쉽습니다.

### 3.4. 코드 참조
- **파일**: `backend/.../analytics/application/SolvedacSyncService.java`
- **메서드**: `registerSolvedacHandle`

```java
// Verification Logic
String verificationCode = "DashHub:" + user.getUsername();
if (!bio.toLowerCase().contains(verificationCode.toLowerCase())) {
    throw new IllegalArgumentException("Solved.ac 소유권 인증 실패: Bio에 '" + verificationCode + "'를 포함해주세요.");
}
```
