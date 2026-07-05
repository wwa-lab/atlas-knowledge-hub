# Detailed Design: Runtime Smoke Config And Runbook

## Status

Accepted and implemented on 2026-07-05. Runtime smoke readiness only; not production operations readiness.

## Source Architecture

This design derives from `docs/03-spec/runtime-smoke-config-and-runbook-spec.md` and `docs/04-architecture/runtime-smoke-config-and-runbook-architecture.md`.

## Grounded Existing Code Context

| Existing element | Verified anchor | Design relevance |
|---|---|---|
| Optional smoke enable flag | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:18` | Current smoke tests use `ATLAS_RUNTIME_SMOKE_ENABLED`. |
| Trinity smoke env vars | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:24` | Current smoke test reads `ATLAS_TRINITY_OFFICE_COMMAND` and smoke args. |
| Document Normalize smoke env vars | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:32` | Current smoke test reads `ATLAS_DOCUMENT_NORMALIZE_COMMAND` and smoke args. |
| Smoke default args | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:58` | Missing args default to `--version`. |
| Smoke raw command assertion | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:52` | Existing test sanitizes diagnostics and asserts command is not present. |
| Runtime config defaults | `backend/src/main/java/com/atlas/metadata/adapter/runtime/RuntimeAdapterConfiguration.java:9` | Adapter runtime default timeout/capture limit are 120 seconds and 65536 bytes. |
| Smoke capture limit | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:19` | Smoke command capture limit is 4096 bytes. |
| Spring Trinity runtime properties | `backend/src/main/java/com/atlas/metadata/adapter/TrinityOfficeConverterAdapter.java:45` | Adapter runtime properties differ from smoke env vars. |
| Spring Document Normalize runtime properties | `backend/src/main/java/com/atlas/metadata/adapter/DocumentNormalizeParserAdapter.java:46` | Adapter runtime properties differ from smoke env vars. |
| Seam guard | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:43` | Direct runtime/process references remain guarded. |

## Design Assumptions

- The accepted implementation will create the runbook at `docs/00-context/runbooks/runtime-smoke-config-and-runbook.md` and `.zh-CN.md` unless the user chooses another path before implementation.
- The first runbook version documents command-level smoke checks only.
- Local command env var values may be private paths and must be handled as sensitive local config.
- Default CI does not set `ATLAS_RUNTIME_SMOKE_ENABLED=true`.

## Design Scope

### In Scope

- Bilingual runbook document.
- Focused optional smoke test documentation and, if needed, small assertions that preserve current behavior.
- Traceability/roadmap evidence language.
- Verification commands and safety scans.

### Out Of Scope

- New endpoints, migrations, frontend UI, production runbook, deployment monitoring, secret manager, RBAC, audit, rate limit, or sample document processing smoke.

## Module Design

### Runbook Module

The runbook must contain these sections:

1. Purpose and maturity statement.
2. Prerequisites and approvals.
3. Smoke env var matrix.
4. Spring adapter runtime property matrix.
5. Default skip verification.
6. Approved local command smoke verification.
7. Outcome matrix.
8. Troubleshooting.
9. Disable and rollback.
10. Evidence checklist.
11. Safety bans.

### Smoke Test Evidence Module

The implementation must preserve the current evidence behavior:

- `ATLAS_RUNTIME_SMOKE_ENABLED` controls whether smoke runs.
- Missing command env vars self-skip.
- Missing args default to `--version`.
- Diagnostics are sanitized before evidence.
- Raw command values are not included in assertions or documentation evidence.

### Configuration Distinction Module

The runbook must include a side-by-side table:

| Purpose | Smoke env vars | Spring adapter runtime properties |
|---|---|---|
| Command health check | `ATLAS_*` env vars read by smoke tests | Not required |
| Configured adapter execution | Not sufficient | `atlas.runtime.*` Spring properties |
| Default CI | unset; smoke skipped | disabled by default |

### Closeout Evidence Module

Traceability must record:

- SDD accepted or Draft status.
- Whether default skip evidence was run.
- Whether approved pass evidence was run or skipped due to missing approved binaries.
- Exact verification commands run.
- Sanitized safety scan results.
- Residual risks and next recommended slice.

## API / Interface Design

No new public API is introduced.

The implementation-facing contract is documentation and test configuration:

- Smoke test env var names remain stable unless this SDD is updated.
- Spring adapter runtime property names remain documented as separate from smoke env vars.
- All command values remain local and never appear raw in API responses, docs, traceability, or final response.

## Data Design

No database schema, DTO, or persisted entity is introduced.

Markdown documentation is the durable artifact for this slice:

- SDD docs under `docs/01-*` through `docs/06-*`.
- Runbook under the accepted runbook path.
- Traceability under `docs/00-context/runtime-smoke-config-and-runbook-traceability.md`.
- Roadmap status under `ROADMAP.md`, `ROADMAP.zh-CN.md`, and slice roadmap companions.

## Workflow / Execution Design

### Default Skip Verification

1. Ensure approved runtime smoke env vars are absent in the command environment.
2. Run `cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test`.
3. Confirm both smoke tests self-skip.
4. Record safe skip evidence only.

### Approved Local Smoke Verification

1. Confirm local binaries are approved for testing and are installed outside the repository.
2. Set smoke env vars outside the repository.
3. Run `cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test`.
4. Confirm exit zero and no timeout for each configured runtime.
5. Record pass/fail summary without raw command values.

### Full Closeout Verification

1. Run `cd backend && mvn verify`.
2. Run seam guard focused test.
3. Run `git diff --check`.
4. Run focused secret/private-path scan over changed files.
5. Run focused network/dependency scan over changed files.
6. Update traceability and roadmap with maturity-qualified language.

## Validation And Error Handling

| Case | Expected behavior |
|---|---|
| Smoke disabled | Tests skip; not a failure. |
| Smoke enabled but command missing | Relevant test skips with safe missing-command reason. |
| Smoke enabled and command exits zero | Test passes; report generic runtime family pass. |
| Smoke enabled and command exits non-zero | Test fails; report sanitized diagnostic only. |
| Smoke command times out | Test fails; report timeout summary without raw command. |
| Safety scan finds raw command/private path | Fix evidence/docs before closeout. |

## Edge Case Trace

| Rule | Case | Result |
|---|---|---|
| Enable only on exact true | `ATLAS_RUNTIME_SMOKE_ENABLED` unset | skip |
| Enable only on exact true | `ATLAS_RUNTIME_SMOKE_ENABLED=TRUE` | enabled |
| Enable only on exact true | `ATLAS_RUNTIME_SMOKE_ENABLED=1` | skip |
| Args defaulting | args env var missing | `--version` |
| Args defaulting | args env var blank | `--version` |
| Args defaulting | args env var has two flags | two whitespace-split args |

## Testing Considerations

- `cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test`
- `cd backend && mvn -Dtest=AdapterSeamGuardTest test`
- `cd backend && mvn verify`
- `git diff --check`
- focused secret/private-path scan
- focused network/dependency scan

Frontend checks are not required unless implementation touches frontend files.

## Risks / Design Tradeoffs

| Risk | Decision |
|---|---|
| Runbook path may need user preference | Default to `docs/00-context/runbooks/` unless user chooses otherwise. |
| Command-level smoke is limited | State limitation clearly; do not imply sample document processing. |
| Local pass evidence may be unavailable | Closeout must report approved pass evidence as skipped with reason. |

## Open Questions

- OQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001: final runbook path.
- OQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002: future sample processing smoke.
