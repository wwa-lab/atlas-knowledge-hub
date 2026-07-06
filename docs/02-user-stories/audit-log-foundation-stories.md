# User Stories: audit-log-foundation

Status: Draft for human review
Last updated: 2026-07-06

## Story Map

| Story | Requirement IDs | Capability |
|---|---|---|
| US-AUDIT-LOG-FOUNDATION-001 | REQ-AUDIT-LOG-FOUNDATION-001, 002, 011, 012 | Append-only safe audit event foundation |
| US-AUDIT-LOG-FOUNDATION-002 | REQ-AUDIT-LOG-FOUNDATION-003, 004, 005, 006 | Audit event capture for core operations |
| US-AUDIT-LOG-FOUNDATION-003 | REQ-AUDIT-LOG-FOUNDATION-007, 008, 009 | Secure audit read APIs |
| US-AUDIT-LOG-FOUNDATION-004 | REQ-AUDIT-LOG-FOUNDATION-010 | Frontend audit inspection |
| US-AUDIT-LOG-FOUNDATION-005 | REQ-AUDIT-LOG-FOUNDATION-002, 009, 012 | Verification and data-safety guardrails |

## User Story 1

**Title:** Persist safe audit events

**Story:**
As a security reviewer,
I want sensitive Atlas operations to create immutable audit events,
so that governance reviews can inspect who did what without exposing protected content.

### Acceptance Criteria

1. **Given** a sensitive Atlas action completes or is denied
   **When** the backend records the audit event
   **Then** the record includes actor id, action, result, target, space, timestamp, request id, and safe summary.

2. **Given** an audit event is recorded
   **When** it is persisted
   **Then** it cannot be updated through product APIs.

3. **Given** an operation contains sensitive input
   **When** audit fields are built
   **Then** raw secrets, raw source text, raw prompts, private paths, provider payloads, and stack traces are excluded.

### Notes / Assumptions

- Audit records complement existing domain records such as `review_record`; they do not replace them.

### Dependencies

- `auth-space-rbac` current-user and authorization decision foundation.

### Out of Scope

- SIEM export, alerting, retention automation, and tamper-evident signing.

### Open Questions

- Should graph audit history be backfilled or bridged lazily?

## User Story 2

**Title:** Capture core governance actions

**Story:**
As a space owner,
I want membership, review, publish, Wiki, graph, Ask, and settings actions to be auditable,
so that I can investigate changes to a knowledge space.

### Acceptance Criteria

1. **Given** a membership change is created, updated, removed, denied, or rejected by last-owner protection
   **When** the operation finishes
   **Then** an audit event captures actor, target user, space, action, result, and safe reason.

2. **Given** a review, publish, Wiki ingest/linkify/lint, graph projection/review, Ask, or adapter operation runs
   **When** the representative operation completes
   **Then** an audit event captures safe counts, ids, and result status without raw content.

3. **Given** a protected request is denied
   **When** the auth boundary returns `401`, `403`, or safe `404`
   **Then** the denial can be audited when the target scope can be resolved safely.

### Notes / Assumptions

- v0 captures representative core operations first, with explicit extension points for later slices.

### Dependencies

- Existing backend services and RBAC path policy.

### Out of Scope

- Auditing every read request by default.

### Open Questions

- Which read events, if any, should be mandatory for internal beta?

## User Story 3

**Title:** Read audit events safely

**Story:**
As an auditor,
I want to filter audit events by space, actor, action, target, result, and time range,
so that I can inspect governance history without seeing data outside my permission scope.

### Acceptance Criteria

1. **Given** an authorized auditor requests space audit events
   **When** they use supported filters and pagination
   **Then** the API returns safe DTOs and page metadata.

2. **Given** a user lacks governance-read permission for a space
   **When** they request audit data
   **Then** the backend returns a safe denied or not-found response without leaking event details.

3. **Given** an audit target id belongs to another space
   **When** a user filters by that target
   **Then** the response does not disclose cross-space metadata.

### Dependencies

- Backend RBAC and safe API envelope behavior.

### Out of Scope

- Global cross-tenant audit console beyond mock/internal `PLATFORM_ADMIN` capability.

### Open Questions

- Should `KNOWLEDGE_MANAGER` read all audit categories or only knowledge-operation categories?

## User Story 4

**Title:** Inspect audit events in the product UI

**Story:**
As a space owner,
I want a read-only audit panel in Atlas,
so that I can quickly review recent governance actions from the space UI.

### Acceptance Criteria

1. **Given** the current user has audit-read capability
   **When** they open the audit panel
   **Then** recent safe audit events are visible with filters and empty/error states.

2. **Given** the current user does not have audit-read capability
   **When** the UI renders governance surfaces
   **Then** audit entry points are hidden or disabled without implying backend access.

3. **Given** the backend returns a denied response
   **When** the UI handles it
   **Then** no stale audit data remains visible.

### Out of Scope

- Audit event editing, deletion, export, and alert creation.

### Open Questions

- Should the first UI surface live under Settings, Processing Center, or a space Governance tab?

## User Story 5

**Title:** Verify audit safety

**Story:**
As a security reviewer,
I want automated tests and scans for audit data safety,
so that audit logging does not become a new leakage channel.

### Acceptance Criteria

1. **Given** audit events are created from sensitive operations
   **When** tests inspect stored events
   **Then** only safe ids, enums, counts, timestamps, and safe summaries are present.

2. **Given** changed files are scanned
   **When** secret/private-path and network/dependency checks run
   **Then** no new real data, raw credentials, external cloud calls, or unapproved dependencies are found.

3. **Given** SDD and implementation diverge
   **When** closeout runs
   **Then** traceability records the mismatch or blocks completion.

### Dependencies

- Existing workflow gates and test harnesses.

### Out of Scope

- Formal compliance certification.

### Open Questions

- None.

