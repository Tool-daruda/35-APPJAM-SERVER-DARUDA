# daruda (다루다)

35기 AND SOPT 앱잼 웹 부문 백엔드 서버. 대학생을 위한 툴 정보 탐색 + 커뮤니티 서비스.
베이스 패키지: `com.daruda.darudaserver`

> **언어**: 사용자에게 보이는 모든 응답은 예외 없이 한국어로 작성한다(코드, 식별자, 로그 메시지는 제외).

## 작업별 로딩 (작업 전)

세부 규칙은 **스킬**(`.claude/skills/<name>/SKILL.md`), 절차는 **커맨드**(`.claude/commands/`)에 있다. 아래 작업이 감지되면 해당 스킬을 로드한다(수동 호출: `/<name>`).

| 작업 유형 | 로드 |
|-----------|------|
| 레이어 구조, 패키지 배치, 트랜잭션 경계, DTO 매핑, 응답 포맷, Swagger | `architecture` |
| Java 코드 작성, 네이밍/포매팅, Lombok, Checkstyle | `code-style` |
| 테스트 작성 (JUnit 5 / Mockito / AssertJ) | `testing` |
| 예외/에러 처리 | `error-handling` |
| 브랜치/커밋/PR 컨벤션 | `git-workflow` |
| 커밋 · 푸시 · PR 생성 | `commit-push-pr` |
| GitHub 이슈 생성 | `create-issue` |
| PR 리뷰 코멘트 반영 및 답변 | `resolve-review` |
| 기존 도메인에 기능 추가 | `/new-feature` |
| PR 전 전체 검증 | `/run-checks` |

> 실패하는 테스트 진단(수정하지 않음)은 `test-validator` 에이전트, 코드 리뷰는 `code-reviewer`, 테스트 작성은 `test-writer`에 위임한다.

## 기술 스택

| 영역 | 기술 |
|------|------|
| WAS | Spring Boot 3.4.1 / JDK 17 / Gradle (Groovy DSL, 단일 모듈) |
| DB / 캐시 | MySQL (JPA·Hibernate, QueryDSL 5.0) / Redis |
| 검색 | Elasticsearch 8.11 (Spring Data Elasticsearch) |
| 스토리지 | OCI Object Storage (presigned URL 발급) |
| 외부 연동 | OpenFeign (카카오 OAuth) |
| 인증 | OAuth 2.0 (Kakao) + JWT / Refresh Token → Redis |
| 알림 | SSE (SseEmitter) |
| 설정 암호화 | Jasypt |
| 모니터링 | Sentry + Discord Appender (logback) |
| 테스트 & 린트 | JUnit 5 / Mockito / AssertJ / Checkstyle(naver rules) / editorconfig |

## 프로젝트 구조

**단일 모듈 + 도메인별 계층형(layered) 패키지** 구조다. 헥사고날/멀티모듈이 아니다.

```
src/main/java/com/daruda/darudaserver/
├── domain/{domain}/              # admin, comment, community, notification, report, search, tool, user
│   ├── controller/               # REST Controller (Swagger 어노테이션 포함)
│   ├── service/                  # 비즈니스 로직 + 트랜잭션 경계
│   ├── repository/               # Spring Data JPA Repository (+ QueryDSL 커스텀)
│   ├── entity/                   # JPA 엔티티 (+ enums/ 하위 패키지)
│   ├── dto/request/              # 요청 DTO (record)
│   ├── dto/response/             # 응답 DTO (record)
│   └── event/                    # 도메인 이벤트 (ApplicationEventPublisher)
└── global/
    ├── annotation/               # @DisableSwaggerSecurity
    ├── auth/                     # jwt(provider/service/repository), security(filter/handler), cookie, client
    ├── common/entity/            # BaseTimeEntity
    ├── common/response/          # ScrollPaginationCollection, ScrollPaginationDto
    ├── config/                   # Security, Cors, Redis, Jpa, Swagger, Elasticsearch, Feign, Jasypt, Oci, Sentry
    ├── error/code/               # ErrorCode, SuccessCode
    ├── error/dto/                # SuccessResponse, ErrorResponse
    ├── error/exception/          # BusinessException + 하위 예외
    ├── handler/                  # GlobalExceptionHandler, ValidatorUtil
    ├── image/                    # 이미지 업로드/삭제 (controller/service/repository/entity/dto)
    └── oci/                      # OciService (Object Storage)
```

**의존성 방향:** `controller` → `service` → `repository` → `entity`. 컨트롤러가 리포지토리를 직접 호출하지 않는다.
**도메인 간 참조:** 다른 도메인의 `Service`를 주입해 사용한다(예: `CommentService` → `NotificationService`). 리포지토리를 도메인 경계 너머로 직접 주입하는 것은 지양한다.

