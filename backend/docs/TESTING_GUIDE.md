# 테스트 가이드 (TESTING GUIDE)

이 프로젝트는 견고한 애플리케이션 구축을 위해 **TDD(Test Driven Development)** 실천을 권장합니다.

## 1. 테스트 원칙

- **Given-When-Then 패턴**: 모든 테스트 메서드는 준비(Given), 실행(When), 검증(Then) 단계로 명확히 구분하여 작성합니다.
- **단위 테스트 중심**: 외부 의존성(DB, Network 등)은 Mocking하여 비즈니스 로직 검증에 집중합니다.
- **테스트 격리**: 각 테스트는 독립적으로 실행되어야 하며, 실행 순서에 의존하지 않아야 합니다.

## 2. 테스트 도구

- **JUnit 5**: 테스트 프레임워크
- **Mockito**: Mocking 라이브러리
- **AssertJ**: 가독성 높은 단언문(Assertion) 라이브러리 (`assertThat`)

## 3. 테스트 구조 및 작성 요령

### 3.1. 공통 픽스처 (TestFixtures)

테스트 데이터 생성의 중복을 줄이고 일관성을 유지하기 위해 `com.ssafy.dash.common.TestFixtures`를 사용합니다.  
**새로운 도메인을 추가할 때 반드시 `TestFixtures`에 팩토리 메서드를 추가하세요.**

```java
// Bad: 각 테스트 파일마다 객체 생성 로직 반복
User user = new User(1L, "tester", ...);

// Good: 중앙화된 픽스처 사용
User user = TestFixtures.createUser();
```

### 3.2. Service 테스트 (단위 테스트)

- `@ExtendWith(MockitoExtension.class)`를 사용하여 Mockito를 활성화합니다.
- `@InjectMocks`: 테스트 대상 서비스
- `@Mock`: 협력 객체 (Mapper, Repository 등)

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("BoardService 단위 테스트")
class BoardServiceTest {

    @InjectMocks private BoardService boardService;
    @Mock private BoardRepository boardRepository;
    @Mock private UserRepository userRepository;

    @Test
    @DisplayName("게시글 생성 성공")
    void createBoard_Success() {
        // given
        User user = TestFixtures.createUser();
        given(userRepository.findById(any())).willReturn(Optional.of(user));

        // when
        BoardResponse response = boardService.create(req);

        // then
        verify(boardRepository).save(any());
        assertThat(response.getTitle()).isEqualTo(req.getTitle());
    }

}
```

### 3.3. Controller 테스트 (슬라이스 테스트)

- `@WebMvcTest(Controller.class)`를 사용하여 웹 계층만 로드합니다.
- `@MockBean`: 서비스 계층을 Mocking합니다.
- `MockMvc`: HTTP 요청을 시뮬레이션합니다.
- `@WithMockUser`: Spring Security 인증 정보를 모의합니다.

```java
@WebMvcTest(BoardController.class)
class BoardControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean BoardService boardService;

    @Test
    @WithMockUser
    void createBoard_Success() throws Exception {
        // given
        given(boardService.create(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/boards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

}
```

## 4. 리팩토링 체크리스트

- [ ] 테스트 코드에 중복된 객체 생성 로직이 있는가? → `TestFixtures`로 이동
- [ ] 테스트 메서드명이 의도를 명확히 드러내는가? (`MethodName_State_ExpectedBehavior`)
- [ ] 불필요한 Stubbing이 없는가? (`UnnecessaryStubbingException` 주의)
