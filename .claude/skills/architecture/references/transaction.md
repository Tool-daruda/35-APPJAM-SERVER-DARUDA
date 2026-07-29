# Transaction boundaries (daruda)

> **This file is the single source of truth (SSOT) for transaction rules.** `CLAUDE.md`, `code-reviewer`, and `test-validator` only reference this file; they do not copy its contents.

## Principle

**Always use `org.springframework.transaction.annotation.Transactional`.**

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)          // class default = read-only
public class BoardService {

	private final BoardRepository boardRepository;

	public BoardResponse getBoard(final Long boardId) {   // read — no annotation needed
		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));
		return BoardResponse.from(board);
	}

	@Transactional                        // re-declare on write methods only
	public CreateBoardResponse createBoard(final Long userId, final CreateBoardRequest request) {
		...
	}
}
```

- Transactions **start in the service**. Do not attach them to controllers or repositories.
- Keep external API calls (Feign, OCI, Elasticsearch) outside the transaction whenever possible. Long I/O inside a transaction holds the connection.
- When you need a separate transaction, split it into **a different bean** and call `@Transactional(propagation = Propagation.REQUIRES_NEW)`. Self-invocation does not go through the proxy, so it is not applied. Examples: `BoardScrapInternalService`, `ToolLikeInternalService`.

## jakarta vs spring — the exact facts

`jakarta.transaction.Transactional` and Spring's `@Transactional` **have different attribute names.**

| | Spring | Jakarta |
|---|--------|---------|
| Read-only | `readOnly = true` | **no such attribute** (specifying it is a compile error) |
| Propagation | `propagation = Propagation.*` | `value = TxType.*` |
| Rollback | `rollbackFor` | `rollbackOn` |
| Defaults | REQUIRED + rollback on unchecked exceptions (`RuntimeException`/`Error`) | REQUIRED + rollback on unchecked exceptions (same) |

**Important — premise to avoid misdiagnosis:** Spring recognizes the Jakarta annotation too, via `JtaTransactionAnnotationParser`. So even with a jakarta import, **the transaction itself opens normally.**

> It is **not** "code that doesn't work" but "code you can't apply the read-only optimization to." Do not treat it as an incident and rush to fix it.

## Order of handling when found in existing code

1. For a bare `@Transactional` with no attributes, **swapping the import alone keeps behavior identical** (both default to REQUIRED + rollback on unchecked exceptions).
2. If it uses `TxType`·`rollbackOn`, it needs **manual mapping** to Spring's `propagation`·`rollbackFor`. Do not convert mechanically.
3. If it is at the class level, swapping the import still leaves every method as a write transaction. You only gain something once you also reposition `readOnly = true`.

When you find a jakarta import in new or changed code, **flag it.** When you find it in existing code, classify it not as Critical but as **cleanup work** (see the `legacy-cleanup` skill for the current state).

## Common misconceptions when diagnosing

| Symptom | Actual cause |
|---------|--------------|
| Compile error on `@Transactional(readOnly = ...)` | jakarta import. This is a `compileJava` failure, not a test failure |
| Rollback differs from expectations | Check whether self-invocation is bypassing the proxy. **The jakarta import itself is not the cause** |
| Connection held long for a read | Class-level write transaction + external I/O inside the transaction |
