---
name: commit-push-pr
description: 프로젝트 컨벤션에 따라 커밋하고, (요청 시) 푸시 및 PR을 생성할 때 로드. 커밋 메시지 포맷, 브랜치 전략, PR 규칙을 자동 적용.
---

> **언어**: 이 작업의 사용자 대상 응답은 모두 한국어로 작성한다. (코드, 식별자, 로그 등 기술적 산출물은 제외.)

# 커밋 · 푸시 · PR (daruda)

컨벤션 상세는 `git-workflow` 스킬에 있다. 이 문서는 실행 절차다.

## 1. 커밋 전 검증 (필수)

```bash
./gradlew editorconfigFormat              # 개행/공백 자동 수정
./gradlew editorconfigCheck               # 탭/개행/인코딩
./gradlew checkstyleMain checkstyleTest   # 스타일 (경고 0 요구)
./gradlew test                            # 테스트
```

`/run-checks` 또는 `./.claude/scripts/check-all.sh`로 한 번에 실행할 수 있다.

- Checkstyle은 **자동 수정이 없다.** 실패 시 `build/reports/checkstyle/main.html`을 보고 직접 고친다. (editorconfig 위반만 `editorconfigFormat`으로 자동 수정된다.)
- **`.env`, `private.pem`, 시크릿이 담긴 yml은 절대 커밋하지 않는다.** `git status`로 스테이징 목록을 반드시 확인한다.
- 변경 사항은 논리적 단위로 나눠 스테이징한다(기능과 테스트는 별도 커밋).

## 2. 브랜치 확인

현재 `develop`이나 `main`이면 **먼저 작업 브랜치를 만든다.**

```bash
git switch -c feat/#357        # {type}/#{이슈번호}
```

## 3. 커밋

포맷: `#{이슈번호} [{type}] {한국어 설명}`

```bash
git commit -m "#357 [feat] 게시글 목록 조회에 정렬 파라미터 추가"
```

본문이 필요하면 제목 다음 빈 줄 뒤에 적는다.

```bash
git commit -m "$(cat <<'EOF'
#357 [feat] 게시글 목록 조회에 정렬 파라미터 추가

정렬 기준이 늘어날 것을 대비해 `BoardSortType` enum으로 분리했다.
EOF
)"
```

type: `feat` / `fix` / `refactor` / `test` / `chore` / `style` / `delete`

> **`Co-Authored-By: Claude ...` 트레일러를 붙이지 않는다.** 커밋 메시지는 제목과 본문만으로 구성한다.

## 4. 푸시 (사용자가 명시적으로 요청할 때만)

- **사용자가 요청하기 전에는 푸시하지 않는다.**
- **`git push --force`는 사용하지 않는다.**
- `main`/`develop`에 직접 푸시하지 않는다.

```bash
git push -u origin feat/#357
```

## 5. PR 생성

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

규칙:
- **PR 대상은 항상 `develop`**이다 (`main` 아님).
- 제목은 `#{이슈번호} [{Type}] {한국어 설명}` — **이슈 제목 앞에 `#{이슈번호} `를 붙인 것**이다. 번호를 빠뜨리거나(`[Feat] ...`) 뒤에 붙이지(`... (#357)`) 않는다. 커밋과 달리 `Type`은 첫 글자가 대문자다.
- **라벨과 assignee를 반드시 설정한다** — 유형 라벨(`feat`/`chore`/`⚒️ Fix`/`🔥 Refactor`) + 본인 이름 라벨(`예찬`) + `--assignee @me`. 상세는 `git-workflow`의 "라벨 & Assignee" 참조.
- 본문은 `.github/PULL_REQUEST_TEMPLATE.md`의 4개 섹션(Related Issue / Summary / Question & PR point / Postman)을 채운다.
- **`🤖 Generated with Claude Code` 서명을 붙이지 않는다.**
- `close #{이슈번호}`로 이슈를 연결한다.
- Postman 스크린샷은 사람이 첨부해야 한다. 첨부할 수 없으면 그 자리에 테스트 통과 결과를 남기고 사용자에게 알린다.
- **자기 PR을 스스로 머지하지 않는다.** 생성까지만 하고 리뷰를 기다린다.

## 6. 생성 후

PR을 만들면 `test.yml` 워크플로우가 `./gradlew --info test`를 실행한다. 결과를 확인하고 실패하면 사용자에게 보고한다.

```bash
gh pr checks            # CI 상태 확인
gh pr view --web        # 브라우저로 열기
```
