---
name: create-issue
description: 프로젝트 ISSUE_TEMPLATE에 따라 GitHub 이슈를 생성할 때 로드. 유형별 템플릿 매핑과 제목/라벨 컨벤션을 자동 적용.
---

> **언어**: 이슈 제목·본문과 사용자 대상 응답은 모두 한국어로 작성한다.

# GitHub 이슈 생성 (daruda)

`.github/ISSUE_TEMPLATE/`의 4개 템플릿 중 작업 성격에 맞는 것을 골라 작성한다.

## 템플릿 매핑

| 작업 성격 | 템플릿 | 제목 접두사 | 라벨 |
|-----------|--------|-------------|------|
| 새 기능 개발 | 🛠 Feature | `[Feat]` | `feat` |
| 버그 수정 | 🔧 Fix | `[Fix]` | `fix` |
| 개발 환경·설정·의존성 | ⚙ Chore | `[Chore]` | `chore` |
| 그 외 (리팩터링, 스타일, 문서 등) | 🎨 Other | `[Refactor]`, `[Style]`, `[Docs]` 등 | 없음 |

## 본문 구조

**Feature**

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

## 생성

```bash
gh issue create \
  --title "[Feat] 게시글 목록 정렬 기능 추가" \
  --label feat \
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

## 규칙

- 제목은 `[Type] 한국어 설명` 형식. 접두사의 첫 글자는 대문자다(`[Feat]`, 커밋의 `[feat]`과 다름).
- 작업 상세 내용은 **체크박스 목록**으로 쪼갠다. 그대로 커밋 단위가 된다.
- 코드 식별자는 백틱으로 감싼다.
- 이슈를 만든 뒤 브랜치 이름은 `{type}/#{이슈번호}`가 된다 (`git-workflow` 참조).
- 사용자가 요청하지 않았는데 임의로 이슈를 만들지 않는다.
