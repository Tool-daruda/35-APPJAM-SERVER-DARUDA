---
name: code-review-checklist
description: The review lens for daruda server code — the layer-boundary/transaction/convention/exception/performance/security/style checklist, severity rules, and the Korean output format. Read by the code-reviewer agent; also load it directly when reviewing a diff.
---

# Code review checklist (daruda)

The single source for **what to check** when reviewing daruda server code (Java 17 / Spring Boot 3.4.1 / single-module layered) and **how to report it**. The `code-reviewer` agent reads this file.

> **Language**: Write all review results in Korean.
> Reviewing does not modify code — report findings only.

## Where judgments come from

Do not copy the rules themselves here. When a judgment is unclear, read the source of record.

| Judgment target | Source file |
|-----------------|-------------|
| Transactions (jakarta vs spring, readOnly, propagation) | `.claude/skills/architecture/references/transaction.md` |
| DTO·response·controller·Swagger | `.claude/skills/architecture/references/web-layer.md` |
| N+1·events·cross-domain references | `.claude/skills/architecture/references/integration.md` |
| Naming·Lombok·formatting | `.claude/skills/code-style/` |
| Exceptions·ErrorCode | `.claude/skills/error-handling/` |
| **Judging whether it's a known problem in existing code** | `.claude/skills/legacy-cleanup/SKILL.md` |

## Review checklist

### 1. Layer boundaries (Critical)

- [ ] The controller doesn't inject/call a repository directly
- [ ] The service doesn't use web types like `ResponseEntity`·`HttpServletRequest`
- [ ] Entities aren't returned directly as responses (must convert to DTO)
- [ ] Another domain's **repository** isn't injected directly (must inject the service)

### 2. Transactions

- [ ] Imports `org.springframework.transaction.annotation.Transactional`
- [ ] Class has `@Transactional(readOnly = true)`, `@Transactional` only on write methods
- [ ] `REQUIRES_NEW` isn't used via self-invocation (it bypasses the proxy and is void)
- [ ] External API calls (Feign/OCI/Elasticsearch) aren't held long inside a transaction

> **Severity judgment**: a jakarta import is recognized by Spring, so the transaction itself opens. **It's a missing optimization, not a malfunction.** In new·changed code flag it as a Warning; in existing code classify it not as Critical but as a cleanup target. For the exact facts, see `transaction.md`.

### 3. Conventions

- [ ] Entities have no `Entity` suffix (`Board`, `Tool`)
- [ ] DTOs use full `Request`/`Response` names and live in `dto/request`, `dto/response`
- [ ] New code uses `SuccessResponse`, not `ApiResponse`
- [ ] Creation APIs respond with `HttpStatus.CREATED` (status matches `SuccessCode`)
- [ ] `@Setter`·`@Data` aren't used on entities
- [ ] `@Builder` isn't declared on both the class and the constructor
- [ ] Associations are `FetchType.LAZY`

### 4. Exception handling

- [ ] Doesn't throw `RuntimeException`/`IllegalArgumentException` directly, uses `BusinessException` subclass + `ErrorCode`
- [ ] A new `ErrorCode`'s code value doesn't duplicate an existing one
- [ ] Uses `orElseThrow` instead of `Optional.get()`
- [ ] Exceptions aren't swallowed by an empty `catch`

### 5. Performance

- [ ] Queries aren't called inside a loop (N+1) — solve with batch fetch/fetch join
- [ ] List fetches have pagination
- [ ] No unnecessary full fetch (`findAll`) followed by in-memory filtering

### 6. Security

- [ ] An API needing authentication isn't wrongly registered in `SecurityConfig.WHITE_LIST`
      (`@DisableSwaggerSecurity` is **only for documentation display; it does not lift auth** — conversely, also check whether a missing WHITE_LIST registration is blocking an unauthenticated API)
- [ ] Resource-owner verification exists (prevent deleting others' comments/posts)
- [ ] Tokens·passwords·personal data aren't left in logs
- [ ] Secrets aren't hardcoded

### 7. Style (Checkstyle)

- [ ] Tab indentation, within 120 chars, trailing newline
- [ ] Import order (`java.` → `javax.` → `org.` → `net.` → `com.`) and blank lines between groups
- [ ] No wildcard imports (except static imports in tests)

## Output format

Report sorted by severity.

```markdown
## 🔴 Critical (반드시 수정)

### 1. `BoardController.java:37` — 컨트롤러가 리포지토리를 직접 호출
`BoardRepository`를 주입해 `findById`를 직접 호출합니다. 레이어 경계 위반이라
트랜잭션 경계와 예외 변환이 컨트롤러로 새어 나옵니다.

**수정:** 조회 로직을 `BoardService`에 두고 컨트롤러는 서비스만 호출합니다.

## 🟡 Warning (수정 권장)

### 2. `BoardController.java:88` — HTTP 상태 불일치
`SuccessCode.SUCCESS_CREATE`(201)를 담으면서 `ResponseEntity.ok()`(200)로 응답합니다.
클라이언트가 바디의 status와 실제 HTTP 상태를 다르게 받습니다.

**수정:** `ResponseEntity.status(HttpStatus.CREATED).body(...)`

## 🟢 Suggestion (개선 제안)

...

## ✅ 좋은 점

...
```

## Principles

- For each item, write **file:line**, **why it's a problem**, and **how to fix it**.
- If it's a guess, say it's a guess. Don't assert what you haven't verified.
- **Distinguish whether it's a problem already spread across existing code or newly introduced by this change.** Base the judgment on the `legacy-cleanup` skill's list.
