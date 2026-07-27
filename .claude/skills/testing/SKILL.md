---
name: testing
description: 테스트를 작성하거나 수정할 때 로드. JUnit 5 + Mockito + AssertJ, @Nested 구조, BDD 스타일, 레이어별 테스트 전략, 픽스처 작성, 컨트롤러 MockMvc 테스트.
---

# 테스트 (daruda)

**JUnit 5 + Mockito + AssertJ.** 단위 테스트(`@ExtendWith(MockitoExtension.class)`)가 기본이고, 통합 테스트(`@SpringBootTest`)는 쓰지 않는다.

## 공통 구조

```java
@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

	@Mock
	private BoardRepository boardRepository;
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private BoardService boardService;

	private User author;
	private Board board;

	@BeforeEach
	void setUp() {
		author = User.builder()
			.email("writer@test.com")
			.nickname("작성자")
			.positions(Positions.WORKER)
			.build();
		ReflectionTestUtils.setField(author, "id", 1L);

		board = Board.of("제목", "본문", author);
		ReflectionTestUtils.setField(board, "id", 10L);
	}

	@Nested
	@DisplayName("게시글 생성")
	class Create {

		@Test
		@DisplayName("정상 생성")
		void create_success() {
			// given
			given(userRepository.findById(1L)).willReturn(Optional.of(author));
			given(boardRepository.save(any(Board.class))).willAnswer(inv -> {
				Board saved = inv.getArgument(0);
				ReflectionTestUtils.setField(saved, "id", 10L);
				return saved;
			});
			CreateBoardRequest request = new CreateBoardRequest("제목", "본문", null);

			// when
			CreateBoardResponse result = boardService.createBoard(1L, request);

			// then
			assertThat(result.boardId()).isEqualTo(10L);
			then(boardRepository).should().save(any(Board.class));
		}

		@Test
		@DisplayName("없는 사용자 → NotFoundException")
		void create_userNotFound() {
			// given
			given(userRepository.findById(999L)).willReturn(Optional.empty());
			CreateBoardRequest request = new CreateBoardRequest("제목", "본문", null);

			// when & then
			assertThatThrownBy(() -> boardService.createBoard(999L, request))
				.isInstanceOf(NotFoundException.class);
		}
	}
}
```

## 규칙

| 항목 | 규칙 |
|------|------|
| 클래스 이름 | `{대상}Test`, 접근제어자 없음(package-private) |
| 클래스 `@DisplayName` | 생략 가능. `@Nested` 클래스에는 한국어로 필수 |
| 메서드 이름 | `{메서드명}_{상황}` 영어 스네이크 (`create_success`, `create_userNotFound`) |
| 메서드 `@DisplayName` | **한국어로 필수.** 실패 시나리오는 `"조건 → 예외명"` 형식 |
| 그룹핑 | 대상 메서드별로 `@Nested` 클래스로 묶는다 |
| 본문 | `// given` / `// when` / `// then` 주석으로 3단 구분 (예외 검증은 `// when & then`) |
| 스텁 | **BDDMockito**: `given(...).willReturn(...)`, `willThrow`, `willAnswer` |
| 검증 | **BDDMockito**: `then(mock).should().method()` |
| 단언 | **AssertJ**: `assertThat(...)`, `assertThatThrownBy(...)` |

**혼용 금지**: `Mockito.when(...).thenReturn(...)` / `Mockito.verify(...)` / JUnit `Assertions.assertEquals`는 신규 코드에서 쓰지 않는다. 기존 파일에 섞여 있지만 새로 쓰는 테스트는 BDD + AssertJ로 통일한다.

## static import

가독성을 위해 아래는 static import를 쓴다(이 코드베이스의 관용).

```java
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
```

## 픽스처 만들기

엔티티 ID는 `@GeneratedValue`라 빌더로 넣을 수 없다. **`ReflectionTestUtils.setField`로 주입**한다.

```java
Board board = Board.of("제목", "본문", author);
ReflectionTestUtils.setField(board, "id", 10L);
```

- 여러 테스트가 공유하는 픽스처는 `@BeforeEach setUp()`에서 만든다.
- 특정 테스트에만 필요한 데이터는 그 테스트의 `// given` 안에서 만든다.
- 엔티티를 `@Mock`으로 만드는 것은 피한다(도메인 메서드 동작까지 사라진다). 실제 인스턴스를 쓴다.

