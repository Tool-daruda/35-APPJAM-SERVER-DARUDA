---
description: Create a GitHub issue following the project ISSUE_TEMPLATE
argument-hint: <issue type and content>
---

# Create a GitHub issue

> **Language**: Write the issue title·body and user-facing responses all in Korean.
> **Conventions**: The title format·type labels·template mapping are owned by the `git-convention` skill (SSOT). Load it first.

**Do not create an issue arbitrarily when the user hasn't asked for one.**

## 1. Determine the type

From `git-convention`'s "Labels & Assignee" table, pick the template · title prefix · type label for the work's nature.
(They correspond to the 4 templates in `.github/ISSUE_TEMPLATE/`.)

## 2. Write the body

**Feature** — 4-part structure:

```markdown
### 목적

> 게시글 목록을 스크랩 수 기준으로 정렬할 수 있게 한다.

### 작업 상세 내용

- [ ] `BoardSortType` enum 추가
- [ ] `BoardService.getBoardList`에 정렬 분기 추가
- [ ] 컨트롤러에 `sortBy` 파라미터 추가
- [ ] 테스트 작성

### 유의사항

- 커서 페이지네이션과 함께 동작해야 함
```

**Fix / Chore / Other** have only the `### 목적` section, so write concisely and add a checklist if needed.

- Break the work details into a **checkbox list**. They become the commit units directly.
- Wrap code identifiers in backticks.

## 3. Create — HITL point

**Before running, present the title·type label·name label·assignee·body and get approval.**

```bash
gh issue create \
  --title "[Feat] 게시글 목록 정렬 기능 추가" \
  --label feat --label 예찬 \
  --assignee @me \
  --body "$(cat <<'EOF'
### 목적

> 게시글 목록을 스크랩 수 기준으로 정렬할 수 있게 한다.

### 작업 상세 내용

- [ ] `BoardSortType` enum 추가
- [ ] `BoardService.getBoardList`에 정렬 분기 추가

### 유의사항

- 커서 페이지네이션과 함께 동작해야 함
EOF
)"
```

For label·assignee values and rules, see `git-convention`'s "Labels & Assignee".

## 4. After creation

- The branch name and the following PR title derive from this issue number — for the rules see `git-convention`'s "Branch strategy" and "The three title formats".
- Set the **same labels and assignee** on the PR too (`/commit-push-pr`).

Report the created issue number and URL in Korean.
