# Writing idioms — entity · service · logging (daruda)

## Entity

```java
@Getter
@Entity
@Table(name = "board")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE board SET is_deleted = true, deleted_at = NOW() WHERE board_id = ?")
@SQLRestriction("is_deleted = false")
public class Board extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "board_id")
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, length = 10_000)
	private String content;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted = false;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)               // associations are always LAZY
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Builder                                          // on the constructor only
	private Board(final String title, final String content, final User user) {
		this.title = title;
		this.content = content;
		this.user = user;
	}

	public static Board of(final String title, final String content, final User user) {
		return Board.builder()
			.title(title)
			.content(content)
			.user(user)
			.build();
	}

	public void updateContent(final String content) {   // an intent-revealing method instead of a setter
		this.content = content;
	}
}
```

Rules:

- `@ManyToOne`/`@OneToOne` are **always `fetch = FetchType.LAZY`** (the default EAGER is a main cause of N+1).
- Expose creation via a static factory (`of`/`create`) and keep the builder on the `private` constructor.
- Use `create` for creation that needs validation (e.g. throwing after a null check on a required associated entity), `of` for plain creation.
- Soft delete uses `@SQLDelete` + `@SQLRestriction`. The flag column is `is_deleted`, the timestamp is `deleted_at`.
- Use `LocalDateTime` for time fields (no new use of `java.sql.Timestamp`).
- Get common created/updated timestamps by extending `BaseTimeEntity`.

## Service

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

	private final BoardRepository boardRepository;
	private final UserRepository userRepository;

	@Transactional
	public CreateBoardResponse createBoard(final Long userId, final CreateBoardRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		Board board = boardRepository.save(Board.of(request.title(), request.content(), user));
		return CreateBoardResponse.from(board);
	}
}
```

- Fields are `private final` + constructor injection (`@RequiredArgsConstructor`). **No `@Autowired` field injection.**
- Marking method parameters `final` is this codebase's idiom (not enforced, but follow it in new code).
- Unwrap `Optional` immediately with `orElseThrow`, converting to an exception. **Do not call `get()`.**
- Declare local variable types explicitly (this codebase does not use `var`).
- The transaction-placement rules are owned by the `architecture` skill's `references/transaction.md` (SSOT).

## Logging

```java
log.debug("게시글 조회: boardId={}", boardId);              // parameters use the {} placeholder
log.error("이미지 삭제 실패: {}", image.getImageUrl(), e);   // the exception is the last argument
```

- Use `{}` instead of string concatenation (`"a" + b`).
- Expected business exceptions → `debug`, recoverable anomalies → `warn`, system errors → `error`.
- **Do not log tokens, passwords, or personal data.**

## Comments

- Write **why**, not **what**. Delete comments that just restate the code (`// 사용자 조회`).
- Write them in Korean.
- Leave temporary workarounds in the form `// TODO: #issue-number description`.
