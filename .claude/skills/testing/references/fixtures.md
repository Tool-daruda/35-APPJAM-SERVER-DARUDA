# 픽스처 · 안티패턴 · 실행 (daruda)

## 픽스처 만들기

엔티티 ID는 `@GeneratedValue`라 빌더로 넣을 수 없다. **`ReflectionTestUtils.setField`로 주입**한다.

```java
Board board = Board.of("제목", "본문", author);
ReflectionTestUtils.setField(board, "id", 10L);
```

- 여러 테스트가 공유하는 픽스처는 `@BeforeEach setUp()`에서 만든다.
- 특정 테스트에만 필요한 데이터는 그 테스트의 `// given` 안에서 만든다.
- 엔티티를 `@Mock`으로 만드는 것은 피한다(도메인 메서드 동작까지 사라진다). **실제 인스턴스를 쓴다.**

## 하지 말 것

| 안티패턴 | 대안 |
|----------|------|
| `private` 메서드를 리플렉션(`getDeclaredMethod` + `setAccessible`)으로 테스트 | public 메서드를 통해 간접 검증한다. 꼭 단독 검증이 필요하면 그 로직은 별도 클래스로 분리할 신호다 |
| `verify`만 있고 단언이 없는 테스트 | 결과 상태를 `assertThat`으로 확인 |
| `any()`만으로 스텁·검증 | 실제 값으로 검증해야 회귀를 잡는다 |
| 한 테스트에서 여러 시나리오 검증 | `@Nested` + 개별 `@Test`로 분리 |
| 테스트 간 상태 공유(static 필드) | `@BeforeEach`에서 매번 새로 만든다 |
| 불필요한 스텁 방치 | Mockito strict stub이 `UnnecessaryStubbingException`으로 실패시킨다. 안 쓰는 `given`은 지운다 |

## 실행

```bash
./gradlew test                                  # 전체
./gradlew test --tests "BoardServiceTest"       # 특정 클래스
./gradlew test --tests "*.community.*"          # 패턴
./gradlew test --info                           # 상세 로그 (CI와 동일)
```

리포트: `build/reports/tests/test/index.html`

## 실패했을 때

실패 원인 진단은 `test-validator` 에이전트에 위임한다(읽기 전용). 자주 나오는 원인:

| 증상 | 원인 |
|------|------|
| `NullPointerException` in `entity.getId()` | 픽스처에 `ReflectionTestUtils.setField(entity, "id", 1L)` 누락 |
| `UnnecessaryStubbingException` | 사용되지 않는 `given(...)` |
| `WrongTypeOfReturnValue` | `@Mock` 대상이 아닌 실제 객체에 스텁 시도 |
| `compileJava` 실패 (테스트 실패 아님) | `@Transactional(readOnly = ...)`에 jakarta import — `architecture` 스킬의 `references/transaction.md` 참조 |
| 빌드 중단인데 테스트는 통과 | `checkstyleTest` 실패인지 확인 |
