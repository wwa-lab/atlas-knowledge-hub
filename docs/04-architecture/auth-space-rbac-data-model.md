# Data Model: auth-space-rbac

Status: Draft SDD
Last updated: 2026-07-05

## Overview

This slice adds an additive user and membership model to support REQ-AUTH-SPACE-RBAC-001 through REQ-AUTH-SPACE-RBAC-014. Existing `atlas.space` remains the space root; membership becomes the source of authorization truth.

## Entity Relationship Diagram

```text
┌──────────────────────┐        1:N        ┌──────────────────────────┐
│ atlas.atlas_user      │──────────────────►│ atlas.space_membership   │
│ id PK                 │                   │ id PK                    │
│ email                 │                   │ user_id FK               │
│ display_name          │                   │ space_id FK              │
│ status                │                   │ role                     │
│ global_roles          │                   │ status                   │
└──────────────────────┘                   └────────────┬─────────────┘
                                                         │ N:1
                                                         ▼
                                             ┌──────────────────────────┐
                                             │ atlas.space              │
                                             │ id PK                    │
                                             │ owner                    │
                                             │ status                   │
                                             └──────────────────────────┘
```

## Entity Definitions

### AtlasUser

- **Table name:** `atlas.atlas_user`
- **Purpose:** Safe internal user profile for Atlas authorization.

| Column | Type | Nullable | Description |
|---|---|---|---|
| id | String | No | Stable Atlas user id. |
| email | String | No | Mock/sample-safe email; unique. |
| display_name | String | No | User-facing display name. |
| status | String | No | `ACTIVE` or `DISABLED`. |
| global_roles | String array | No | Global roles such as `PLATFORM_ADMIN`; empty array when none. |
| auth_subject | String | Yes | Future provider subject reference; mock-safe only in this slice. |
| created_at | Timestamp | No | Creation time. |
| updated_at | Timestamp | No | Last update time. |

Constraints:

- Unique email.
- `status` in `ACTIVE`, `DISABLED`.
- No raw tokens, passwords, provider payloads, or real company data.

### SpaceMembership

- **Table name:** `atlas.space_membership`
- **Purpose:** Space-scoped role assignment and membership state.

| Column | Type | Nullable | Description |
|---|---|---|---|
| id | String | No | Stable membership id. |
| space_id | String | No | FK to `atlas.space.id`. |
| user_id | String | No | FK to `atlas.atlas_user.id`. |
| role | String | No | One role from the role matrix. |
| status | String | No | `INVITED`, `ACTIVE`, `SUSPENDED`, or `REMOVED`. |
| invited_by_user_id | String | Yes | User id that created the membership/invitation. |
| created_at | Timestamp | No | Creation time. |
| updated_at | Timestamp | No | Last update time. |

Constraints:

- Unique active-style membership per `space_id` and `user_id`.
- `role` in `VIEWER`, `EDITOR`, `KNOWLEDGE_MANAGER`, `SPACE_OWNER`, `AUDITOR`, `PLATFORM_ADMIN`.
- `status` in `INVITED`, `ACTIVE`, `SUSPENDED`, `REMOVED`.
- Service-level invariant: at least one active `SPACE_OWNER` per space.

### AuthDecision

- **Persistence:** Transient event shape in this slice; full persistence belongs to `audit-log-foundation`.
- **Purpose:** Preserve minimum fields needed to add audit persistence later.

| Field | Type | Description |
|---|---|---|
| requestId | String | Safe correlation id. |
| actorUserId | String | Current user id or `anonymous`. |
| role | String | Effective role used for decision. |
| spaceId | String | Target space id when available. |
| action | String | Capability/action name. |
| targetType | String | Target resource type. |
| targetId | String | Safe target id when available. |
| result | String | `ALLOWED`, `UNAUTHENTICATED`, `FORBIDDEN`, `SAFE_NOT_FOUND`, `CONFLICT`. |

## State Models

### User Status

```text
ACTIVE ──admin disable──► DISABLED
DISABLED ──admin enable──► ACTIVE
```

### Membership Status

```text
INVITED ──accept/admin activate──► ACTIVE
ACTIVE ──suspend───────────────► SUSPENDED
SUSPENDED ──reactivate─────────► ACTIVE
INVITED/ACTIVE/SUSPENDED ──────► REMOVED
REMOVED ──new invitation only──► INVITED
```

### Role Matrix

| Capability | VIEWER | EDITOR | KNOWLEDGE_MANAGER | SPACE_OWNER | AUDITOR | PLATFORM_ADMIN |
|---|---|---|---|---|---|---|
| Read space content | Yes | Yes | Yes | Yes | Yes | Yes |
| Create/update batch, file, parser/conversion/storage runs | No | Yes | Yes | Yes | No | Yes |
| Review, publish, Wiki ingest/linkify/lint | No | No | Yes | Yes | No | Yes |
| Graph projection/review and Ask create | No | No | Yes | Yes | No | Yes |
| Read governance metadata | No | No | Yes | Yes | Yes | Yes |
| Manage space membership | No | No | No | Yes | No | Yes |
| Manage provider/settings configuration | No | No | No | Yes | No | Yes |

## Configuration Entities

| Config | Type | Description |
|---|---|---|
| auth.provider | enum | `mock` in this slice; future `oidc`. |
| auth.mock.default-user | string | Safe mock user id used in local/test mode. |
| auth.mock.require-header | boolean | Whether local/test requests must provide a mock user header. |

## Field Mapping

| Source | Target |
|---|---|
| Mock auth header/default | `AtlasUser.id` lookup |
| `/api/auth/me` | Safe user profile + memberships + capabilities |
| Space member API request | `SpaceMembership.role` and `SpaceMembership.status` |
| Existing `space.owner` | Migration seed hint for initial `SPACE_OWNER` membership |

## Traceability

- REQ-AUTH-SPACE-RBAC-004 maps to `AtlasUser` and `/api/auth/me` fields.
- REQ-AUTH-SPACE-RBAC-005 maps to `AtlasUser` and `SpaceMembership`.
- REQ-AUTH-SPACE-RBAC-006 maps to role enum and role matrix.
- REQ-AUTH-SPACE-RBAC-012 maps to last-owner invariant.
- REQ-AUTH-SPACE-RBAC-013 maps to `AuthDecision`.
