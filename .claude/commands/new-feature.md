---
description: Add a new feature (Controller → Service → Repository) to an existing domain
argument-hint: <domain-name> <feature description>
---

# Add a new feature

Add a feature to the domain given in `$ARGUMENTS`.

## 0. Preparation

1. **First read the existing controllers·services** of the target domain package to learn the style.
   `src/main/java/com/daruda/darudaserver/domain/{domain}/`
2. Load the `architecture`, `code-style`, `testing` skills. Detailed rules are in each skill's `references/`, so read **only what you need**.
   (If you delegate ⑦ tests to the `test-writer` agent, that agent loads `testing` itself, so you may skip it.)

## 1. Writing order — from the inside out

The detailed rules for each step are in the skills. Here we cover only the order and checkpoints.

| Order | Deliverable | Easy to miss | Detail |
|-------|-------------|--------------|--------|
| ① Entity (only if needed) | `entity/{Xxx}.java` | **no `Entity` suffix**, `LAZY` associations, static factory `of()` | `code-style` → `references/idioms.md` |
| ② Repository | `repository/{Xxx}Repository.java` | when N+1 is expected, build fetch join·batch fetch together | `architecture` → `references/integration.md` |
| ③ DTO | `dto/request/`, `dto/response/` | `record` + Bean Validation, **no `Req`/`Res` abbreviations** | `architecture` → `references/web-layer.md` |
| ④ Service | `service/{Xxx}Service.java` | class `@Transactional(readOnly = true)` + re-declare on write methods only, **spring import** | `architecture` → `references/transaction.md` |
| ⑤ Controller | `controller/{Xxx}Controller.java` | `@Operation`/`@Parameter`, creation matches 201 status, register in `SecurityConfig.WHITE_LIST` if unauthenticated | `architecture` → `references/web-layer.md` |
| ⑥ ErrorCode (when needed) | `global/error/code/ErrorCode.java` | **must check code-value duplicates** | `error-handling` |
| ⑦ Test | `src/test/.../{Xxx}ServiceTest.java` | happy + exception paths are required | `testing` |

ErrorCode duplicate check:

```bash
grep -o '"E[0-9]\{6\}"' src/main/java/com/daruda/darudaserver/global/error/code/ErrorCode.java | sort | uniq -d
```

You may delegate test writing to the `test-writer` agent.

## 2. Verification

```bash
./.claude/scripts/check-all.sh
```

## 3. Report — HITL point

Summarize the list of created/modified files and the added API endpoints in Korean.

**Commit only when the user asks.** Do not auto-commit just because the feature is complete.
