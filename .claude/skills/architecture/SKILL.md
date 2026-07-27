---
name: architecture
description: 레이어 구조를 설계하거나 새 기능의 코드 배치를 정할 때 로드. 계층별 책임, 패키지 배치, 트랜잭션 경계, DTO 매핑, 응답 포맷, 요청 검증, Swagger, 도메인 이벤트 패턴.
---

# 아키텍처 (daruda)

**단일 모듈 + 도메인별 계층형(layered) 구조.** 헥사고날/포트&어댑터가 아니다. 새 코드를 어디에 둘지 이 문서로 판단한다.

## 레이어별 책임

```
Controller  → HTTP 경계. 요청 검증(@Valid), DTO ↔ 서비스 호출, 응답 래핑, Swagger 문서
Service     → 비즈니스 로직, 트랜잭션 경계, 도메인 간 조율, 엔티티 → 응답 DTO 변환
Repository  → 영속성. Spring Data JPA 메서드 + @Query + QueryDSL 커스텀 구현
Entity      → 상태 + 상태를 바꾸는 도메인 메서드 (setter 금지)
```

**호출 방향은 단방향이다.** `Controller → Service → Repository → Entity`.

| 금지 | 이유 |
|------|------|
| 컨트롤러가 리포지토리 직접 호출 | 트랜잭션 경계 밖에서 영속성 접근 |
| 서비스가 `ResponseEntity`·`HttpServletRequest` 사용 | 웹 의존이 비즈니스 레이어로 새어 나감 |
| 엔티티를 응답으로 직접 반환 | 순환 참조·지연 로딩 예외·필드 노출 |
| 리포지토리에 비즈니스 분기 | 조건 판단은 서비스에서 |

## 패키지 배치

```
domain/{domain}/
├── controller/{Xxx}Controller.java
├── service/{Xxx}Service.java
├── repository/{Xxx}Repository.java          # + {Xxx}RepositoryCustom/Impl (QueryDSL)
│   └── projection/                          # 조회 전용 projection 인터페이스/record
├── entity/{Xxx}.java                        # 접미사 Entity 붙이지 않음
│   └── enums/                               # 도메인 enum
├── dto/request/{동사}{대상}Request.java
├── dto/response/{동사}{대상}Response.java
└── event/{Xxx}Event.java                    # ApplicationEvent
```

도메인에 속하지 않는 횡단 관심사(인증, 이미지, 설정, 예외)는 `global/` 아래로 간다.

## 트랜잭션 경계

**반드시 `org.springframework.transaction.annotation.Transactional`을 쓴다.**
`jakarta.transaction.Transactional`에는 `readOnly` 속성이 없어(지정하면 컴파일 에러) 조회 최적화를 걸 수 없고, 전파 속성도 `propagation = Propagation.*`이 아니라 `value = TxType.*`이다. 그래서 기본값 `TxType.REQUIRED`로 조회에도 쓰기 트랜잭션이 열린다.

> **기존 코드에서 발견했을 때**: Spring은 `JtaTransactionAnnotationParser`로 Jakarta 애노테이션도 인식하므로 **트랜잭션 자체는 정상적으로 열린다.** "동작하지 않는 코드"가 아니라 "읽기 전용 최적화를 걸 수 없는 코드"다. 장애로 취급해 성급히 고치지 말고, 아래 순서로 처리한다.
>
> 1. 속성 없는 bare `@Transactional`이면 import 교체만으로 동작이 동일하다(양쪽 다 기본값이 REQUIRED + unchecked 예외 롤백).
> 2. `TxType`·`rollbackOn`을 쓰고 있으면 Spring의 `propagation`·`rollbackFor`로 **수동 매핑**이 필요하다. 기계적으로 바꾸지 않는다.
> 3. 클래스 레벨에 붙어 있으면 import만 바꿔도 여전히 모든 메서드가 쓰기 트랜잭션이다. `readOnly = true` 재배치까지 해야 실익이 생긴다.

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)          // 클래스 기본값 = 읽기 전용
public class BoardService {

