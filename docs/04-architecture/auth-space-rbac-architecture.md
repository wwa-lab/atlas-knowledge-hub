# System Architecture: auth-space-rbac

Status: Draft SDD
Last updated: 2026-07-05
Source specification: `docs/03-spec/auth-space-rbac-spec.md`

## Overview

- **Architecture Summary:** This slice adds a cross-cutting auth/RBAC layer to the existing Atlas Spring Boot metadata API. It introduces current-user resolution, membership-backed permission checks, safe denied responses, and frontend capabilities while keeping production SSO/OIDC behind a future provider boundary.
- **Design Objective:** Backend-enforced space authorization for controlled internal beta without external identity-provider calls.
- **Architectural Style:** Layered Spring Boot control-plane with auth provider boundary, centralized authorization policy, domain controllers, service/repository persistence, and Vue permission-aware UI.

## Source Specification

- **Feature / System Name:** `auth-space-rbac`
- **Scope Summary:** Implement current user context, local mock auth, role matrix, additive user/membership persistence, backend guards for core APIs, and frontend permission awareness.

## Architectural Drivers

### Key Functional Drivers

- REQ-AUTH-SPACE-RBAC-001 requires current user context for protected requests.
- REQ-AUTH-SPACE-RBAC-006 defines the six-role matrix.
- REQ-AUTH-SPACE-RBAC-007 requires guard coverage across existing API domains.
- REQ-AUTH-SPACE-RBAC-012 protects membership administration and owner invariants.

### Key Non-Functional Drivers

- Denied responses must not leak resource existence or sensitive details.
- Default local/test behavior must stay offline and mock-safe.
- Future SSO/OIDC must remain behind a provider boundary.
- Existing API envelope and safe error style must be preserved.

### Constraints and Assumptions

- Verified current graph authorization is controller-local at `backend/src/main/java/com/atlas/metadata/controller/GraphController.java:35`.
- Verified most core controllers do not currently resolve user context, including `SpaceController.java:35`, `ReviewPublishController.java:37`, and `AskController.java:29`.
- Verified base schema has `space.owner` but no persisted membership table at `backend/src/main/resources/db/migration/V1__init_schema.sql:3`.
- [ASSUMPTION] Adding Spring Security is acceptable if the implementation keeps a local mock provider and existing API envelope semantics stable.

## System Context

| Actor/System | Role |
|---|---|
| Vue frontend | Loads `/api/auth/me`, renders permissions, and calls Atlas APIs. |
| Atlas metadata API | Owns backend auth context, authorization, resource APIs, and membership persistence. |
| Local mock auth provider | Provides deterministic mock users for dev/test/E2E. |
| Future SSO/OIDC provider | Future external identity provider behind a boundary; not called in this slice. |
| PostgreSQL/Flyway | Persists users and memberships through additive migrations. |

System boundary: The slice lives inside Atlas product surfaces and metadata API. It does not create an external identity service, call a company directory, or introduce production SSO secrets.

## High-Level Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│  Internal Users                                               │
│  Viewer · Editor · Knowledge Manager · Space Owner · Auditor  │
└─────────────────────────┬────────────────────────────────────┘
                          │ HTTPS / local dev
                          ▼
┌──────────────────────────────────────────────────────────────┐
│  Vue App                                                      │
│  /api/auth/me · capability-aware controls · safe denial states│
└─────────────────────────┬────────────────────────────────────┘
                          │ REST / JSON
                          ▼
┌──────────────────────────────────────────────────────────────┐
│  Spring Boot Metadata API                                     │
│  Auth Filter/Resolver · Authorization Policy · Error Envelope  │
├──────────────────────────────────────────────────────────────┤
│  Space/Batch/File · Review/Wiki · Graph/Ask · Settings APIs   │
├──────────────────────────────────────────────────────────────┤
│  User + Membership Services · Existing Domain Services        │
└──────────────┬──────────────────────────────┬────────────────┘
               │ JPA/Flyway                   │ provider boundary
               ▼                              ▼
