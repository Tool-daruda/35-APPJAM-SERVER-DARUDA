---
name: resolve-review
description: GitHub PR의 코드 리뷰 코멘트를 읽고, 피드백을 반영한 뒤, 각 코멘트에 한국어로 답변할 때 로드. PR 리뷰 반영 워크플로우.
---

> **언어**: 이 작업의 사용자 대상 응답은 모두 한국어로 작성해야 한다. (코드, 식별자, 로그 등 기술적 산출물은 제외.)

# PR 리뷰 반영

현재 브랜치(또는 지정된 PR)의 리뷰 코멘트를 읽고, 반영한 뒤, 각 코멘트에 답변한다.

## 1. 리뷰 코멘트 수집

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

## 2. 반영

- 각 코멘트를 **분류**한다: ① 즉시 반영 / ② 논의 필요 / ③ 반영 불가(사유 명시).
- 코드 변경은 프로젝트 규칙을 따른다 — 아키텍처(`architecture`), 코드 스타일(`code-style`), 테스트(`testing`).
- 변경 후 항상 검증: `./gradlew checkstyleMain checkstyleTest editorconfigCheck test` (`/run-checks`).

## 3. 커밋

- 리뷰 반영 커밋 메시지: `#{이슈번호} [refactor] 리뷰 피드백 반영 - <요약>` (또는 변경 성격에 맞는 type).
- 커밋 컨벤션과 푸시 규칙은 `git-workflow` 스킬을 따른다(**푸시는 사용자가 명시적으로 요청할 때만**).

## 4. 답변

각 인라인 코멘트에 **한국어로** 답변한다. 반영했다면 어떻게 했는지, 안 했다면 왜 안 했는지 밝힌다.

```bash
# 인라인 코멘트에 답변
gh api repos/{owner}/{repo}/pulls/<PR-number>/comments/<comment_id>/replies \
  -f body='반영했습니다. <change summary>. (commit <sha>)'
```

- 답변 톤: 간결하고 정중하게. "반영했습니다 / 다음 이유로 유지했습니다 / 별도 이슈로 분리했습니다" 같은 형태.
- 모든 코멘트를 처리한 뒤, 어떤 코멘트를 어떻게 처리했는지 표로 요약해 사용자에게 보고한다.

## 원칙

- 리뷰어의 의도가 모호하면 임의로 판단하지 말고 사용자에게 확인한다.
- 자기 PR을 스스로 머지하지 않는다(`git-workflow`). 머지하지 않는다.
