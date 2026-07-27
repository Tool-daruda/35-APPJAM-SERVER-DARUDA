# 작성 관용구 — 엔티티 · 서비스 · 로깅 (daruda)

## 엔티티

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

	@ManyToOne(fetch = FetchType.LAZY)               // 연관관계는 항상 LAZY
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Builder                                          // 생성자에만
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

	public void updateContent(final String content) {   // setter 대신 의도가 드러나는 메서드
		this.content = content;
	}
}
```

규칙:

- `@ManyToOne`/`@OneToOne`은 **항상 `fetch = FetchType.LAZY`** (기본값이 EAGER라 N+1의 주원인).
- 생성은 정적 팩토리(`of`/`create`)로 노출하고, 빌더는 `private` 생성자에 둔다.
- 검증이 필요한 생성은 `create`(예: 필수 연관 엔티티 null 검사 후 예외), 단순 생성은 `of`를 쓴다.
- soft delete는 `@SQLDelete` + `@SQLRestriction`. 플래그 컬럼은 `is_deleted`, 시각은 `deleted_at`.
- 시간 필드는 `LocalDateTime`을 쓴다(`java.sql.Timestamp` 신규 사용 금지).
- 공통 생성/수정 시각은 `BaseTimeEntity`를 상속해서 얻는다.

## 서비스

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

- 필드는 `private final` + 생성자 주입(`@RequiredArgsConstructor`). **`@Autowired` 필드 주입 금지.**
- 메서드 파라미터에 `final`을 붙이는 것이 이 코드베이스의 관용이다(강제는 아니지만 신규 코드에서 지킨다).
- `Optional`은 `orElseThrow`로 즉시 풀어 예외로 전환한다. **`get()` 호출 금지.**
- 지역 변수 타입은 명시한다(`var` 사용은 이 코드베이스에 없다).
- 트랜잭션 배치 규칙은 `architecture` 스킬의 `references/transaction.md`가 SSOT다.

## 로깅

```java
log.debug("게시글 조회: boardId={}", boardId);              // 파라미터는 {} 플레이스홀더
log.error("이미지 삭제 실패: {}", image.getImageUrl(), e);   // 예외는 마지막 인자
```

- 문자열 연결(`"a" + b`) 대신 `{}`를 쓴다.
- 예상된 비즈니스 예외는 `debug`, 복구 가능한 이상은 `warn`, 시스템 오류는 `error`.
- **토큰·비밀번호·개인정보는 로그에 남기지 않는다.**

## 주석

- **무엇을**이 아니라 **왜**를 적는다. 코드를 그대로 읊는 주석(`// 사용자 조회`)은 지운다.
- 한국어로 쓴다.
- 임시 회피는 `// TODO: #이슈번호 설명` 형식으로 남긴다.