## 레이어별 전략

### Service — 핵심 대상

리포지토리·외부 서비스를 모두 `@Mock`으로 두고 **비즈니스 분기**를 검증한다.

검증할 것: 정상 경로, 예외 경로(없는 리소스 / 권한 없음 / 중복), 경계값, 다른 서비스 호출 여부.

```java
@Test
@DisplayName("작성자가 아닌 사용자가 삭제 시도 → ForbiddenException")
void delete_notAuthor() {
	// given
	given(commentRepository.findById(100L)).willReturn(Optional.of(comment));

	// when & then
	assertThatThrownBy(() -> commentService.deleteComment(999L, 100L))
		.isInstanceOf(ForbiddenException.class);
	then(commentRepository).should(never()).delete(any());
}
```

### Controller — MockMvc standalone

`@WebMvcTest`가 아니라 `MockMvcBuilders.standaloneSetup`을 쓴다(컨텍스트를 띄우지 않아 빠르다).

```java
@ExtendWith(MockitoExtension.class)
class BoardControllerTest {

	@Mock
	private BoardService boardService;
	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private BoardController boardController;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(boardController)
			.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
			.addFilters(new JwtAuthenticationFilter(jwtTokenProvider))
			.build();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}
}
```

인증이 필요한 API는 `SecurityContextHolder`에 `UserAuthentication`을 미리 넣는다.

검증할 것: 상태 코드, 응답 JSON 구조, **파라미터가 서비스에 제대로 위임되는지**(`ArgumentCaptor`).

```java
mockMvc.perform(get("/api/v1/board").param("sortBy", "SCRAP"))
	.andExpect(status().isOk())
	.andExpect(jsonPath("$.status").value(200))
	.andExpect(jsonPath("$.data.boards[0].boardId").value(10));

ArgumentCaptor<BoardSortType> captor = ArgumentCaptor.forClass(BoardSortType.class);
then(boardService).should().getBoardList(any(), captor.capture(), anyInt());
assertThat(captor.getValue()).isEqualTo(BoardSortType.SCRAP);
```

### Entity / Enum / DTO

의존성이 없으므로 `@ExtendWith` 없이 순수 단위 테스트로 쓴다.

- 엔티티: 도메인 메서드(상태 변경, 검증 예외)
- enum: 파싱·매핑 로직 (`BoardSortTypeTest`, `PositionsTest` 참고)
- DTO: Bean Validation 제약 — `Validator`를 직접 만들어 검증한다

```java
private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

@Test
@DisplayName("제목이 비면 검증 실패")
void title_blank() {
	CreateBoardRequest request = new CreateBoardRequest("", "본문", null);
	assertThat(validator.validate(request)).isNotEmpty();
}
```

## 하지 말 것

| 안티패턴 | 대안 |
|----------|------|
| `private` 메서드를 리플렉션(`getDeclaredMethod` + `setAccessible`)으로 테스트 | public 메서드를 통해 간접 검증한다. 꼭 단독 검증이 필요하면 그 로직은 별도 클래스로 분리할 신호다 |
| `verify`만 있고 단언이 없는 테스트 | 결과 상태를 `assertThat`으로 확인 |
| `any()`만으로 스텁·검증 | 실제 값으로 검증해야 회귀를 잡는다 |
| 한 테스트에서 여러 시나리오 검증 | `@Nested` + 개별 `@Test`로 분리 |
| 테스트 간 상태 공유(static 필드) | `@BeforeEach`에서 매번 새로 만든다 |
| 불필요한 스텁 방치 | Mockito strict stub이 `UnnecessaryStubbingException`으로 실패시킨다. 안 쓰는 `given`은 지운다 |

## 실행

```bash
./gradlew test                                  # 전체
./gradlew test --tests "BoardServiceTest"       # 특정 클래스
./gradlew test --tests "*.community.*"          # 패턴
./gradlew test --info                           # 상세 로그 (CI와 동일)
```

리포트: `build/reports/tests/test/index.html`

> 테스트 코드도 Checkstyle 대상이다(`checkstyleTest`). 탭 들여쓰기·120자·import 순서를 지킨다.
