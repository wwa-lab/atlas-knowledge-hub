# Requirements: auth-space-rbac

Status: Draft SDD
Last updated: 2026-07-05
Slice: `auth-space-rbac`
Wave: Wave 3 / Trust And Governance

## Purpose

Atlas needs a backend-enforced authentication and space-scoped RBAC foundation before controlled internal beta. This slice turns the current internal/mock-safe API posture into a consistent auth context, role matrix, membership model, and authorization guard plan without implementing external company SSO calls or production account administration.

## Grounded Current State

| Evidence | Verified current state |
|---|---|
| `backend/pom.xml:37` | Backend uses Spring Web, JPA, validation, Flyway, PostgreSQL, and tests; no Spring Security dependency is currently present in the checked dependency list. |
| `backend/src/main/java/com/atlas/metadata/controller/GraphController.java:35` | Graph has a local header-based role guard for `VIEWER`, `REVIEWER`, and `ADMIN`. |
| `backend/src/main/java/com/atlas/metadata/controller/SpaceController.java:35` | Space list/detail/create endpoints currently do not require an authenticated user context. |
| `backend/src/main/java/com/atlas/metadata/controller/ReviewPublishController.java:37` | Review queue and Wiki endpoints currently do not require an authenticated user context. |
| `backend/src/main/java/com/atlas/metadata/controller/AskController.java:29` | Ask run creation currently does not require an authenticated user context. |
| `backend/src/main/java/com/atlas/metadata/domain/Space.java:37` | `Space` has an `owner` string, but no persisted membership model. |
| `backend/src/main/resources/db/migration/V1__init_schema.sql:3` | Base metadata schema has `space`, `batch`, `file_item`, `source_chunk`, `review_record`, `wiki_page`, `graph_node`, and `graph_edge`; no user or membership table exists in V1. |
| `frontend/src/api.ts:37` | Frontend currently injects graph-only demo headers for graph read/admin calls. |

## Requirements

| ID | Priority | Requirement |
|---|---|---|
| REQ-AUTH-SPACE-RBAC-001 | Must | The backend must derive a current Atlas user context for every protected API request. |
| REQ-AUTH-SPACE-RBAC-002 | Must | The auth layer must support a local mock auth provider for development, tests, and E2E without external network calls. |
| REQ-AUTH-SPACE-RBAC-003 | Must | The auth layer must expose a company SSO/OIDC adapter boundary for future production integration without binding controllers to one identity provider. |
| REQ-AUTH-SPACE-RBAC-004 | Must | The current user context must include user id, display name, email, global roles, space memberships, active space, and effective space roles. |
| REQ-AUTH-SPACE-RBAC-005 | Must | Atlas must persist users and space memberships using additive Flyway-managed schema changes. |
| REQ-AUTH-SPACE-RBAC-006 | Must | The role matrix must include `VIEWER`, `EDITOR`, `KNOWLEDGE_MANAGER`, `SPACE_OWNER`, `AUDITOR`, and `PLATFORM_ADMIN`. |
| REQ-AUTH-SPACE-RBAC-007 | Must | Backend RBAC guards must cover space, batch, file, chunk, review/publish, Wiki, graph, Ask, and model/storage/vector/parser settings APIs. |
| REQ-AUTH-SPACE-RBAC-008 | Must | Frontend permission awareness may hide or disable controls, but it must not be treated as a security boundary. |
| REQ-AUTH-SPACE-RBAC-009 | Must | Unauthenticated protected API requests must return `401` through the Atlas API envelope. |
| REQ-AUTH-SPACE-RBAC-010 | Must | Authenticated but unauthorized protected API requests must return `403` through the Atlas API envelope without leaking sensitive details. |
| REQ-AUTH-SPACE-RBAC-011 | Must | Cross-space resource access must not reveal whether a protected resource exists outside the caller's permitted space. |
| REQ-AUTH-SPACE-RBAC-012 | Must | Space membership write operations must be restricted to `SPACE_OWNER` or `PLATFORM_ADMIN` and must protect the last active space owner. |
| REQ-AUTH-SPACE-RBAC-013 | Should | Audit events emitted by this slice may be minimal, but auth and RBAC decisions must carry actor, space, action, target, and result fields for the later `audit-log-foundation` slice. |
| REQ-AUTH-SPACE-RBAC-014 | Must | The slice must use mock/sample identities only and must not commit real company users, domains, tokens, passwords, private paths, or provider logs. |

## Acceptance Criteria

| ID | Requirement | Criterion |
|---|---|---|
| AC-AUTH-SPACE-RBAC-001 | REQ-AUTH-SPACE-RBAC-001 | A protected endpoint without current user context returns `401` with `success=false` and a user-safe error body. |
| AC-AUTH-SPACE-RBAC-002 | REQ-AUTH-SPACE-RBAC-007 | A `VIEWER` calling write APIs, including batch create, review action, publish, graph projection, Ask create, and settings write, receives `403`. |
| AC-AUTH-SPACE-RBAC-003 | REQ-AUTH-SPACE-RBAC-011 | A user requesting a resource in a space where they have no membership receives a generic denied response that does not disclose resource metadata. |
| AC-AUTH-SPACE-RBAC-004 | REQ-AUTH-SPACE-RBAC-006 | Tests cover at least ordinary user, `KNOWLEDGE_MANAGER`, and `SPACE_OWNER` role differences. |
| AC-AUTH-SPACE-RBAC-005 | REQ-AUTH-SPACE-RBAC-008 | Frontend role-aware UI states are backed by `/api/auth/me` and do not hardcode graph-only demo headers. |
| AC-AUTH-SPACE-RBAC-006 | REQ-AUTH-SPACE-RBAC-014 | Secret/private-path scans and dependency/network scans show no new raw credentials, private paths, real identities, or external cloud calls. |

## Scope

In scope:

- Auth context abstraction and local mock auth provider.
- Additive user and space membership data model.
- Role matrix and permission policy.
- Backend guard integration for existing API domains.
- `/api/auth/me` and membership management API contracts.
- Frontend permission-aware state using API-provided capabilities.
- Tests and E2E for `VIEWER`, `KNOWLEDGE_MANAGER`, and `SPACE_OWNER`.

Out of scope:

- Production SSO/OIDC provider configuration and live identity-provider calls.
- Password registration/login, account recovery, MFA, and SCIM/directory sync.
- Full audit event retention and audit UI beyond minimum event shape.
- Secret manager integration, rate limiting, deployment hardening, or production operations.
- Real company identities, credentials, domains, or logs.

## Constraints

- Backend enforcement is mandatory; UI-only authorization is not acceptable.
- All schema changes must be Flyway migrations.
- Auth provider and future SSO/OIDC integration must sit behind adapter-style boundaries.
- Existing parser/converter/model/vector/storage adapter boundaries must remain untouched.
- Default development and CI paths must remain mock/sample-safe and offline.
