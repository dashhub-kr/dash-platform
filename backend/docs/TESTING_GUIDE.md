# 테스트 가이드 (TESTING GUIDE)

안정적인 배포를 위해 모든 변경 사항은 **TDD/Test-First** 흐름과 `mvn test` 전체 스위트를 통과해야 합니다. 아래 원칙과 절차를 따라 테스트 품질을 유지하세요.

## 1. 테스트 레이어와 책임

| 레이어             | 대상                         | 비고                                                                  |
| ------------------ | ---------------------------- | --------------------------------------------------------------------- |
| 단위(Unit)         | Application/Domain 서비스    | Mockito 기반, 외부 의존성 Mock                                        |
| 슬라이스(WebMvc)   | Presentation Controller, DTO | `@WebMvcTest`, `MockMvc`, `@AutoConfigureMockMvc(addFilters = false)` |
| 통합(Integration)  | REST + DB(H2) 조합           | `@SpringBootTest` + `@ActiveProfiles("test")`                         |
| 아키텍처(ArchUnit) | 패키지/레이어 규칙           | `ArchitectureRulesTest` 가 자동 실행                                  |

## 2. 테스트 실행 명령

```powershell
# 전체 스위트 (권장)
PS C:\dash\backend> .\mvnw.cmd clean test

# 아키텍처 규칙만 빠르게 확인
PS C:\dash\backend> .\mvnw.cmd test -Dtest=*ArchitectureRulesTest

# 단일 도메인 슬라이스 테스트 예시
PS C:\dash\backend> .\mvnw.cmd test -Dtest=OAuthSessionControllerTest
```

- CI 는 `.\n+mvnw.cmd clean test` 에 의존하므로, 로컬에서도 동일 명령을 실행해 아키텍처/통합 테스트 실패를 조기에 발견합니다.

## 3. 테스트 작성 원칙

- **Given-When-Then**: 준비/실행/검증 구간을 주석 또는 빈 줄로 분리해 의도를 명확히 합니다.
- **테스트 격리**: 각 테스트는 독립 실행 가능해야 하며, 전역 상태(예: `SecurityContextHolder`)는 `@AfterEach` 에서 정리합니다.
- **중앙 픽스처**: `com.ssafy.dash.common.TestFixtures` 으로 테스트 데이터를 만들고, 새로운 도메인이 추가되면 동일한 위치에 팩토리를 확장합니다.
- **명확한 이름**: `methodName_state_expectedResult` 혹은 자연어 `DisplayName` 으로 실패 지점을 빠르게 파악합니다.

## 4. 레이어별 작성 팁

### 4.1. Application/Domain 단위 테스트

- `@ExtendWith(MockitoExtension.class)` 와 `@InjectMocks`/`@Mock` 조합을 기본으로 사용합니다.
- 도메인 엔티티는 빌더보다 팩토리 메서드(예: `TestFixtures.createUser()`)를 사용해 중복을 줄입니다.
- 외부 포트(Mapper, Client)는 Mock 으로 대체하고, 반환값/예외 케이스를 명시적으로 검증합니다.

### 4.2. Presentation 슬라이스 테스트

- `@WebMvcTest(SomeController.class)` 로 컨트롤러 계층만 로드하고, 협력 서비스는 `@Import` 또는 `@TestConfiguration` 으로 Mock 빈을 정의합니다.
- Security 필터 없이 HTTP 인터페이스를 검증하려면 `@AutoConfigureMockMvc(addFilters = false)` 를 사용하고, 인증 흐름 검증이 필요하면 `SecurityContextHolder` 에 `CustomOAuth2User` 를 직접 주입합니다.
- DTO 검증은 `jsonPath` 로 필드별 확인, 예외 응답은 상태 코드와 에러 코드를 함께 확인합니다.

### 4.3. 통합 테스트

- `@SpringBootTest` 와 H2 인메모리 DB (`jdbc:h2:mem:testdb`) 를 사용합니다.
- `@ActiveProfiles("test")` 로 테스트 설정을 분리하고, 트랜잭션 롤백 여부를 명확히 합니다.

## 5. 아키텍처 규칙 테스트

- `ArchitectureRulesTest` 는 ArchUnit 으로 레이어 간 의존 규칙을 검사합니다. 예를 들어 Presentation 이 Infrastructure 를 직접 참조하면 즉시 실패합니다.
- OAuth 인증과 같이 프레임워크 의존도가 높은 코드는 `oauth.presentation.security` 하위에 위치해야 ArchUnit 이 허용합니다.

## 6. 보안 컨텍스트 테스트 가이드

- OAuth 기반 컨트롤러 테스트에서는 `CustomOAuth2User` 를 생성해 `SecurityContextHolder` 에 설정하고, 테스트 종료 후 `clearContext()` 를 호출해 다음 테스트에 영향을 주지 않도록 합니다.
- 인증 흐름을 통합 테스트에서 검증할 때는 `MockMvc` 의 `with(authentication)` 헬퍼나 `@WithMockUser` 를 적절히 선택합니다.

## 7. 리팩토링 체크리스트

- [ ] 테스트 코드에 중복된 객체 생성 로직이 있는가? → `TestFixtures` 로 이동
- [ ] 테스트 메서드명이 의도를 명확히 드러내는가?
- [ ] 불필요한 Stubbing 은 없는가? (`UnnecessaryStubbingException` 주의)
- [ ] 새 아키텍처 규칙에 맞지 않는 참조는 없는가? (`mvn test` 로 즉시 검증)
