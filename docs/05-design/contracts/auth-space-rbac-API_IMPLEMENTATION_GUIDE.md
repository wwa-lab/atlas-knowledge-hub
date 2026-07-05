# auth-space-rbac — API Implementation Guide

Date: 2026-07-05
Status: Draft SDD
Base path: `/api`
Backend stack: Spring Boot metadata API
Auth model: Backend-enforced mock auth now; future SSO/OIDC boundary; no live external provider calls in this slice.

## Overview

This guide defines the auth and membership API contracts for REQ-AUTH-SPACE-RBAC-001 through REQ-AUTH-SPACE-RBAC-014 and the guard expectations for existing Atlas APIs.

## Authentication

Local/test:

- Provider: `mock`.
- User context comes from safe mock headers or configured local/test default user.
- Mock identities must be sample-safe and committed only as fixtures or seed data.

Future production:

- Provider boundary: `oidc`.
- Live SSO/OIDC setup, secrets, group mapping, and provider calls are out of scope.

## Error Response Format

All errors use the existing Atlas envelope:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "FORBIDDEN",
    "message": "Action is not allowed.",
    "fields": null,
    "timestamp": 1783180800000,
    "path": "/api/spaces/space-ibmi/batches"
  },
  "meta": null
}
```

Error table:

| Status | Code | When |
|---|---|---|
| 401 | `UNAUTHORIZED` | Protected API lacks current user context. |
| 403 | `FORBIDDEN` | User is authenticated but lacks capability. |
| 400 | `VALIDATION_ERROR` | Invalid role, status, email, or request shape. |
| 409 | `CONFLICT` | Membership change violates owner invariant. |
| 404 | `NOT_FOUND` | Safe not-found within permitted scope. |

## API Endpoints Summary

| Operation | Method | Endpoint | Auth |
|---|---|---|---|
| Current user context | GET | `/api/auth/me` | Authenticated mock/local context |
| List members | GET | `/api/spaces/{spaceId}/members` | `SPACE_OWNER`, `PLATFORM_ADMIN`; `AUDITOR` read may be added later |
| Create member/invitation | POST | `/api/spaces/{spaceId}/members` | `SPACE_OWNER`, `PLATFORM_ADMIN` |
| Update member | PATCH | `/api/spaces/{spaceId}/members/{membershipId}` | `SPACE_OWNER`, `PLATFORM_ADMIN` |
| Remove member | DELETE | `/api/spaces/{spaceId}/members/{membershipId}` | `SPACE_OWNER`, `PLATFORM_ADMIN` |

## Endpoint Reference

### GET `/api/auth/me`

Purpose: Return current user context, memberships, active space, roles, and capabilities.

Example response:

```json
{
  "success": true,
  "data": {
    "user": {
      "id": "mock-owner",
      "email": "owner@example.test",
      "displayName": "Atlas Owner",
      "status": "ACTIVE",
      "globalRoles": []
    },
    "activeSpaceId": "space-ibmi",
    "memberships": [
      {
        "id": "membership-owner-ibmi",
        "spaceId": "space-ibmi",
        "spaceName": "IBM i Modernization",
        "role": "SPACE_OWNER",
        "status": "ACTIVE"
      }
    ],
    "capabilities": [
      "SPACE_READ",
      "BATCH_CREATE",
      "WIKI_PUBLISH",
      "MEMBER_MANAGE"
    ]
  },
  "error": null,
  "meta": null
}
```

Validation:

- User must be active.
- Disabled user returns `403`.

### GET `/api/spaces/{spaceId}/members`

Purpose: List safe members for a Knowledge Space.

Response fields:

| Field | Type | Required |
|---|---|---|
| id | string | Yes |
| userId | string | Yes |
| email | string | Yes |
| displayName | string | Yes |
| role | enum | Yes |
| status | enum | Yes |
| createdAt | timestamp | Yes |
| updatedAt | timestamp | Yes |

### POST `/api/spaces/{spaceId}/members`

Purpose: Create a mock-safe invitation or active membership.

Example request:

```json
{
  "email": "reviewer@example.test",
  "displayName": "Atlas Reviewer",
  "role": "KNOWLEDGE_MANAGER",
  "status": "INVITED"
}
```

Rules:

- Email must be sample-safe in committed fixtures.
- Role must be from the accepted role matrix.
- Caller must be `SPACE_OWNER` for the target space or `PLATFORM_ADMIN`.

### PATCH `/api/spaces/{spaceId}/members/{membershipId}`

Purpose: Update role or status.

Example request:

```json
{
  "role": "VIEWER",
  "status": "ACTIVE"
}
```

Rules:

- Cannot remove or downgrade the last active `SPACE_OWNER`.
- `PLATFORM_ADMIN` can update across spaces.

### DELETE `/api/spaces/{spaceId}/members/{membershipId}`

Purpose: Mark a membership as `REMOVED`.

Rules:

- Physical deletion is not required.
- Last-owner invariant applies.

## Protected Existing APIs

| Domain | Representative endpoints | Required behavior |
|---|---|---|
| Space | `/api/spaces`, `/api/spaces/{spaceId}` | Read requires membership/global admin; create requires `PLATFORM_ADMIN` or accepted local owner setup. |
| Batch/file/chunk | `/api/spaces/{spaceId}/batches`, `/api/batches/{batchId}/files`, `/api/files/{fileId}/chunks` | Read requires membership; writes require `EDITOR`+. |
| Review/publish/Wiki | `/api/spaces/{spaceId}/review-queues`, `/api/files/{fileId}/publish`, `/api/spaces/{spaceId}/wiki-pages` | Reads require membership; writes require `KNOWLEDGE_MANAGER`+. |
| Graph/Ask | `/api/spaces/{spaceId}/graph`, `/api/spaces/{spaceId}/ask` | Reads require membership; graph projection and Ask create require `KNOWLEDGE_MANAGER`+. |
| Settings/adapters | `/api/model-configurations/deepseek`, `/api/storage-adapters`, `/api/vector-adapters`, `/api/parser-adapters` | Reads/writes use role matrix; raw secrets never returned. |

## State Reference

```text
INVITED -> ACTIVE -> SUSPENDED -> ACTIVE
INVITED/ACTIVE/SUSPENDED -> REMOVED
```

Role values:

- `VIEWER`
- `EDITOR`
- `KNOWLEDGE_MANAGER`
- `SPACE_OWNER`
- `AUDITOR`
- `PLATFORM_ADMIN`

## Concurrency

- Membership updates should reject stale or invariant-breaking changes.
- Last-owner validation must run inside the same transaction as membership status/role update.

## Integration Dependencies

- Existing Atlas API envelope and global exception handling.
- Flyway migration for `atlas_user` and `space_membership`.
- No external SSO/OIDC, SMTP, directory, or secret-manager dependency in this slice.

## Testing Contracts

- `VIEWER` write calls return `403`.
- Missing auth returns `401`.
- `SPACE_OWNER` can list and update members.
- Last owner removal returns `409`.
- `/api/auth/me` returns capabilities and no raw secrets.
