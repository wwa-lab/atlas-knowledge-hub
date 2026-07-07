# Data Model: connector-sync-v0

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07

## Overview

connector-sync-v0 stores connector metadata, user-visible sync jobs, deterministic sync runs, per-source sync items, and review-required output artifacts. The model is additive and keeps connector-derived content separate from approved Wiki, Ask, and Graph knowledge.

## Entity Relationship

```text
space 1:N connector_sync_job
connector_definition 1:N connector_sync_job
connector_sync_job 1:N connector_sync_run
connector_sync_run 1:N connector_sync_item
connector_sync_item 1:N connector_output_artifact
```

## Entities

### ConnectorDefinition

Traceability: REQ-CONNECTOR-SYNC-V0-002.

Fields: id, connector_key, name, connector_type, status, version, capability_summary, configuration_state, review_policy, created_at, updated_at.

Rules:
- `connector_key` is stable and unique.
- v0 seed row is `mock-local-fixture`.
- Configuration state is status-only.

### ConnectorSyncJob

Traceability: REQ-CONNECTOR-SYNC-V0-003.

Fields: id, space_id, connector_definition_id, requested_by, requested_at, source_scope, status, safe_message.

Rules:
- Space must exist.
- Job status mirrors the latest run terminal status for v0.

### ConnectorSyncRun

Traceability: REQ-CONNECTOR-SYNC-V0-004.

Fields: id, job_id, connector_definition_id, status, started_at, completed_at, item_count, review_required_count, failed_count, safe_message.

Run statuses: QUEUED, RUNNING, COMPLETED, FAILED, REVIEW_REQUIRED.

### ConnectorSyncItem

Traceability: REQ-CONNECTOR-SYNC-V0-005.

Fields: id, run_id, external_id, title, item_status, source_reference, source_trace, provenance, confidence, review_eligible, safe_error_category, safe_error_message, discovered_at.

Item statuses: DISCOVERED, FETCHED, OUTPUT_CREATED, REVIEW_REQUIRED, FAILED.

Safe error categories: NONE, VALIDATION, CONNECTOR_UNAVAILABLE, UNSUPPORTED_SOURCE, SOURCE_UNREADABLE, SAFE_SYSTEM.

### ConnectorOutputArtifact

Traceability: REQ-CONNECTOR-SYNC-V0-006.

Fields: id, item_id, artifact_type, review_status, title, target_path, source_trace, provenance, created_at.

Rules:
- v0 artifacts use review_status REVIEW_REQUIRED.
- v0 artifacts are not published Wiki pages and are not Ask/Graph trust inputs.

## JSON Fields

- `source_trace`: object with sourceName, sourceLocator, section, page, itemExternalId, connectorKey.
- `provenance`: object with connectorKey, adapterVersion, fixtureId, contentHash, syncRunId.

## Related Tasks

T-CONNECTOR-SYNC-V0-002, T-CONNECTOR-SYNC-V0-003, T-CONNECTOR-SYNC-V0-004, T-CONNECTOR-SYNC-V0-005.