## 핵심 제약

### ❌ 금지

| 제약 | 이유 |
|------|------|
| `jakarta.transaction.Transactional` import | Jakarta 쪽에는 `readOnly` 속성이 **아예 없어서** 지정하면 컴파일 에러이고, 전파는 `propagation`이 아니라 `value = TxType.*`이다. 결국 조회에도 쓰기 트랜잭션(`TxType.REQUIRED`)이 열린다. 반드시 `org.springframework.transaction.annotation.Transactional`을 쓴다 |
| `ApiResponse` 신규 사용 | 응답 래퍼는 `SuccessResponse`/`ErrorResponse`로 통일 (아래 "응답 포맷" 참조) |
| DTO 이름에 `Res`/`Req` 축약 접미사 | `XxxRequest`/`XxxResponse` 풀네임으로 통일 |
| JPA 엔티티 이름에 `Entity` 접미사 | 도메인 용어 그대로 쓴다 (`Board`, `Tool`, `Comment`) |
| `RuntimeException` 직접 throw | `BusinessException` 하위 클래스 + `ErrorCode`만 사용 (`error-handling`) |
| 컨트롤러에서 리포지토리 직접 호출 | 레이어 경계 위반 |
| 엔티티를 컨트롤러 응답으로 직접 반환 | 응답 DTO(record)로 변환해서 반환 |
| 서비스에서 `HttpServletRequest`/`ResponseEntity` 등 웹 타입 사용 | 웹 의존은 컨트롤러 레이어에만 |
| 새 `ErrorCode` 추가 시 기존 코드값 재사용 | 코드값(`E4xxxxx`)은 전역 유일해야 한다 |
| `.env`·`application-*.yml`의 시크릿 하드코딩 커밋 | 환경변수 또는 Jasypt 암호화로 주입 |
| `@Builder`를 클래스 레벨과 생성자에 중복 선언 | 생성자(또는 정적 팩토리) 한 곳에만 붙인다 |

### ✅ 필수

| 실천 사항 | 이유 |
|-----------|------|
| 서비스 클래스에 `@Transactional(readOnly = true)`, 쓰기 메서드에만 `@Transactional` | 불필요한 쓰기 트랜잭션 방지 |
| 응답은 `SuccessResponse<T>`로 감싸고, HTTP 상태는 `SuccessCode`와 일치시킨다 | 201 코드인데 200으로 응답하는 불일치 방지 |
| 에러는 `throw new XxxException(ErrorCode.YYY)` — 컨트롤러에서 에러 응답을 조립하지 않는다 | `GlobalExceptionHandler`가 일괄 처리 |
| 요청/응답 DTO는 `record` + Bean Validation(`@Valid`) | 불변성 + 선언적 검증 |
| 엔티티 생성은 정적 팩토리 `of(...)` / `create(...)`, 기본 생성자는 `@NoArgsConstructor(access = PROTECTED)` | 무분별한 생성 방지 |
| 컨트롤러 메서드에 `@Operation`, 파라미터에 `@Parameter`; 미인증 API에는 `@DisableSwaggerSecurity` | Swagger 문서 일관성 |
| 들여쓰기는 **탭**, 한 줄 최대 120자, 파일 끝 개행 | Naver Checkstyle + editorconfig 강제 |
| import는 `java.` → `javax.` → `org.` → `net.` → `com.` 순서, 그룹 사이 빈 줄 1개, 와일드카드 금지 | Checkstyle `ImportOrder` |
| 커밋 메시지는 `#이슈번호 [type] 한국어 설명`, `Co-Authored-By: Claude` 트레일러는 붙이지 않는다 | 컨벤션 (`git-workflow`) |
| 제목 포맷: 이슈 `[Type] 설명` (번호 없음) / PR `#이슈번호 [Type] 설명` / 커밋 `#이슈번호 [type] 설명` (소문자) | 세 포맷이 다르다 (`git-workflow`) |
| 이슈·PR에 유형 라벨(`feat`/`chore`/`⚒️ Fix`/`🔥 Refactor`) + 이름 라벨 + assignee 설정 | 팀 트래킹 컨벤션 (`git-workflow`) |

## 응답 포맷

성공은 `SuccessResponse<T>`, 실패는 `GlobalExceptionHandler`가 `ErrorResponse`로 자동 변환한다. 컨트롤러는 실패 응답을 직접 만들지 않는다.

