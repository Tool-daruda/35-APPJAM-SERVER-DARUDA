---
name: architecture
description: Load when designing the layer structure or deciding where new-feature code goes. Holds per-layer responsibilities and package placement; details on transaction boundaries, the web layer (DTO/response/Swagger), and integration (pagination/events) are split into references/.
---

# Architecture (daruda)

**Single module + per-domain layered structure.** Not hexagonal / ports & adapters. Use this doc to decide where new code goes.

## Detailed-rule routing

This file is the entry point. **Read everything relevant to your task.** If the task touches transactions, DTOs, and integration at once, read all three files. Only skip the files that do not apply.

| What you are deciding | File to read |
|-----------------------|--------------|
| `@Transactional` placement, readOnly, propagation, jakarta vs spring | `references/transaction.md` |
| DTO mapping, request validation, response format, writing controllers & Swagger | `references/web-layer.md` |
| Cursor pagination, domain events, cross-domain references | `references/integration.md` |

## Per-layer responsibilities

```text
Controller  → HTTP boundary. Request validation (@Valid), DTO ↔ service calls, response wrapping, Swagger docs
Service     → business logic, transaction boundaries, cross-domain orchestration, entity → response-DTO conversion
Repository  → persistence. Spring Data JPA methods + @Query + QueryDSL custom implementations
Entity      → state + domain methods that change state (no setters)
```

**The call direction is one-way.** `Controller → Service → Repository → Entity`.

| Forbidden | Reason |
|-----------|--------|
| A controller calls a repository directly | Persistence access outside the transaction boundary |
| A service uses `ResponseEntity`·`HttpServletRequest` | Web dependencies leak into the business layer |
| Returning an entity directly as a response | Circular references, lazy-loading exceptions, field exposure |
| Business branching in a repository | Condition checks belong in the service |

## Package placement

```text
domain/{domain}/
├── controller/{Xxx}Controller.java
├── service/{Xxx}Service.java
├── repository/{Xxx}Repository.java          # + {Xxx}RepositoryCustom/Impl (QueryDSL)
│   └── projection/                          # read-only projection interfaces/records
├── entity/{Xxx}.java                        # no Entity suffix
│   └── enums/                               # domain enums
├── dto/request/{verb}{target}Request.java
├── dto/response/{verb}{target}Response.java
└── event/{Xxx}Event.java                    # ApplicationEvent
```

Cross-cutting concerns that do not belong to a domain (auth, image, config, exceptions) go under `global/`.

## Decision summary

- Transactions **start in the service**. Do not attach them to controllers or repositories.
- When you need another domain, **inject that domain's `Service`**. Do not inject a repository across a domain boundary.
- Always wrap responses in a DTO (`record`). This holds even for a single field.
- Keep controller methods thin. Service call → response wrapping, 3 lines is the baseline.
