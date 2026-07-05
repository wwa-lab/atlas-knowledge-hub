# System Architecture: Wiki Ingest v0

## Status

Draft for user acceptance.

## Overview

- **Architecture summary:** `wiki-ingest-v0` adds a backend-owned Auto Wiki generation workflow that consumes approved source chunks and writes review-required Wiki candidates into the existing Wiki data model. The design is layered around Atlas APIs, domain services, repositories, and adapter boundaries.
- **Design objective:** Produce traceable, idempotent, review-required Wiki candidates without weakening existing published Wiki, Graph, or Ask trust boundaries.
- **Architectural style:** Layered Spring Boot control-plane workflow with deterministic v0 generation and a documented future ModelAdapter boundary.

## Source Specification

- **Feature name:** Wiki Ingest v0
- **Scope summary:** Space-scoped ingest run, approved chunk selection, deterministic candidate generation, slug merge, run/log/issue evidence, and status display.

## Architectural Drivers

### Key Functional Drivers

- Generate candidates from approved source chunks only.
- Preserve source trace, chunk refs, confidence, and review status.
- Merge by space-scoped slug idempotently.
- Keep generated output review-required.
- Preserve current published Wiki behavior.

### Key Non-Functional Drivers

- No raw secret, prompt, provider payload, stack trace, private path, or raw source text exposure.
- v0 execution uses deterministic local logic and no external cloud calls.
- Future model assistance must use ModelAdapter and is out of scope for v0 implementation.

### Constraints and Assumptions

- `wiki-data-model` is complete and provides Wiki page/run/log/issue support.
- Existing PDF ingestion produces review-required chunks; this slice starts from approved chunks rather than parser runtime work.
- [ASSUMPTION] v0 candidate text can be generated deterministically from source chunk metadata and excerpts already approved for safe handling.

## System Context

### Primary Actors

| Actor | Role |
|---|---|
| Knowledge manager | Starts ingest runs. |
| SME reviewer | Reviews candidates and evidence. |
| Knowledge user | Reads published Wiki and sees generated draft state when exposed. |

### External Systems

| System | Integration Purpose |
|---|---|
| ModelAdapter | Future model-assisted candidate text generation; not invoked by v0. |
| Parser/converter adapters | Out of scope for this slice; existing chunks are the input. |

### System Boundary

Inside this slice: API contracts, backend ingest service, candidate generation policy, Wiki page/run/log/issue persistence, tests, and optional UI status display. Outside this slice: parser runtime, linkify/lint, review approval workflow, production auth/RBAC, connectors, external provider setup, and operations hardening.

## High-Level Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│ Users                                                        │
│ Knowledge Manager · SME Reviewer · Knowledge User            │
└─────────────────────────┬────────────────────────────────────┘
                          │ HTTPS / JSON
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ Vue Product Surface                                          │
│ Wiki tab · Processing Center status · generated draft labels │
└─────────────────────────┬────────────────────────────────────┘
                          │ REST / JSON
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ Spring Boot API                                              │
│ Wiki ingest run endpoints · Wiki page/run read endpoints     │
├──────────────────────────────────────────────────────────────┤
│ Wiki Ingest Application Service                              │
│ input selection · deterministic candidates · slug merge logs │
├──────────────────────────────────────────────────────────────┤
│ Domain / Repositories                                        │
│ SourceChunk · WikiPage · WikiGenerationRun · WikiLogEntry    │
└──────────────┬──────────────────────────────┬────────────────┘
               │ JDBC / Flyway-managed schema │ future adapter call
               ▼                              ▼
┌─────────────────────────┐      ┌─────────────────────────────┐
│ PostgreSQL metadata     │      │ ModelAdapter boundary        │
│ atlas wiki/source tables│      │ future only; not used in v0  │
└─────────────────────────┘      └─────────────────────────────┘
```

## Layer Summary

- **Presentation layer:** Shows generated/review-required status and existing published Wiki pages.
- **API layer:** Starts ingest runs and exposes safe run/candidate metadata.
- **Application layer:** Selects inputs, creates candidates, merges by slug, and records run evidence.
- **Domain/persistence layer:** Stores Wiki pages, runs, logs, issues, and source chunk references.
- **Adapter layer:** Records that future model assistance must use ModelAdapter; v0 does not call a model provider.

## Component Breakdown

### Frontend Components

- **Wiki tab status display:** Shows generated candidates as review-required if implementation exposes drafts.
- **Processing Center ingest status:** Shows safe run status/counts when added to the UI.

### Backend Services

- **Wiki ingest service:** Owns run lifecycle, input selection, candidate generation, and idempotent merge.
- **Review-publish service compatibility path:** Existing published-page behavior remains unchanged.
- **Model adapter client boundary:** Future-only boundary; deterministic mode is the only v0 behavior.

### Persistence Modules

- **WikiPage:** Stores generated candidates with source refs, chunk refs, review status, and source mode.
- **WikiGenerationRun:** Stores safe run summary.
- **WikiLogEntry:** Stores safe lifecycle events.
- **WikiPageIssue:** Stores conflicts and missing-evidence issues.

## Data Architecture

| Entity | Description | Key Attributes |
|---|---|---|
| SourceChunk | Approved evidence input. | id, file item id, page/section, confidence, review status |
| WikiPage | Generated candidate or published page. | slug, page type, source refs, chunk refs, source mode, review status |
| WikiGenerationRun | Ingest execution record. | status, input refs, created/updated page ids, issue ids, safe summary |
| WikiLogEntry | Safe lifecycle log. | event type, page id, run id, safe message |
| WikiPageIssue | Conflict or data quality issue. | issue type, severity, status, evidence refs |

## Workflow / Runtime Architecture

### Request Flow

1. User starts a space-scoped ingest run.
2. API validates the space and requested mode.
3. Ingest service loads approved, traceable source chunks for that space.
4. Service groups chunks into deterministic `TOPIC` candidates and derives slugs.
5. Service creates or merges review-required candidates by slug.
6. Service records run/log/issue metadata.
7. API returns safe run summary.
8. UI displays generated/review-required status without trusted-publish claims.

### Failure and Retry Handling

- No eligible chunks returns a successful safe run with zero candidates.
- Partial candidate failures produce `PARTIAL_FAILED` and safe summaries.
- Repeated retries use slug-based idempotency.

## API / Interface Boundaries

| Interface | Consumer | Purpose |
|---|---|---|
| `POST /api/spaces/{spaceId}/wiki-ingest-runs` | Vue or API client | Start v0 ingest run. |
| `GET /api/spaces/{spaceId}/wiki-generation-runs/{runId}` | Vue or API client | Read safe run summary. |
| Existing Wiki reads | Vue product | Preserve published-page behavior. |
| ModelAdapter | Backend service | Future configured model assistance only; not used by v0. |

## Security / Reliability / Observability

- Generated pages are `REVIEW_REQUIRED`.
- Logs and errors are sanitized.
- No default external call is introduced.
- Idempotency is based on `(spaceId, slug)`.
- Existing trusted pages are not overwritten by generated candidates.

## Risks / Tradeoffs

| Risk | Notes |
|---|---|
| Candidate quality is thin in deterministic mode | Acceptable for v0 because review-required status is explicit. |
| Draft pages could be mistaken for trusted Wiki | UI and API must label generated/review-required state clearly. |
| Slug collision with trusted pages | Preserve trusted page and record safe issue. |

## Resolved Decisions

1. v0 creates `TOPIC` pages only.
2. v0 writes generated Markdown artifacts with deterministic safe summaries.
3. v0 aggregates confidence using the minimum included chunk confidence.
