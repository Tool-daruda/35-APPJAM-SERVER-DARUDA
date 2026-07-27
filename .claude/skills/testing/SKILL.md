---
name: testing
description: 테스트를 작성하거나 수정할 때 로드. JUnit 5 + Mockito(BDD) + AssertJ 기본 구조와 필수 규칙을 담고, 레이어별 전략과 픽스처·안티패턴은 references/에 분리돼 있다.
---

# 테스트 (daruda)

**JUnit 5 + Mockito + AssertJ.** 단위 테스트(`@ExtendWith(MockitoExtension.class)`)가 기본이고, 통합 테스트(`@SpringBootTest`)는 쓰지 않는다.

## 상세 규칙 라우팅

| 무엇을 하려는가 | 읽을 파일 |
|-----------------|-----------|
| 서비스/컨트롤러/엔티티 레이어별 테스트 전략, MockMvc 설정 | `references/layer-strategy.md` |
| 픽스처 만들기, 안티패턴, 실행 명령 | `references/fixtures.md` |

## 기본 구조

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

## 필수 규칙

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
| 엔티티 ID | `ReflectionTestUtils.setField(entity, "id", 1L)` |

**혼용 금지**: `Mockito.when(...).thenReturn(...)` / `Mockito.verify(...)` / JUnit `Assertions.assertEquals`는 신규 코드에서 쓰지 않는다. 기존 파일에 섞여 있지만 새로 쓰는 테스트는 BDD + AssertJ로 통일한다.

## static import

가독성을 위해 아래는 static import를 쓴다(이 코드베이스의 관용).

```java
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
```

## 커버할 시나리오

서비스 메서드마다 최소한:

- 정상 경로 (반환값 + 부수 효과 검증)
- 리소스 없음 → `NotFoundException`
- 권한 없음 → `ForbiddenException` (소유자 검증이 있는 경우)
- 경계값 (페이지 크기, 커서, 빈 목록)

> 테스트 코드도 Checkstyle 대상이다(`checkstyleTest`). 탭 들여쓰기·120자·import 순서를 지킨다.
