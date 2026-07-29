---
name: testing
description: Load when writing or modifying tests. Holds the JUnit 5 + Mockito (BDD) + AssertJ base structure and required rules; per-layer strategy, fixtures, and anti-patterns are split into references/.
---

# Testing (daruda)

**JUnit 5 + Mockito + AssertJ.** Unit tests (`@ExtendWith(MockitoExtension.class)`) are the default; integration tests (`@SpringBootTest`) are not used.

## Detailed-rule routing

| What you are doing | File to read |
|--------------------|--------------|
| Per-layer test strategy for service/controller/entity, MockMvc setup | `references/layer-strategy.md` |
| Building fixtures, anti-patterns, run commands | `references/fixtures.md` |

## Base structure

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

## Required rules

| Item | Rule |
|------|------|
| Class name | `{target}Test`, no access modifier (package-private) |
| Class `@DisplayName` | optional. Required (in Korean) on `@Nested` classes |
| Method name | `{methodName}_{situation}` in English snake_case (`create_success`, `create_userNotFound`) |
| Method `@DisplayName` | **required, in Korean.** Failure scenarios use the `"condition → ExceptionName"` form |
| Grouping | group by target method into `@Nested` classes |
| Body | 3-part split with `// given` / `// when` / `// then` comments (exception checks use `// when & then`) |
| Stubbing | **BDDMockito**: `given(...).willReturn(...)`, `willThrow`, `willAnswer` |
| Verification | **BDDMockito**: `then(mock).should().method()` |
| Assertion | **AssertJ**: `assertThat(...)`, `assertThatThrownBy(...)` |
| Entity ID | `ReflectionTestUtils.setField(entity, "id", 1L)` |

**No mixing**: `Mockito.when(...).thenReturn(...)` / `Mockito.verify(...)` / JUnit `Assertions.assertEquals` are not used in new code. They're mixed in existing files, but new tests are unified to BDD + AssertJ.

## static import

For readability, use static imports for the following (this codebase's idiom).

```java
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
```

## Scenarios to cover

For each service method, at minimum:

- Happy path (verify return value + side effects)
- Resource not found → `NotFoundException`
- No permission → `ForbiddenException` (when there is an owner check)
- Boundary values (page size, cursor, empty list)

> Test code is also subject to Checkstyle (`checkstyleTest`). Follow tab indentation·120 chars·import order.
