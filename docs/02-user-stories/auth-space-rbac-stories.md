# User Stories: auth-space-rbac

Status: Draft SDD
Last updated: 2026-07-05

## Story Set

| Story | Title | Requirements |
|---|---|---|
| US-AUTH-SPACE-RBAC-001 | Resolve Current User Context | REQ-AUTH-SPACE-RBAC-001, REQ-AUTH-SPACE-RBAC-002, REQ-AUTH-SPACE-RBAC-003, REQ-AUTH-SPACE-RBAC-004 |
| US-AUTH-SPACE-RBAC-002 | Enforce Space Role Matrix | REQ-AUTH-SPACE-RBAC-005, REQ-AUTH-SPACE-RBAC-006, REQ-AUTH-SPACE-RBAC-007 |
| US-AUTH-SPACE-RBAC-003 | Protect Membership Management | REQ-AUTH-SPACE-RBAC-012, REQ-AUTH-SPACE-RBAC-013, REQ-AUTH-SPACE-RBAC-014 |
| US-AUTH-SPACE-RBAC-004 | Prevent Resource Disclosure | REQ-AUTH-SPACE-RBAC-009, REQ-AUTH-SPACE-RBAC-010, REQ-AUTH-SPACE-RBAC-011 |
| US-AUTH-SPACE-RBAC-005 | Show Permission-Aware UI | REQ-AUTH-SPACE-RBAC-004, REQ-AUTH-SPACE-RBAC-008 |
| US-AUTH-SPACE-RBAC-006 | Verify Role Differences | REQ-AUTH-SPACE-RBAC-006, REQ-AUTH-SPACE-RBAC-007, REQ-AUTH-SPACE-RBAC-014 |

---

## User Story US-AUTH-SPACE-RBAC-001

**Title:** Resolve current user context

**Story:**
As an authenticated internal user,
I want Atlas to know my identity, memberships, active space, and effective roles,
so that every product action can be authorized consistently.

## Acceptance Criteria

1. **Given** a mock-auth request in local/test mode
   **When** the backend resolves current user context
   **Then** the context includes user id, display name, email, global roles, memberships, active space, and capabilities.

2. **Given** no authenticated context on a protected API
   **When** the request reaches backend authorization
   **Then** the API returns `401` through the Atlas envelope.

3. **Given** the future company SSO/OIDC provider is not configured
   **When** the local/test profile runs
   **Then** mock auth works without external network calls.

## Notes / Assumptions
- Local mock auth is the implementation default for this slice.
- Company SSO/OIDC is represented as a provider boundary, not a live provider call.

## Dependencies
- Existing Atlas API envelope and safe error body.
- Existing Spring Boot metadata API.

## Out of Scope
- Password login, MFA, SCIM, production IdP configuration.

## Open Questions
- None blocking for SDD acceptance.

---

## User Story US-AUTH-SPACE-RBAC-002

**Title:** Enforce space role matrix

**Story:**
As a space member,
I want Atlas to enforce permissions based on my role in the current Knowledge Space,
so that users can only read, change, review, publish, ask, or configure what they are allowed to access.

## Acceptance Criteria

1. **Given** a `VIEWER`
   **When** they call a write API
   **Then** the backend returns `403`.

2. **Given** a `KNOWLEDGE_MANAGER`
   **When** they run knowledge operations such as ingest, review, publish, graph refresh, or Ask
   **Then** the backend allows only the operations mapped to that role.

3. **Given** a `SPACE_OWNER`
   **When** they manage membership and space-scoped settings
   **Then** the backend permits the action if membership invariants remain valid.

## Notes / Assumptions
- Role names are uppercase enum values in API and persisted metadata.

## Dependencies
- Additive user and membership schema.

## Out of Scope
- Organization-level directory sync.

## Open Questions
- None blocking for SDD acceptance.

---

## User Story US-AUTH-SPACE-RBAC-003

**Title:** Protect membership management

