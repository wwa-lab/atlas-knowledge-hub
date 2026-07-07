# Architecture: manual-url-knowledge-ingest

## Overview

`manual-url-knowledge-ingest` is an additive metadata slice. It places manual URL source registration behind Atlas backend API, validates and sanitizes URL metadata, persists source state, and creates review-required batch/file/chunk metadata that existing Processing Center and Wiki surfaces can consume.

## Architecture Drivers

- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-001` requires a user-facing registration path.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002` requires persistent URL source metadata.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-004` forbids real external fetch.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006` requires trace preservation.
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-008` requires additive behavior only.

## High-Level Architecture

```text
User
  |
  v
Vue Knowledge Space / Processing Center
  |
  | REST / JSON
  v
Spring Boot API
  |
  +-- Manual URL Controller
  |     validates request and returns ApiEnvelope
  |
  +-- Manual URL Service
  |     sanitizes URL, records fetch intent, creates review-required metadata
  |
  +-- Existing Batch/File/SourceChunk Services
  |     exposes URL source to Processing Center and future Wiki ingest
  |
  v
PostgreSQL + Flyway
```

## Components

| Component | Responsibility |
|---|---|
| Manual URL UI panel | Submit sample-safe URL and display source status/trace. |
| Manual URL API client | Call new REST endpoints and map safe error states. |
| Manual URL controller | Expose additive endpoints under `/api`, use `ApiEnvelope<T>`. |
| Manual URL service | Validate URL, sanitize display metadata, create source record and review-required metadata artifacts. |
| Manual URL repository | Persist URL source state. |
| Existing batch/file/chunk repositories | Store downstream review-required metadata artifacts. |
| Existing review/Wiki surfaces | Read review-required metadata without approval bypass. |

## Boundary Rules

- No parser/converter/model/vector/storage adapter is invoked by this slice.
- No external network call is executed.
- Existing auth guard, rate limit interceptor, safe error handler, and API envelope conventions remain unchanged.
- URL source registration is additive and does not alter Wiki publish, Ask, or Graph eligibility.

## Security And Data Safety

- Raw submitted URL is validated and not returned on validation failure.
- Accepted URL metadata strips query and fragment and rejects userinfo.
- Internal-looking hosts are rejected.
- No credentials, cookies, private endpoints, or raw source content are persisted.
- Review status defaults to `REVIEW_REQUIRED`.

## Risks

| Risk | Mitigation |
|---|---|
| Manual URL metadata could be mistaken for approved content. | Persist and display `REVIEW_REQUIRED` and eligibility metadata; do not publish automatically. |
| URL path may contain sensitive tokens. | Query/fragment are rejected; path is bounded and validation tests cover redaction. |
| Future connector-sync-v0 may need richer fetch semantics. | Store fetch intent and policy now, but keep real fetch out of scope. |

## Traceability

This architecture supports `AC-MANUAL-URL-KNOWLEDGE-INGEST-001` through `AC-MANUAL-URL-KNOWLEDGE-INGEST-006`.
