# Web layer — DTO · validation · response · controller (daruda)

## DTO mapping

Make request/response DTOs as **`record`**s, and put the conversion responsibility in the DTO's static factory.

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

- Wrap responses in a DTO even with a single field (adding a field later won't break the client contract).
- Do entity → DTO conversion in the **service or the DTO's static factory**. Do not convert in the controller.
- If there are more than 3 parameters and same-typed ones run consecutively, consider a builder instead of a static factory.
- Names are `{verb}{target}Request` / `{verb}{target}Response`. **No `Req`/`Res` abbreviations.**

## Request validation

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

Trigger it in the controller with `@RequestBody @Valid`. On violation, `MethodArgumentNotValidException` → `GlobalExceptionHandler` responds with `ErrorCode.INVALID_FIELD_ERROR` + per-field messages. **Do not re-implement validation logic with `if` in the service.**

Validate query parameters directly on controller parameters (`@Min`, `@Max`). Spring MVC's built-in method validation (Spring 6.1+, used by this project's Boot 3.4) runs them without a class-level `@Validated`; add `@Validated` only when AOP-based validation or validation groups are intentionally required.

## Controller pattern

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/board")
@Tag(name = "board 컨트롤러", description = "게시글과 관련된 API를 처리합니다.")
public class BoardController {

	private final BoardService boardService;

	@DisableSwaggerSecurity                  // only on APIs that need no auth
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

Rules:

- URLs are `/api/v1/{domain}`, path variables are **kebab-case** (`{board-id}`), Java parameter names are camelCase.
- Authenticated user: `@AuthenticationPrincipal Long userId`. For APIs that also allow anonymous access, use a name that reveals nullability, like `Long userIdOrNull`.
- Unauthenticated APIs must also be registered in `SecurityConfig`'s `WHITE_LIST` to actually be open. **`@DisableSwaggerSecurity` is only for documentation display; it does not lift authentication.**
- Keep controller methods thin. 3 lines (service call → response wrapping) is the baseline.

## Response format

Success uses `SuccessResponse<T>`; for failures just throw and leave it to `GlobalExceptionHandler`.

```java
// 200 read/update/delete
return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, data));
return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_DELETE));   // no data

// 201 create — if SuccessCode is CREATED, the HTTP status must also be 201
return ResponseEntity.status(HttpStatus.CREATED)
	.body(SuccessResponse.of(SuccessCode.SUCCESS_CREATE, data));
```

**Always keep the `SuccessCode`'s status and the actual HTTP response status in sync.** For the error response format and `ErrorCode`-addition rules, see the `error-handling` skill.
