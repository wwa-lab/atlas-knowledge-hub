# Implementation Tasks: Converter Adapter

## Status

Draft. Phase 3 adapter slice. Execute only after SDD review/acceptance.

## Implementation Contract

- **Spec source of truth:** `docs/03-spec/converter-adapter-spec.md`.
- **Design contract:** `docs/05-design/converter-adapter-design.md`.
- **API/adapter contract:** `docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md`.
- **Constraints:** adapter-only, no direct tool calls from product layers, no external cloud/network dependency, mock-engine tests, secret-masked config, relative paths only, preserve source trace/confidence/review status.

## Verification Commands

Run before completion:

```bash
cd backend && mvn verify
git diff --check
rg -n "WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/converter-adapter-requirements.md docs/02-user-stories/converter-adapter-stories.md docs/03-spec/converter-adapter-spec.md docs/04-architecture/converter-adapter-architecture.md docs/04-architecture/converter-adapter-data-flow.md docs/04-architecture/converter-adapter-data-model.md docs/05-design/converter-adapter-design.md docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/converter-adapter-tasks.md
```

The command/network scan must return no matches in non-adapter product layers. If adapter implementation code contains command-runner boundaries, it must be covered by seam guard tests and must not leak into controllers/services/repositories/domain.

## Task Breakdown

| Task ID | Task | Owner | Priority | Dependencies | Requirement / Spec Mapping | Verification |
|---|---|---|---|---|---|---|
| T-CA-001 | Add converter domain types and persistence model for `conversion_run` and `conversion_file_result`, including `ConversionRunStatus`, repositories, migrations, and DTOs. Keep fields aligned with data model and use relative path validation for path-bearing fields. | Codex | Must | None | REQ-CA-004, REQ-CA-007, REQ-CA-008 / Spec "Metadata Write-Back", "State Model"; Data Model "New Logical Entities" | `cd backend && mvn -q test -Dtest=*Conversion*` passes; Flyway validation included in `mvn verify`. |
| T-CA-002 | Implement converter adapter contracts in the adapter boundary: capability, request, result, per-file result, adapter status, and `ConverterAdapter` interface. Do not expose vendor-specific command flags in product-facing records. | Codex | Must | T-CA-001 | REQ-CA-001, REQ-CA-002, REQ-CA-003 / Spec "Adapter Boundary", "Capability Metadata"; Design "Converter Adapter Interface" | Unit tests instantiate contract records and verify supported source type/status mapping. |
| T-CA-003 | Implement converter adapter registry and masked capability metadata, including default adapter resolution and `AVAILABLE` / `DISABLED` / `MISCONFIGURED` statuses. | Codex | Must | T-CA-002 | REQ-CA-002, REQ-CA-003, REQ-CA-009 / Spec "Capability Metadata"; API guide "Adapter Capability Contract" | API contract test for `GET /api/converter-adapters`; secret/path scan shows no raw command path or secret. |
| T-CA-004 | Implement mock/fake converter adapter for CI and tests. Cover Office success, Office failure, PDF pass-through, image `OCR_REQUIRED`, unsupported `UNSUPPORTED`, and misconfigured adapter behavior. | Codex | Must | T-CA-002, T-CA-003 | REQ-CA-005, REQ-CA-006, REQ-CA-010 / Spec "Conversion Run", "Per-File Status Mapping"; Design "Mock/Fake Converter Adapter" | Unit tests for all mock outcomes; no real `trinity-office` required. |
| T-CA-005 | Implement conversion application service: validate batch/file targets, create run, resolve adapter, execute adapter interface, validate/sanitize results, persist result rows, update file item status/PDF/confidence/error, and derive summary. | Codex | Must | T-CA-001..T-CA-004 | REQ-CA-004, REQ-CA-005, REQ-CA-006, REQ-CA-007, REQ-CA-008 / Spec "Metadata Write-Back", "Validation Rules", "Reporting And Failure Behavior" | Service tests for success, partial failure, adapter unavailable, unsafe path rejection, and review status preservation. |
| T-CA-006 | Add API endpoints for converter capabilities, create conversion run, and retrieve conversion run. Use `ApiEnvelope` and user-safe errors; do not add frontend routes. | Codex | Must | T-CA-005 | REQ-CA-003, REQ-CA-004, REQ-CA-009, REQ-CA-012 / Spec "API / Interface Surface"; API guide endpoint sections | MockMvc/API contract tests for status codes, envelope shape, payload fields, and safe errors. |
| T-CA-007 | Implement optional `trinity-office` wrapper behind adapter implementation with configuration gating. It must report `MISCONFIGURED` when required config is absent and must sanitize command output. Real binary execution must not be required by automated tests. | Codex | Should | T-CA-002, T-CA-003, T-CA-004 | REQ-CA-002, REQ-CA-009, REQ-CA-010 / Design "Trinity-office Adapter Boundary"; Spec "Constraints" | Tests use fake command runner; capability tests prove missing config returns `MISCONFIGURED`; no raw paths/secrets in response. |
| T-CA-008 | Update adapter seam guard from Phase 2 empty-package rule to Phase 3 scoped rule: concrete engine names and command execution APIs are allowed only in adapter implementation packages and test fakes. | Codex | Must | T-CA-002, T-CA-007 | REQ-CA-001, REQ-CA-011 / Spec "Adapter Boundary"; Architecture "Layer Boundaries" | Static guard test fails on injected direct reference in controller/service/repository/domain scan; command/network `rg` returns no matches in non-adapter layers. |
| T-CA-009 | Add integration tests against PostgreSQL/test profile using mock converter adapter and seeded metadata. Verify file item updates, result persistence, summary counts, and report retrieval. | Codex | Must | T-CA-005, T-CA-006 | REQ-CA-004, REQ-CA-005, REQ-CA-006, REQ-CA-010 / Spec "Acceptance Matrix"; API guide "Contract Tests" | `cd backend && mvn verify` passes. |
| T-CA-010 | Run final verification and update slice traceability/status evidence without marking implementation complete unless all checks pass. Record skipped checks with reasons. | Codex | Must | T-CA-001..T-CA-009 | REQ-CA-010, REQ-CA-012 / Spec "Acceptance Matrix"; Project Rules "Quality Gates" | Run all commands in "Verification Commands"; update `docs/00-context/converter-adapter-traceability.md` and `.zh-CN.md` with evidence. |

## Dependency Plan

Critical path:

```text
T-CA-001 -> T-CA-002 -> T-CA-003 -> T-CA-004 -> T-CA-005 -> T-CA-006 -> T-CA-009 -> T-CA-010
```

Parallel opportunities:

- T-CA-007 can proceed after T-CA-004 because it uses fake command-runner tests.
- T-CA-008 can proceed after T-CA-002 and be finalized after T-CA-007.

## Done Means

- All Must tasks complete.
- `cd backend && mvn verify` passes.
- Static guard proves product layers do not call converter engines directly.
- No raw secrets, private paths, real company data, or external cloud calls are introduced.
- Conversion results preserve source path, PDF path, confidence, review status, adapter identity, and safe errors.
- Traceability files record verification evidence and residual risks.

## Residual Risks / Open Questions

- OQ-CA-001: local process wrapper vs external worker.
- OQ-CA-002: PDF pass-through copy/reference policy.
- OQ-CA-003: exact `trinity-office` command-line contract.
