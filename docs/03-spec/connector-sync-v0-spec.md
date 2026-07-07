# Feature Specification: connector-sync-v0

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07
Source stories: US-CONNECTOR-SYNC-V0-001 through US-CONNECTOR-SYNC-V0-005

## Overview

connector-sync-v0 turns connector sync from a concept into a local, auditable, review-required Atlas workflow. It adds connector definitions, sync jobs, deterministic sync runs, sync items, source trace/provenance, review-required output artifacts, safe error categories, and frontend inspection using only a mock/local fixture connector.

## Scope

In scope:
- Connector registry and connector definition listing.
- Mock/local fixture connector adapter behind an Atlas connector adapter interface.
- Sync job creation and sync run execution for one Knowledge Space.
- Sync item persistence with source trace and provenance.
- Review-required output artifact handoff metadata.
- Safe status/error categories and redacted messages.
- Frontend connector sync entry, run status, item details, and source trace inspection.

Out of scope:
- Real external connectors, real credentials, external network calls, provider SDKs, OAuth, crawlers, scheduled/background workers, incremental sync, webhooks, production marketplace, production auth/RBAC/audit/secret-manager changes, or direct parser/converter invocation.

## Functional Requirements

| ID | Spec Requirement |
|---|---|
| FR-CONNECTOR-SYNC-V0-001 | REQ-CONNECTOR-SYNC-V0-001: Connector execution must go through an Atlas connector adapter interface. |
| FR-CONNECTOR-SYNC-V0-002 | REQ-CONNECTOR-SYNC-V0-002: The API must list connector definitions with safe metadata and mock-safe configuration state. |
| FR-CONNECTOR-SYNC-V0-003 | REQ-CONNECTOR-SYNC-V0-003: The API must create a sync job/run for a Knowledge Space and a selected connector definition. |
| FR-CONNECTOR-SYNC-V0-004 | REQ-CONNECTOR-SYNC-V0-004: Sync runs must transition QUEUED -> RUNNING -> COMPLETED, REVIEW_REQUIRED, or FAILED deterministically inside the request. |
| FR-CONNECTOR-SYNC-V0-005 | REQ-CONNECTOR-SYNC-V0-005: Sync items must store source reference, source trace, provenance, confidence, review eligibility, status, and safe error category. |
| FR-CONNECTOR-SYNC-V0-006 | REQ-CONNECTOR-SYNC-V0-006: Output artifacts generated from connector items must be REVIEW_REQUIRED by default. |
| FR-CONNECTOR-SYNC-V0-007 | REQ-CONNECTOR-SYNC-V0-007: API errors and item errors must expose only safe categories/messages. |
| FR-CONNECTOR-SYNC-V0-008 | REQ-CONNECTOR-SYNC-V0-008: Frontend must render connector definitions, sync run status, item list, selected item trace, provenance, and review-required artifact status. |
| FR-CONNECTOR-SYNC-V0-009 | REQ-CONNECTOR-SYNC-V0-009: No code path may call a real external provider or require a real credential. |
| FR-CONNECTOR-SYNC-V0-010 | REQ-CONNECTOR-SYNC-V0-010: Backend tests must cover adapter boundary, state transitions, safe redaction, trace preservation, and review-required handoff. |
| FR-CONNECTOR-SYNC-V0-011 | REQ-CONNECTOR-SYNC-V0-011: Frontend verification must include typecheck, tests, and build. |
| FR-CONNECTOR-SYNC-V0-012 | REQ-CONNECTOR-SYNC-V0-012: Traceability and roadmap evidence must record final status and residual risk. |

## Status Model

Run status:
- QUEUED: persisted before adapter execution.
- RUNNING: execution has started.
- REVIEW_REQUIRED: adapter produced at least one review-required output artifact and no blocking failure.
- COMPLETED: all items completed without review-required output. This is allowed by the model but not expected for the default fixture.
- FAILED: adapter unavailable, request invalid, or all items fail safely.

Item status:
- DISCOVERED: adapter discovered source metadata.
- FETCHED: local fixture content was read from in-memory fixture data.
- OUTPUT_CREATED: output artifact metadata was created.
- REVIEW_REQUIRED: item output requires review before trust use.
- FAILED: item failed with safe category/message.

Safe error categories:
- NONE, VALIDATION, CONNECTOR_UNAVAILABLE, UNSUPPORTED_SOURCE, SOURCE_UNREADABLE, SAFE_SYSTEM.

## Main Flow

1. User opens a Knowledge Space and sees the Connector Sync entry.
2. Frontend lists connector definitions from `/api/connector-definitions`.
3. User starts sync for a mock/local fixture connector through `/api/spaces/{spaceId}/connector-sync-jobs`.
4. Backend validates space and connector definition, creates sync job and run, then marks the run RUNNING.
5. Backend executes the mock/local fixture adapter through the connector adapter interface.
6. Backend persists sync items and review-required output artifacts with source trace/provenance.
7. Backend marks the run REVIEW_REQUIRED when review-required output exists.
8. Frontend shows run summary, deterministic statuses, item details, safe errors, source trace, provenance, and review-required handoff.

## Data / Configuration Requirements

Key entities:
- ConnectorDefinition: stable connector metadata and capability summary.
- ConnectorSyncJob: user-visible sync request for a Knowledge Space.
- ConnectorSyncRun: deterministic execution record for a job.
- ConnectorSyncItem: per-source item status, trace, provenance, confidence, and safe error category.
- ConnectorOutputArtifact: review-required handoff metadata created from sync items.

Configuration:
- The only v0 connector definition is `mock-local-fixture`.
- Configuration state is `MOCK_CONFIGURED`; no raw credentials are accepted or returned.

## Non-Functional Requirements

- Security: no secrets, private paths, raw connector payloads, internal endpoints, or external URLs in committed data or API errors.
- Auditability: run and item records preserve trace/provenance for review and future audit integration.
- Reliability: v0 execution is synchronous and deterministic for local tests.
- Environment: no external network dependency or provider credential.

## Edge Cases

- Unknown connector definition returns safe `NOT_FOUND`.
- Invalid connector key returns `VALIDATION_FAILED`.
- Adapter unavailable produces FAILED run with safe category.
- Unsafe adapter text is sanitized before storage and API response.
- A fixture item without sufficient source trace is marked FAILED or REVIEW_REQUIRED, never trusted.

## Task Mapping

T-CONNECTOR-SYNC-V0-001 through T-CONNECTOR-SYNC-V0-009 implement FR-CONNECTOR-SYNC-V0-001 through FR-CONNECTOR-SYNC-V0-012.

## Open Questions

None blocking under the attached goal's preauthorization boundary.
