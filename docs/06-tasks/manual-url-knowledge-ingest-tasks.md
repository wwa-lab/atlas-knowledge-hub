# Tasks: manual-url-knowledge-ingest

## Overview

Implement the slice in ID order. `docs/03-spec/manual-url-knowledge-ingest-spec.md` is the behavior source of truth. Do not add real URL fetch, crawl, connector sync, browser automation, or production compliance behavior.

## Task List

### `T-MANUAL-URL-KNOWLEDGE-INGEST-001` Confirm SDD Gate And Manifest

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-001` through `REQ-MANUAL-URL-KNOWLEDGE-INGEST-013`
- **Scope:** Ensure bilingual SDD artifacts, execution manifest, API guide, and traceability exist.
- **Verification:** `npm run agent:check-sdd -- --slice manual-url-knowledge-ingest --require-api-guide --report docs/00-context/manual-url-knowledge-ingest-sdd-completion-report.md`

### `T-MANUAL-URL-KNOWLEDGE-INGEST-002` Add Backend Data Model And Migration

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-005`
- **Scope:** Add `manual_url_source` entity/repository, status enums, DTOs, mapper, and Flyway migration. Extend `SourceKind` and `SourceType` with URL values.
- **Verification:** Backend compile and focused tests.

### `T-MANUAL-URL-KNOWLEDGE-INGEST-003` Implement Validation And Service

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-003`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-004`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-007`
- **Scope:** Implement metadata-only URL validation, sanitization, duplicate handling, batch/file/chunk metadata creation, and review-required defaults.
- **Verification:** Backend unit tests for accepted and rejected URLs.

### `T-MANUAL-URL-KNOWLEDGE-INGEST-004` Add Backend API Contract

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-001`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-008`
- **Scope:** Add controller endpoints for create/list/get using `ApiEnvelope<T>`.
- **Verification:** Integration/API contract tests for create/list/get.

### `T-MANUAL-URL-KNOWLEDGE-INGEST-005` Prove Safe Errors And Redaction

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-003`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-007`
- **Scope:** Tests must prove unsafe URL submissions do not echo credentials, query strings, fragments, private hosts, raw stack traces, or private endpoint values.
- **Verification:** Integration tests for unsafe URL cases.

### `T-MANUAL-URL-KNOWLEDGE-INGEST-006` Add Frontend Types, API Client, And Mock Data

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-009`
- **Scope:** Extend `frontend/src/types.ts`, `frontend/src/api.ts`, and mock data with manual URL source types and sample-safe fallback state.
- **Verification:** `cd frontend && npm run typecheck`

### `T-MANUAL-URL-KNOWLEDGE-INGEST-007` Add Frontend UI And Tests

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-009`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-010`
- **Scope:** Add manual URL form/status/source trace display to the Knowledge Space or Processing Center surface. Add Vitest and E2E coverage.
- **Verification:** `cd frontend && npm run test`, `cd frontend && npm run build`

### `T-MANUAL-URL-KNOWLEDGE-INGEST-008` Closeout, Roadmap, And Safety Gates

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-008`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-010`
- **Scope:** Update traceability, slice roadmap, repo status roadmap, and completion evidence. Run closeout and hygiene scans.
- **Verification:** `cd backend && mvn verify`; `cd frontend && npm run typecheck`; `cd frontend && npm run test`; `cd frontend && npm run build`; `npm run agent:closeout`; `git diff --check`.

## Dependency Plan

Critical path: `T-MANUAL-URL-KNOWLEDGE-INGEST-001` -> `T-MANUAL-URL-KNOWLEDGE-INGEST-002` -> `T-MANUAL-URL-KNOWLEDGE-INGEST-003` -> `T-MANUAL-URL-KNOWLEDGE-INGEST-004` -> `T-MANUAL-URL-KNOWLEDGE-INGEST-005` -> `T-MANUAL-URL-KNOWLEDGE-INGEST-006` -> `T-MANUAL-URL-KNOWLEDGE-INGEST-007` -> `T-MANUAL-URL-KNOWLEDGE-INGEST-008`.

## Stop Conditions

Stop if implementation requires real URL fetch, production connector credentials, auth/RBAC/audit/secret/rate-limit/provider semantic changes, or exposing unreviewed URL content as approved Wiki/Ask/Graph knowledge.
