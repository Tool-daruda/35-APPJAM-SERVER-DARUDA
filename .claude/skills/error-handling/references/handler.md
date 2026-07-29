# GlobalExceptionHandler · response format · auth exceptions (daruda)

## Already-handled exceptions

`global/handler/GlobalExceptionHandler.java` handles the following. **Before adding a new handler, check whether it's a duplicate.**

| Exception | Response |
|-----------|----------|
| `BusinessException` | the corresponding `ErrorCode` as-is |
| `MethodArgumentNotValidException` | 400 + per-field messages |
| `ConstraintViolationException` | 400 + per-path messages |
| `MissingServletRequestParameterException` | 400 missing parameter |
| `MissingRequestHeaderException` | 400 missing header |
| `MethodArgumentTypeMismatchException` | 400 type mismatch |
| `DataIntegrityViolationException` | 400 integrity violation |
| `IllegalArgumentException` / `IllegalStateException` | 400 |
| `MultipartException` / `MaxUploadSizeExceededException` | 500 upload failure |
| `IOException` / `Exception` | 500 |

## Adding a handler

Follow the existing style.

```java
@ExceptionHandler(XxxException.class)
public ResponseEntity<ErrorResponse> handleXxxException(XxxException ex) {
	log.warn("XxxException 발생: {}", ex.getMessage(), ex);
	return ResponseEntity.status(ErrorCode.YYY.getHttpStatus())
		.body(ErrorResponse.of(ErrorCode.YYY));
}
```

Log level: client fault (4xx) → `debug`/`warn`, server fault (5xx) → `error` (with stack trace).

## Response format

**Error** (`ErrorResponse`):

```json
{
  "status": 404,
  "code": "E404006",
  "message": "게시글이 존재하지 않습니다"
}
```

**Validation failure** (includes field info):

```json
{
  "status": 400,
  "code": "E400002",
  "message": "요청 필드 값이 유효하지 않습니다.",
  "errors": [
    { "field": "title", "message": "제목은 필수입니다" }
  ]
}
```

**Success** (`SuccessResponse`):

```json
{
  "status": 200,
  "message": "요청 데이터가 성공적으로 조회되었습니다",
  "data": { }
}
```

If `data` is null it is omitted from the response (`@JsonInclude(NON_NULL)`).

## Auth/authorization exceptions

Exceptions raised in the Security filter chain **are not caught by `GlobalExceptionHandler`** (they occur before the controller is reached).

| Situation | Handling location |
|-----------|-------------------|
| Authentication failure | `JwtAuthenticationEntryPoint` |
| Authorization failure | `CustomAccessDeniedHandler` |
| Exception inside a filter | `ExceptionHandlerFilter` |

When dealing with JWT-related exceptions, modify under `global/auth/security/`.
