# Formatting · imports (daruda)

## Formatting (auto-verified)

| Rule | Value |
|------|-------|
| Indentation | **tabs** (space indentation is a Checkstyle violation) |
| Tab width | 4 |
| Max line length | 120 chars (excluding package/import/URL) |
| End of file | trailing newline required |
| Newline char | LF |
| Encoding | UTF-8 |
| Trailing whitespace | forbidden |
| Braces | K&R style (`if (x) {` — opening brace on the same line) |
| Single statement | braces required (`if (x) return;` forbidden) |
| One statement per line | required |

## Import order

Group order: `java.` → `javax.` → `org.` → `net.` → `com.` → others. **One blank line between groups**, alphabetical within a group, no blank lines within a group. **No wildcard (`*`) imports** (static imports in tests are an in-use exception).

```java
package com.daruda.darudaserver.domain.board.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.board.entity.Board;
import com.daruda.darudaserver.global.error.code.ErrorCode;

import lombok.RequiredArgsConstructor;
```

> `lombok.` and `jakarta.` go in the "others" group after `com.`. Follow the placement in existing files.

## Resolving Checkstyle violations

Checkstyle **has no auto-fix.** Find the flagged location in `build/reports/checkstyle/main.html` (tests: `test.html`) and fix it by hand.

Common causes:

| Violation | Fix |
|-----------|-----|
| `Indentation` | space indentation → replace with tabs |
| `LineLength` | over 120 chars → wrap arguments/chaining |
| `ImportOrder` | wrong group order or missing blank line between groups |
| `UnusedImports` | remove unused imports |
| `AvoidStarImport` | expand wildcard imports |
| `AbbreviationAsWordInName` | consecutive-uppercase acronym → `HttpClient` form |

Only editorconfig violations (newline/whitespace/encoding) are auto-fixed by `./gradlew editorconfigFormat`. In the IDE `.editorconfig` is applied automatically, so tab indentation is mostly correct on its own.
