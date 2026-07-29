# Per-layer test strategy (daruda)

## Service — the core target

Put repositories and external services all under `@Mock` and verify the **business branching**.

Verify: happy path, exception paths (missing resource / no permission / duplicate), boundary values, whether other services are called.

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

When `save()` must fill in an ID, mimic it with `willAnswer`.

```java
given(boardRepository.save(any(Board.class))).willAnswer(inv -> {
	Board saved = inv.getArgument(0);
	ReflectionTestUtils.setField(saved, "id", 10L);
	return saved;
});
```

## Controller — MockMvc standalone

Use `MockMvcBuilders.standaloneSetup`, not `@WebMvcTest` (it doesn't spin up the context, so it's fast).

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

For APIs that need authentication, put a `UserAuthentication` into `SecurityContextHolder` in advance.

Verify: status code, response JSON structure, **whether parameters are properly delegated to the service** (`ArgumentCaptor`).

```java
mockMvc.perform(get("/api/v1/board").param("sortBy", "SCRAP"))
	.andExpect(status().isOk())
	.andExpect(jsonPath("$.status").value(200))
	.andExpect(jsonPath("$.data.boards[0].boardId").value(10));

ArgumentCaptor<BoardSortType> captor = ArgumentCaptor.forClass(BoardSortType.class);
then(boardService).should().getBoardList(any(), captor.capture(), anyInt());
assertThat(captor.getValue()).isEqualTo(BoardSortType.SCRAP);
```

For creation APIs, check that the status code is **201** (this is where you catch a `SuccessCode`/HTTP-status mismatch).

## Entity / Enum / DTO

They have no dependencies, so write pure unit tests without `@ExtendWith`.

- **Entity**: domain methods (state changes, validation exceptions)
- **enum**: parsing·mapping logic (see `BoardSortTypeTest`, `PositionsTest`)
- **DTO**: Bean Validation constraints — build a `Validator` directly and validate

```java
private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

@Test
@DisplayName("제목이 비면 검증 실패")
void title_blank() {
	CreateBoardRequest request = new CreateBoardRequest("", "본문", null);
	assertThat(validator.validate(request)).isNotEmpty();
}
```
