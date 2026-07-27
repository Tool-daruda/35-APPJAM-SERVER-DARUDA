---
name: error-handling
description: 예외/에러를 처리할 때 로드. BusinessException 계층과 ErrorCode 추가 규칙을 담고, GlobalExceptionHandler 처리 목록·응답 포맷·인증 예외는 references/에 분리돼 있다.
---

# 예외 처리 (daruda)

모든 비즈니스 예외는 `BusinessException` 하위 클래스 + `ErrorCode` 조합으로 던지고, `GlobalExceptionHandler`가 `ErrorResponse`로 변환한다. **컨트롤러에서 에러 응답을 직접 조립하지 않는다.**

## 상세 규칙 라우팅

| 무엇을 하려는가 | 읽을 파일 |
|-----------------|-----------|
| 핸들러 추가, 이미 처리되는 예외 확인, 응답 JSON 포맷, 인증/인가 예외 | `references/handler.md` |

## 예외 계층

```text
RuntimeException
└── BusinessException (ErrorCode 보유)
    ├── BadRequestException      → 400 잘못된 요청 값
    ├── InvalidValueException    → 400 값 자체가 유효하지 않음
    ├── UnauthorizedException    → 401 인증 실패/토큰 문제
    ├── ForbiddenException       → 403 권한 없음
    └── NotFoundException        → 404 리소스 없음
```

`BusinessException`은 `ErrorCode`를 담고, HTTP 상태는 `ErrorCode`가 결정한다.

## 던지는 방법

```java
// 리소스 없음
Board board = boardRepository.findById(boardId)
	.orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));

// 권한 없음
if (!comment.getUser().getId().equals(userId)) {
	throw new ForbiddenException(ErrorCode.NO_PERMISSION_TO_DELETE);
}

// 잘못된 요청
if (tool == null) {
	throw new BadRequestException(ErrorCode.BAD_REQUEST_DATA);
}
```

**금지**:

- `throw new RuntimeException(...)`, `throw new IllegalArgumentException(...)` — 어떤 에러 코드로 응답할지 알 수 없다.
- 예외 메시지를 문자열로 직접 만들기 — 메시지는 `ErrorCode`에 둔다.
- `catch (Exception e) { }` 빈 블록 — 삼키려면 이유를 주석으로 남기고 최소한 `log.warn`을 남긴다.

## ErrorCode 추가 규칙

`global/error/code/ErrorCode.java`의 enum에 추가한다.

```java
BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "E404006", "게시글이 존재하지 않습니다"),
```

| 규칙 | 내용 |
|------|------|
| 코드 형식 | `E{HTTP상태}{일련번호 3자리}` (예: `E404006`) |
| 코드값 유일성 | **전역에서 중복 금지.** 추가 전 아래 명령으로 확인 |
| 배치 | HTTP 상태별 주석 구획(`/* 404 NOT FOUND */`) 안에, 일련번호 순서대로 |
| 메시지 | 한국어, 사용자에게 그대로 노출되므로 내부 구현을 드러내지 않는다 |
| 상수명 | `{대상}_{사유}` (예: `BOARD_NOT_FOUND`, `ALREADY_REPORTED`) |

```bash
# 코드값 중복 확인 (추가 전 필수)
grep -o '"E[0-9]\{6\}"' src/main/java/com/daruda/darudaserver/global/error/code/ErrorCode.java | sort | uniq -d
```

> 현재 일부 코드값이 중복돼 있고 오타 상수도 남아 있다. 신규 코드가 이를 참조하지 않도록 주의한다 (현황은 `legacy-cleanup` 스킬 참조).

## SuccessCode

성공 응답의 HTTP 상태 + 메시지를 정의한다(`global/error/code/SuccessCode.java`).

```java
SUCCESS_CREATE(HttpStatus.CREATED, "생성이 완료되었습니다"),
SUCCESS_FETCH(HttpStatus.OK, "요청 데이터가 성공적으로 조회되었습니다"),
```

**`SuccessCode`의 상태와 실제 HTTP 응답 상태를 반드시 일치시킨다.**

```java
// 잘못됨 — 바디는 201인데 실제 응답은 200
return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_CREATE, data));

// 올바름
return ResponseEntity.status(HttpStatus.CREATED)
	.body(SuccessResponse.of(SuccessCode.SUCCESS_CREATE, data));
```

## 새 예외 타입이 필요할 때

기존 5개 하위 예외로 표현되지 않는 경우에만 추가한다. **대부분은 새 `ErrorCode`만 추가하면 충분하다.**

```java
public class ConflictException extends BusinessException {
	public ConflictException(final ErrorCode errorCode) {
		super(errorCode);
	}
}
```

추가했다면 `GlobalExceptionHandler`는 수정할 필요가 없다(`BusinessException` 핸들러가 상속받아 처리한다).
