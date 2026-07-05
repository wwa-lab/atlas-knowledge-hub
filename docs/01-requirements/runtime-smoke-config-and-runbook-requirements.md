# Requirements: Runtime Smoke Config And Runbook

## Status

Accepted and implemented on 2026-07-05. Runtime smoke readiness only; not production operations readiness.

## Slice Contract

- **Slice:** `runtime-smoke-config-and-runbook`
- **Wave:** Wave 3 / Trust And Governance readiness for runtime operations
- **Goal:** Make configured real-runtime smoke checks safe, repeatable, and explainable without changing the default mock-safe CI behavior.
- **Maturity target:** L4 readiness preparation for approved local runtime verification; not production readiness.

## Scope

### In Scope

- Document the approved local runtime smoke configuration contract for `trinity-office` and `document-normalize`.
- Define how optional smoke checks self-skip, pass, or fail with bounded diagnostics.
- Define the implementation runbook that will explain setup, execution, evidence capture, troubleshooting, rollback, and safety limits.
- Preserve adapter boundaries from `real-office-parser-runtime`.
- Keep default CI and ordinary `mvn verify` mock-safe when approved local runtime binaries are absent.
- Keep all test fixtures mock/sample-safe.

### Out Of Scope

- Production deployment runbook, SLOs, alerting, monitoring dashboards, or rollback for deployed environments.
- Secret manager integration, production RBAC, audit-log foundation, rate limiting, or safe-error envelope changes.
- New public API endpoints.
- Real company documents, private paths, raw binary logs, credentials, internal hostnames, screenshots, or provider logs.
- Changing parser/converter runtime behavior, manifest shape, or adapter registry behavior unless required only to support safe smoke evidence.

## Requirements

| ID | Requirement | Priority | Verification |
|---|---|---|---|
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 | The full bilingual SDD set must be accepted before implementation or runbook changes begin. | Must | Traceability records Draft status and user acceptance gate. |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 | The slice must document the current smoke environment variables: `ATLAS_RUNTIME_SMOKE_ENABLED`, `ATLAS_TRINITY_OFFICE_COMMAND`, `ATLAS_TRINITY_OFFICE_SMOKE_ARGS`, `ATLAS_DOCUMENT_NORMALIZE_COMMAND`, and `ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS`. | Must | Spec and API guide list the exact variables and defaults. |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003 | The slice must distinguish Spring runtime adapter properties from optional smoke-test environment variables. | Must | Design explains adapter runtime configuration versus smoke-only command checks. |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004 | Optional runtime smoke tests must self-skip unless explicitly enabled and an approved command is configured. | Must | Tasks require focused test evidence for skip behavior. |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005 | Successful smoke execution must produce safe pass/fail evidence without exposing raw command paths. | Must | Tasks require diagnostics assertions and safety scans. |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006 | Smoke diagnostics must remain bounded and sanitized. | Must | Design references the existing bounded capture and sanitizer behavior; tasks require tests or static inspection. |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007 | The runbook must use mock/sample-safe verification inputs only and must forbid real company documents. | Must | Runbook task includes explicit fixture/data safety checklist. |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008 | Adapter boundaries must remain unchanged; product services, controllers, Wiki, Graph, Ask, and frontend code must not invoke real runtimes directly. | Must | Tasks include seam guard verification. |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009 | Default CI must remain independent of real `trinity-office` or `document-normalize` binaries. | Must | `mvn verify` must pass with smoke tests skipped when env vars are absent. |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-010 | The runbook must explain skip, pass, fail, timeout, missing command, and unsafe diagnostic outcomes. | Must | Design and tasks include an outcome interpretation matrix. |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-011 | The implementation must include a closeout evidence checklist covering backend verification, smoke skip/pass evidence, diff hygiene, secret/private-path scan, and network/dependency scan. | Must | Tasks define required verification commands and scans. |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-012 | Roadmap and traceability must state that this slice is runtime smoke readiness only, not production operations readiness. | Must | Traceability and roadmap update use maturity-qualified language. |

## Assumptions

- The approved local binaries, when available, are already installed outside the repository.
- The existing smoke test currently performs command-level checks and defaults smoke args to `--version`.
- Full upload-to-Wiki real document processing is still outside this slice.

## Open Questions

| ID | Question | Owner |
|---|---|---|
| OQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 | Should the accepted runbook live under `docs/00-context/runbooks/` or `docs/07-acceptance/`? | Product / Platform |
| OQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 | Should future smoke checks validate a tiny approved sample conversion/parse fixture, or stay at command health checks only? | Platform / Security |
