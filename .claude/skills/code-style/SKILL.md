---
name: code-style
description: Java 코드를 작성하거나 수정할 때 로드. 네이밍 컨벤션, 패키지 구조, 포매팅(탭/120자), import 순서, Lombok 사용 규칙, 엔티티 작성 관용구, Checkstyle 규칙.
---

# 코드 스타일 (daruda)

Java 17 + Lombok. **Naver Checkstyle**(`config/naver-checkstyle-rules.xml`, `maxWarnings = 0`) + **editorconfig**가 빌드에서 강제된다. 둘 중 하나라도 실패하면 `./gradlew build`가 깨진다.

## 포매팅 (자동 검증됨)

| 규칙 | 값 |
|------|-----|
| 들여쓰기 | **탭** (스페이스 들여쓰기는 Checkstyle 위반) |
| 탭 너비 | 4 |
| 한 줄 최대 길이 | 120자 (package/import/URL 제외) |
| 파일 끝 | 개행 필수 |
| 개행 문자 | LF |
| 인코딩 | UTF-8 |
| 줄 끝 공백 | 금지 |
| 중괄호 | K&R 스타일 (`if (x) {` — 여는 중괄호 같은 줄) |
| 단일 문장 | 중괄호 필수 (`if (x) return;` 금지) |
| 한 줄에 한 문장 | 필수 |

## import 순서

그룹 순서: `java.` → `javax.` → `org.` → `net.` → `com.` → 기타. **그룹 사이에 빈 줄 1개**, 그룹 내부는 알파벳순, 그룹 내부에 빈 줄 금지. **와일드카드(`*`) import 금지** (테스트의 static import는 예외적으로 사용 중).

```java
package com.daruda.darudaserver.domain.board.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.board.entity.Board;
import com.daruda.darudaserver.global.error.code.ErrorCode;

import lombok.RequiredArgsConstructor;
```

> `lombok.`, `jakarta.`는 `com.` 뒤 "기타" 그룹에 위치한다. 기존 파일들의 배치를 따르면 된다.

## 네이밍

| 대상 | 규칙 | 예 |
|------|------|-----|
| 클래스 | PascalCase | `BoardService` |
| 메서드/변수 | camelCase | `getBoardList` |
| 상수 | UPPER_SNAKE_CASE | `ALLOWED_EXTENSIONS` |
| 패키지 | 소문자 단일어 | `com.daruda.darudaserver.domain.board` |
| JPA 엔티티 | 도메인 용어, **`Entity` 접미사 없음** | `Board`, `Tool`, `Comment` |
| 요청 DTO | `{동사}{대상}Request` | `CreateBoardRequest` |
| 응답 DTO | `{동사}{대상}Response` 또는 `{대상}Response` | `BoardResponse`, `CreateBoardResponse` |
| 리포지토리 | `{엔티티}Repository` | `BoardRepository` |
| 테스트 | `{대상}Test` | `BoardServiceTest` |
| DB 컬럼 | snake_case | `board_id`, `created_at` |
| URL 경로 변수 | kebab-case | `/{board-id}` |

**축약 금지**: `Res`/`Req`가 아니라 `Response`/`Request`. 연속 대문자 약어는 `AbbreviationAsWordInName` 규칙에 걸리므로 `ToolPlatForm` 같은 기존 오탈자를 따라 하지 말고 `HttpClient` 형태로 쓴다.

## Lombok 사용 규칙

| 어노테이션 | 용도 |
|-----------|------|
| `@Getter` | 엔티티·DTO. **`@Setter`는 쓰지 않는다** (상태 변경은 도메인 메서드로) |
| `@RequiredArgsConstructor` | 서비스·컨트롤러의 생성자 주입. 필드는 `private final` |
| `@NoArgsConstructor(access = AccessLevel.PROTECTED)` | JPA 엔티티 필수 |
| `@Builder` | 생성자에만 붙인다. 클래스 레벨과 중복 선언 금지 |
| `@Slf4j` | 로깅. `log.debug/info/warn/error` |

**금지**: `@Data`(setter + equals/hashCode를 엔티티에 자동 생성), `@AllArgsConstructor`를 엔티티에 사용, 클래스 레벨 `@Builder` + 생성자 `@Builder` 중복.

