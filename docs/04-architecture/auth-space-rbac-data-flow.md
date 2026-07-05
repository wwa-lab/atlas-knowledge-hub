# Data Flow: auth-space-rbac

Status: Draft SDD
Last updated: 2026-07-05

## Overview

This document describes runtime flows for current-user resolution, protected API authorization, membership management, and frontend permission awareness for REQ-AUTH-SPACE-RBAC-001 through REQ-AUTH-SPACE-RBAC-014.

## Flow 1: Frontend Boot And Current User Context

```text
Vue App
  │
  │ GET /api/auth/me
  ▼
Auth Controller
  │
  │ resolve current user
  ▼
Current User Resolver
  │
  ├── local/test → Mock Auth Provider
  │
  └── future prod → SSO/OIDC Provider Boundary
  │
  ▼
Membership Service
  │
  │ load active memberships and capabilities
  ▼
Auth Me Response
  │
  ▼
Vue Auth Context Store
```

Data handled:

- Safe profile: id, displayName, email.
- Safe membership list: space id/name, role, status.
- Capabilities: action names such as `SPACE_READ`, `BATCH_CREATE`, `WIKI_PUBLISH`, `MEMBER_MANAGE`.

Failure behavior:

- Missing context returns `401` for protected APIs.
- `/api/auth/me` in local/test mode returns a configured mock user.

## Flow 2: Protected API Authorization

```text
Client Request
  │
  ▼
Auth Filter / Interceptor
  │
  ├─ no current user ───────────────► 401 UNAUTHORIZED envelope
  │
  ▼
Authorization Policy
  │
  ├─ no membership / no capability ─► 403 FORBIDDEN envelope
  │
  ▼
Existing Controller / Service
  │
  ▼
ApiEnvelope success or domain validation error
```

Permission examples:

| Domain | Read role | Write role |
|---|---|---|
| Space metadata | `VIEWER`, `EDITOR`, `KNOWLEDGE_MANAGER`, `SPACE_OWNER`, `AUDITOR`, `PLATFORM_ADMIN` | `SPACE_OWNER`, `PLATFORM_ADMIN` |
| Batch/file/chunk | `VIEWER`+ | `EDITOR`, `KNOWLEDGE_MANAGER`, `SPACE_OWNER`, `PLATFORM_ADMIN` |
| Review/publish/Wiki operations | `VIEWER`+ for read | `KNOWLEDGE_MANAGER`, `SPACE_OWNER`, `PLATFORM_ADMIN` |
| Graph/Ask | `VIEWER`+ for read reports | `KNOWLEDGE_MANAGER`, `SPACE_OWNER`, `PLATFORM_ADMIN` |
| Model/storage/vector/parser settings | `SPACE_OWNER`, `PLATFORM_ADMIN` for read | `SPACE_OWNER`, `PLATFORM_ADMIN` |
| Membership | `SPACE_OWNER`, `PLATFORM_ADMIN` for read/write | `SPACE_OWNER`, `PLATFORM_ADMIN` |

## Flow 3: Membership Change

```text
PATCH /api/spaces/{spaceId}/members/{membershipId}
  │
  ▼
Auth Guard: require SPACE_OWNER or PLATFORM_ADMIN
  │
  ├─ denied ───────────────────────► 403
  │
  ▼
Membership Service
  │
  ├─ would remove last SPACE_OWNER ─► 409 CONFLICT
  │
  ├─ invalid role/status ───────────► 400 VALIDATION_ERROR
  │
  ▼
Persist membership update
  │
  ▼
Return safe membership response
```

State transitions:

```text
INVITED ──accept/admin activate──► ACTIVE
ACTIVE ──suspend───────────────► SUSPENDED
SUSPENDED ──reactivate─────────► ACTIVE
INVITED/ACTIVE/SUSPENDED ──────► REMOVED
REMOVED ──new invitation only──► INVITED
```

Invariant:

- A space must always retain at least one active `SPACE_OWNER`.

## Flow 4: Cross-Space Resource Protection

```text
Request resource id
  │
  ▼
Resolve caller permissions first
  │
  ├─ no access to resource's space ─► generic denied response
  │
  ▼
Load resource within permitted scope
  │
  ├─ not found in permitted scope ──► safe not found
  │
  ▼
Return resource
```

Non-disclosure rule:

- Do not reveal protected titles, source paths, owner emails, graph labels, Ask question text, model endpoints, or settings state for resources outside the caller's permitted spaces.

## Error Cascade

| Condition | Status | Code | Body constraints |
|---|---|---|---|
| Missing auth context | `401` | `UNAUTHORIZED` | No provider details or raw token hints |
| Authenticated but role denied | `403` | `FORBIDDEN` | Generic action denial |
| Invalid membership mutation | `400` | `VALIDATION_ERROR` | Field-level safe validation |
| Last owner violation | `409` | `CONFLICT` | Safe owner-invariant message |
| Resource outside membership scope | `403` or safe `404` | `FORBIDDEN` or `NOT_FOUND` | No protected resource detail |

## Refresh Strategy

- `/api/auth/me` is loaded on app boot and after active-space or membership-changing actions.
- Backend authorization never relies on a stale frontend cache.
- E2E tests must exercise role switching through mock users or test headers, not hardcoded graph-only headers.

## Traceability

- REQ-AUTH-SPACE-RBAC-001, REQ-AUTH-SPACE-RBAC-004 map to Flow 1.
- REQ-AUTH-SPACE-RBAC-007, REQ-AUTH-SPACE-RBAC-009, REQ-AUTH-SPACE-RBAC-010 map to Flow 2.
- REQ-AUTH-SPACE-RBAC-012 maps to Flow 3.
- REQ-AUTH-SPACE-RBAC-011 maps to Flow 4.
