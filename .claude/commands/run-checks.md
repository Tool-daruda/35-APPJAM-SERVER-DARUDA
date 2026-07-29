---
description: Run full verification before a PR (editorconfig → Checkstyle → test)
---

# Full verification

**The script is the SSOT for the verification steps and their order.** Do not list individual Gradle commands here — so that if the order changes, only the script needs editing.

```bash
./.claude/scripts/check-all.sh
```

If it fails, **stop immediately** and find which step failed and what failed.

## Handling failures

| Step | Action |
|------|--------|
| editorconfig | The script first attempts an auto-fix with `editorconfigFormat`. If it still remains, check for CRLF·encoding issues |
| Checkstyle | **No auto-fix.** Find the violation location in `build/reports/checkstyle/main.html` (tests: `test.html`) and fix by hand. Common causes and fixes are in `code-style` → `references/formatting.md` |
| test | Diagnose the failure cause. If only diagnosis is needed, **delegate to the `test-validator` agent** (read-only). Report: `build/reports/tests/test/index.html` |

## Report

- If everything passes, report the pass concisely in Korean.
- If it fails, report **which step failed and what failed, together with the actual output**.
- **Do not claim something passed when it did not.**
