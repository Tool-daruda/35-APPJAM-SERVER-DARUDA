---
description: PR 전 전체 검증을 실행한다 (editorconfig → Checkstyle → test)
---

# 전체 검증

**검증 단계와 순서는 스크립트가 SSOT다.** 개별 Gradle 명령을 여기에 나열하지 않는다 — 순서가 바뀌면 스크립트만 고치면 되도록.

```bash
./.claude/scripts/check-all.sh
```

실패하면 **즉시 멈춰** 어느 단계에서 무엇이 실패했는지 확인한다.

## 실패 시 대응

| 단계 | 조치 |
|------|------|
| editorconfig | 스크립트가 `editorconfigFormat`으로 자동 수정을 먼저 시도한다. 그래도 남으면 CRLF·인코딩 문제를 확인한다 |
| Checkstyle | **자동 수정 불가.** `build/reports/checkstyle/main.html`(테스트는 `test.html`)에서 위반 위치를 확인해 직접 고친다. 흔한 원인과 해결은 `code-style` → `references/formatting.md` |
| test | 실패 원인을 진단한다. 진단만 필요하면 **`test-validator` 에이전트에 위임한다**(읽기 전용). 리포트: `build/reports/tests/test/index.html` |

## 보고

- 모두 통과하면 통과 사실을 한국어로 간결히 보고한다.
- 실패하면 **어느 단계에서 무엇이 실패했는지 실제 출력과 함께** 보고한다.
- **통과하지 않은 것을 통과했다고 말하지 않는다.**
