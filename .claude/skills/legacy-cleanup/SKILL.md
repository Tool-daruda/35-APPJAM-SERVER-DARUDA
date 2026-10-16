---
name: legacy-cleanup
description: A list of known cleanup targets in existing code written before the guidelines were unified. Load when doing refactoring work, or when you find a rule violation in existing code and need to judge whether it is "newly introduced or was already there".
---

# Known cleanup targets (daruda)

This is code written before the guidelines were unified. **New code follows the current rules, but the items below are handled as separate refactoring work.** Even if you find one of these while modifying a related file, **do not fix it along the way.** If cleanup seems needed, tell the user, get approval, then proceed as separate work (see "Refactoring principles" below).

> **Judgment criterion:** if you find one of these items during code review or diagnosis, report it by **distinguishing whether this change newly introduced it or it was already there.** Classify existing items not as Critical but as cleanup targets.

| Item | Current state | Goal |
|------|---------------|------|
| Soft-delete columns | `is_deleted` (comment/user) / `del_yn` (board/tool_scrap/tool_like) mixed | Unify column name·strategy (deferred due to no DB migration tooling) |

## Scope rules for cleanup work

- Handle one item at a time; don't mix several items into one PR.
- Cleanup must be behavior-preserving. (The `refactor` commit type is owned by `git-convention`, and the full check by `/run-checks` — those are procedure, not rules restated here.)
- Wide renames have references spread across the codebase, so rename via the IDE, then run the full check before opening the PR.
