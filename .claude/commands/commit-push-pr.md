---
description: 프로젝트 컨벤션에 따라 커밋하고, 요청 시 푸시 및 PR을 생성한다
---

# 커밋 · 푸시 · PR

> **언어**: 사용자 대상 응답은 모두 한국어로 작성한다.
> **컨벤션**: 제목 포맷·라벨·커밋 type은 `git-convention` 스킬이 SSOT다. 먼저 로드한다.

## 1. 검증 (필수)

```bash
./.claude/scripts/check-all.sh
```

통과하지 못하면 **커밋하지 않는다.** 실패 단계와 실제 출력을 보고한다.

## 2. 스테이징 확인

```bash
git status
```

- **`.env`, `private.pem`, 시크릿이 담긴 yml은 절대 커밋하지 않는다.**
- 커밋 분리 단위는 `git-convention`의 "커밋 type" 절 참조.

## 3. 브랜치 확인

현재 `develop`이나 `main`이면 **먼저 작업 브랜치를 만든다.** 직접 커밋하지 않는다.

```bash
git switch -c feat/#357
```

브랜치 이름 형식은 `git-convention`의 "브랜치 전략" 참조.

## 4. 커밋 — HITL 지점

**커밋 전에 확정된 내용을 제시하고 승인을 받는다.** 사용자가 커밋을 요청했더라도, 아래를 보여준 뒤 진행한다.

- 최종 스테이징 파일 목록 (`git status --short`)
- 커밋 메시지 전문

포맷은 `git-convention` 참조. 한 줄이면:

```bash
git commit -m "#357 [feat] 게시글 목록 조회에 정렬 파라미터 추가"
```

본문이 필요하면:

```bash
git commit -m "$(cat <<'EOF'
#357 [feat] 게시글 목록 조회에 정렬 파라미터 추가

정렬 기준이 늘어날 것을 대비해 `BoardSortType` enum으로 분리했다.
EOF
)"
```

> 서명 트레일러를 붙이지 않는다 — `git-convention`의 "커밋 서명 금지" 참조.

## 5. 푸시 — HITL 지점

**사용자가 명시적으로 요청하기 전에는 푸시하지 않는다.** 요청받았으면 **대상 원격과 브랜치를 제시하고 승인을 받은 뒤** 실행한다.

```bash
git push -u origin feat/#357
```

**`--force`는 사용하지 않는다. `main`/`develop`에 직접 푸시하지 않는다.**

## 6. PR 생성 — HITL 지점

**사용자가 명시적으로 요청할 때만** 만든다. 실행 전에 **제목·본문·라벨·assignee를 제시하고 승인을 받는다.**

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

- 대상 브랜치·라벨(유형 + 이름)·assignee 규칙은 `git-convention`의 "브랜치 전략", "라벨 & Assignee" 참조.
- 본문은 `.github/PULL_REQUEST_TEMPLATE.md`의 4개 섹션을 채운다.
- `close #{이슈번호}`로 이슈를 연결한다.
- Postman 스크린샷은 사람이 첨부해야 한다. 첨부할 수 없으면 그 자리에 테스트 통과 결과를 남기고 사용자에게 알린다.
- **자기 PR을 스스로 머지하지 않는다.**

## 7. 생성 후

PR을 만들면 `test.yml` 워크플로우가 `./gradlew --info test`를 실행한다.

```bash
gh pr checks            # CI 상태 확인
gh pr view --web        # 브라우저로 열기
```

실패하면 사용자에게 보고한다. 통과하지 않은 것을 통과했다고 말하지 않는다.
