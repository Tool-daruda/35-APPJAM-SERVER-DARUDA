# Fixtures · anti-patterns · running (daruda)

## Building fixtures

Entity IDs are `@GeneratedValue`, so you can't set them via the builder. **Inject them with `ReflectionTestUtils.setField`.**

```java
Board board = Board.of("제목", "본문", author);
ReflectionTestUtils.setField(board, "id", 10L);
```

- Build fixtures shared by multiple tests in `@BeforeEach setUp()`.
- Build data needed only by a specific test inside that test's `// given`.
- Avoid making entities with `@Mock` (it removes even the domain-method behavior). **Use real instances.**

## Do not

| Anti-pattern | Alternative |
|--------------|-------------|
| Testing a `private` method via reflection (`getDeclaredMethod` + `setAccessible`) | Verify indirectly through the public method. If standalone verification is truly needed, that's a signal to extract the logic into a separate class |
| A test with only `verify` and no assertion | Check the resulting state with `assertThat` |
| Stubbing·verifying with only `any()` | Verify with real values to catch regressions |
| Verifying multiple scenarios in one test | Split into `@Nested` + individual `@Test` |
| Sharing state between tests (static fields) | Rebuild each time in `@BeforeEach` |
| Leaving unnecessary stubs | Mockito strict stubbing fails with `UnnecessaryStubbingException`. Delete unused `given`s |

## Running a specific test

Full verification is owned by `/run-checks` (→ `check-all.sh`). Use these single-shot runs while writing one test class (an allowed exception to "the script is the SSOT"):

```bash
./gradlew test --tests "BoardServiceTest"       # a specific class
./gradlew test --tests "*.community.*"          # a pattern
./gradlew test --info                           # verbose log (same as CI)
```

Report: `build/reports/tests/test/index.html`

## Common failure causes

Diagnosing a failure is delegated to the `test-validator` agent (read-only). Frequent causes:

| Symptom | Cause |
|---------|-------|
| `NullPointerException` in `entity.getId()` | Fixture missing `ReflectionTestUtils.setField(entity, "id", 1L)` |
| `UnnecessaryStubbingException` | An unused `given(...)` |
| `WrongTypeOfReturnValue` | Stubbing attempted on a real object that isn't a `@Mock` |
| `compileJava` failure (not a test failure) | jakarta import on `@Transactional(readOnly = ...)` — see the `architecture` skill's `references/transaction.md` |
| Build aborts but tests pass | Check whether it's a `checkstyleTest` failure |
