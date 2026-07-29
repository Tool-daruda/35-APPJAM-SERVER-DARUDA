# daruda (다루다)

Backend server for the web track of the AND SOPT 35th App Jam. A tool-discovery + community service for university students.
Base package: `com.daruda.darudaserver`

> **Language**: All user-facing responses must be written in Korean without exception (excluding code, identifiers, and log messages).

## Harness layers

The `.claude/` directory of this repo is organized as a layered architecture. **Do not exceed the responsibility of each layer.**

| Layer | Location | Responsibility | Examples |
|-------|----------|----------------|----------|
| **Entry point** (Controller) | `.claude/commands/` | Receive a user request and orchestrate the procedure. Reference skills instead of copying rules | `/new-feature`, `/commit-push-pr` |
| **Orchestration** (Service) | `.claude/agents/` | Delegate multi-step work that needs context isolation | `code-reviewer`, `test-writer` |
| **Knowledge** (Domain) | `.claude/skills/` | Single-responsibility rules. Entry point is `SKILL.md`, details in `references/` | `architecture`, `code-style` |
| **Execution** (Infra) | `.claude/scripts/`, `.claude/hooks/` | Encapsulation of a decided procedure. Code, not docs, is the SSOT | `check-all.sh` |

Principles:

- **One fact lives in exactly one place.** No copying between docs — link by reference. If fixing a rule requires editing two places, the structure is wrong.
- **Procedures go in commands, rules go in skills.** Do not put workflows in skills.
- **The script is the SSOT for the full verification procedure.** Do not transcribe the steps and order that `check-all.sh` runs as individual Gradle commands in the docs. (One-off commands that are not full verification, such as running a specific test, are an exception — see the "Commands" section.)

## Task-based loading

When one of the tasks below is detected, load the corresponding skill (manual invocation: `/<name>`). Read only the skill's entry point, and read `references/` **only as needed**.

| Task type | Load |
|-----------|------|
| Layer structure, package placement, transaction boundaries, DTO mapping, response format, Swagger | `architecture` |
| Writing Java code, naming/formatting, Lombok, Checkstyle | `code-style` |
| Writing tests (JUnit 5 / Mockito / AssertJ) | `testing` |
| Exception/error handling | `error-handling` |
| Branch/commit/issue/PR name & label conventions | `git-convention` |
| Judging whether a rule violation in existing code was already there, refactoring | `legacy-cleanup` |

| Procedure | Command |
|-----------|---------|
| Add a feature to an existing domain | `/new-feature` |
| Full verification before a PR | `/run-checks` |
| Commit · push · create PR | `/commit-push-pr` |
| Create a GitHub issue | `/create-issue` |
| Apply PR review feedback and reply | `/resolve-review` |

> Delegate diagnosing failing tests (without fixing them) to the `test-validator` agent, code review to `code-reviewer`, and test writing to `test-writer`.

## Human-in-the-Loop (HITL)

Agents may ask questions while running. **Ask about anything hard to reverse or with no single right answer; handle agreed conventions on your own.**

| Always ask | Handle on your own |
|------------|--------------------|
| `git push`, creating a PR, creating an issue | Writing/editing code, writing tests |
| Committing (do not do it before the user asks) | Running verification (`check-all.sh`), auto-formatting fixes |
| Deleting files/code, schema changes | Deciding names per convention, package placement |
| Applying feedback whose reviewer intent is ambiguous | Applying already-decided error codes / response formats |
| Bulk legacy replacement (`legacy-cleanup`) | Applying current rules to new code |

**Do not merge your own PR.** Do not claim verification passed when it did not.

## Tech stack

| Area | Technology |
|------|------------|
| WAS | Spring Boot 3.4.1 / JDK 17 / Gradle (Groovy DSL, single module) |
| DB / cache | MySQL (JPA·Hibernate, QueryDSL 5.0) / Redis |
| Search | Elasticsearch 8.11 (Spring Data Elasticsearch) |
| Storage | OCI Object Storage (issues presigned URLs) |
| External integration | OpenFeign (Kakao OAuth) |
| Auth | OAuth 2.0 (Kakao) + JWT / Refresh Token → Redis |
| Notifications | SSE (SseEmitter) |
| Config encryption | Jasypt |
| Monitoring | Sentry + Discord Appender (logback) |
| Test & lint | JUnit 5 / Mockito / AssertJ / Checkstyle (naver rules) / editorconfig |