**Story:**
As a Space Owner,
I want to invite, update, suspend, and remove members with guardrails,
so that a Knowledge Space can be managed without accidentally losing ownership or exposing sensitive membership data.

## Acceptance Criteria

1. **Given** the caller is not `SPACE_OWNER` or `PLATFORM_ADMIN`
   **When** they create, change, or remove a membership
   **Then** the backend returns `403`.

2. **Given** a membership change would remove the last active `SPACE_OWNER`
   **When** the request is submitted
   **Then** the backend rejects it with a validation/conflict response.

3. **Given** a membership operation succeeds or fails
   **When** the auth decision is recorded
   **Then** the minimum event fields are available for the later audit slice.

## Notes / Assumptions
- Invitation delivery is not real email in this slice.

## Dependencies
- Auth context and role matrix.

## Out of Scope
- Email invitation workflow and audit UI.

## Open Questions
- None blocking for SDD acceptance.

---

## User Story US-AUTH-SPACE-RBAC-004

**Title:** Prevent unauthorized resource disclosure

**Story:**
As a security reviewer,
I want unauthorized access to avoid revealing protected resource existence or sensitive details,
so that internal beta users cannot enumerate spaces, files, pages, graph objects, or Ask runs they should not know about.

## Acceptance Criteria

1. **Given** a user has no membership in a target space
   **When** they request a space-scoped resource
   **Then** the response does not include protected resource metadata.

2. **Given** a user is authenticated but lacks the required permission
   **When** authorization fails
   **Then** the response is a generic `403` unless the endpoint contract intentionally uses safe not-found semantics.

3. **Given** an error occurs in auth or authorization
   **When** the response is returned
   **Then** no stack trace, secret, private path, raw token, or provider payload is exposed.

## Notes / Assumptions
- Generic denied messaging is preferred for protected resources.

## Dependencies
- Global safe error envelope.

## Out of Scope
- Full rate limiting and anomaly detection.

## Open Questions
- None blocking for SDD acceptance.

---

## User Story US-AUTH-SPACE-RBAC-005

**Title:** Show permission-aware UI

**Story:**
As an Atlas user,
I want the UI to reflect what I can do in the selected space,
so that I do not see misleading write controls that will be denied by the backend.

## Acceptance Criteria

1. **Given** the frontend starts
   **When** it loads `/api/auth/me`
   **Then** it receives current user, memberships, active space, roles, and capabilities.

2. **Given** the user is a `VIEWER`
   **When** write controls are rendered
   **Then** controls are hidden or disabled with non-sensitive copy.

3. **Given** a user manipulates the client
   **When** they call a forbidden backend API
   **Then** backend authorization still returns `403`.

## Notes / Assumptions
- Frontend permission awareness improves UX only.

## Dependencies
- `/api/auth/me`.

## Out of Scope
- New visual redesign of settings or member management.

## Open Questions
- None blocking for SDD acceptance.

---

## User Story US-AUTH-SPACE-RBAC-006

**Title:** Verify role differences

**Story:**
As a delivery lead,
I want automated tests to prove role-specific behavior,
so that internal beta readiness is based on enforced backend permissions rather than UI assumptions.

## Acceptance Criteria

1. **Given** integration tests run
   **When** `VIEWER`, `KNOWLEDGE_MANAGER`, and `SPACE_OWNER` call representative APIs
   **Then** allowed and denied outcomes match the role matrix.

2. **Given** E2E tests run
   **When** users with different roles open core product surfaces
   **Then** visible actions match the backend-provided capabilities.

3. **Given** verification scans run
   **When** the slice closes
   **Then** no real company data, private paths, raw secrets, or external cloud calls are introduced.

## Notes / Assumptions
- Mock users are seeded test identities only.

## Dependencies
- Backend integration test harness and frontend E2E harness.

## Out of Scope
- Production penetration test.

## Open Questions
- None blocking for SDD acceptance.
