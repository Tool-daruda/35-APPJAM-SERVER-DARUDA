---
name: architecture
description: 레이어 구조를 설계하거나 새 기능의 코드 배치를 정할 때 로드. 계층별 책임과 패키지 배치를 담고, 트랜잭션 경계·웹 레이어(DTO/응답/Swagger)·연동(페이지네이션/이벤트) 상세는 references/에 분리돼 있다.
---

# 아키텍처 (daruda)

**단일 모듈 + 도메인별 계층형(layered) 구조.** 헥사고날/포트&어댑터가 아니다. 새 코드를 어디에 둘지 이 문서로 판단한다.

## 상세 규칙 라우팅

이 파일은 진입점이다. **작업에 해당하는 것을 모두 읽는다.** 트랜잭션·DTO·연동을 한꺼번에 다루는 작업이면 세 파일을 다 읽는다. 해당하지 않는 파일만 읽지 않는다.

| 무엇을 정하려는가 | 읽을 파일 |
|-------------------|-----------|
| `@Transactional` 위치, readOnly, 전파, jakarta vs spring | `references/transaction.md` |
| DTO 매핑, 요청 검증, 응답 포맷, 컨트롤러·Swagger 작성 | `references/web-layer.md` |
| 커서 페이지네이션, 도메인 이벤트, 도메인 간 참조 | `references/integration.md` |

## 레이어별 책임

```text
Controller  → HTTP 경계. 요청 검증(@Valid), DTO ↔ 서비스 호출, 응답 래핑, Swagger 문서
Service     → 비즈니스 로직, 트랜잭션 경계, 도메인 간 조율, 엔티티 → 응답 DTO 변환
Repository  → 영속성. Spring Data JPA 메서드 + @Query + QueryDSL 커스텀 구현
Entity      → 상태 + 상태를 바꾸는 도메인 메서드 (setter 금지)
```

**호출 방향은 단방향이다.** `Controller → Service → Repository → Entity`.

| 금지 | 이유 |
|------|------|
| 컨트롤러가 리포지토리 직접 호출 | 트랜잭션 경계 밖에서 영속성 접근 |
| 서비스가 `ResponseEntity`·`HttpServletRequest` 사용 | 웹 의존이 비즈니스 레이어로 새어 나감 |
| 엔티티를 응답으로 직접 반환 | 순환 참조·지연 로딩 예외·필드 노출 |
| 리포지토리에 비즈니스 분기 | 조건 판단은 서비스에서 |

## 패키지 배치

```text
domain/{domain}/
├── controller/{Xxx}Controller.java
├── service/{Xxx}Service.java
├── repository/{Xxx}Repository.java          # + {Xxx}RepositoryCustom/Impl (QueryDSL)
│   └── projection/                          # 조회 전용 projection 인터페이스/record
├── entity/{Xxx}.java                        # 접미사 Entity 붙이지 않음
│   └── enums/                               # 도메인 enum
├── dto/request/{동사}{대상}Request.java
├── dto/response/{동사}{대상}Response.java
└── event/{Xxx}Event.java                    # ApplicationEvent
```

도메인에 속하지 않는 횡단 관심사(인증, 이미지, 설정, 예외)는 `global/` 아래로 간다.

## 판단 기준 요약

- 트랜잭션은 **서비스에서 시작**한다. 컨트롤러·리포지토리에 붙이지 않는다.
- 다른 도메인이 필요하면 그 도메인의 **`Service`를 주입**한다. 리포지토리를 도메인 경계 너머로 주입하지 않는다.
- 응답은 항상 DTO(`record`)로 감싼다. 필드가 하나뿐이어도 마찬가지다.
- 컨트롤러 메서드는 얇게 유지한다. 서비스 호출 → 응답 래핑, 3줄이 기본형이다.
