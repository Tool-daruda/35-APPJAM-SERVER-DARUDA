---
description: Commit following project conventions, and push and create a PR on request
---

# Commit · push · PR

> **Language**: Write all user-facing responses in Korean.
> **Conventions**: The title format·labels·commit types are owned by the `git-convention` skill (SSOT). Load it first.

## 1. Verification (required)

```bash
./.claude/scripts/check-all.sh
```

If it doesn't pass, **do not commit.** Report the failing step and the actual output.

> `check-all.sh` runs `editorconfigFormat` first, which may modify the working tree. If files were already staged, re-stage the approved files and re-run this verification so the committed index matches exactly what was verified.

## 2. Check staging

```bash
git status
```

- **Never commit secrets, regardless of file name** (`.env`, `private.pem`, `application-*.yml`, `.properties`, `.json`, shell scripts, …). Scan the staged diff for credentials before committing.
- For commit-split units, see `git-convention`'s "Commit granularity" section — split to the smallest self-contained unit, even within one file.

## 3. Check the branch

If you are currently on `develop` or `main`, **create a working branch first.** Do not commit directly.

```bash
git switch -c feat/#357
```

For the branch-name format, see `git-convention`'s "Branch strategy".

## 4. Commit — HITL point

**Before committing, present the finalized content and get approval.** Even if the user asked to commit, proceed after showing the below.

- The final staged file list (`git status --short`)
- The full commit message

For the format, see `git-convention`. For a one-liner:

```bash
git commit -m "#357 [feat] 게시글 목록 조회에 정렬 파라미터 추가"
```

If a body is needed:

```bash
git commit -m "$(cat <<'EOF'
#357 [feat] 게시글 목록 조회에 정렬 파라미터 추가

정렬 기준이 늘어날 것을 대비해 `BoardSortType` enum으로 분리했다.
EOF
)"
```

> Commit-signature policy is owned by `git-convention` ("No commit signatures") — follow it there.

## 5. Push — HITL point

**Do not push before the user explicitly asks.** When asked, **present the target remote and branch, get approval, then** run it.

```bash
git push -u origin feat/#357
```

**Do not use `--force`. Do not push directly to `main`/`develop`.**

## 6. Create a PR — HITL point

Create one **only when the user explicitly asks.** Before running, **present the title·body·labels·assignee and get approval.**

```bash
gh pr create --base develop \
  --title "#357 [Feat] 게시글 목록 조회 정렬 기능" \
  --label feat --label 예찬 \
  --assignee @me \
  --body "$(cat <<'EOF'
## 📣 Related Issue

- close #357

## 📝 Summary

- 게시글 목록 조회에 `sortBy` 파라미터 추가
- 스크랩 수 기준 정렬 시 커서 페이지네이션 처리

## 🙏 Question & PR point

- 정렬 기준이 늘어날 경우 enum 확장 방식에 대한 의견 부탁드립니다

## 📬 Postman

<!-- 스크린샷 첨부 필요 -->
EOF
)"
```

- For target branch·label (type + name)·assignee rules, see `git-convention`'s "Branch strategy" and "Labels & Assignee".
- The body fills the 4 sections of `.github/PULL_REQUEST_TEMPLATE.md`.
- Link the issue with `close #{issue-number}`.
- The Postman screenshot must be attached by a human. If you can't attach it, leave the test-pass result there instead and tell the user.
- **Do not merge your own PR.**

## 7. After creation

Once you create a PR, the `test.yml` workflow runs `./gradlew --info test`.

```bash
gh pr checks            # check CI status
gh pr view --web        # open in the browser
```

If it fails, report to the user. Do not claim something passed when it did not.
