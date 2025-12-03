## 테스트 가이드 (권장 기준)

> 아키텍처/코딩 규칙은 `docs/ARCHITECTURE.md`에 정리되어 있다. 본 문서는 “테스트를 어떻게 작성/검증할 것인가”에만 집중한다.

### 1. 테스트 철학

1. **레이어 의도 노출**: Domain 테스트는 규칙을, Service 테스트는 유즈케이스 시나리오를, Controller 테스트는 HTTP 계약을 설명한다.
2. **성공/실패 균형**: Happy path와 동일한 비중으로 예외·경계 케이스를 포함한다.
3. **독립 재현**: 외부 상태나 순서에 의존하지 않고 Fixture/Mock만으로 실행 가능해야 한다.
4. **아키텍처 검증과 연동**: ArchUnit 테스트가 레이어 경계를 지키는 동안, 모듈 테스트는 계약을 보증한다.

### 2. 레이어별 플레이북

#### Domain

- 목적: 엔티티/밸류의 불변식과 상태 전이를 검증한다.
- DisplayName: 규칙 설명형 문장 (`생성 시 타임스탬프가 동일하게 초기화된다`).
- 값 생성: literal + `FixtureTime`. 외부 협력자는 사용하지 않는다.
- 필수 시나리오: 정상 생성, 상태 변경, 잘못된 인자/시간 누락 등 예외 경로.

#### Application(Service)

- 도구: JUnit5 + `@ExtendWith(MockitoExtension.class)`.
- DisplayName: 시나리오 문장 (`사용자가 존재하면 게시글을 생성한다`).
- 패턴: `given`(fixture/mock) → `when` (service 호출) → `then`(assert + `verify`).
- 실패 케이스는 `assertThatThrownBy`로 예외 타입과 메시지를 검증하고, 저장소에 `never()` 호출을 단언한다.

#### Presentation(Controller/Adapter)

- 표준: `@WebMvcTest` + `@AutoConfigureMockMvc(addFilters = false)`.
- Mock 주입은 `@TestConfiguration` 또는 `@MockBean`으로 명시한다.
- DisplayName: `엔드포인트 + 성공/실패_사유` (`세션 조회 실패_인증 없으면 401`).
- 실제 계약과 동일한 JSON/Multipart 파라미터, HTTP Status, 헤더를 검증하고 서비스 위임 여부를 `verify`한다.
- 통합 흐름(`UserControllerIntegrationTest`)처럼 Spring Context가 필요한 경우만 `@SpringBootTest`를 사용한다.

### 3. Fixture & 시간 전략

- 모든 상수/DTO/엔티티는 `com.ssafy.dash.common.TestFixtures` 또는 모듈별 fixture record에서 가져온다.
- 시간은 항상 `FixtureTime.now()` 값을 기준으로 하고, 상대 시나리오는 `plusHours`, `plusMinutes` 등으로 표현한다.
- Fixture 사용 우선순위
    1. 간단한 literal (파라미터 ≤2, 의도가 명확할 때)
    2. `TestFixtures` helper (재사용이 필요한 DTO/엔티티)
    3. 모듈 전용 fixture record (`FooFixtures.FooFixture.with...`)
- OAuth/Onboarding처럼 타 모듈과 얽히는 경우에는 전용 fixture record를 만들어 협력자 조합을 명시한다.

### 4. 예외/경계 테스트 원칙

- Repository/Service 호출은 “ID가 존재하지 않을 때” 시나리오를 기본 포함한다.
- 입력 검증은 “빈 문자열, null, 음수” 등 경계값을 최소 1개 이상 다룬다.
- 업데이트/부분 수정 메서드는 “필드 일부만 변경”과 “시간 누락” 시나리오를 모두 테스트한다.
- 성공 케이스에서도 side effect (`updatedAt`, `webhookConfigured`)를 명시적으로 검증한다.

### 5. 기술 스택 & 실행

- Assertions: AssertJ (`assertThat`, `assertThatThrownBy`).
- Mocking: Mockito (`BDDMockito.given`, `verify`, `never`).
- Spring Test: `MockMvc`, `@WebMvcTest`, 필요 시 `@SpringBootTest`.
- 실행 명령

```powershell
cd backend
..\mvnw.cmd test  # 전체 테스트
..\mvnw.cmd -Dtest=AlgorithmRecordServiceTest test
```

### 6. 모듈별 커버리지 매트릭스

| 모듈         | Controller/Adapter                                          | Service                                         | Domain                | 특이 사항                          |
|------------|-------------------------------------------------------------|-------------------------------------------------|-----------------------|--------------------------------|
| Algorithm  | `AlgorithmRecordControllerTest`                             | `AlgorithmRecordServiceTest`                    | `AlgorithmRecordTest` | Multipart 업로드, OAuth 사용자 ID 추출 |
| Board      | `BoardControllerTest`                                       | `BoardServiceTest`                              | `BoardTest`           | 작성자 조회와 권한 검증                  |
| User       | `UserControllerIntegrationTest`                             | `UserServiceTest`                               | `UserTest`            | CRUD + `/me` 엔드포인트 포함          |
| OAuth      | `OAuthSessionControllerTest`, `CustomOAuth2UserServiceTest` | `OAuthUserServiceTest`, `OAuthTokenServiceTest` | `UserOAuthTokenTest`  | Security 어댑터/Access Token 갱신   |
| Onboarding | `OnboardingControllerTest`                                  | `OnboardingServiceTest`                         | `OnboardingTest`      | GitHub Webhook/토큰 경로 필수        |

신규 모듈도 Controller + Service + Domain 세 축을 기본선으로 삼고, 별도 어댑터(Security, GitHub 등)가 있다면 전용 테스트 클래스를 추가한다.

### 7. DisplayName 컨벤션

- Controller: `행위 + 성공/실패_사유` 예) `저장소 설정 실패_웹훅 오류로 502`
- Service: 문장형 시나리오 예) `존재하지 않는 기록을 수정하면 AlgorithmRecordNotFoundException이 발생한다`
- Domain: 규칙 설명 예) `잘못된 파라미터로 생성하면 IllegalArgumentException이 발생한다`

DisplayName 패턴과 실제 구현이 불일치하면 즉시 수정한다.

### 8. 리뷰 체크리스트

1. 테스트가 해당 레이어의 책임을 드러내는가?
2. 성공/실패 케이스와 경계값이 모두 존재하는가?
3. Fixture/시간 의도가 명확하게 표현되었는가?
4. Repository/Service 상호작용(`verify`, `never`)이 필요한 곳에 있는가?
5. HTTP 테스트에서 계약(상태 코드, JSON Path, 헤더)이 명확한가?
6. ArchUnit/아키텍처 규칙을 깨는 직접 의존이 없는가?
7. 새로운 모듈과 동일한 테스트 매트릭스를 유지했는가?

본 문서를 기본 체크리스트로 삼고, 새로운 패턴이 추가되면 내용을 확장해 공유한다.
