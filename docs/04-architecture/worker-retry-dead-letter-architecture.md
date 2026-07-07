# Architecture: worker-retry-dead-letter

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07

## Overview

This slice extends the Atlas metadata control plane with a local worker reliability read/write model. It is a layered Spring Boot + Vue feature over PostgreSQL/Flyway and the existing API envelope/safe error pattern. It does not add a queue engine, scheduler, distributed worker runtime, or external integration.

## Architectural Drivers

- Preserve source trace and review eligibility across failure handling.
- Keep retry/dead-letter deterministic for local tests and operator inspection.
- Reuse existing `ApiEnvelope`, `SafeErrorSanitizer`, and safe code/category conventions.
- Keep connector, parser, converter, model, vector, storage, and search execution behind adapters.
- Avoid production auth/RBAC/audit/secret-manager changes.

## High-Level Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│ Operators and reviewers                                      │
│ Processing Center · Failed jobs · Dead-letter detail          │
└─────────────────────────┬────────────────────────────────────┘
                          │ REST / JSON ApiEnvelope
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ Atlas Metadata API                                           │
│ WorkerRecoveryController · DTO mapping · safe API responses   │
├──────────────────────────────────────────────────────────────┤
│ Worker Reliability Domain                                    │
│ WorkerJobService · RetryPolicy · DeadLetter transitions       │
├──────────────────────────────────────────────────────────────┤
│ Persistence                                                  │
│ WorkerJob · WorkerJobAttempt · DeadLetterEntry repositories   │
└─────────────────────────┬────────────────────────────────────┘
                          │ JDBC / JPA
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ PostgreSQL / Flyway                                          │
│ Additive local reliability tables; mock/sample-safe seed only │
└──────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

### Frontend

- Processing Center renders a dead-letter list, selected detail, attempts, safe error, source trace, and local action controls.
- Frontend API types model safe worker recovery responses.
- UI never renders raw exception text or raw source content.

### Backend API

- Worker recovery controller exposes list/detail/action endpoints.
- Responses use `ApiEnvelope`.
- Known invalid states return safe `CONFLICT`; missing records return safe `NOT_FOUND`.

### Domain Service

- Worker job service owns job creation, attempt recording, retry transition, dead-letter creation, manual retry, and acknowledge.
- Retry policy is deterministic: max attempts 3, delay seconds 30 and 120.
- Dead-letter creation is idempotent per terminal job.

### Persistence

- Additive Flyway migration creates worker job, job attempt, and dead-letter tables.
- Source trace and error snapshots use safe JSON/text fields.
- Operator action metadata is local foundation data, not production audit.

## Boundaries

- Inside this slice: local reliability metadata, deterministic state transitions, safe inspection UI/API, tests.
- Outside this slice: production queue, worker scheduler, distributed cluster, exactly-once, saga, real connector/API calls, production RBAC/audit/secret-manager changes.

## Security And Data Safety

- Safe error snapshots are sanitized before persistence and response serialization.
- Source trace stores relative/mock-safe identifiers only.
- No secrets, private paths, raw stack traces, tokens, cookies, API keys, internal endpoints, or real company data are committed or exposed.

## Open Questions

None blocking under the preauthorized local v0 scope.
