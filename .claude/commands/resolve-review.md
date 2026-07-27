---
description: PR의 리뷰 코멘트를 읽고 반영한 뒤 각 코멘트에 한국어로 답변한다
argument-hint: [PR 번호]
---

# PR 리뷰 반영

> **언어**: 사용자 대상 응답과 코멘트 답변은 모두 한국어로 작성한다.

현재 브랜치(또는 `$ARGUMENTS`로 지정된 PR)의 리뷰 코멘트를 처리한다.

## 1. 수집

```bash
# 현재 브랜치의 PR 번호 찾기
gh pr view --json number,title,headRefName

# 인라인 리뷰 코멘트 (파일/라인별)
gh api repos/{owner}/{repo}/pulls/<PR-number>/comments \
  --jq '.[] | {id, path, line, body, user: .user.login}'

# 일반 리뷰 코멘트 (전체 리뷰 본문)
gh api repos/{owner}/{repo}/pulls/<PR-number>/reviews \
  --jq '.[] | select(.body != "") | {id, state, body, user: .user.login}'
```

## 2. 분류 — HITL 지점

각 코멘트를 셋 중 하나로 분류한다.

| 분류 | 처리 |
|------|------|
| ① 즉시 반영 | 바로 고친다 |
| ② 논의 필요 | **사용자에게 확인한다.** 임의로 판단하지 않는다 |
| ③ 반영 불가 | 사유를 명시해 답변한다 |

**리뷰어의 의도가 모호하면 추측해서 고치지 말고 사용자에게 묻는다.**

## 3. 반영

코드 변경은 프로젝트 규칙을 따른다 — `architecture`, `code-style`, `testing`, `error-handling` 스킬.

```bash
./.claude/scripts/check-all.sh
```

## 4. 커밋

```text
#{이슈번호} [refactor] 리뷰 피드백 반영 - <요약>
```

변경 성격에 맞는 type을 쓴다. 절차는 `/commit-push-pr`, 컨벤션은 `git-convention` 참조.
**푸시는 사용자가 명시적으로 요청할 때만 한다.**

## 5. 답변

각 인라인 코멘트에 **한국어로** 답변한다. 반영했다면 어떻게 했는지, 안 했다면 왜 안 했는지 밝힌다.

```bash
gh api repos/{owner}/{repo}/pulls/<PR-number>/comments/<comment_id>/replies \
  -f body='반영했습니다. <변경 요약>. (commit <sha>)'
```

톤: 간결하고 정중하게. "반영했습니다 / 다음 이유로 유지했습니다 / 별도 이슈로 분리했습니다" 같은 형태.

## 6. 보고

어떤 코멘트를 어떻게 처리했는지 표로 요약해 보고한다.

> **자기 PR을 스스로 머지하지 않는다.**
