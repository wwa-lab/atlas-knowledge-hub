# Requirements: audit-log-foundation

Status: Draft for human review
Last updated: 2026-07-06
Slice: `audit-log-foundation`
Wave: Wave 3 / Trust And Governance

## Goal

Add a safe, append-only audit log foundation for Atlas governance operations after `auth-space-rbac`, so security reviewers and space owners can inspect who attempted or performed sensitive actions without exposing secrets, raw document content, private paths, provider payloads, or stack traces.

## Scope

In scope:

- A general audit event data model for core Atlas operations.
- Append-only backend audit recording for auth decisions, membership changes, review/publish actions, Wiki generation/linkify/lint operations, graph/Ask operations, model configuration changes, and adapter/runtime operation summaries.
- Read-only audit APIs for space-scoped and target-scoped inspection.
- Frontend audit views for governance metadata in the existing Atlas product shell.
- Migration path from the narrow `graph_audit_record` pattern to the general audit event foundation.
- Mock/sample-safe seed and tests only.

Out of scope:

- Production SIEM export, alerting, retention automation, legal hold, tamper-evident signing, rate limiting, secret manager integration, live SSO/OIDC, and external cloud calls.
- Raw request/response body capture, raw prompts, source document text, parser/runtime logs, stack traces, provider payloads, tokens, credentials, or private absolute paths.
- Replacing domain history tables such as review records; audit provides governance evidence, not the full domain source of truth.

## Requirements

| ID | Requirement | Priority | Verification |
|---|---|---|---|
| REQ-AUDIT-LOG-FOUNDATION-001 | The system shall persist audit events in an append-only table with stable id, timestamp, actor, action, result, target, space, request correlation, and safe summary fields. | Must | Migration and repository tests. |
| REQ-AUDIT-LOG-FOUNDATION-002 | Audit events shall never store raw secrets, tokens, passwords, API keys, provider payloads, raw prompts, raw document text, private paths, stack traces, or raw runtime logs. | Must | Secret/private-path scans and redaction tests. |
| REQ-AUDIT-LOG-FOUNDATION-003 | The auth boundary shall emit audit events for denied protected API requests and high-value allowed governance actions. | Must | Auth integration tests for `401`, `403`, and allowed action events. |
| REQ-AUDIT-LOG-FOUNDATION-004 | Membership create, update, suspend, remove, and last-owner conflict attempts shall emit audit events with actor and target user references. | Must | Membership API contract tests. |
| REQ-AUDIT-LOG-FOUNDATION-005 | Review, publish, Wiki ingest, Wiki linkify/lint, graph projection/review, Ask create, vector/model/storage/parser/converter operation starts or completions shall emit bounded audit summaries. | Must | Service tests for representative domains. |
| REQ-AUDIT-LOG-FOUNDATION-006 | Existing graph audit behavior shall be preserved or migrated without losing graph governance evidence. | Must | Graph tests still pass and graph audit mappings are documented. |
| REQ-AUDIT-LOG-FOUNDATION-007 | Audit read APIs shall support filtering by space, actor, action, result, target type/id, and time window with pagination. | Must | API contract tests. |
| REQ-AUDIT-LOG-FOUNDATION-008 | Audit APIs shall enforce RBAC: `AUDITOR`, `SPACE_OWNER`, `KNOWLEDGE_MANAGER`, and `PLATFORM_ADMIN` can read permitted audit scopes; `VIEWER` and `EDITOR` cannot read governance audit logs by default. | Must | Role-specific integration tests. |
| REQ-AUDIT-LOG-FOUNDATION-009 | Cross-space audit access shall return safe denied or not-found responses without leaking protected resource names, user emails outside scope, source paths, or content. | Must | Cross-space API tests. |
| REQ-AUDIT-LOG-FOUNDATION-010 | Frontend shall expose a read-only audit panel using backend-provided capabilities and safe audit DTOs. | Should | Frontend unit and E2E coverage. |
| REQ-AUDIT-LOG-FOUNDATION-011 | Audit event action and result enums shall be stable enough for later retention, export, and alerting slices. | Should | Enum coverage tests and API guide. |
| REQ-AUDIT-LOG-FOUNDATION-012 | The implementation shall use existing Spring Boot, PostgreSQL/Flyway, API envelope, and auth/RBAC patterns without introducing external dependencies. | Must | `mvn verify`, dependency scan. |

## Assumptions

- `auth-space-rbac` is the prerequisite for implementation because audit needs `CurrentUserContext`, role/capability decisions, and safe denied responses.
- Local/test identities use mock/sample users only.
- Audit records are immutable after creation; future retention or export behavior will be separate slices.

## Open Questions

| ID | Question | Owner |
|---|---|---|
| OQ-AUDIT-LOG-FOUNDATION-001 | Should `AUDITOR` see all events in a space, or only security/governance categories by default? | Product / Security |
| OQ-AUDIT-LOG-FOUNDATION-002 | Should existing `graph_audit_record` be backfilled into the general table during migration, or kept as a legacy companion until a cleanup slice? | Engineering |

