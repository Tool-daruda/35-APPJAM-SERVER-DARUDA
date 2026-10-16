---
name: error-handling
description: Load when handling exceptions/errors. Holds the BusinessException hierarchy and ErrorCode-addition rules; the GlobalExceptionHandler handling list, response format, and auth exceptions are split into references/.
---

# Exception handling (daruda)

Throw every business exception as a `BusinessException` subclass + `ErrorCode` combination, and `GlobalExceptionHandler` converts it into an `ErrorResponse`. **Do not assemble error responses in the controller.**

## Detailed-rule routing

| What you are doing | File to read |
|--------------------|--------------|
| Adding a handler, checking an already-handled exception, response JSON format, auth/authorization exceptions | `references/handler.md` |

## Exception hierarchy

```text
RuntimeException
└── BusinessException (holds an ErrorCode)
    ├── BadRequestException      → 400 bad request value
    ├── InvalidValueException    → 400 the value itself is invalid
    ├── UnauthorizedException    → 401 authentication failure / token problem
    ├── ForbiddenException       → 403 no permission
    └── NotFoundException        → 404 resource not found
```

`BusinessException` holds an `ErrorCode`, and the `ErrorCode` determines the HTTP status.

## How to throw

```java
// resource not found
Board board = boardRepository.findById(boardId)
	.orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));

// no permission
if (!comment.getUser().getId().equals(userId)) {
	throw new ForbiddenException(ErrorCode.NO_PERMISSION_TO_DELETE);
}

// bad request
if (tool == null) {
	throw new BadRequestException(ErrorCode.BAD_REQUEST_DATA);
}
```

**Forbidden**:

- `throw new RuntimeException(...)`, `throw new IllegalArgumentException(...)` — even where `GlobalExceptionHandler` maps them (e.g. `IllegalArgumentException` → 400 `BAD_REQUEST_DATA`), you lose the specific domain `ErrorCode`. Use a `BusinessException` subclass + explicit `ErrorCode`.
- Building exception messages as strings directly — messages live on the `ErrorCode`.
- An empty `catch (Exception e) { }` block — if you must swallow it, leave the reason in a comment and at least a `log.warn`.

## ErrorCode-addition rules

Add to the enum in `global/error/code/ErrorCode.java`.

```java
BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "E404006", "게시글이 존재하지 않습니다"),
```

| Rule | Content |
|------|---------|
| Code format | `E{HTTP status}{3-digit serial}` (e.g. `E404006`) |
| Code-value uniqueness | **No global duplicates.** Verify with the command below before adding |
| Placement | Inside the comment section for that HTTP status (`/* 404 NOT FOUND */`), in serial order |
| Message | Korean; exposed to the user as-is, so do not reveal internal implementation |
| Constant name | `{target}_{reason}` (e.g. `BOARD_NOT_FOUND`, `ALREADY_REPORTED`) |

```bash
# Check for duplicate code values (required before adding)
grep -o '"E[0-9]\{6\}"' src/main/java/com/daruda/darudaserver/global/error/code/ErrorCode.java | sort | uniq -d
```

## SuccessCode

Defines the HTTP status + message of success responses (`global/error/code/SuccessCode.java`).

```java
SUCCESS_CREATE(HttpStatus.CREATED, "생성이 완료되었습니다"),
SUCCESS_FETCH(HttpStatus.OK, "요청 데이터가 성공적으로 조회되었습니다"),
```

**Always keep the `SuccessCode`'s status and the actual HTTP response status in sync.**

```java
// wrong — the body says 201 but the actual response is 200
return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_CREATE, data));

// correct
return ResponseEntity.status(HttpStatus.CREATED)
	.body(SuccessResponse.of(SuccessCode.SUCCESS_CREATE, data));
```

## When you need a new exception type

Add one only when the existing 5 subclasses cannot express it. **In most cases, adding a new `ErrorCode` is enough.**

```java
public class ConflictException extends BusinessException {
	public ConflictException(final ErrorCode errorCode) {
		super(errorCode);
	}
}
```

If you add one, you don't need to modify `GlobalExceptionHandler` (the `BusinessException` handler processes it via inheritance).
