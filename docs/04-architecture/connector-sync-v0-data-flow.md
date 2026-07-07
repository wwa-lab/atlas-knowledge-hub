# Data Flow: connector-sync-v0

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07

## Flow 1: Connector Discovery

```text
Frontend Connector panel
  -> GET /api/connector-definitions
  -> ConnectorSyncController
  -> ConnectorAdapterRegistry.capabilities()
  -> ApiEnvelope<List<ConnectorDefinitionResponse>>
  -> UI connector list
```

Traceability: REQ-CONNECTOR-SYNC-V0-001, REQ-CONNECTOR-SYNC-V0-002, AC-CONNECTOR-SYNC-V0-001.

## Flow 2: Start Sync Run

```text
User action
  -> POST /api/spaces/{spaceId}/connector-sync-jobs
  -> validate space and connector definition
  -> persist ConnectorSyncJob
  -> persist ConnectorSyncRun as QUEUED
  -> mark run RUNNING
  -> execute MockLocalFixtureConnectorAdapter
  -> persist ConnectorSyncItem rows
  -> persist ConnectorOutputArtifact rows as REVIEW_REQUIRED
  -> mark run REVIEW_REQUIRED or FAILED
  -> return ConnectorSyncRunResponse
```

Traceability: REQ-CONNECTOR-SYNC-V0-003, REQ-CONNECTOR-SYNC-V0-004, REQ-CONNECTOR-SYNC-V0-006, AC-CONNECTOR-SYNC-V0-002, AC-CONNECTOR-SYNC-V0-003.

## Flow 3: Inspect Run And Items

```text
Frontend
  -> GET /api/connector-sync-runs/{runId}
  -> GET /api/connector-sync-runs/{runId}/items
  -> render run summary
  -> render item source reference, trace, provenance, review eligibility, output artifact
```

Traceability: REQ-CONNECTOR-SYNC-V0-005, REQ-CONNECTOR-SYNC-V0-008, AC-CONNECTOR-SYNC-V0-004, AC-CONNECTOR-SYNC-V0-005.

## State Transitions

Run:
- QUEUED -> RUNNING: service starts local execution.
- RUNNING -> REVIEW_REQUIRED: at least one review-required artifact created and no blocking run failure.
- RUNNING -> COMPLETED: all items complete and no review-required artifact exists.
- RUNNING -> FAILED: adapter unavailable, validation fault, or all items fail.

Item:
- DISCOVERED -> FETCHED -> OUTPUT_CREATED -> REVIEW_REQUIRED.
- DISCOVERED -> FAILED when fixture source is unsupported or unreadable.

## Error Flow

```text
Adapter exception or unsafe adapter message
  -> ConnectorSyncService sanitizes with safe-error sanitizer
  -> item.safe_error_category + item.safe_error_message
  -> run.safe_message
  -> ApiEnvelope safe response
```

Traceability: REQ-CONNECTOR-SYNC-V0-007, AC-CONNECTOR-SYNC-V0-006.

## Data Safety Notes

- Source references are sample-safe fixture locators, not real URLs or private paths.
- Provenance records connector key, fixture id, source locator, content hash, and adapter version, not raw payload.
- Output artifacts are metadata-only review handoffs in v0.

## Related Tasks

T-CONNECTOR-SYNC-V0-002, T-CONNECTOR-SYNC-V0-003, T-CONNECTOR-SYNC-V0-004, T-CONNECTOR-SYNC-V0-005.