## 엔티티 작성 관용구

```java
@Getter
@Entity
@Table(name = "board")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE board SET is_deleted = true, deleted_at = NOW() WHERE board_id = ?")
@SQLRestriction("is_deleted = false")
public class Board extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "board_id")
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, length = 10_000)
	private String content;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted = false;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)               // 연관관계는 항상 LAZY
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Builder                                          // 생성자에만
	private Board(final String title, final String content, final User user) {
		this.title = title;
		this.content = content;
		this.user = user;
	}

	public static Board of(final String title, final String content, final User user) {
		return Board.builder()
			.title(title)
			.content(content)
			.user(user)
			.build();
	}

	public void updateContent(final String content) {   // setter 대신 의도가 드러나는 메서드
		this.content = content;
	}
}
```

규칙:
- `@ManyToOne`/`@OneToOne`은 **항상 `fetch = FetchType.LAZY`** (기본값이 EAGER라 N+1의 주원인).
- 생성은 정적 팩토리(`of`/`create`)로 노출하고, 빌더는 `private` 생성자에 둔다.
- 검증이 필요한 생성은 `create`(예: 필수 연관 엔티티 null 검사 후 예외), 단순 생성은 `of`를 쓴다.
- soft delete는 `@SQLDelete` + `@SQLRestriction`. 플래그 컬럼은 `is_deleted`, 시각은 `deleted_at`.
- 시간 필드는 `LocalDateTime`을 쓴다(`java.sql.Timestamp` 신규 사용 금지).
- 공통 생성/수정 시각은 `BaseTimeEntity`를 상속해서 얻는다.

## 서비스 작성 관용구

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

	private final BoardRepository boardRepository;
	private final UserRepository userRepository;

	@Transactional
	public CreateBoardResponse createBoard(final Long userId, final CreateBoardRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

		Board board = boardRepository.save(Board.of(request.title(), request.content(), user));
		return CreateBoardResponse.from(board);
	}
}
```

- 필드는 `private final` + 생성자 주입(`@RequiredArgsConstructor`). **`@Autowired` 필드 주입 금지.**
- 메서드 파라미터에 `final`을 붙이는 것이 이 코드베이스의 관용이다(강제는 아니지만 신규 코드에서 지킨다).
- `Optional`은 `orElseThrow`로 즉시 풀어 예외로 전환한다. `get()` 호출 금지.
- 지역 변수 타입은 명시한다(`var` 사용은 이 코드베이스에 없다).

## 로깅

```java
log.debug("게시글 조회: boardId={}", boardId);          // 파라미터는 {} 플레이스홀더
log.error("이미지 삭제 실패: {}", image.getImageUrl(), e);  // 예외는 마지막 인자
```

- 문자열 연결(`"a" + b`) 대신 `{}`를 쓴다.
- 예상된 비즈니스 예외는 `debug`, 복구 가능한 이상은 `warn`, 시스템 오류는 `error`.
- 토큰·비밀번호·개인정보는 로그에 남기지 않는다.

## 주석

- **무엇을**이 아니라 **왜**를 적는다. 코드를 그대로 읊는 주석(`// 사용자 조회`)은 지운다.
- 한국어로 쓴다.
- 임시 회피는 `// TODO: #이슈번호 설명` 형식으로 남긴다.

## 검증 명령

```bash
./gradlew editorconfigFormat              # 개행/공백/인코딩 자동 수정
./gradlew editorconfigCheck               # 탭/개행/인코딩 확인
./gradlew checkstyleMain checkstyleTest   # 스타일 위반 확인 (경고 0 요구)
./gradlew build                           # 위 전부 + 테스트
```

| 도구 | 자동 수정 |
|------|-----------|
| editorconfig | **가능** — `./gradlew editorconfigFormat` |
| Checkstyle | **불가능** — 리포트를 보고 직접 고친다 |

Checkstyle 위반은 `build/reports/checkstyle/main.html`(테스트는 `test.html`)에서 지적 위치를 확인해 수정한다. IDE에서는 `.editorconfig`가 자동 적용되므로 탭 들여쓰기는 대부분 자동으로 맞는다.
