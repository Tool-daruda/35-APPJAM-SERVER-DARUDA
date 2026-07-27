# 웹 레이어 — DTO · 검증 · 응답 · 컨트롤러 (daruda)

## DTO 매핑

요청/응답 DTO는 **`record`**로 만들고, 변환 책임은 DTO의 정적 팩토리에 둔다.

```java
// dto/response/BoardResponse.java
public record BoardResponse(
	Long boardId,
	String title,
	String content,
	String nickname,
	int scrapCount
) {
	public static BoardResponse from(final Board board, final int scrapCount) {
		return new BoardResponse(
			board.getId(),
			board.getTitle(),
			board.getContent(),
			board.getUser().getNickname(),
			scrapCount
		);
	}
}
```

- 필드가 1개뿐이어도 응답은 DTO로 감싼다(추후 필드 추가 시 클라이언트 계약이 깨지지 않는다).
- 엔티티 → DTO 변환은 **서비스 또는 DTO 정적 팩토리**에서. 컨트롤러에서 변환하지 않는다.
- 파라미터가 3개를 넘고 같은 타입이 이어지면 정적 팩토리 대신 빌더를 고려한다.
- 이름은 `{동사}{대상}Request` / `{동사}{대상}Response`. **`Req`/`Res` 축약 금지.**

## 요청 검증

```java
public record CreateBoardRequest(
	@NotBlank(message = "제목은 필수입니다")
	@Size(max = 100, message = "제목은 100자 이하여야 합니다")
	String title,

	@NotBlank(message = "내용은 필수입니다")
	String content,

	Long toolId
) {
}
```

컨트롤러에서 `@RequestBody @Valid`로 트리거한다. 위반 시 `MethodArgumentNotValidException` → `GlobalExceptionHandler`가 `ErrorCode.INVALID_FIELD_ERROR` + 필드별 메시지로 응답한다. **검증 로직을 서비스에서 `if`로 중복 구현하지 않는다.**

쿼리 파라미터 검증은 컨트롤러 파라미터에 직접(`@Min`, `@Max`) 붙이고 클래스에 `@Validated`가 필요하다.

## 컨트롤러 작성 패턴

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/board")
@Tag(name = "board 컨트롤러", description = "게시글과 관련된 API를 처리합니다.")
public class BoardController {

	private final BoardService boardService;

	@DisableSwaggerSecurity                  // 인증 불필요한 API에만
	@GetMapping("/{board-id}")
	@Operation(summary = "게시글 조회", description = "게시글 단건을 조회합니다.")
	public ResponseEntity<SuccessResponse<BoardResponse>> getBoard(
		@Parameter(description = "board Id", example = "1")
		@PathVariable("board-id") final Long boardId
	) {
		BoardResponse response = boardService.getBoard(boardId);
		return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, response));
	}
}
```

규칙:

- URL은 `/api/v1/{도메인}`, 경로 변수는 **kebab-case**(`{board-id}`), 자바 파라미터명은 camelCase.
- 인증 사용자: `@AuthenticationPrincipal Long userId`. 비로그인도 허용하는 API는 `Long userIdOrNull`처럼 nullable임이 드러나는 이름을 쓴다.
- 미인증 API는 `SecurityConfig`의 `WHITE_LIST`에도 등록해야 실제로 열린다. **`@DisableSwaggerSecurity`는 문서 표시용일 뿐 인증을 풀지 않는다.**
- 컨트롤러 메서드는 얇게 유지한다. 3줄(서비스 호출 → 응답 래핑)이 기본형이다.

## 응답 포맷

성공은 `SuccessResponse<T>`, 실패는 던지기만 하고 `GlobalExceptionHandler`에 맡긴다.

```java
// 200 조회/수정/삭제
return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, data));
return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_DELETE));   // 데이터 없음

// 201 생성 — SuccessCode가 CREATED면 HTTP 상태도 반드시 201
return ResponseEntity.status(HttpStatus.CREATED)
	.body(SuccessResponse.of(SuccessCode.SUCCESS_CREATE, data));
```

**`SuccessCode`의 상태와 실제 HTTP 응답 상태를 반드시 일치시킨다.** 에러 응답 포맷과 `ErrorCode` 추가 규칙은 `error-handling` 스킬을 본다.

> `ApiResponse`는 레거시다. 신규 코드에서 쓰지 않는다(`legacy-cleanup` 스킬 참조).
