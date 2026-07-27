# 연동 — 페이지네이션 · 도메인 이벤트 · 도메인 간 참조 (daruda)

## 커서 기반 페이지네이션

무한 스크롤 목록은 `ScrollPaginationCollection`을 쓴다. `size + 1`건을 조회해 다음 페이지 존재 여부를 판단하는 방식이다.

```java
PageRequest pageRequest = PageRequest.of(0, size + 1);
List<Comment> rows = commentRepository.findCommentsByBoardId(boardId, cursor, pageRequest);

ScrollPaginationCollection<Comment> scroll = ScrollPaginationCollection.of(rows, size);
long nextCursor = scroll.isLastScroll() ? -1L : scroll.getNextCursor().getId();
```

응답에는 항목 리스트와 `ScrollPaginationDto`(건수 + 다음 커서)를 함께 담는다. 목록 조회 API에는 **반드시 페이지네이션이 있어야 한다** — `findAll` 후 메모리 필터링은 금지다.

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
- 리스너에서 다시 쓰기 트랜잭션이 필요하면 `@Transactional(propagation = REQUIRES_NEW)`를 함께 붙인다 (`references/transaction.md`의 self-invocation 주의사항 참조).

## 도메인 간 참조

다른 도메인이 필요하면 **그 도메인의 `Service`를 주입**한다. 리포지토리를 도메인 경계 너머로 주입하지 않는다.

```java
// 좋음 — CommentService가 알림 도메인의 서비스를 사용
private final NotificationService notificationService;

// 지양 — 남의 도메인 리포지토리를 직접 다룸
private final NotificationRepository notificationRepository;
```

순환 의존(A 서비스 ↔ B 서비스)이 생기면 **이벤트로 한쪽을 끊는다.**

## N+1 방지

- 연관관계는 항상 `fetch = FetchType.LAZY` (기본값 EAGER가 N+1의 주원인).
- 반복문 안에서 쿼리를 호출하지 않는다. 배치 조회 쿼리 또는 `fetch join`으로 해결한다.
- 복잡한 조회는 `@Query` 또는 QueryDSL 커스텀 인터페이스(`{Xxx}RepositoryCustom` + `Impl`)로 분리한다. 서비스에서 `JPAQueryFactory`를 직접 다루지 않는다.
