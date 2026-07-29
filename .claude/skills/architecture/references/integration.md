# Integration — pagination · domain events · cross-domain references (daruda)

## Cursor-based pagination

Infinite-scroll lists use `ScrollPaginationCollection`. It fetches `size + 1` rows to decide whether a next page exists.

```java
PageRequest pageRequest = PageRequest.of(0, size + 1);
List<Comment> rows = commentRepository.findCommentsByBoardId(boardId, cursor, pageRequest);

ScrollPaginationCollection<Comment> scroll = ScrollPaginationCollection.of(rows, size);
long nextCursor = scroll.isLastScroll() ? -1L : scroll.getNextCursor().getId();
```

The response carries the item list together with a `ScrollPaginationDto` (count + next cursor). List-fetch APIs **must have pagination** — fetching with `findAll` then filtering in memory is forbidden.

## Domain events

Side effects that should be decoupled between domains (search-index updates, notifications) are split out via `ApplicationEventPublisher`.

```java
// publish (service)
eventPublisher.publishEvent(new CommentCreatedEvent(comment.getId(), boardId));

// subscribe — a side effect that must run after commit
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handle(final CommentCreatedEvent event) { ... }
```

- Put event records in `domain/{domain}/event/`.
- Use `@TransactionalEventListener` when it must run after transaction commit, `@EventListener` when immediate.
- If the listener needs its own write transaction, add `@Transactional(propagation = REQUIRES_NEW)` as well (see the self-invocation caveat in `references/transaction.md`).

## Cross-domain references

When you need another domain, **inject that domain's `Service`**. Do not inject a repository across a domain boundary.

```java
// Good — CommentService uses the notification domain's service
private final NotificationService notificationService;

// Avoid — handling another domain's repository directly
private final NotificationRepository notificationRepository;
```

If a circular dependency arises (service A ↔ service B), **break one side with an event.**

## Preventing N+1

- Associations are always `fetch = FetchType.LAZY` (the default EAGER is a main cause of N+1).
- Do not call queries inside a loop. Solve it with a batch-fetch query or a `fetch join`.
- Split complex reads into `@Query` or a QueryDSL custom interface (`{Xxx}RepositoryCustom` + `Impl`). Do not handle `JPAQueryFactory` directly in the service.
