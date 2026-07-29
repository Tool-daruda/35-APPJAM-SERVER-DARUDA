---
name: git-convention
description: Load when checking the name and label conventions for branches, commits, issues, and PRs. The single source for the three title formats (issue/PR/commit), branch strategy, commit types, and label/assignee rules. The actual execution procedures live in the /commit-push-pr and /create-issue commands.
---

# Git conventions (daruda)

> **This file is the single source of truth (SSOT) for name & label conventions.** The commands (`/commit-push-pr`, `/create-issue`, `/resolve-review`) hold only procedures and reference here for conventions. Do not copy the contents.

## Branch strategy

```text
main       ← production deploy (cd-prod.yml)
develop    ← dev integration + default branch, PR target (cd-dev.yml)
{type}/#{issue-number}   ← working branch
```

Working branches **branch off `develop`** and **open a PR to `develop`**.

```bash
git switch develop
git pull origin develop
git switch -c feat/#357
```

Branch names follow the `{type}/#{issue-number}` format: `feat/#355`, `fix/#302`, `refactor/#270`, `chore/#250`.

## The three title formats — subtly different

**Issues have no number, PRs get a number, and only commits use a lowercase type.**

| Target | Format | Example |
|--------|--------|---------|
| Issue | `[{Type}] {Korean description}` | `[Fix] JWT Token 재발급이 안되는 오류 수정` |
| PR | `#{issue-number} [{Type}] {Korean description}` | `#260 [Fix] JWT Token 재발급이 안되는 오류 수정` |
| Commit | `#{issue-number} [{type}] {Korean description}` | `#260 [feat] 알 수 없는 리프레쉬 토큰 오류메시지 추가` |

- **PR title = issue title with `#{issue-number}` and a space prepended.** Don't drop the number (`[Feat] ...`) or append it (`... (#355)`).
- The `Type` in issues·PRs is capitalized (`[Feat]`, `[Fix]`, `[Chore]`, `[Refactor]`); the commit `type` is lowercase (`[feat]`).
- The commit type reflects the nature of the commit unit, so it may differ from the 4 issue·PR Types. Example: a `#357 [test] ...` commit on a `[Feat]` issue.

## Commit types

| type | Use |
|------|-----|
| `feat` | new feature |
| `fix` | bug fix |
| `refactor` | structural improvement with no behavior change |
| `test` | adding/modifying tests |
| `chore` | build·config·dependency and other incidental work |
| `style` | formatting, semicolons, and other logic-irrelevant changes |
| `delete` | deleting files·code |

Real examples:

```text
#355 [feat] 게시글별 스크랩 수 배치 조회 쿼리 추가
#355 [test] 게시글 목록 응답에 스크랩 수 포함 검증 테스트 추가
#353 [fix] 게시글 목록 조회 시 `size < 1` 입력 방어 로직 추가
#351 [refactor] `ToolService`의 `getLiked` 메서드를 `private`으로 변경
```

Rules:

- Write the description in **Korean**, ending in a noun form like `~ 추가`, `~ 수정`, `~ 변경`.
- Wrap code identifiers in backticks: `` `BoardService` ``.
- The issue number includes `#`, no brackets, and comes first. (It is **not** the `[#355] feat: ...` format.)
- Keep each commit to the **smallest self-contained unit** — split by kind/concern even within one file. See the **Commit granularity** section below.
- If a body is needed, write it in Korean after a blank line following the title.

## Commit granularity — split to the smallest units

Prefer the **smallest self-contained unit** that still builds and can be reviewed and reverted on its own. Split whenever changes differ in **kind** or **concern** — **even when they touch the same file. Sitting in one file is not a reason to combine them.**

- Two unrelated bug fixes → two `[fix]` commits.
- Different concerns of one issue → one commit each (`[fix]` vs `[feat]` vs `[refactor]`).
- Production code and its tests → separate commits (`[feat]`, then `[test]`).
- Mechanical changes (formatting, renames, import order) → their own `[style]`/`[refactor]` commit, kept out of logic commits.

**How, given no interactive staging** (`git add -p` / `git add -i` are unavailable — see "Git safety rules"): commit **incrementally as you write**, rather than batching every edit and trying to split afterward.

1. Make one logical change.
2. Verify (`check-all.sh`, or the relevant subset).
3. Stage only that change's file(s) (`git add <path>`) and commit.
4. Repeat for the next change.

When several independent changes get interleaved inside one file, treat that as the signal to stop and commit the current one before writing the next.

> Example: a "free-board create" error and an "edit/delete index-lookup" error both live in `BoardService`, but they are independent bugs → commit them separately (`#359 [fix] 자유게시판 작성 오류 수정`, then `#359 [fix] 게시글 수정·삭제 색인 판단 오류 수정`), not as one combined commit.

## Labels & Assignee (required on both issues and PRs)

| Item | Value |
|------|-------|
| Type label | one of `feat` / `chore` / `⚒️ Fix` / `🔥 Refactor` |
| Assignee-name label | your own name (`예찬`, `재민`, `지원`, `수인`) |
| Assignee | yourself (`@me`) |

Work nature → type label + issue-template mapping:

| Work | Type label | Issue template | Title prefix | Note |
|------|------------|----------------|--------------|------|
| New feature | `feat` | 🛠 Feature | `[Feat]` | not `📍 Feat` |
| Bug fix | `⚒️ Fix` | 🔧 Fix | `[Fix]` | not `bug`/`fix` |
| Refactoring | `🔥 Refactor` | 🎨 Other | `[Refactor]` | includes the emoji |
| Config·env·dependency | `chore` | ⚙ Chore | `[Chore]` | |

> The name labels indicate teammates separately from GitHub accounts. Determine the current user's own name label at runtime (map their GitHub account to one of `예찬`/`재민`/`지원`/`수인`); if the mapping is unclear, ask the user rather than guessing. **Do not attach someone else's name label arbitrarily.**

## No commit signatures

Write **only the title (+ body if needed)** in commit messages. **Do not add** the trailers below.

```text
❌ Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>
❌ 🤖 Generated with Claude Code
```

The commit author is the actual worker (a teammate); do not credit tools as co-authors. Do not add generation signatures to PR bodies either.

## Git safety rules

| Rule | Content |
|------|---------|
| When to commit/push | **Only when the user asks.** Do not auto-commit just because work is done |
| Direct commit to `develop`/`main` | Forbidden. Always create a working branch first |
| `git push --force` | Forbidden (`--force-with-lease` only on user request) |
| Merging your own PR | Forbidden. Only create it, then wait for review |
| Interactive flags | `git rebase -i`, `git add -i`, `git add -p` do not work in this environment (stage per-file and commit incrementally instead) |
| `.env`·secrets | Never commit them. Always check the staged list with `git status` |
