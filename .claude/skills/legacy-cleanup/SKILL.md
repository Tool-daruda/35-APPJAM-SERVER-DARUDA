---
name: legacy-cleanup
description: 지침 통일 이전에 작성된 기존 코드의 정리 대상 목록. 리팩터링 작업을 하거나, 기존 코드에서 규칙 위반을 발견해 "새로 도입된 문제인지 원래 있던 문제인지" 판정할 때 로드한다.
---

# 알려진 정리 대상 (daruda)

지침 통일 이전에 작성된 코드다. **새 코드는 현행 규칙을 따르되, 아래는 별도 리팩터링 작업으로 처리**한다. 관련 파일을 수정하다 아래 항목을 발견해도 **그 김에 함께 고치지 않는다.** 정리가 필요해 보이면 사용자에게 알리고 승인을 받은 뒤 별도 작업으로 진행한다(아래 "리팩터링 진행 원칙" 참조).

> **판정 기준:** 코드 리뷰나 진단 중 아래 항목을 발견하면 **이번 변경이 새로 도입한 것인지 원래 있던 것인지 구분**해서 보고한다. 기존 항목은 Critical이 아니라 정리 대상으로 분류한다.

| 항목 | 현황 | 목표 |
|------|------|------|
| 응답 래퍼 이원화 | `ApiResponse`(7개 컨트롤러) / `SuccessResponse`(4개) 병존 | `SuccessResponse`로 일원화 후 `ApiResponse` 삭제 |
| DTO 축약 접미사 | `ToolDetailGetRes`, `CategoryRes`, `BoardRes` 등 (`dto/res`, `dto/req` 패키지) | `XxxResponse`/`XxxRequest` + `dto/response`, `dto/request` |
| 엔티티 접미사 | `CommentEntity`, `UserEntity`, `NotificationEntity`, `ReportEntity` | `Comment`, `User`, `Notification`, `Report` |
| 트랜잭션 import | `jakarta.transaction.Transactional`을 import하는 **17개 파일** — 리포지토리 `@Modifying` 12개 + 서비스 5개(`UserService`, `CommentService`, `AuthService`, `NotificationService`, `TokenService`). 이 파일들의 **애노테이션 선언 31개** 전부 속성 없는 bare 애노테이션이라 import 교체만으로 동작이 동일하다 | `org.springframework.transaction.annotation.Transactional` |
| 트랜잭션 범위 | 서비스 클래스 레벨 `@Transactional`(쓰기) 23곳. 이 중 `UserService`·`CommentService`는 jakarta import까지 겹쳐 있어, import만 바꾸면 여전히 조회도 쓰기 트랜잭션이다 | 클래스 `readOnly = true` + 쓰기 메서드만 `@Transactional` |
| ErrorCode 중복 | `E400009`·`E400012`·`E400013` 코드값 중복, `REFREH_TOKEN_EMPTY_ERROR` 오타 상수가 `REFRESH_TOKEN_EMPTY_ERROR`와 중복 | 코드값 유일화 + 오타 상수 제거 |
| HTTP 상태 불일치 | `ResponseEntity.ok()` + `SuccessCode.SUCCESS_CREATE(201)` → 바디는 201, 실제 응답은 200 | 생성 API는 `status(HttpStatus.CREATED)` |
| soft delete 컬럼 | `is_deleted`(comment) / `del_yn`(board) / hard delete(ToolLike) 혼재 | 컬럼명·전략 통일 |
| 미사용 코드 | `S3Service`(실사용은 `OciService`), `ApiResponse.ofFailure` | 삭제 |
| QueryDSL 위치 | `BoardService`가 `JPAQueryFactory`를 직접 사용 | 커스텀 리포지토리(`BoardRepositoryCustom`)로 분리 |
| `BaseTimeEntity` 타입 | `java.sql.Timestamp` | `LocalDateTime` |

## 트랜잭션 import 정리 시 주의

기계적으로 일괄 치환하지 않는다. 처리 순서와 정확한 사실관계는 `architecture` 스킬의 `references/transaction.md`가 SSOT다. 요약하면:

1. **bare `@Transactional`** → import 교체만으로 동작 동일 (안전)
2. **`TxType`·`rollbackOn` 사용** → Spring 속성으로 수동 매핑 필요
3. **클래스 레벨** → import 교체만으로는 실익 없음. `readOnly = true` 재배치까지 해야 한다

## 리팩터링 진행 원칙

- 한 번에 한 항목만 다룬다. 여러 항목을 한 PR에 섞지 않는다.
- 동작 변화가 없어야 한다 — 커밋 type은 `refactor`.
- 이름 변경(엔티티 접미사, DTO 축약)은 참조가 넓게 퍼져 있으므로 IDE 리네임 후 `./.claude/scripts/check-all.sh`로 전체 검증한다.
