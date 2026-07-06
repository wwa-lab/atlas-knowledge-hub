# audit-log-foundation API Implementation Guide

Status: Draft for human review
Last updated: 2026-07-06
Base path: `/api`
Auth model: `auth-space-rbac` current-user and capability enforcement.

## Overview

This guide defines read-only audit APIs and internal audit event contracts. Clients cannot create, update, or delete audit events.

## Authentication And Authorization

Audit read endpoints require governance-read capability for the target space. `PLATFORM_ADMIN` may read mock/internal global scopes if explicitly supported. `VIEWER` and `EDITOR` do not receive audit-read access by default.

Denied responses use the standard `ApiEnvelope` error shape.

## Error Response Format

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "FORBIDDEN",
    "message": "The current user is not allowed to perform this action.",
    "fields": null,
    "timestamp": 1783372800000,
    "path": "/api/spaces/ibm-i-modernization/audit-events"
  },
  "meta": null
}
```

## API Endpoints Summary

| Operation | Method | Endpoint | Auth |
|---|---|---|---|
| List space audit events | GET | `/api/spaces/{spaceId}/audit-events` | Governance read for space |
| List target audit events | GET | `/api/spaces/{spaceId}/audit-events/targets/{targetType}/{targetId}` | Governance read for space |

## Endpoint Reference

### List Space Audit Events

`GET /api/spaces/{spaceId}/audit-events`

Query parameters:

| Name | Type | Required | Notes |
|---|---|---|---|
| actorUserId | string | No | Safe Atlas user id. |
| category | string | No | Audit category enum. |
| action | string | No | Stable action string. |
| result | string | No | Audit result enum. |
| targetType | string | No | Safe target type. |
| targetId | string | No | Safe target id. |
| createdFrom | ISO timestamp | No | Inclusive lower bound. |
| createdTo | ISO timestamp | No | Exclusive upper bound. |
| page | integer | No | Default `0`. |
| size | integer | No | Bounded by implementation. |

Example response:

```json
{
  "success": true,
  "data": [
    {
      "id": "audit-001",
      "createdAt": "2026-07-06T00:00:00Z",
      "actorUserId": "mock-owner",
      "actorDisplay": "Atlas Owner",
      "spaceId": "ibm-i-modernization",
      "category": "MEMBERSHIP",
      "action": "MEMBERSHIP_UPDATE",
      "result": "SUCCEEDED",
      "severity": "NOTICE",
      "targetType": "space_membership",
      "targetId": "mock-viewer",
      "requestId": "req-001",
      "safeSummary": "Space membership role updated.",
      "metadata": {
        "previousRole": "VIEWER",
        "nextRole": "AUDITOR"
      }
    }
  ],
  "error": null,
  "meta": {
    "page": 0,
    "size": 25,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### List Target Audit Events

`GET /api/spaces/{spaceId}/audit-events/targets/{targetType}/{targetId}`

Purpose: list audit history for one permitted target.

Validation:

- `targetType` and `targetId` are required.
- Cross-space targets must not disclose existence.
- Pagination and time filters match the space audit list endpoint.

## Internal Event Command Contract

Internal code should pass a command with:

| Field | Required | Notes |
|---|---|---|
| actorUserId | No | Null for anonymous/system-safe events. |
| actorDisplay | No | Safe display label. |
| spaceId | No | Required for space-scoped event when safely known. |
| category | Yes | Enum. |
| action | Yes | Stable action. |
| result | Yes | Enum. |
| severity | Yes | Enum. |
| targetType | No | Safe target type. |
| targetId | No | Safe target id. |
| requestId | No | Correlation id. |
| safeSummary | No | No sensitive content. |
| metadata | No | Allowlisted safe JSON only. |

Forbidden in commands:

- Raw request/response bodies.
- Raw source content or generated content.
- Raw prompt/answer/provider payload.
- Tokens, credentials, API keys, passwords.
- Private paths, internal hostnames, stack traces, raw runtime logs.

## Testing Contracts

- Unauthorized read returns `401`.
- Viewer/editor audit read returns `403` or safe `404`.
- Auditor/owner/manager permitted read returns safe DTOs.
- Invalid filter returns `400`.
- Cross-space target query does not leak event existence.
- Stored events do not contain forbidden sensitive strings.

## Traceability

- Space and target list endpoints cover REQ-AUDIT-LOG-FOUNDATION-007, REQ-AUDIT-LOG-FOUNDATION-008, REQ-AUDIT-LOG-FOUNDATION-009, T-AUDIT-LOG-FOUNDATION-006, and AC-AUDIT-LOG-FOUNDATION-004.
- Internal event command contract covers REQ-AUDIT-LOG-FOUNDATION-001, REQ-AUDIT-LOG-FOUNDATION-002, REQ-AUDIT-LOG-FOUNDATION-005, T-AUDIT-LOG-FOUNDATION-003, and T-AUDIT-LOG-FOUNDATION-005.
