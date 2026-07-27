# daruda (다루다)

35기 AND SOPT 앱잼 웹 부문 백엔드 서버. 대학생을 위한 툴 정보 탐색 + 커뮤니티 서비스.
베이스 패키지: `com.daruda.darudaserver`

> **언어**: 사용자에게 보이는 모든 응답은 예외 없이 한국어로 작성한다(코드, 식별자, 로그 메시지는 제외).

## 하네스 계층

이 저장소의 `.claude/`는 레이어드 아키텍처로 구성된다. **각 계층의 책임을 넘지 않는다.**

| 계층 | 위치 | 책임 | 예 |
|------|------|------|-----|
| **진입점** (Controller) | `.claude/commands/` | 사용자 요청을 받아 절차를 조율. 규칙을 복사하지 않고 스킬을 참조 | `/new-feature`, `/commit-push-pr` |
| **조율** (Service) | `.claude/agents/` | 컨텍스트 격리가 필요한 다단계 작업을 위임 | `code-reviewer`, `test-writer` |
| **지식** (Domain) | `.claude/skills/` | 단일 책임 규칙. 진입점은 `SKILL.md`, 상세는 `references/` | `architecture`, `code-style` |
| **실행** (Infra) | `.claude/scripts/`, `.claude/hooks/` | 결정된 절차의 캡슐화. 문서가 아닌 코드가 SSOT | `check-all.sh` |

원칙:

- **한 사실은 한 곳에만 둔다.** 문서 간 복사 금지 — 참조로 연결한다. 규칙을 고칠 때 두 곳을 고쳐야 한다면 구조가 잘못된 것이다.
- **절차는 커맨드, 규칙은 스킬.** 스킬에 워크플로우를 넣지 않는다.
- **전체 검증 절차는 스크립트가 SSOT다.** `check-all.sh`가 실행하는 단계와 순서를 문서에 개별 Gradle 명령으로 옮겨 적지 않는다. (특정 테스트 실행처럼 전체 검증이 아닌 단발성 명령은 예외 — "명령어" 절 참조.)

## 작업별 로딩

아래 작업이 감지되면 해당 스킬을 로드한다(수동 호출: `/<name>`). 스킬은 진입점만 읽고, `references/`는 **필요한 것만** 읽는다.

| 작업 유형 | 로드 |
|-----------|------|
| 레이어 구조, 패키지 배치, 트랜잭션 경계, DTO 매핑, 응답 포맷, Swagger | `architecture` |
| Java 코드 작성, 네이밍/포매팅, Lombok, Checkstyle | `code-style` |
| 테스트 작성 (JUnit 5 / Mockito / AssertJ) | `testing` |
| 예외/에러 처리 | `error-handling` |
| 브랜치/커밋/이슈/PR 이름·라벨 컨벤션 | `git-convention` |
| 기존 코드의 규칙 위반이 원래 있던 것인지 판정, 리팩터링 | `legacy-cleanup` |

| 절차 | 커맨드 |
|------|--------|
| 기존 도메인에 기능 추가 | `/new-feature` |
| PR 전 전체 검증 | `/run-checks` |
| 커밋 · 푸시 · PR 생성 | `/commit-push-pr` |
| GitHub 이슈 생성 | `/create-issue` |
| PR 리뷰 반영 및 답변 | `/resolve-review` |

> 실패하는 테스트 진단(수정하지 않음)은 `test-validator` 에이전트, 코드 리뷰는 `code-reviewer`, 테스트 작성은 `test-writer`에 위임한다.

## Human-in-the-Loop (HITL)

에이전트는 실행 중 질문할 수 있다. **되돌리기 어렵거나 정답이 없는 일은 묻고, 합의된 컨벤션은 알아서 한다.**

| 반드시 묻는다 | 알아서 한다 |
|---------------|-------------|
| `git push`, PR 생성, 이슈 생성 | 코드 작성·수정, 테스트 작성 |
| 커밋 (사용자가 요청하기 전에는 하지 않는다) | 검증 실행(`check-all.sh`), 포매팅 자동 수정 |
| 파일·코드 삭제, 스키마 변경 | 컨벤션에 따른 이름 결정, 패키지 배치 |
| 리뷰어 의도가 모호한 피드백 반영 | 이미 정해진 에러 코드·응답 포맷 적용 |
| 레거시 일괄 치환(`legacy-cleanup`) | 신규 코드에 현행 규칙 적용 |

**자기 PR을 스스로 머지하지 않는다.** 통과하지 않은 검증을 통과했다고 말하지 않는다.

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

**단일 모듈 + 도메인별 계층형(layered) 패키지.** 헥사고날/멀티모듈이 아니다.

