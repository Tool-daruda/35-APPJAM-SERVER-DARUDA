---
description: PR 전 전체 검증을 순서대로 실행한다 (editorconfig → Checkstyle → test)
---

# 전체 검증

PR을 올리기 전에 아래를 **순서대로** 실행하고, 실패하면 즉시 멈춰 원인을 보고한다.

```bash
./.claude/scripts/check-all.sh
```

개별 실행:

```bash
./gradlew editorconfigFormat              # 1. 개행/공백 자동 수정
./gradlew editorconfigCheck               # 2. 탭/개행/인코딩 검증
./gradlew checkstyleMain checkstyleTest   # 3. 스타일 (naver rules, 경고 0)
./gradlew test                            # 4. 전체 테스트
```

## 실패 시 대응

| 단계 | 조치 |
|------|------|
| editorconfig | `./gradlew editorconfigFormat`으로 자동 수정된다. 그래도 남으면 CRLF·인코딩 문제를 확인한다 |
| Checkstyle | **자동 수정 불가.** `build/reports/checkstyle/main.html`(또는 `test.html`)에서 위반 위치를 확인해 직접 고친다. 흔한 원인: 스페이스 들여쓰기, 120자 초과, import 순서/빈 줄, 와일드카드 import |
| test | 실패 원인을 진단한다. 진단만 필요하면 `test-validator` 에이전트에 위임한다. 리포트: `build/reports/tests/test/index.html` |

## 보고

모두 통과하면 통과 사실을 한국어로 간결히 보고한다. 실패하면 **어느 단계에서 무엇이 실패했는지 실제 출력과 함께** 보고한다. 통과하지 않은 것을 통과했다고 말하지 않는다.
