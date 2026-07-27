---
name: git-workflow
description: 브랜치를 만들거나 커밋/PR을 작성할 때 로드. 브랜치 전략, 커밋 메시지 포맷(한국어), PR 규칙, Claude Code Git 규칙.
---

# Git 워크플로우 (daruda)

## 브랜치 전략

```
main       ← 운영 배포 (cd-prod.yml)
develop    ← 개발 통합 + 기본 브랜치, PR 대상 (cd-dev.yml)
{type}/#{이슈번호}   ← 작업 브랜치
```

작업 브랜치는 **`develop`에서 분기**하고 **`develop`으로 PR**을 올린다.

```bash
git switch develop
git pull origin develop
git switch -c feat/#357
```

브랜치 이름은 `{type}/#{이슈번호}` 형식이다: `feat/#355`, `fix/#302`, `refactor/#270`, `chore/#250`.

## 커밋 메시지

```
#{이슈번호} [{type}] {한국어 설명}
```

실제 예:

```
#355 [feat] 게시글별 스크랩 수 배치 조회 쿼리 추가
#355 [test] 게시글 목록 응답에 스크랩 수 포함 검증 테스트 추가
#353 [fix] 게시글 목록 조회 시 `size < 1` 입력 방어 로직 추가
#351 [refactor] `ToolService`의 `getLiked` 메서드를 `private`으로 변경
```

### type

| type | 용도 |
|------|------|
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `refactor` | 동작 변화 없는 구조 개선 |
| `test` | 테스트 추가/수정 |
| `chore` | 빌드·설정·의존성 등 부수 작업 |
| `style` | 포매팅, 세미콜론 등 로직 무관 변경 |
| `delete` | 파일·코드 삭제 |

### 규칙

- 설명은 **한국어**로, `~ 추가`, `~ 수정`, `~ 변경` 같은 명사형 종결로 끝낸다.
- 코드 식별자는 백틱으로 감싼다: `` `BoardService` ``.
- 이슈 번호는 `#` 포함, 대괄호 없이 맨 앞에 온다. (`[#355] feat: ...` 형식이 **아니다**)
- 한 커밋은 한 가지 일만 한다. 기능 + 테스트도 가급적 분리한다(위 예시처럼 `[feat]`과 `[test]`를 나눈다).
- 본문이 필요하면 제목 다음 빈 줄 뒤에 한국어로 적는다.

## PR

- 제목: `[{Type}] {한국어 설명}` 또는 커밋 메시지와 동일한 형식
- 대상 브랜치: `develop`
- 템플릿(`.github/PULL_REQUEST_TEMPLATE.md`)의 4개 섹션을 채운다.

```markdown
## 📣 Related Issue

- close #355

## 📝 Summary

- 게시글 목록 조회 응답에 게시글별 스크랩 수를 포함하도록 수정
- 스크랩 수 배치 조회 쿼리 추가로 N+1 제거

## 🙏 Question & PR point

- 스크랩 수 정렬 시 커서 처리 방식에 대한 의견 부탁드립니다

## 📬 Postman

<!-- 스크린샷 첨부 -->
```

- `close #{이슈번호}`로 이슈를 연결한다.
- PR을 올리면 `test.yml` 워크플로우가 `./gradlew --info test`를 실행한다. 실패하면 머지할 수 없다.

## Claude Code Git 규칙

| 규칙 | 내용 |
|------|------|
| 커밋/푸시 시점 | **사용자가 요청할 때만** 한다. 작업이 끝났다고 자동 커밋하지 않는다 |
| `develop`/`main` 직접 커밋 | 금지. 반드시 작업 브랜치를 먼저 만든다 |
| 커밋 전 검증 | `./gradlew checkstyleMain checkstyleTest editorconfigCheck test` 통과 확인 (`/run-checks`) |
| 커밋 메시지 서명 | 아래 트레일러를 붙인다 |
| `git push --force` | 금지 (`--force-with-lease`도 사용자 요청 시에만) |
| 대화형 플래그 | `git rebase -i`, `git add -i`는 이 환경에서 동작하지 않는다 |
| `.env`·시크릿 | 절대 커밋하지 않는다. `git status`에서 확인 후 스테이징 |

커밋 메시지 끝에 붙이는 트레일러:

```
Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01TA69yPr3JSuepad72AXJcz
```

PR 본문 끝에 붙이는 서명:

```
🤖 Generated with [Claude Code](https://claude.com/claude-code)

https://claude.ai/code/session_01TA69yPr3JSuepad72AXJcz
```

> 실제 커밋·푸시·PR 생성 절차는 `commit-push-pr` 스킬을 따른다.
