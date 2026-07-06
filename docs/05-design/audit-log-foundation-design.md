# Detailed Design: audit-log-foundation

Status: Draft for human review
Last updated: 2026-07-06

## Overview

The design introduces a centralized audit module in the backend and a read-only audit panel in the frontend. The backend owns all enforcement, event creation, sanitization, persistence, and read filtering. The frontend renders only safe DTOs.

## Design Assumptions

- `auth-space-rbac` is available and provides current-user context, role/capability decisions, and safe denied responses.
- PostgreSQL/Flyway remains the persistence mechanism.
- `graph_audit_record` stays intact until the implementation chooses a safe bridge or dual-write strategy.

## Design Scope

In scope:

- Audit event entity, repository, service, DTOs, controller, migration, and tests.
- Audit emitters for representative sensitive operations.
- Read-only Vue audit surface.

Out of scope:

- Retention jobs, SIEM export, alerting, signing, secret manager, rate limiting, and live identity provider integration.

## Module Design

### Audit Domain

- `AuditEvent` represents append-only records.
- Audit enums define category, result, and severity.
- DTOs expose safe response fields only.

### AuditLogService

Responsibilities:

- Accept audit event commands from auth and domain services.
- Validate safe event shape.
- Apply metadata allowlist.
- Persist events through the repository.
- Query events with RBAC-aware service methods.

The service should expose intention-revealing methods such as record denied auth decision, record membership change, and record representative knowledge operation. Settings/adapter operation methods are deferred to dedicated governance slices. The implementation may share an internal builder but must keep metadata constrained by a service-owned allowlist.

### Audit Read API

Responsibilities:

- Parse filters and pagination.
- Enforce governance-read permissions through the existing auth boundary.
- Return `ApiEnvelope` with page metadata.
- Avoid disclosure on cross-space target lookups.

### Domain Emitters

Representative emitters are added to:

- Auth/RBAC denied outcomes.
- Membership service.
- Wiki publish path in the review/publish service.
- Wiki ingest and linkify/lint services are deferred follow-on emitters.
- Graph service, while preserving existing graph audit evidence.
- Ask operations.
- Model/vector/storage/parser/converter operation emitters are deferred to adapter/runtime governance slices.

### Frontend Audit Panel

Responsibilities:

- Fetch safe audit events through a typed API helper.
- Render filters, list, loading, empty, denied, and error states.
- Hide or disable entry points when capabilities do not allow governance audit reads.

## API / Interface Design

The API guide is authoritative for endpoint paths and JSON fields. Required operations:

- List events for a space.
- List events for a target within a space.
- Optional current-user recent governance events only if RBAC allows it.

No create/update/delete audit API is exposed to clients.

## Data Design

Use `atlas.audit_event` with append-only records and indexes for:

- Space timeline.
- Target timeline.
- Actor timeline.
- Category/result reporting.

`metadata` is a JSON object but must be built from an allowlist. Arbitrary map passthrough from requests is forbidden.

## UI / User Flow Design

Recommended first UI location: a Governance/Audit panel inside the existing space settings or governance surface. If the current UI lacks a dedicated governance tab, the first implementation may add an audit section under settings while keeping it read-only.

Rows display:

- Time.
- Actor label.
- Category/action.
- Result/severity.
- Target type and target id.
- Safe summary.

Filters:

- Category.
- Result.
- Actor.
- Target type/id.
- Time range.

## Validation And Error Handling

- Page size must be bounded.
- Time range must reject invalid intervals.
- Unknown category/result/action filters return safe validation errors.
- Denied audit reads show non-sensitive UI messages.
- Audit write sanitization failures should fail tests and reject unsafe event creation.

## Testing Considerations

- Backend unit tests for safe metadata builder and enum coverage.
- Backend integration tests for migration, append-only behavior, RBAC, filters, and cross-space denial.
- Service tests for representative emitters.
- Frontend unit tests for rendering and denied/empty states.
- Playwright E2E for audit panel if the UI surface is implemented in this slice.
- Safety scans for secrets, private paths, external network calls, and dependency drift.

## Risks / Design Tradeoffs

- Capturing many read events is noisy, so v0 should focus on denied access and governance writes.
- Metadata allowlist is safer than free-form maps but requires small additions when new operations need audit context.
- Graph audit bridge may temporarily duplicate data, but avoids destructive migration.

## Open Questions

- OQ-AUDIT-LOG-FOUNDATION-001: Auditor category visibility by default.
- OQ-AUDIT-LOG-FOUNDATION-002: Graph audit backfill versus bridge.

## Traceability

- Audit domain and `AuditLogService` cover REQ-AUDIT-LOG-FOUNDATION-001, REQ-AUDIT-LOG-FOUNDATION-002, REQ-AUDIT-LOG-FOUNDATION-011, T-AUDIT-LOG-FOUNDATION-001, T-AUDIT-LOG-FOUNDATION-002, and T-AUDIT-LOG-FOUNDATION-003.
- Domain emitters cover REQ-AUDIT-LOG-FOUNDATION-003 through REQ-AUDIT-LOG-FOUNDATION-006 and T-AUDIT-LOG-FOUNDATION-004 through T-AUDIT-LOG-FOUNDATION-005.
- Audit read API and frontend panel cover REQ-AUDIT-LOG-FOUNDATION-007 through REQ-AUDIT-LOG-FOUNDATION-010 and T-AUDIT-LOG-FOUNDATION-006 through T-AUDIT-LOG-FOUNDATION-007.
