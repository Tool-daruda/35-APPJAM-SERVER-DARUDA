---
description: 프로젝트 ISSUE_TEMPLATE에 따라 GitHub 이슈를 생성한다
argument-hint: <이슈 유형과 내용>
---

# GitHub 이슈 생성

> **언어**: 이슈 제목·본문과 사용자 대상 응답은 모두 한국어로 작성한다.
> **컨벤션**: 제목 포맷·유형 라벨·템플릿 매핑은 `git-convention` 스킬이 SSOT다. 먼저 로드한다.

**사용자가 요청하지 않았는데 임의로 이슈를 만들지 않는다.**

## 1. 유형 판정

`git-convention`의 "라벨 & Assignee" 표에서 작업 성격 → 템플릿 · 제목 접두사 · 유형 라벨을 고른다.
(`.github/ISSUE_TEMPLATE/`의 4개 템플릿에 대응한다.)

## 2. 본문 작성

**Feature** — 4단 구조:

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

**Fix / Chore / Other**는 `### 목적` 섹션만 있으므로 간결하게 작성하고, 필요하면 체크리스트를 덧붙인다.

- 작업 상세 내용은 **체크박스 목록**으로 쪼갠다. 그대로 커밋 단위가 된다.
- 코드 식별자는 백틱으로 감싼다.

## 3. 생성 — HITL 지점

**실행 전에 제목·유형 라벨·이름 라벨·assignee·본문을 제시하고 승인을 받는다.**

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

라벨·assignee 값과 규칙은 `git-convention`의 "라벨 & Assignee" 참조.

## 4. 생성 후

- 브랜치 이름과 뒤이을 PR 제목은 이 이슈 번호에서 파생된다 — 규칙은 `git-convention`의 "브랜치 전략", "제목 포맷 3종" 참조.
- PR에도 **동일한 라벨과 assignee**를 설정한다 (`/commit-push-pr`).

생성한 이슈 번호와 URL을 한국어로 보고한다.
