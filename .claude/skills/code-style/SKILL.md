---
name: code-style
description: Load when writing or modifying Java code. Holds the core naming, Lombok, and Checkstyle rules; formatting/import-order details and entity/service writing idioms are split into references/.
---

# Code style (daruda)

Java 17 + Lombok. **Naver Checkstyle** (`config/naver-checkstyle-rules.xml`, `maxWarnings = 0`) + **editorconfig** are enforced by the build. If either fails, `./gradlew build` breaks.

## Detailed-rule routing

| What you are doing | File to read |
|--------------------|--------------|
| Formatting specifics, import-order examples, resolving Checkstyle violations | `references/formatting.md` |
| Entity/service writing idioms, logging, comments | `references/idioms.md` |

## Always follow (summary)

| Rule | Value |
|------|-------|
| Indentation | **tabs** (spaces are a Checkstyle violation) |
| Max line length | 120 chars |
| End of file | trailing newline required (LF, UTF-8) |
| Import order | `java.` → `javax.` → `org.` → `net.` → `com.` → others, one blank line between groups, **no wildcards** |
| Braces | K&R, braces required even for a single statement |

## Naming

| Target | Rule | Example |
|--------|------|---------|
| Class | PascalCase | `BoardService` |
| Method/variable | camelCase | `getBoardList` |
| Constant | UPPER_SNAKE_CASE | `ALLOWED_EXTENSIONS` |
| JPA entity | domain term, **no `Entity` suffix** | `Board`, `Tool`, `Comment` |
| Request DTO | `{verb}{target}Request` | `CreateBoardRequest` |
| Response DTO | `{verb}{target}Response` or `{target}Response` | `BoardResponse` |
| Repository | `{entity}Repository` | `BoardRepository` |
| Test | `{target}Test` | `BoardServiceTest` |
| DB column | snake_case | `board_id`, `created_at` |
| URL path variable | kebab-case | `/{board-id}` |

**No abbreviations**: `Response`/`Request`, not `Res`/`Req`. Consecutive-uppercase acronyms hit the `AbbreviationAsWordInName` rule, so don't copy existing typos like `ToolPlatForm`; write it as `HttpClient`.

## Lombok

| Annotation | Use |
|------------|-----|
| `@Getter` | Entities·DTOs. **Do not use `@Setter`** (state changes go through domain methods) |
| `@RequiredArgsConstructor` | Constructor injection for services·controllers. Fields are `private final` |
| `@NoArgsConstructor(access = AccessLevel.PROTECTED)` | Required on JPA entities |
| `@Builder` | **On the constructor only.** No duplicate declaration with the class level |
| `@Slf4j` | Logging |

**Forbidden**: `@Data` (auto-generates setters + equals/hashCode on entities), `@AllArgsConstructor` on entities, duplicate class-level `@Builder` + constructor `@Builder`, `@Autowired` field injection.

## Verification

```bash
./.claude/scripts/check-all.sh        # full verification (recommended · this script is the SSOT for order)
```

| Tool | Auto-fix |
|------|----------|
| editorconfig | **Yes** — `./gradlew editorconfigFormat` |
| Checkstyle | **No** — read `build/reports/checkstyle/main.html` and fix by hand |
