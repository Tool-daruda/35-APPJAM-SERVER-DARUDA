# 트랜잭션 경계 (daruda)

> **이 파일이 트랜잭션 규칙의 단일 출처(SSOT)다.** `CLAUDE.md`, `code-reviewer`, `test-validator`는 이 파일을 참조만 하고 내용을 복사하지 않는다.

## 원칙

**반드시 `org.springframework.transaction.annotation.Transactional`을 쓴다.**

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
- 외부 API 호출(Feign, OCI, Elasticsearch)은 가능한 한 트랜잭션 밖에서. 트랜잭션 안의 긴 I/O는 커넥션을 잡아둔다.
- 별도 트랜잭션이 필요하면 `@Transactional(propagation = Propagation.REQUIRES_NEW)`를 **다른 빈**으로 분리해 호출한다. 자기 호출(self-invocation)은 프록시를 타지 않아 적용되지 않는다. 예: `BoardScrapInternalService`, `ToolLikeInternalService`.

## jakarta vs spring — 정확한 사실관계

`jakarta.transaction.Transactional`과 Spring의 `@Transactional`은 **속성 이름이 다르다.**

| | Spring | Jakarta |
|---|--------|---------|
| 읽기 전용 | `readOnly = true` | **속성 자체가 없다** (지정하면 컴파일 에러) |
| 전파 | `propagation = Propagation.*` | `value = TxType.*` |
| 롤백 | `rollbackFor` | `rollbackOn` |
| 기본값 | REQUIRED + unchecked 예외 롤백 | REQUIRED + unchecked 예외 롤백 (동일) |

**중요 — 오진하지 않기 위한 전제:** Spring은 `JtaTransactionAnnotationParser`로 Jakarta 애노테이션도 인식한다. 따라서 jakarta import가 있어도 **트랜잭션 자체는 정상적으로 열린다.**

> "동작하지 않는 코드"가 **아니라** "읽기 전용 최적화를 걸 수 없는 코드"다. 장애로 취급해 성급히 고치지 않는다.

## 기존 코드에서 발견했을 때의 처리 순서

1. 속성 없는 bare `@Transactional`이면 **import 교체만으로 동작이 동일하다** (양쪽 다 기본값이 REQUIRED + unchecked 예외 롤백).
2. `TxType`·`rollbackOn`을 쓰고 있으면 Spring의 `propagation`·`rollbackFor`로 **수동 매핑**이 필요하다. 기계적으로 바꾸지 않는다.
3. 클래스 레벨에 붙어 있으면 import만 바꿔도 여전히 모든 메서드가 쓰기 트랜잭션이다. `readOnly = true` 재배치까지 해야 실익이 생긴다.

신규·변경 코드에서 jakarta import를 발견하면 **지적한다.** 기존 코드에서 발견하면 Critical이 아니라 **정리 대상**으로 분류한다 (현황은 `legacy-cleanup` 스킬 참조).

## 진단 시 흔한 오해

| 증상 | 실제 원인 |
|------|-----------|
| `@Transactional(readOnly = ...)`에서 컴파일 에러 | jakarta import. 테스트 실패가 아니라 `compileJava` 실패다 |
| 트랜잭션 롤백이 기대와 다름 | self-invocation으로 프록시를 타지 않는지 확인. **jakarta import 자체는 원인이 아니다** |
| 조회인데 커넥션이 오래 잡힘 | 클래스 레벨 쓰기 트랜잭션 + 트랜잭션 안 외부 I/O |