## Project structure

**Single module + per-domain layered packages.** Not hexagonal / multi-module.

```text
src/main/java/com/daruda/darudaserver/
├── domain/{domain}/              # admin, comment, community, notification, report, search, tool, user
│   ├── controller/ service/ repository/ entity/
│   ├── dto/request/ dto/response/     # record
│   └── event/                          # ApplicationEventPublisher
└── global/
    ├── auth/ config/ error/ handler/   # auth, config, ErrorCode/response, GlobalExceptionHandler
    ├── annotation/ common/             # @DisableSwaggerSecurity, BaseTimeEntity, pagination
    └── image/ oci/                     # image upload, OCI Object Storage
```

**Dependency direction:** `controller` → `service` → `repository` → `entity`. Controllers do not call repositories directly.
**Cross-domain references:** inject another domain's `Service`. Do not inject a repository across a domain boundary.

## Core constraints

Background and examples live in each skill. Here we only judge whether a rule is violated.

### ❌ Forbidden

| Constraint | Detail |
|------------|--------|
| Importing `jakarta.transaction.Transactional` | `architecture` → `references/transaction.md` |
| New use of `ApiResponse` | Response wrapper is unified as `SuccessResponse`/`ErrorResponse` |
| `Res`/`Req` abbreviated suffixes in DTO names | Full names `XxxRequest`/`XxxResponse` |
| `Entity` suffix in JPA entity names | Use the domain term as-is (`Board`, `Tool`, `Comment`) |
| Throwing `RuntimeException` directly | Only `BusinessException` subclasses + `ErrorCode` (`error-handling`) |
| Calling a repository directly from a controller | Layer boundary violation |
| Returning an entity directly as a controller response | Convert to a response DTO (record) |
| Using `HttpServletRequest`/`ResponseEntity` in a service | Web dependencies belong only to the controller layer |
| Reusing an existing code value for a new `ErrorCode` | Code values (`E4xxxxx`) are globally unique |
| Committing hardcoded secrets in `.env`·`application-*.yml` | Inject via environment variables or Jasypt |
| Declaring `@Builder` on both the class level and the constructor | On the constructor (or static factory) only |

### ✅ Required

| Practice | Detail |
|----------|--------|
| `@Transactional(readOnly = true)` on the service class, `@Transactional` on write methods only | `architecture` → `references/transaction.md` |
| Wrap responses in `SuccessResponse<T>` and match the HTTP status to `SuccessCode` | `architecture` → `references/web-layer.md` |
| Throw errors as `throw new XxxException(ErrorCode.YYY)` — do not assemble in the controller | `error-handling` |
| Request/response DTOs are `record` + Bean Validation (`@Valid`) | `architecture` → `references/web-layer.md` |
| Create entities via static factory `of()`/`create()`, default constructor is `@NoArgsConstructor(PROTECTED)` | `code-style` → `references/idioms.md` |
| `@Operation`/`@Parameter` on controllers; register unauthenticated APIs in `SecurityConfig.WHITE_LIST` too | `architecture` → `references/web-layer.md` |
| Indent with **tabs**, 120 chars per line, trailing newline, import group order | `code-style` → `references/formatting.md` |
| Commit message `#issue-number [type] Korean description`, no signature trailer | `git-convention` |

## Commands

```bash
./.claude/scripts/check-all.sh                              # full verification (before a PR · this script is the SSOT for order)
./gradlew test --tests "BoardServiceTest"                   # a specific test class
./gradlew bootRun --args='--spring.profiles.active=local'   # run locally
```

> Before running, prepare `.env` / `docker-compose.yml` (MySQL·Redis·Elasticsearch).

## Profiles

| Profile | File | Purpose |
|---------|------|---------|
| `local` | `application-local.yml` | Local development |
| `dev` | `application-dev.yml` | Dev server (CI/CD: `cd-dev.yml`) |
| `prod` | `application-prod.yml` | Production server (CI/CD: `cd-prod.yml`) |

Inject secrets via environment variables or Jasypt (`ENC(...)`). **No plaintext commits.**
