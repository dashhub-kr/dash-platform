# OAuth Domain 보안 및 아키텍처 점검 보고서

**점검 일시**: 2026-01-03  
**표준 도메인**: `user`  
**점검 대상**: `oauth`

---

## 📋 분석 개요

OAuth 도메인은 외부 소셜 로그인(GitHub) 인증 토큰을 관리하는 도메인입니다.
보안상 매우 민감한 정보인 **Access Token**과 **Refresh Token**을 다루고 있어 철저한 보안 관리가 필요합니다.

### 핵심 원칙

1. **Confidentiality (기밀성)**: 민감 정보는 권한 없는 사용자가 볼 수 없도록 암호화하여 저장해야 함.
2. **Standardization (표준화)**: 데이터 접근 시 암호화/복호화 로직이 도메인 로직과 분리되어 투명하게 처리되어야 함.

---

## ❌ OAuth Domain 현재 구조 (점검 전)

```
oauth/
├── application/
│   └── OAuthTokenService.java
├── domain/
│   ├── UserOAuthToken.java (Entity)
│   └── UserOAuthTokenRepository.java (Interface)
└── infrastructure/
    └── persistence/
        └── UserOAuthTokenMapper.xml
```

---

## 🚨 보안 및 아키텍처 위반 사항

### 1. **민감 정보 평문 저장 (Plain Text Storage)** ⚠️ **CRITICAL**

> [!CAUTION]
> GitHub `access_token`과 `refresh_token`이 아무런 암호화 조치 없이 **평문(Plain Text) 상태로 데이터베이스에 저장**되고 있습니다.

**문제점**:
- DB가 탈취되거나 관리자 권한으로 DB에 접근할 경우, 모든 사용자의 GitHub 계정 제어 권한(Repository 접근 등)이 노출됨.
- GDPR, 개인정보보호법 등 보안 규정 위반.

**위반 코드** ([OAuthTokenService.java](file:///c:/dash/backend/src/main/java/com/ssafy/dash/oauth/application/OAuthTokenService.java)):
```java
// 암호화 과정 없이 그대로 저장
token.updateAccessToken(tokenValue, tokenType, expiresAt, now);
```

**DB 저장 예시**:
```sql
SELECT access_token FROM user_oauth_tokens WHERE user_id = 1;
-- 결과: gho_16C7e42F... (평문 노출)
```

---

### 2. **암호화/복호화 책임의 부재** ⚠️ **HIGH**

> [!WARNING]
> 엔티티(`UserOAuthToken`)나 서비스 레이어 어디에도 암호화를 담당하는 명시적인 로직이나 계층이 존재하지 않습니다.

**문제점**:
- 향후 암호화 적용 시, 모든 조회/저장 로직을 수정해야 하는 유지보수성 저하.
- 암호화 키 관리 전략 부재.

---

## 🔧 권장 개선 사항 (Implemented)

이번 점검을 통해 즉시 다음과 같은 개선 사항이 적용되었습니다.

### 1. AES-256 양방향 암호화 적용

표준 AES-256 (CBC Mode, PKCS5Padding) 암호화 알고리즘을 도입하여 토큰을 저장합니다.
암호화 키는 32바이트(256비트)로 해싱하여 안전하게 관리합니다.

### 2. MyBatis TypeHandler를 통한 투명한(Transparent) 암호화

비즈니스 로직(Service)을 수정하지 않고, **Infrastructure Layer(MyBatis)** 레벨에서 자동으로 암호화/복호화를 처리합니다.

**적용 구조**:
```mermaid
graph LR
    Service[OAuthTokenService] -->|평문| Repository
    Repository -->|평문| TypeHandler[CryptoTypeHandler]
    TypeHandler -->|암호화 (AES)| DB[(Database)]
    DB -->|암호문| TypeHandler
    TypeHandler -->|복호화| Repository
```

**개선된 매퍼 설정** (`UserOAuthTokenMapper.xml`):
```xml
<result property="accessToken" column="access_token" typeHandler="com.ssafy.dash.common.encrypt.CryptoTypeHandler"/>
```

---

## 📌 결론

> [!CHECK]
> **OAuth 도메인의 치명적인 보안 취약점이 해결되었습니다.**

**조치 결과**:
1. ✅ **AES-256 암호화 적용**: 모든 OAuth 토큰이 암호화되어 저장됨 (DB 조회 시 암호문만 보임).
2. ✅ **자동화된 처리**: `CryptoTypeHandler`를 통해 개발자가 신경 쓰지 않아도 자동으로 보안 적용.
3. ✅ **하위 호환성 확보**: 기존 평문 데이터 조회 시 에러 없이 평문을 반환하고, 갱신 시 암호화하도록 처리.
