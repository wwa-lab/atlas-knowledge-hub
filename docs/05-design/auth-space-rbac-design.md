# Detailed Design: auth-space-rbac

Status: Draft SDD
Last updated: 2026-07-05

## Overview

This design turns REQ-AUTH-SPACE-RBAC-001 through REQ-AUTH-SPACE-RBAC-014 into implementable backend, frontend, API, data, and verification decisions. It introduces backend enforcement while keeping local/test mock auth and no live SSO/OIDC calls.

## Source Architecture

Source: `docs/04-architecture/auth-space-rbac-architecture.md`

Grounding anchors:

- Existing graph guard reads `X-Atlas-User` and `X-Atlas-Role` in `backend/src/main/java/com/atlas/metadata/controller/GraphController.java:54`.
- Existing API envelope is `backend/src/main/java/com/atlas/metadata/dto/ApiEnvelope.java:4`.
- Existing safe error body is `backend/src/main/java/com/atlas/metadata/dto/ErrorBody.java:6`.
- Existing frontend graph demo headers are in `frontend/src/api.ts:37`.

## Design Assumptions

- [ASSUMPTION] Spring Security can be introduced as the request authentication/authorization substrate if tests preserve the Atlas envelope and local mock profile.
- [ASSUMPTION] Existing seeded space owners from `space.owner` are sufficient to create initial mock `SPACE_OWNER` memberships during migration/seed.
- [ASSUMPTION] The implementation may use headers only for mock/test auth; production SSO remains out of scope.

## Design Scope

In scope:

- Auth context resolver and provider boundary.
- Role enum, capability enum, authorization policy.
- User/membership persistence and service logic.
- Guard integration for existing API domains.
- `/api/auth/me` and membership APIs.
- Vue auth state and permission-aware controls.
- Integration/E2E role coverage.

Out of scope:

- Real SSO/OIDC provider calls.
- Password auth, account recovery, MFA, SCIM, directory sync.
- Secret manager, rate limiting, production audit UI.
- Broad UI redesign.

## Module Design

### Auth Provider Module

Responsibilities:

- Resolve current user for protected requests.
- Support `mock` provider in local/test.
- Expose a stable provider interface for future `oidc`.
- Fail closed when no context is available on protected APIs.

Inputs:

- Mock headers or local/test default user id.
- Future provider claims through boundary only.

Outputs:

- Current Atlas user id.
- Optional active space hint.

### Current User Context Module

Responsibilities:

- Load `AtlasUser`.
- Load active memberships.
- Derive global roles, active space, effective space roles, and capabilities.
- Provide `/api/auth/me` response DTO.

### Authorization Policy Module

Responsibilities:

- Map API domain/action/resource scope to required capabilities.
- Use membership status and role to allow or deny.
- Convert denied results to safe `401`, `403`, or safe not-found behavior.
- Replace or wrap graph-local role checks.

Permission decisions:

- `VIEWER`: read-only.
- `EDITOR`: content ingestion/update, no review/publish/settings/member writes.
- `KNOWLEDGE_MANAGER`: knowledge operations including review, publish, graph, vector, Ask, Wiki maintenance.
- `SPACE_OWNER`: all space-scoped operations including membership and settings.
- `AUDITOR`: read governance metadata only.
- `PLATFORM_ADMIN`: platform-wide admin.

### Membership Module

Responsibilities:

- List members for a space.
- Create invitation/membership records.
- Update role/status.
- Remove memberships using `REMOVED` status.
- Protect last active `SPACE_OWNER`.

### Frontend Auth Module

Responsibilities:

- Load `/api/auth/me` during app startup.
- Store current user, memberships, active space, and capabilities.
- Render disabled/hidden controls for denied actions.
- Remove graph-only hardcoded role headers from primary product authorization.

## API / Interface Design

Authoritative details are in `docs/05-design/contracts/auth-space-rbac-API_IMPLEMENTATION_GUIDE.md`.