```text
src/main/java/com/daruda/darudaserver/
├── domain/{domain}/              # admin, comment, community, notification, report, search, tool, user
│   ├── controller/ service/ repository/ entity/
│   ├── dto/request/ dto/response/     # record
│   └── event/                          # ApplicationEventPublisher
└── global/
    ├── auth/ config/ error/ handler/   # 인증, 설정, ErrorCode/응답, GlobalExceptionHandler
    ├── annotation/ common/             # @DisableSwaggerSecurity, BaseTimeEntity, 페이지네이션
    └── image/ oci/                     # 이미지 업로드, OCI Object Storage
```

**의존성 방향:** `controller` → `service` → `repository` → `entity`. 컨트롤러가 리포지토리를 직접 호출하지 않는다.
**도메인 간 참조:** 다른 도메인의 `Service`를 주입한다. 리포지토리를 도메인 경계 너머로 주입하지 않는다.

## 핵심 제약

배경과 예시는 각 스킬에 있다. 여기서는 위반 여부만 판정한다.

### ❌ 금지

| 제약 | 상세 |
|------|------|
| `jakarta.transaction.Transactional` import | `architecture` → `references/transaction.md` |
| `ApiResponse` 신규 사용 | 응답 래퍼는 `SuccessResponse`/`ErrorResponse`로 통일 |
| DTO 이름에 `Res`/`Req` 축약 접미사 | `XxxRequest`/`XxxResponse` 풀네임 |
| JPA 엔티티 이름에 `Entity` 접미사 | 도메인 용어 그대로 (`Board`, `Tool`, `Comment`) |
| `RuntimeException` 직접 throw | `BusinessException` 하위 + `ErrorCode`만 (`error-handling`) |
| 컨트롤러에서 리포지토리 직접 호출 | 레이어 경계 위반 |
| 엔티티를 컨트롤러 응답으로 직접 반환 | 응답 DTO(record)로 변환 |
| 서비스에서 `HttpServletRequest`/`ResponseEntity` 사용 | 웹 의존은 컨트롤러 레이어에만 |
| 새 `ErrorCode`에 기존 코드값 재사용 | 코드값(`E4xxxxx`)은 전역 유일 |
| `.env`·`application-*.yml`의 시크릿 하드코딩 커밋 | 환경변수 또는 Jasypt로 주입 |
| `@Builder`를 클래스 레벨과 생성자에 중복 선언 | 생성자(또는 정적 팩토리) 한 곳에만 |

### ✅ 필수

| 실천 사항 | 상세 |
|-----------|------|
| 서비스 클래스에 `@Transactional(readOnly = true)`, 쓰기 메서드에만 `@Transactional` | `architecture` → `references/transaction.md` |
| 응답은 `SuccessResponse<T>`로 감싸고 HTTP 상태를 `SuccessCode`와 일치 | `architecture` → `references/web-layer.md` |
| 에러는 `throw new XxxException(ErrorCode.YYY)` — 컨트롤러에서 조립하지 않는다 | `error-handling` |
| 요청/응답 DTO는 `record` + Bean Validation(`@Valid`) | `architecture` → `references/web-layer.md` |
| 엔티티 생성은 정적 팩토리 `of()`/`create()`, 기본 생성자는 `@NoArgsConstructor(PROTECTED)` | `code-style` → `references/idioms.md` |
| 컨트롤러에 `@Operation`/`@Parameter`, 미인증 API는 `SecurityConfig.WHITE_LIST`에도 등록 | `architecture` → `references/web-layer.md` |
| 들여쓰기 **탭**, 한 줄 120자, 파일 끝 개행, import 그룹 순서 | `code-style` → `references/formatting.md` |
| 커밋 메시지 `#이슈번호 [type] 한국어 설명`, 서명 트레일러 금지 | `git-convention` |

## 명령어

```bash
./.claude/scripts/check-all.sh                              # 전체 검증 (PR 전 · 순서는 이 스크립트가 SSOT)
./gradlew test --tests "BoardServiceTest"                   # 특정 테스트 클래스
./gradlew bootRun --args='--spring.profiles.active=local'   # 로컬 실행
```

> 실행 전 `.env` / `docker-compose.yml`(MySQL·Redis·Elasticsearch)을 준비한다.

## 프로파일

| 프로파일 | 파일 | 용도 |
|----------|------|------|
| `local` | `application-local.yml` | 로컬 개발 |
| `dev` | `application-dev.yml` | 개발 서버 (CI/CD: `cd-dev.yml`) |
| `prod` | `application-prod.yml` | 운영 서버 (CI/CD: `cd-prod.yml`) |

시크릿은 환경변수 또는 Jasypt(`ENC(...)`)로 주입한다. **평문 커밋 금지.**
