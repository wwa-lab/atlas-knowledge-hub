# Architecture: connector-sync-v0

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07

## Overview

connector-sync-v0 uses the existing Atlas layered Spring Boot and Vue product architecture. It adds a connector adapter boundary, mock/local fixture adapter, connector sync application service, additive persistence tables, API endpoints, and a compact frontend inspection surface.

## Architectural Drivers

- REQ-CONNECTOR-SYNC-V0-001: adapter-first connector boundary.
- REQ-CONNECTOR-SYNC-V0-004: deterministic sync run state transitions.
- REQ-CONNECTOR-SYNC-V0-005: durable source trace and provenance.
- REQ-CONNECTOR-SYNC-V0-006: review-required output handoff.
- REQ-CONNECTOR-SYNC-V0-007: safe errors and no unsafe leakage.
- REQ-CONNECTOR-SYNC-V0-009: mock/local fixture only, no external network.

## High-Level Architecture

```text
Users
  |
  v
Vue Product Shell
  Connector Sync panel: definitions, run status, item trace, review handoff
  |
  | REST / JSON using ApiEnvelope
  v
Spring Boot Metadata API
  ConnectorSyncController
  |
  v
Connector Sync Service
  validates space + connector definition
  owns QUEUED -> RUNNING -> terminal transitions
  sanitizes safe status/error details
  |
  +--> Connector Adapter Registry
  |      MockLocalFixtureConnectorAdapter only in v0
  |
  v
PostgreSQL / Flyway additive metadata
  connector_definition
  connector_sync_job
  connector_sync_run
  connector_sync_item
  connector_output_artifact
```

## Component Responsibilities

- Frontend Connector Sync panel: exposes the user entry point, calls typed API helpers, renders run/item status, and shows source trace/provenance without accepting credentials.
- ConnectorSyncController: exposes connector definition listing, sync job creation, run lookup, and run item listing through `ApiEnvelope`.
- ConnectorSyncService: orchestrates job/run creation, adapter resolution, deterministic transitions, item persistence, review-required artifact creation, and safe messages.
- ConnectorAdapterRegistry: returns safe connector capabilities and resolves adapters by key.
- MockLocalFixtureConnectorAdapter: returns deterministic local fixture items with source references, trace, provenance, confidence, and review-required output hints.
- Persistence layer: stores connector definitions, jobs, runs, items, and output artifacts using additive Flyway migration and JPA repositories.

## Boundaries

- Connector adapters expose Atlas concepts: connector key, source references, source trace, provenance, confidence, and review-required output. They do not expose provider SDKs or vendor request/response payloads.
- Connector sync does not call parser, converter, model, vector, storage, or search adapters directly. Future slices may hand review-required artifacts into review/Wiki pipelines through documented contracts.
- Existing safe error infrastructure remains the API error boundary.

## Security / Data Safety

- No credentials are accepted by v0 requests.
- Connector configuration status is mock/status-only.
- Unsafe strings are sanitized before persistence and API response.
- Fixture data uses sample-safe relative identifiers only.
- Raw connector payloads are not persisted or returned.

## Risks / Tradeoffs

| Risk | Mitigation |
|---|---|
| Users may infer real provider support from connector naming. | Use `mock-local-fixture` and UI copy/status that labels v0 as local fixture only. |
| Synchronous execution is not production worker architecture. | Record as v0 prototype foundation; scheduled/background worker remains excluded. |
| Connector output could be mistaken for trusted Wiki knowledge. | Persist output artifacts as REVIEW_REQUIRED and render explicit review-required badges. |

## Related Tasks

T-CONNECTOR-SYNC-V0-001 through T-CONNECTOR-SYNC-V0-009.