Endpoint summary:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/auth/me` | Return current user context and capabilities. |
| GET | `/api/spaces/{spaceId}/members` | List safe members in a space. |
| POST | `/api/spaces/{spaceId}/members` | Create a mock invitation/membership. |
| PATCH | `/api/spaces/{spaceId}/members/{membershipId}` | Update role/status. |
| DELETE | `/api/spaces/{spaceId}/members/{membershipId}` | Mark membership removed. |

Protected existing APIs:

- All space, batch, file, chunk, review/publish, Wiki, graph, Ask, model, storage, vector, parser, conversion, ingestion, and downstream-refresh APIs must pass through authorization.

## Data Design

Data model source: `docs/04-architecture/auth-space-rbac-data-model.md`.

Persistent tables:

- `atlas.atlas_user`
- `atlas.space_membership`

Enum decisions:

- User status: `ACTIVE`, `DISABLED`.
- Membership status: `INVITED`, `ACTIVE`, `SUSPENDED`, `REMOVED`.
- Roles: `VIEWER`, `EDITOR`, `KNOWLEDGE_MANAGER`, `SPACE_OWNER`, `AUDITOR`, `PLATFORM_ADMIN`.

## UI / User Flow Design

1. User opens Atlas.
2. Vue loads `/api/auth/me`.
3. If authenticated, shell renders user identity and current-space capabilities.
4. Write actions that are not permitted are disabled or hidden.
5. If a backend call returns `401` or `403`, UI shows a safe message and does not display protected resource metadata.
6. Space Owner views member management with allowed invite/update/remove controls.

## Workflow / Execution Design

Protected write example:

1. User clicks publish.
2. Frontend checks `WIKI_PUBLISH` capability for UX.
3. Backend receives publish request.
4. Auth layer resolves current user.
5. Authorization policy requires `KNOWLEDGE_MANAGER`, `SPACE_OWNER`, or `PLATFORM_ADMIN`.
6. If allowed, existing publish service runs.
7. If denied, response is `403` and contains no file title, source path, or reviewer detail beyond safe error fields.

## Security / Audit / Reliability Design

- The backend is the source of truth for permissions.
- Mock auth data must be sample-safe.
- All denied responses use existing `ApiEnvelope` and `ErrorBody`.
- Auth decision event shape is generated for later audit persistence but full audit retention is a later slice.
- Membership changes validate last-owner invariant.
- Future provider failures fail closed.

## Validation and Error Handling

| Case | Response |
|---|---|
| Missing auth on protected API | `401 UNAUTHORIZED` envelope |
| Authenticated but missing capability | `403 FORBIDDEN` envelope |
| Invalid role/status | `400 VALIDATION_ERROR` |
| Last active owner removal | `409 CONFLICT` |
| Cross-space resource id | Generic `403` or safe `404` without protected details |

## Testing Considerations

- Unit tests: role matrix, capability mapping, membership invariant.
- Integration tests: representative endpoints for `VIEWER`, `KNOWLEDGE_MANAGER`, `SPACE_OWNER`.
- API contract tests: `/api/auth/me`, membership APIs, `401`, `403`, `409`.
- Frontend tests: auth store and permission-aware controls.
- E2E tests: role-specific path differences.
- Safety scans: no real identities, raw secrets, private paths, or external cloud calls.

## Risks / Design Tradeoffs

| Risk | Tradeoff / mitigation |
|---|---|
| Spring Security may alter envelope handling. | Add exception/entrypoint handling so `401`/`403` keep Atlas envelope style. |
| Broad guard coverage touches many controllers. | Start with a central policy and representative endpoint tests rather than bespoke guards per controller. |
| Production SSO remains out of scope. | Keep boundary explicit and mock provider default deterministic. |

## Open Questions

- OQ-AUTH-SPACE-RBAC-001: Future `AUDITOR` audit-log visibility boundary.
- OQ-AUTH-SPACE-RBAC-002: Future SSO group-to-role mapping policy.
