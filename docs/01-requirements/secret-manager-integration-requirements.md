# Requirements: secret-manager-integration

Status: Preauthorized SDD accepted for implementation when confined to the goal objective.
Last updated: 2026-07-07
Phase: Wave 3 / Trust And Governance

## Goal

Move Atlas configuration surfaces from raw secret handling toward a secret-reference and masked-status model for model providers, parser/converter runtimes, storage, vector, and adjacent adapter configuration metadata. APIs and UI must expose only status, masked labels, replacement/removal affordances, and future secret-manager references.

## Scope

- Add product-facing secret reference and secret status DTOs.
- Keep the current in-memory/runtime configuration behavior mock-safe.
- Preserve adapter boundaries for model, parser, converter, storage, vector, and runtime configuration.
- Return configured/missing/disabled/env-configured style status only from read APIs.
- Update Vue settings/model/adapter displays to consume status-only fields.
- Add tests proving create/update/read behavior never returns raw secrets, private endpoints, private paths, credentials, or raw runtime command values.

## Exclusions

- No real secret manager instance, SSO/OIDC, rotation automation, SIEM export, production monitoring, or real provider connection tests.
- No auth/RBAC semantic changes beyond using the existing protected endpoints.
- No audit semantics beyond the existing audit-log-foundation behavior.
- No external cloud calls or real company configuration.

## Requirements

| ID | Requirement | Priority |
|---|---|---|
| REQ-SECRET-MANAGER-INTEGRATION-001 | Read APIs for provider/runtime/storage/vector configuration must expose secret references and masked statuses, not raw values. | Must |
| REQ-SECRET-MANAGER-INTEGRATION-002 | Write APIs may accept replacement secret material only as write-only input and must not echo it. | Must |
| REQ-SECRET-MANAGER-INTEGRATION-003 | Model configuration must support retain/replace/remove style behavior without returning raw API keys or endpoints. | Must |
| REQ-SECRET-MANAGER-INTEGRATION-004 | Parser and converter runtime capability metadata must report command status without exposing command values or local paths. | Must |
| REQ-SECRET-MANAGER-INTEGRATION-005 | Storage and vector adapter capability metadata must report credential/endpoint/collection status without exposing raw infrastructure details. | Must |
| REQ-SECRET-MANAGER-INTEGRATION-006 | Frontend settings must render configured/missing/replace/remove states and never display secret material. | Must |
| REQ-SECRET-MANAGER-INTEGRATION-007 | Existing adapter boundaries must remain intact; no UI or product service may depend on concrete provider/engine secret formats. | Must |
| REQ-SECRET-MANAGER-INTEGRATION-008 | Tests and scans must prove no raw secret, token, password, credential, private endpoint, private path, or runtime command leaks in API responses, UI source, or fixtures. | Must |

## Assumptions

- Existing auth-space-rbac and audit-log-foundation behavior is stable and not redefined here.
- Runtime configuration remains local and mock-safe; production secret manager resolution is represented as an adapter boundary only.
- Current public compatibility for `maskedConfigSummary` remains while typed status fields are introduced.

## SDD Skill Chain Evidence

SDD skill chain used: yes. Files read include `atlas-sdd-generate-all`, `req-to-user-story`, `user-story-to-spec`, `spec-to-architecture`, `architecture-to-design`, `design-to-tasks`, `review-doc-quality`, `architecture-review`, and shared grounding rules.
