# Requirements: connector-sync-v0

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07
Wave: Wave 5 / Connector And Operations

## Slice Contract

Atlas must support a connector sync v0 product and engineering skeleton without binding to any real external provider. The slice introduces connector definitions, sync jobs, sync runs, sync items, source trace/provenance, review-required handoff artifacts, and safe status/error surfaces using only mock or local fixture data.

## Requirements

| ID | Requirement |
|---|---|
| REQ-CONNECTOR-SYNC-V0-001 | Atlas must expose an adapter-first connector boundary so product logic depends on Atlas connector concepts, not vendor APIs. |
| REQ-CONNECTOR-SYNC-V0-002 | Atlas must provide a connector registry with connector definition metadata, type, capability summary, mock-safe configuration status, and review policy. |
| REQ-CONNECTOR-SYNC-V0-003 | Atlas must allow a user to create or view a sync job for a Knowledge Space using a registered mock/local fixture connector. |
| REQ-CONNECTOR-SYNC-V0-004 | Atlas must create deterministic sync runs with explicit statuses: QUEUED, RUNNING, COMPLETED, FAILED, and REVIEW_REQUIRED. |
| REQ-CONNECTOR-SYNC-V0-005 | Atlas must persist sync items with per-item status, safe error category, source reference, source trace, provenance, confidence, and review eligibility. |
| REQ-CONNECTOR-SYNC-V0-006 | Connector-derived output artifacts must default to review-required and must not become approved Wiki, Ask, or Graph knowledge without SME review or deterministic validation. |
| REQ-CONNECTOR-SYNC-V0-007 | API responses must use the existing `ApiEnvelope` and safe error pattern; unsafe values such as secrets, private paths, raw payloads, and internal endpoints must be redacted or omitted. |
| REQ-CONNECTOR-SYNC-V0-008 | The frontend must expose a connector sync entry point, connector list, sync run status, sync item inspection, source trace/provenance view, and review-required indication. |
| REQ-CONNECTOR-SYNC-V0-009 | The implementation must use mock/local fixture connector data only and must not call Confluence, SharePoint, Google Drive, Lark, Notion, Slack, GitHub, web crawlers, databases, or external APIs. |
| REQ-CONNECTOR-SYNC-V0-010 | Backend tests must cover adapter boundary behavior, deterministic status transitions, safe error redaction, source trace preservation, and review-required output handoff. |
| REQ-CONNECTOR-SYNC-V0-011 | Frontend typecheck, tests, and build must cover the connector sync v0 UI without regressing upload, wiki, review, ask, graph, or safe-error flows. |
| REQ-CONNECTOR-SYNC-V0-012 | SDD, task, traceability, roadmap, and closeout evidence must stay aligned with the implemented behavior. |

## Explicit Exclusions

- No real external connector provider, network call, OAuth, API key, cookie, service account, crawler, or provider SDK.
- No real company documents, private URLs, internal endpoints, raw connector payloads, or production credentials.
- No production connector marketplace, scheduled sync, incremental sync, webhook sync, distributed worker, production auth/RBAC/audit/secret-manager change, or destructive migration.
- No direct parser/converter/model/vector/storage dependency from connector UI or service logic.

## Acceptance Criteria

- AC-CONNECTOR-SYNC-V0-001: Users can see a connector sync v0 entry in the product UI.
- AC-CONNECTOR-SYNC-V0-002: Users can start and inspect a mock/local fixture sync run.
- AC-CONNECTOR-SYNC-V0-003: Sync run and item statuses are deterministic and inspectable.
- AC-CONNECTOR-SYNC-V0-004: Each sync item preserves source trace and provenance.
- AC-CONNECTOR-SYNC-V0-005: Connector-derived output artifacts are review-required by default.
- AC-CONNECTOR-SYNC-V0-006: API errors are safe and do not leak secrets, private paths, raw payloads, or internal endpoints.
- AC-CONNECTOR-SYNC-V0-007: Existing upload, wiki, review, ask, graph, and rate-limit-safe-errors flows continue to pass verification.

## Assumptions

- Connector sync v0 is a Wave 5 prototype foundation with real backend persistence but mock-only adapter execution.
- The current Spring Boot + PostgreSQL/Flyway backend pattern is active, so additive Flyway migration is required.
- The current Vue product shell is a single-file surface; this slice adds the smallest scoped connector UI area inside the existing space experience.

## Open Questions

None blocking under the attached goal's preauthorization boundary.