```java
// 조회 (200)
@GetMapping("/{board-id}")
public ResponseEntity<SuccessResponse<BoardResponse>> getBoard(
	@Parameter(description = "board Id", example = "1")
	@PathVariable("board-id") final Long boardId
) {
	BoardResponse response = boardService.getBoard(boardId);
	return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_FETCH, response));
}

// 생성 (201) — SuccessCode가 CREATED면 HTTP 상태도 201이어야 한다
@PostMapping
public ResponseEntity<SuccessResponse<CreateBoardResponse>> createBoard(
	@AuthenticationPrincipal Long userId,
	@RequestBody @Valid CreateBoardRequest request
) {
	CreateBoardResponse response = boardService.createBoard(userId, request);
	return ResponseEntity.status(HttpStatus.CREATED)
		.body(SuccessResponse.of(SuccessCode.SUCCESS_CREATE, response));
}

// 데이터 없음
return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_DELETE));
```

인증된 사용자 ID는 `@AuthenticationPrincipal Long userId`로 받는다. 선택적 인증(비로그인 허용) API는 `Long userIdOrNull`처럼 nullable임이 드러나는 이름을 쓴다.

## 프로파일

| 프로파일 | 파일 | 용도 |
|----------|------|------|
| `local` | `application-local.yml` | 로컬 개발 |
| `dev` | `application-dev.yml` | 개발 서버 (CI/CD: `cd-dev.yml`) |
| `prod` | `application-prod.yml` | 운영 서버 (CI/CD: `cd-prod.yml`) |

시크릿은 환경변수 또는 Jasypt(`ENC(...)`)로 주입한다. 평문 커밋 금지.

## 명령어

```bash
./gradlew clean build                        # 전체 빌드 (테스트 + Checkstyle + editorconfig 포함)
./gradlew test                               # 전체 테스트
./gradlew test --tests "BoardServiceTest"    # 특정 클래스
./gradlew checkstyleMain checkstyleTest      # 코드 스타일 검증 (maxWarnings=0, 자동 수정 없음)
./gradlew editorconfigFormat                 # 개행/공백 자동 수정 (검증만: editorconfigCheck)
./gradlew bootRun --args='--spring.profiles.active=local'   # 로컬 실행
```

> 실행 전 `.env` / `docker-compose.yml`(MySQL·Redis·Elasticsearch)을 준비한다.
> PR 전 전체 검증은 `/run-checks` 또는 `./.claude/scripts/check-all.sh`.

## 알려진 정리 대상 (기존 코드)

지침 통일 이전에 작성된 코드로, **새 코드는 위 규칙을 따르되 아래는 별도 리팩터링 작업으로 처리**한다. 관련 파일을 수정할 일이 생기면 그 김에 함께 정리하는 것을 권장한다.

| 항목 | 현황 | 목표 |
|------|------|------|
| 응답 래퍼 이원화 | `ApiResponse`(7개 컨트롤러) / `SuccessResponse`(4개) 병존 | `SuccessResponse`로 일원화 후 `ApiResponse` 삭제 |
| DTO 축약 접미사 | `ToolDetailGetRes`, `CategoryRes`, `BoardRes` 등 (`dto/res`, `dto/req` 패키지) | `XxxResponse`/`XxxRequest` + `dto/response`, `dto/request` |
| 엔티티 접미사 | `CommentEntity`, `UserEntity`, `NotificationEntity`, `ReportEntity` | `Comment`, `User`, `Notification`, `Report` |
| 트랜잭션 import | `jakarta.transaction.Transactional` 17곳 (`readOnly` 지정 불가 → 조회도 쓰기 트랜잭션) | `org.springframework.transaction.annotation.Transactional` |
| 트랜잭션 범위 | 서비스 클래스 레벨 `@Transactional`(쓰기) 23곳 | 클래스 `readOnly = true` + 쓰기 메서드만 `@Transactional` |
| ErrorCode 중복 | `E400009`·`E400012`·`E400013` 코드값 중복, `REFREH_TOKEN_EMPTY_ERROR` 오타 상수가 `REFRESH_TOKEN_EMPTY_ERROR`와 중복 | 코드값 유일화 + 오타 상수 제거 |
| HTTP 상태 불일치 | `ResponseEntity.ok()` + `SuccessCode.SUCCESS_CREATE(201)` → 바디는 201, 실제 응답은 200 | 생성 API는 `status(HttpStatus.CREATED)` |
| soft delete 컬럼 | `is_deleted`(comment) / `del_yn`(board) / hard delete(ToolLike) 혼재 | 컬럼명·전략 통일 |
| 미사용 코드 | `S3Service`(실사용은 `OciService`), `ApiResponse.ofFailure` | 삭제 |
| QueryDSL 위치 | `BoardService`가 `JPAQueryFactory`를 직접 사용 | 커스텀 리포지토리(`BoardRepositoryCustom`)로 분리 |
| `BaseTimeEntity` 타입 | `java.sql.Timestamp` | `LocalDateTime` |
