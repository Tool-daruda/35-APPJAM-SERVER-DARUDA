# GlobalExceptionHandler · 응답 포맷 · 인증 예외 (daruda)

## 이미 처리되는 예외

`global/handler/GlobalExceptionHandler.java`가 아래를 처리한다. **새 핸들러를 추가하기 전에 중복인지 확인한다.**

| 예외 | 응답 |
|------|------|
| `BusinessException` | 해당 `ErrorCode` 그대로 |
| `MethodArgumentNotValidException` | 400 + 필드별 메시지 |
| `ConstraintViolationException` | 400 + 경로별 메시지 |
| `MissingServletRequestParameterException` | 400 파라미터 누락 |
| `MissingRequestHeaderException` | 400 헤더 누락 |
| `MethodArgumentTypeMismatchException` | 400 타입 불일치 |
| `DataIntegrityViolationException` | 400 무결성 위반 |
| `IllegalArgumentException` / `IllegalStateException` | 400 |
| `MultipartException` / `MaxUploadSizeExceededException` | 500 업로드 실패 |
| `IOException` / `Exception` | 500 |

## 핸들러 추가

기존 스타일을 따른다.

```java
@ExceptionHandler(XxxException.class)
public ResponseEntity<ErrorResponse> handleXxxException(XxxException ex) {
	log.warn("XxxException 발생: {}", ex.getMessage(), ex);
	return ResponseEntity.status(ErrorCode.YYY.getHttpStatus())
		.body(ErrorResponse.of(ErrorCode.YYY));
}
```

로그 레벨: 클라이언트 잘못(4xx)은 `debug`/`warn`, 서버 잘못(5xx)은 `error`(스택 트레이스 포함).

## 응답 포맷

**에러**(`ErrorResponse`):

```json
{
  "status": 404,
  "code": "E404006",
  "message": "게시글이 존재하지 않습니다"
}
```

**검증 실패**(필드 정보 포함):

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

**성공**(`SuccessResponse`):

```json
{
  "status": 200,
  "message": "요청 데이터가 성공적으로 조회되었습니다",
  "data": { }
}
```

`data`가 null이면 응답에서 생략된다(`@JsonInclude(NON_NULL)`).

## 인증/인가 예외

Security 필터 체인에서 발생하는 예외는 **`GlobalExceptionHandler`가 잡지 못한다**(컨트롤러 진입 전에 발생한다).

| 상황 | 처리 위치 |
|------|-----------|
| 인증 실패 | `JwtAuthenticationEntryPoint` |
| 인가 실패 | `CustomAccessDeniedHandler` |
| 필터 내부 예외 | `ExceptionHandlerFilter` |

JWT 관련 예외를 다룰 때는 `global/auth/security/` 아래를 수정한다.
