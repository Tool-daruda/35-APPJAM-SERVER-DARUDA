---
description: 기존 도메인에 새 기능(Controller → Service → Repository)을 추가한다
argument-hint: <도메인명> <기능 설명>
---

# 새 기능 추가

`$ARGUMENTS`에 지정된 도메인에 기능을 추가한다.

## 0. 준비

1. 대상 도메인 패키지의 **기존 컨트롤러·서비스를 먼저 읽어** 스타일을 파악한다.
   `src/main/java/com/daruda/darudaserver/domain/{도메인}/`
2. `architecture`, `code-style`, `testing` 스킬을 로드한다. 상세 규칙은 각 스킬의 `references/`에 있으니 **필요한 것만** 읽는다.
   (⑦ 테스트를 `test-writer` 에이전트에 위임하면 그 에이전트가 `testing`을 직접 로드하므로 생략해도 된다.)

## 1. 작성 순서 — 안쪽부터

각 단계의 상세 규칙은 스킬에 있다. 여기서는 순서와 체크포인트만 다룬다.

| 순서 | 산출물 | 놓치기 쉬운 것 | 상세 |
|------|--------|----------------|------|
| ① Entity (필요할 때만) | `entity/{Xxx}.java` | **`Entity` 접미사 없음**, 연관관계 `LAZY`, 정적 팩토리 `of()` | `code-style` → `references/idioms.md` |
| ② Repository | `repository/{Xxx}Repository.java` | N+1 예상 시 fetch join·배치 조회를 함께 만든다 | `architecture` → `references/integration.md` |
| ③ DTO | `dto/request/`, `dto/response/` | `record` + Bean Validation, **`Req`/`Res` 축약 금지** | `architecture` → `references/web-layer.md` |
| ④ Service | `service/{Xxx}Service.java` | 클래스 `@Transactional(readOnly = true)` + 쓰기 메서드만 재선언, **spring import** | `architecture` → `references/transaction.md` |
| ⑤ Controller | `controller/{Xxx}Controller.java` | `@Operation`/`@Parameter`, 생성은 201 상태 일치, 미인증이면 `SecurityConfig.WHITE_LIST` 등록 | `architecture` → `references/web-layer.md` |
| ⑥ ErrorCode (필요할 때) | `global/error/code/ErrorCode.java` | **코드값 중복 확인 필수** | `error-handling` |
| ⑦ Test | `src/test/.../{Xxx}ServiceTest.java` | 정상 + 예외 경로는 필수 | `testing` |

ErrorCode 중복 확인:

```bash
grep -o '"E[0-9]\{6\}"' src/main/java/com/daruda/darudaserver/global/error/code/ErrorCode.java | sort | uniq -d
```

테스트 작성은 `test-writer` 에이전트에 위임해도 된다.

## 2. 검증

```bash
./.claude/scripts/check-all.sh
```

## 3. 보고 — HITL 지점

생성/수정한 파일 목록과 추가된 API 엔드포인트를 한국어로 요약해 보고한다.

**커밋은 사용자가 요청할 때만 한다.** 기능이 완성됐다고 자동으로 커밋하지 않는다.
