# System Architecture: Ask Session Citations

## Overview

Ask Session Citations extends the existing layered Ask implementation with an additive session aggregate and richer citation metadata. The architecture stays inside the Spring Boot metadata API, PostgreSQL/Flyway persistence, existing vector/model adapter boundaries, and Vue Trusted Ask surface.

## High-Level Architecture

```text
Users
  |
  v
Vue Trusted Ask UI
  |
  v
AskController REST / ApiEnvelope
  |
  v
AskService
  |-- AskSession aggregate
  |-- AskRun answer record
  |-- AskEvidence citation snapshot
  |
  v
Spring Data repositories
  |
  v
PostgreSQL / Flyway

Existing adapter calls remain unchanged:
AskService -> VectorService -> VectorAdapter
AskService -> ModelService -> ModelAdapter
```

## Architectural Drivers

- Preserve source trace, confidence, and review status in every citation.
- Keep existing Ask create/read API compatible.
- Keep provider/model/vector behavior behind existing services and adapters.
- Add session read models without changing production auth/RBAC/audit semantics.
- Use safe labels instead of raw source content.

## Component Responsibilities

| Component | Responsibility |
|---|---|
| `AskSession` domain | Persist session id, space id, title, creator, created/updated timestamps. |
| `AskRun` domain | Continue owning one question/answer lifecycle; add session ownership. |
| `AskEvidence` domain | Continue owning citation snapshot; add safe citation label/status fields. |
| `AskService` | Create/reuse sessions, create runs, persist citations, list/read sessions, enforce safe fields. |
| `AskController` | Expose Ask run and session APIs through `ApiEnvelope`. |
| Frontend API client | Call Ask run and session endpoints with typed responses. |
| Trusted Ask UI | Render session summaries, answer history, and citation detail. |

## Boundaries

- The UI does not call vector/model providers directly.
- The backend does not expose raw source snippets, provider payloads, private paths, or stack traces.
- Session citations do not approve answers; answer review governance remains a later slice.
- Persistence changes are additive Flyway migrations.

## Architecture Review Result

| Check | Result |
|---|---|
| Feature boundaries | Pass: session/citation code stays in Ask domain. |
| API envelope | Pass: new endpoints use existing `ApiEnvelope`. |
| Adapter boundaries | Pass: no new direct provider/vector/model calls. |
| Schema management | Pass: additive Flyway migration planned as `V15__ask_session_citations.sql`. |
| Safe evidence | Pass: safe display labels and review eligibility are explicit. |

## Risks

- Session list response could grow over time; this slice uses a bounded recent list contract.
- Citation status names must stay frontend/backend aligned.
