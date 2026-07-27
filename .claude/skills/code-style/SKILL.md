---
name: code-style
description: Java 코드를 작성하거나 수정할 때 로드. 네이밍·Lombok·Checkstyle 핵심 규칙을 담고, 포매팅/import 순서 상세와 엔티티·서비스 작성 관용구는 references/에 분리돼 있다.
---

# 코드 스타일 (daruda)

Java 17 + Lombok. **Naver Checkstyle**(`config/naver-checkstyle-rules.xml`, `maxWarnings = 0`) + **editorconfig**가 빌드에서 강제된다. 둘 중 하나라도 실패하면 `./gradlew build`가 깨진다.

## 상세 규칙 라우팅

| 무엇을 하려는가 | 읽을 파일 |
|-----------------|-----------|
| 포매팅 세부값, import 순서 예시, Checkstyle 위반 해결 | `references/formatting.md` |
| 엔티티·서비스 작성 관용구, 로깅, 주석 | `references/idioms.md` |

## 항상 지키는 것 (요약)

| 규칙 | 값 |
|------|-----|
| 들여쓰기 | **탭** (스페이스는 Checkstyle 위반) |
| 한 줄 최대 | 120자 |
| 파일 끝 | 개행 필수 (LF, UTF-8) |
| import 순서 | `java.` → `javax.` → `org.` → `net.` → `com.` → 기타, 그룹 사이 빈 줄 1개, **와일드카드 금지** |
| 중괄호 | K&R, 단일 문장에도 중괄호 필수 |

## 네이밍

| 대상 | 규칙 | 예 |
|------|------|-----|
| 클래스 | PascalCase | `BoardService` |
| 메서드/변수 | camelCase | `getBoardList` |
| 상수 | UPPER_SNAKE_CASE | `ALLOWED_EXTENSIONS` |
| JPA 엔티티 | 도메인 용어, **`Entity` 접미사 없음** | `Board`, `Tool`, `Comment` |
| 요청 DTO | `{동사}{대상}Request` | `CreateBoardRequest` |
| 응답 DTO | `{동사}{대상}Response` 또는 `{대상}Response` | `BoardResponse` |
| 리포지토리 | `{엔티티}Repository` | `BoardRepository` |
| 테스트 | `{대상}Test` | `BoardServiceTest` |
| DB 컬럼 | snake_case | `board_id`, `created_at` |
| URL 경로 변수 | kebab-case | `/{board-id}` |

**축약 금지**: `Res`/`Req`가 아니라 `Response`/`Request`. 연속 대문자 약어는 `AbbreviationAsWordInName` 규칙에 걸리므로 `ToolPlatForm` 같은 기존 오탈자를 따라 하지 말고 `HttpClient` 형태로 쓴다.

## Lombok

| 어노테이션 | 용도 |
|-----------|------|
| `@Getter` | 엔티티·DTO. **`@Setter`는 쓰지 않는다** (상태 변경은 도메인 메서드로) |
| `@RequiredArgsConstructor` | 서비스·컨트롤러의 생성자 주입. 필드는 `private final` |
| `@NoArgsConstructor(access = AccessLevel.PROTECTED)` | JPA 엔티티 필수 |
| `@Builder` | **생성자에만** 붙인다. 클래스 레벨과 중복 선언 금지 |
| `@Slf4j` | 로깅 |

**금지**: `@Data`(setter + equals/hashCode를 엔티티에 자동 생성), 엔티티에 `@AllArgsConstructor`, 클래스 레벨 `@Builder` + 생성자 `@Builder` 중복, `@Autowired` 필드 주입.

## 검증

```bash
./.claude/scripts/check-all.sh        # 전체 검증 (권장 · 순서는 이 스크립트가 SSOT)
```

| 도구 | 자동 수정 |
|------|-----------|
| editorconfig | **가능** — `./gradlew editorconfigFormat` |
| Checkstyle | **불가능** — `build/reports/checkstyle/main.html`을 보고 직접 고친다 |
