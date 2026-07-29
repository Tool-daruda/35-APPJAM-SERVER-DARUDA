---
description: Read a PR's review comments, apply them, then reply to each comment in Korean
argument-hint: [PR number]
---

# Apply PR review feedback

> **Language**: Write user-facing responses and comment replies all in Korean.

Handle the review comments of the current branch (or the PR given in `$ARGUMENTS`).

## 1. Collect

```bash
# find the PR number of the current branch
gh pr view --json number,title,headRefName

# inline review comments (per file/line)
gh api --paginate repos/{owner}/{repo}/pulls/<PR-number>/comments \
  --jq '.[] | {id, path, line, body, user: .user.login}'

# general review comments (whole review body)
gh api --paginate repos/{owner}/{repo}/pulls/<PR-number>/reviews \
  --jq '.[] | select(.body != "") | {id, state, body, user: .user.login}'
```

## 2. Classify — HITL point

Classify each comment into one of three.

| Class | Handling |
|-------|----------|
| ① Apply immediately | fix it right away |
| ② Needs discussion | **confirm with the user.** Do not decide arbitrarily |
| ③ Cannot apply | reply stating the reason |

**If the reviewer's intent is ambiguous, don't guess and fix — ask the user.**

## 3. Apply

Code changes follow the project rules — the `architecture`, `code-style`, `testing`, `error-handling` skills.

```bash
./.claude/scripts/check-all.sh
```

## 4. Commit

For the procedure see `/commit-push-pr`; for the title format and commit type see `git-convention`.
**Push only when the user explicitly asks.**

## 5. Reply

Reply to each inline comment **in Korean**. If applied, state how; if not, state why not.

```bash
gh api repos/{owner}/{repo}/pulls/<PR-number>/comments/<comment_id>/replies \
  -f body='반영했습니다. <변경 요약>. (commit <sha>)'
```

Tone: concise and polite. Forms like "반영했습니다 / 다음 이유로 유지했습니다 / 별도 이슈로 분리했습니다".

## 6. Report

Summarize which comments you handled and how, as a table.

> **Do not merge your own PR.**
