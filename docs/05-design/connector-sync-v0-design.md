# Design: connector-sync-v0

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07

## Design Scope

Implement connector sync v0 as a small full-stack vertical slice: adapter contracts, mock/local fixture adapter, persistence, API, frontend bindings, UI inspection, and tests. The design implements REQ-CONNECTOR-SYNC-V0-001 through REQ-CONNECTOR-SYNC-V0-012.

## Backend Module Design

- Connector adapter contract: `ConnectorAdapter`, `ConnectorCapability`, `ConnectorSyncRequest`, and `ConnectorSyncResult` model Atlas-level connector behavior.
- Mock adapter: `MockLocalFixtureConnectorAdapter` returns deterministic sample-safe items. It does not read files, call URLs, or accept credentials.
- Registry: `ConnectorAdapterRegistry` lists capabilities and resolves adapter keys.
- Service: `ConnectorSyncService` creates jobs/runs, drives state transitions, persists items/artifacts, and sanitizes safe messages.
- Controller: `ConnectorSyncController` exposes REST endpoints under `/api`.
- Mapper/DTO layer: immutable response records expose only safe fields.

## Frontend Design

The Vue product shell adds a Connector Sync panel inside the Knowledge Space product surface. The panel includes:

- Connector definitions list with type, status, capability summary, and configuration state.
- Start sync action for `mock-local-fixture`.
- Latest run status summary with counts.
- Sync item table.
- Selected item detail showing source reference, source trace, provenance, confidence, review eligibility, safe error category, and review-required output artifacts.

## API / Interface Design

Endpoints:
- GET `/api/connector-definitions`
- POST `/api/spaces/{spaceId}/connector-sync-jobs`
- GET `/api/connector-sync-runs/{runId}`
- GET `/api/connector-sync-runs/{runId}/items`

All endpoints use `ApiEnvelope`. Error responses use existing safe error mapping.

## Validation And Error Handling

- Unknown connector keys return safe validation or not-found errors.
- Unknown spaces return safe not-found errors.
- Adapter unavailable creates a FAILED run when a run can be created safely.
- Adapter exceptions are sanitized before persistence.
- Raw fixture content and raw payloads are never returned.

## UI States

- Loading: definitions or latest run request in progress.
- Empty: no run started yet.
- Running: run status QUEUED or RUNNING.
- Review required: run status REVIEW_REQUIRED and artifact badges visible.
- Failed: run status FAILED or API error safe message.

## Testing Considerations

- Unit tests for adapter output and service status transitions.
- Integration/API contract tests for connector definitions, sync creation, run lookup, item listing, safe redaction, and source trace preservation.
- Frontend tests for connector panel rendering and review-required indication.
- Existing regression commands: backend verify, frontend typecheck/test/build, closeout gate, diff/secret/network scans.

## Risks / Tradeoffs

- v0 synchronous execution is simpler and deterministic, but not a production worker model.
- Mock/local fixture adapter proves the boundary but does not validate any real provider behavior.
- Review-required handoff is metadata-only until a future slice connects connector artifacts into review/Wiki generation.

## Related Tasks

T-CONNECTOR-SYNC-V0-001 through T-CONNECTOR-SYNC-V0-009.
