---
name: legacy-cleanup
description: A list of known cleanup targets in existing code written before the guidelines were unified. Load when doing refactoring work, or when you find a rule violation in existing code and need to judge whether it is "newly introduced or was already there".
---

# Known cleanup targets (daruda)

This is code written before the guidelines were unified. **New code follows the current rules, but the items below are handled as separate refactoring work.** Even if you find one of these while modifying a related file, **do not fix it along the way.** If cleanup seems needed, tell the user, get approval, then proceed as separate work (see "Refactoring principles" below).

> **Judgment criterion:** if you find one of these items during code review or diagnosis, report it by **distinguishing whether this change newly introduced it or it was already there.** Classify existing items not as Critical but as cleanup targets.

| Item | Current state | Goal |
|------|---------------|------|
| Dual response wrappers | `ApiResponse` (7 controllers) / `SuccessResponse` (4) coexist | Unify to `SuccessResponse`, then delete `ApiResponse` |
| Abbreviated DTO suffixes | `ToolDetailGetRes`, `CategoryRes`, `BoardRes`, etc. (`dto/res`, `dto/req` packages) | `XxxResponse`/`XxxRequest` + `dto/response`, `dto/request` |
| Entity suffix | `CommentEntity`, `UserEntity`, `NotificationEntity`, `ReportEntity` | `Comment`, `User`, `Notification`, `Report` |
| Transaction import | **17 files** importing `jakarta.transaction.Transactional` — 12 repositories with `@Modifying` + 5 services (`UserService`, `CommentService`, `AuthService`, `NotificationService`, `TokenService`). All **31 annotation declarations** in these files are bare, attribute-less annotations, so swapping the import alone keeps behavior identical | `org.springframework.transaction.annotation.Transactional` |
| Transaction scope | Class-level `@Transactional` (write) in 23 places. Of these, `UserService`·`CommentService` also overlap with a jakarta import, so swapping the import alone still leaves reads as write transactions | Class `readOnly = true` + `@Transactional` on write methods only |
| ErrorCode duplicates | Code values `E400009`·`E400012`·`E400013` are duplicated; the typo constant `REFREH_TOKEN_EMPTY_ERROR` duplicates `REFRESH_TOKEN_EMPTY_ERROR` | Make code values unique + remove the typo constant |
| HTTP status mismatch | `ResponseEntity.ok()` + `SuccessCode.SUCCESS_CREATE(201)` → body says 201, actual response is 200 | Creation APIs use `status(HttpStatus.CREATED)` |
| Soft-delete columns | `is_deleted` (comment) / `del_yn` (board) / hard delete (ToolLike) mixed | Unify column name·strategy |
| Unused code | `S3Service` (actually uses `OciService`), `ApiResponse.ofFailure` | Delete |
| QueryDSL location | `BoardService` uses `JPAQueryFactory` directly | Split into a custom repository (`BoardRepositoryCustom`) |
| `BaseTimeEntity` type | `java.sql.Timestamp` | `LocalDateTime` |

## Caution when cleaning up transaction imports

Do not do a mechanical bulk replacement. The `architecture` skill's `references/transaction.md` is the SSOT for the order of handling and the exact facts. In summary:

1. **Bare `@Transactional`** → swapping the import alone keeps behavior identical (safe)
2. **Uses `TxType`·`rollbackOn`** → needs manual mapping to Spring attributes
3. **Class level** → swapping the import alone gains nothing. You must also reposition `readOnly = true`

## Refactoring principles

- Handle one item at a time. Don't mix several items into one PR.
- There must be no behavior change — the commit type is `refactor`.
- Renames (entity suffix, DTO abbreviation) have references spread widely, so do an IDE rename then run full verification with `./.claude/scripts/check-all.sh`.
