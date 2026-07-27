# 레이어별 테스트 전략 (daruda)

## Service — 핵심 대상

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

`save()`가 ID를 채워야 하는 경우는 `willAnswer`로 흉내낸다.

```java
given(boardRepository.save(any(Board.class))).willAnswer(inv -> {
	Board saved = inv.getArgument(0);
	ReflectionTestUtils.setField(saved, "id", 10L);
	return saved;
});
```

## Controller — MockMvc standalone

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

생성 API는 상태 코드가 **201**인지 확인한다(`SuccessCode`와 HTTP 상태 불일치를 잡는 지점이다).

## Entity / Enum / DTO

의존성이 없으므로 `@ExtendWith` 없이 순수 단위 테스트로 쓴다.

- **엔티티**: 도메인 메서드(상태 변경, 검증 예외)
- **enum**: 파싱·매핑 로직 (`BoardSortTypeTest`, `PositionsTest` 참고)
- **DTO**: Bean Validation 제약 — `Validator`를 직접 만들어 검증한다

```java
private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

@Test
@DisplayName("제목이 비면 검증 실패")
void title_blank() {
	CreateBoardRequest request = new CreateBoardRequest("", "본문", null);
	assertThat(validator.validate(request)).isNotEmpty();
}
```
