# User Stories: connector-sync-v0

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07

## US-CONNECTOR-SYNC-V0-001: Discover available connector definitions

As a knowledge space operator, I want to see mock-safe connector definitions, so that I can understand which source types Atlas can sync without exposing real credentials or provider details.

Acceptance:
- AC-CONNECTOR-SYNC-V0-001: Given the Connector Sync surface is open, when connector definitions load, then each connector shows type, status, capability summary, and mock-safe configuration state.
- AC-CONNECTOR-SYNC-V0-006: Given a connector has unsafe implementation details internally, when the API responds, then the UI receives only safe status and summary fields.

Dependencies: REQ-CONNECTOR-SYNC-V0-001, REQ-CONNECTOR-SYNC-V0-002, REQ-CONNECTOR-SYNC-V0-007.

## US-CONNECTOR-SYNC-V0-002: Start a mock/local fixture sync run

As a knowledge space operator, I want to start a connector sync run from a local fixture connector, so that Atlas can demonstrate the connector workflow without reaching external systems.

Acceptance:
- AC-CONNECTOR-SYNC-V0-002: Given a mock/local connector is available, when the user starts sync, then Atlas creates a sync job and sync run.
- AC-CONNECTOR-SYNC-V0-003: Given the run executes, when it completes, then status moves deterministically from QUEUED to RUNNING to REVIEW_REQUIRED or COMPLETED.

Dependencies: REQ-CONNECTOR-SYNC-V0-003, REQ-CONNECTOR-SYNC-V0-004, REQ-CONNECTOR-SYNC-V0-009.

## US-CONNECTOR-SYNC-V0-003: Inspect source trace and provenance

As an SME reviewer, I want to inspect each synced item with its source reference, source trace, provenance, confidence, and review eligibility, so that connector-derived content can be reviewed before trust use.

Acceptance:
- AC-CONNECTOR-SYNC-V0-004: Given a sync item exists, when it is displayed, then source trace and provenance are visible.
- AC-CONNECTOR-SYNC-V0-005: Given connector output is produced, when it is handed off, then the output artifact remains review-required by default.

Dependencies: REQ-CONNECTOR-SYNC-V0-005, REQ-CONNECTOR-SYNC-V0-006.

## US-CONNECTOR-SYNC-V0-004: See safe failures

As an operator, I want connector sync failures to use safe categories and redacted messages, so that I can troubleshoot status without leaking credentials, endpoints, private paths, or raw payloads.

Acceptance:
- AC-CONNECTOR-SYNC-V0-006: Given a connector adapter fails, when the API responds or the UI renders the item, then only a safe category and safe message are visible.

Dependencies: REQ-CONNECTOR-SYNC-V0-007, REQ-CONNECTOR-SYNC-V0-010.

## US-CONNECTOR-SYNC-V0-005: Preserve existing trusted flows

As an Atlas user, I want connector sync v0 to coexist with upload, wiki, review, ask, graph, and safe-error flows, so that adding connector foundations does not regress the current product path.

Acceptance:
- AC-CONNECTOR-SYNC-V0-007: Given verification runs, when backend and frontend gates complete, then existing flows continue passing.

Dependencies: REQ-CONNECTOR-SYNC-V0-011, REQ-CONNECTOR-SYNC-V0-012.

## Notes / Assumptions

- The actor "knowledge space operator" maps to existing mock user flows and does not introduce production RBAC semantics.
- All stories are v0 mock/local-fixture stories; real provider onboarding is out of scope.

## Open Questions

None blocking under the attached goal's preauthorization boundary.
