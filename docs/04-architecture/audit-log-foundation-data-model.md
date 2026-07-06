# Data Model: audit-log-foundation

Status: Draft for human review
Last updated: 2026-07-06

## Overview

This slice adds a general append-only audit event model. It references existing Atlas users, spaces, and target resources by safe ids and stores only bounded safe summaries and metadata.

## Entity Relationship Diagram

```text
┌──────────────────────┐       1:N       ┌──────────────────────────┐
│ atlas.atlas_user      │────────────────►│ atlas.audit_event         │
│ id PK                 │ actor_user_id   │ id PK                     │
└──────────────────────┘                  │ actor_user_id             │
                                          │ space_id                  │
┌──────────────────────┐       1:N        │ action/category/result    │
│ atlas.space           │────────────────►│ target_type/target_id     │
│ id PK                 │ space_id        │ safe_summary/metadata     │
└──────────────────────┘                  └──────────────────────────┘
```

## Entity Definitions

### AuditEvent

- **Table name:** `atlas.audit_event`
- **Purpose:** Append-only governance/security event.

| Column | Type | Nullable | Description |
|---|---|---|---|
| id | String | No | Stable audit event id. |
| created_at | Timestamp | No | Event creation time. |
| actor_user_id | String | Yes | Atlas user id when resolved; null for anonymous/system-safe events. |
| actor_display | String | Yes | Safe display label, not raw provider claim. |
| space_id | String | Yes | Target space id when safely known. |
| category | String | No | Event category enum. |
| action | String | No | Stable action enum/string. |
| result | String | No | `SUCCEEDED`, `DENIED`, `FAILED`, `CONFLICT`, `SKIPPED`, or `SAFE_NOT_FOUND`. |
| severity | String | No | `INFO`, `NOTICE`, `WARNING`, or `SECURITY`. |
| target_type | String | Yes | Safe target type. |
| target_id | String | Yes | Safe target id. |
| request_id | String | Yes | Safe correlation id. |
| safe_summary | String | Yes | Human-readable safe summary. |
| metadata | JSON | No | Bounded safe metadata object. |

Constraints:

- No update/delete product API.
- Metadata allowlist only: counts, ids, enum states, reason codes, route patterns, and booleans.
- Index `(space_id, created_at desc)`, `(target_type, target_id, created_at desc)`, `(actor_user_id, created_at desc)`, and `(category, result, created_at desc)`.

## Enums

### AuditCategory

`AUTH`, `MEMBERSHIP`, `REVIEW`, `PUBLISH`, `WIKI`, `GRAPH`, `ASK`, `MODEL`, `ADAPTER`, `SETTINGS`

### AuditResult

`SUCCEEDED`, `DENIED`, `FAILED`, `CONFLICT`, `SKIPPED`, `SAFE_NOT_FOUND`

### AuditSeverity

`INFO`, `NOTICE`, `WARNING`, `SECURITY`

## Safe Metadata Examples

| Operation | Allowed metadata |
|---|---|
| Auth denied | method, routePattern, reasonCode, statusCode |
| Membership change | targetUserId, previousRole, nextRole, previousStatus, nextStatus |
| Wiki ingest | runId, createdCount, updatedCount, skippedCount, failedCount |
| Graph projection | runId, nodeCount, edgeCount, skippedCount |
| Ask create | askRunId, citationCount, reviewPolicy, resultStatus |
| Model configuration | providerKey, configuredFlagChanged, adapterStatus |

Forbidden metadata:

- Raw request or response bodies.
- Raw prompts or generated answers.
- Raw source document content.
- API keys, tokens, passwords, provider payloads.
- Private paths, internal hostnames, stack traces, raw runtime logs.

## Relationship To Existing Graph Audit

`atlas.graph_audit_record` exists as graph-specific governance evidence. This slice should preserve it and add a documented bridge or dual-write path rather than dropping it. A future cleanup slice may consolidate graph audit storage after acceptance.

## Traceability

- `AuditEvent` maps to REQ-AUDIT-LOG-FOUNDATION-001, REQ-AUDIT-LOG-FOUNDATION-002, T-AUDIT-LOG-FOUNDATION-001, and T-AUDIT-LOG-FOUNDATION-002.
- Audit enums and indexes map to REQ-AUDIT-LOG-FOUNDATION-007, REQ-AUDIT-LOG-FOUNDATION-011, and T-AUDIT-LOG-FOUNDATION-006.
- The graph audit bridge note maps to REQ-AUDIT-LOG-FOUNDATION-006 and T-AUDIT-LOG-FOUNDATION-005.