┌──────────────────────────────┐   ┌──────────────────────────┐
│  PostgreSQL atlas schema      │   │  Mock Auth / Future SSO  │
│  atlas_user · space_membership│   │  No live SSO in slice    │
└──────────────────────────────┘   └──────────────────────────┘
```

## Layer Summary

- **Presentation Layer:** Vue fetches current context and capabilities, disables or hides denied controls, and still treats backend as authoritative.
- **API/Auth Layer:** Resolves user context, maps endpoint actions to capabilities, and returns `401`/`403` via the existing envelope.
- **Domain Layer:** Existing space, batch, review, wiki, graph, ask, and settings services continue to own business logic after authorization succeeds.
- **Persistence Layer:** Flyway-managed user and membership tables provide role data and active-owner invariants.
- **Provider Boundary:** Mock provider is active for local/test; future SSO/OIDC provider is a swappable boundary.

## Component Breakdown

### Frontend Components

- **Auth Context Store:** Loads `/api/auth/me`, exposes current user, active space, roles, and capabilities.
- **Permission-Aware Controls:** Gate visible actions for upload, batch create, review, publish, graph, Ask, settings, and members.
- **Denied/Error States:** Display non-sensitive `401`/`403` messages and do not expose protected metadata.

### Backend Services

- **Current User Resolver:** Builds Atlas user context from mock auth or future provider claims.
- **Authorization Policy:** Maps endpoint/action/resource to required roles and capabilities.
- **Membership Service:** Reads and updates user memberships, role assignments, and owner invariants.
- **Safe Error Handling:** Returns envelope-compatible `401`, `403`, conflict, and validation responses.

### Configuration / Administration Modules

- **Mock Auth Configuration:** Defines safe test identities and defaults.
- **Provider Boundary Configuration:** Records provider type and configured state without raw secrets.

### Monitoring / Audit Modules

- **Auth Decision Event Shape:** Emits safe actor, role, space, action, target, result, and request id fields for the later audit slice.

### Integration Adapters

- **Auth Provider Boundary:** Local mock now; future SSO/OIDC later. No parser/converter/model/vector/storage adapter boundary changes.

## Data Architecture

| Entity | Description | Key Attributes |
|---|---|---|
| AtlasUser | Safe internal user profile | id, email, displayName, status, globalRoles, createdAt, updatedAt |
| SpaceMembership | User membership in a Knowledge Space | id, userId, spaceId, role, status, invitedBy, createdAt, updatedAt |
| AuthDecision | Safe transient event shape | requestId, actor, role, spaceId, action, targetType, targetId, result |

State models:

- User status: `ACTIVE`, `DISABLED`.
- Membership status: `INVITED`, `ACTIVE`, `SUSPENDED`, `REMOVED`.
- Denied result: `UNAUTHENTICATED`, `FORBIDDEN`, `SAFE_NOT_FOUND`, `CONFLICT`.

## Integration Architecture

### Local Mock Auth Provider

- **Interaction Pattern:** In-process resolver using safe headers/test defaults.
- **Triggered by:** Every protected request.
- **Data exchanged:** Mock user id and active space preference; no secrets.

### Future SSO/OIDC Provider

- **Interaction Pattern:** Boundary only in this slice.
- **Triggered by:** Future production auth configuration.
- **Data exchanged:** Not exchanged in this slice.

## Workflow / Runtime Architecture

Request flow:

1. Request enters backend auth layer.
2. Current user resolver authenticates or returns `401`.
3. Authorization policy resolves action and required capability.
4. Membership service checks active role for target space or global platform role.
5. Allowed request reaches existing controller/service logic.
6. Denied request returns safe `403` or safe not-found where specified.

## Security Architecture

- Backend authorization is mandatory for protected APIs.
- Frontend capabilities are UX hints only.
- Cross-space denied responses avoid resource metadata disclosure.
- Provider payloads, raw tokens, passwords, private paths, and real company identities are prohibited.
- Member write operations protect the last active `SPACE_OWNER`.

## Risks / Tradeoffs

| Risk | Impact | Mitigation |
|---|---|---|
| Introducing central guards may reveal endpoints without tests. | High | Add representative integration tests by domain and role. |
| Spring Security setup may disrupt local dev. | Medium | Provide mock provider defaults and document test headers. |
| Existing graph-local guard duplication may drift. | Medium | Replace or wrap it with the common authorization policy. |

## Open Questions

- OQ-AUTH-SPACE-RBAC-001: Should `AUDITOR` later read audit logs across spaces or only space-scoped governance metadata?
- OQ-AUTH-SPACE-RBAC-002: Should production SSO map groups to roles automatically or require explicit Atlas memberships?
