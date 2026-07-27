---
description: 기존 도메인에 새 기능(Controller → Service → Repository)을 추가한다
argument-hint: <도메인명> <기능 설명>
---

# 새 기능 추가

`$ARGUMENTS`에 지정된 도메인에 기능을 추가한다. 기존 코드 컨벤션을 먼저 읽고 그것을 따른다.

## 0. 사전 확인

- 대상 도메인 패키지: `src/main/java/com/daruda/darudaserver/domain/{도메인}/`
- 해당 도메인의 기존 컨트롤러·서비스를 먼저 읽어 스타일을 파악한다.
- `architecture`, `code-style` 스킬을 로드한다.

## 1. 작성 순서

아래로 내려가며(안쪽부터) 작성한다.

### ① Entity (필요할 때만)
- `entity/{Xxx}.java` — **`Entity` 접미사 없음**
- `@NoArgsConstructor(PROTECTED)`, 생성자에 `@Builder`, 정적 팩토리 `of()`
- 연관관계는 `fetch = FetchType.LAZY`
- 상태 변경은 setter가 아니라 의도가 드러나는 도메인 메서드

### ② Repository
- `repository/{Xxx}Repository.java` — `JpaRepository<{Xxx}, Long>` 상속
- 복잡한 조회는 `@Query` 또는 QueryDSL 커스텀 인터페이스(`{Xxx}RepositoryCustom` + `Impl`)
- N+1이 예상되면 `fetch join` 또는 배치 조회 쿼리를 함께 만든다

### ③ DTO
- `dto/request/{동사}{대상}Request.java` — `record` + Bean Validation
- `dto/response/{동사}{대상}Response.java` — `record` + 정적 팩토리 `from()`
- **축약형(`Req`/`Res`) 금지**

### ④ Service
- `service/{Xxx}Service.java`
- 클래스: `@Service @RequiredArgsConstructor @Transactional(readOnly = true)`
- 쓰기 메서드에만 `@Transactional` 재선언
- `import org.springframework.transaction.annotation.Transactional` (**jakarta 아님**)
- 예외는 `throw new NotFoundException(ErrorCode.XXX)` 형태

### ⑤ Controller
- `controller/{Xxx}Controller.java`
- `@Operation` / `@Parameter` 로 Swagger 문서화
- 응답은 `ResponseEntity<SuccessResponse<T>>`
- 생성 API는 `HttpStatus.CREATED` + `SuccessCode.SUCCESS_CREATE` (상태 일치)
- 인증 불필요하면 `@DisableSwaggerSecurity` + **`SecurityConfig`의 `WHITE_LIST`에도 등록**

### ⑥ ErrorCode (필요할 때)
- `global/error/code/ErrorCode.java`에 추가
- **코드값 중복 확인 필수**: `grep -o '"E[0-9]\{6\}"' src/main/java/com/daruda/darudaserver/global/error/code/ErrorCode.java | sort | uniq -d`

### ⑦ Test
- `testing` 스킬을 따른다. `test-writer` 에이전트에 위임해도 된다.
- 서비스 테스트(정상 + 예외 경로)는 필수, 컨트롤러 테스트는 파라미터 위임 검증 중심

## 2. 검증

```bash
./.claude/scripts/check-all.sh
```

## 3. 보고

생성/수정한 파일 목록과 추가된 API 엔드포인트를 한국어로 요약해 보고한다. 커밋은 **사용자가 요청할 때만** 한다.
