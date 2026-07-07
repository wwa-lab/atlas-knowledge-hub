# Feature Specification: secret-manager-integration

> Source stories: US-SECRET-MANAGER-INTEGRATION-001 through 004
> Spec status: Preauthorized accepted
> Last updated: 2026-07-07

## Overview

Atlas shall expose secret-bearing configuration as status-only metadata and future-compatible secret references. This slice strengthens the existing model configuration and adapter capability contracts without connecting a real secret manager or changing auth/audit semantics.

## Functional Scope

In scope:

- Shared DTOs for secret references and secret statuses.
- Model configuration read/save/clear responses that include typed secret status.
- Model, parser, converter, storage, and vector capability responses with typed secret statuses.
- Frontend settings rendering based on configured/missing/disabled status fields.
- Tests and scans for redaction.

Out of scope:

- Production secret manager connection, encrypted database storage, SSO/OIDC, new audit semantics, rate limiting, live provider tests, deployment, monitoring, or rotation automation.

## Functional Requirements

| ID | Requirement |
|---|---|
| FR-SECRET-MANAGER-INTEGRATION-001 | Read responses must never include raw API keys, tokens, passwords, credentials, private endpoints, private paths, runtime commands, hostnames, or stack traces. |
| FR-SECRET-MANAGER-INTEGRATION-002 | `SecretReferenceResponse` must identify a logical provider/scope/key without containing secret material. |
| FR-SECRET-MANAGER-INTEGRATION-003 | `SecretStatusResponse` must expose status, source, masked label, replaceable flag, removable flag, and reference metadata. |
| FR-SECRET-MANAGER-INTEGRATION-004 | Model save accepts write-only replacement secret input and returns only typed status and masked summary. |
| FR-SECRET-MANAGER-INTEGRATION-005 | Omitted model secret input retains effective runtime/environment credential status when available. |
| FR-SECRET-MANAGER-INTEGRATION-006 | Clear model configuration removes runtime state and falls back to environment or missing status. |
| FR-SECRET-MANAGER-INTEGRATION-007 | Runtime parser/converter capability metadata reports command as disabled/configured/missing only. |
| FR-SECRET-MANAGER-INTEGRATION-008 | Storage/vector capability metadata reports endpoint/credential/collection status only. |
| FR-SECRET-MANAGER-INTEGRATION-009 | Frontend settings display configured/missing/masked states and replacement/removal affordances only. |
| FR-SECRET-MANAGER-INTEGRATION-010 | Existing `maskedConfigSummary` remains available for compatibility, but consumers should prefer typed secret statuses. |

## Non-Functional Requirements

- Security: write-only secret material; no raw secret in responses, logs added by this slice, docs examples, tests, or UI source.
- Adapter boundary: product layers depend on Atlas DTOs and adapter capabilities, not provider secret formats.
- Verification: backend unit/API tests, frontend tests, typecheck/build, secret/private-path scans, and closeout gate.
- Data safety: mock/sample-safe only; no external calls.

## Workflow

1. Admin opens settings or calls configuration endpoints.
2. Backend computes effective configuration status from runtime state and process environment.
3. Backend returns `SecretStatusResponse` objects and compatible masked summary maps.
4. Admin may replace or clear model credential state through existing model configuration endpoints.
5. Backend stores runtime replacement in memory for the current process and returns status-only state.
6. Adapter execution continues to resolve raw values only inside the existing adapter boundary.

## API Surface

- Existing: `GET /api/model-adapters`
- Existing: `GET /api/model-configurations/deepseek`
- Existing: `PUT /api/model-configurations/deepseek`
- Existing: `DELETE /api/model-configurations/deepseek`
- Existing: `GET /api/converter-adapters`
- Existing: `GET /api/parser-adapters`
- Existing: `GET /api/storage-adapters`
- Existing: `GET /api/vector-adapters`

No new public endpoints are required.

## Acceptance Matrix

| Acceptance | Requirements | Evidence |
|---|---|---|
| API responses expose only secret references/status | REQ-001, REQ-002, REQ-003, REQ-008 | Backend unit/API tests and scans |
| Frontend never displays raw secret | REQ-006, REQ-008 | Frontend component tests |
| Adapter boundaries preserved | REQ-004, REQ-005, REQ-007 | Capability DTO mapping and existing seam tests |
| Verification passes | REQ-008 | Required commands and closeout gate |

## Open Questions

None for this goal. Production secret storage, rotation, and secret-manager provider selection remain future slices.