	private final BoardRepository boardRepository;

	public BoardResponse getBoard(final Long boardId) {   // 조회 — 어노테이션 불필요
		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));
		return BoardResponse.from(board);
	}

	@Transactional                        // 쓰기 메서드에만 재선언
	public CreateBoardResponse createBoard(final Long userId, final CreateBoardRequest request) {
		...
	}
}
```

- 트랜잭션은 **서비스에서 시작**한다. 컨트롤러·리포지토리에 붙이지 않는다.
- 외부 API 호출(Feign, OCI, Elasticsearch)은 가능한 한 트랜잭션 밖에서. 트랜잭션 안에서 오래 걸리는 I/O는 커넥션을 잡아둔다.
- 별도 트랜잭션이 필요하면 `@Transactional(propagation = Propagation.REQUIRES_NEW)`를 **다른 빈**으로 분리해 호출한다(자기 호출은 프록시를 타지 않아 적용되지 않는다). 예: `BoardScrapInternalService`, `ToolLikeInternalService`.

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

컨트롤러에서 `@RequestBody @Valid`로 트리거한다. 위반 시 `MethodArgumentNotValidException` → `GlobalExceptionHandler`가 `ErrorCode.INVALID_FIELD_ERROR` + 필드별 메시지로 응답한다. 검증 로직을 서비스에서 `if`로 중복 구현하지 않는다.

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
- 인증 사용자: `@AuthenticationPrincipal Long userId`. 비로그인도 허용하는 API는 `Long userIdOrNull`.
- 미인증 API는 `SecurityConfig`의 `WHITE_LIST`에도 등록해야 실제로 열린다. `@DisableSwaggerSecurity`는 **문서 표시용일 뿐 인증을 풀지 않는다.**
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

> `ApiResponse`는 레거시다. 신규 코드에서 쓰지 않는다(`CLAUDE.md`의 "알려진 정리 대상" 참조).

## 커서 기반 페이지네이션

무한 스크롤 목록은 `ScrollPaginationCollection`을 쓴다. `size + 1`건을 조회해 다음 페이지 존재 여부를 판단하는 방식이다.

```java
PageRequest pageRequest = PageRequest.of(0, size + 1);
List<Comment> rows = commentRepository.findCommentsByBoardId(boardId, cursor, pageRequest);

ScrollPaginationCollection<Comment> scroll = ScrollPaginationCollection.of(rows, size);
long nextCursor = scroll.isLastScroll() ? -1L : scroll.getNextCursor().getId();
```

응답에는 항목 리스트와 `ScrollPaginationDto`(건수 + 다음 커서)를 함께 담는다.

## 도메인 이벤트

도메인 간 결합을 낮춰야 할 부수 효과(검색 색인 갱신, 알림)는 `ApplicationEventPublisher`로 분리한다.

```java
// 발행 (서비스)
eventPublisher.publishEvent(new CommentCreatedEvent(comment.getId(), boardId));

// 구독 — 커밋 이후 실행해야 하는 부수 효과
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handle(final CommentCreatedEvent event) { ... }
```

- 이벤트 record는 `domain/{domain}/event/`에 둔다.
- 트랜잭션 커밋 후 실행이 필요하면 `@TransactionalEventListener`, 즉시면 `@EventListener`.
- 리스너에서 다시 쓰기 트랜잭션이 필요하면 `@Transactional(propagation = REQUIRES_NEW)`를 함께 붙인다.

## 도메인 간 참조

다른 도메인이 필요하면 **그 도메인의 `Service`를 주입**한다. 리포지토리를 도메인 경계 너머로 주입하지 않는다.

```java
// 좋음 — CommentService가 알림 도메인의 서비스를 사용
private final NotificationService notificationService;

// 지양 — 남의 도메인 리포지토리를 직접 다룸
private final NotificationRepository notificationRepository;
```

순환 의존(A 서비스 ↔ B 서비스)이 생기면 이벤트로 한쪽을 끊는다.
