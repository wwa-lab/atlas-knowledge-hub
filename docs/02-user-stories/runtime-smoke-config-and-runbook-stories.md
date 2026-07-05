# User Stories: Runtime Smoke Config And Runbook

## Status

Accepted and implemented on 2026-07-05. Runtime smoke readiness only; not production operations readiness.

## Story 1

**ID:** US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001
**Title:** Accept runtime smoke scope before implementation

**Story:**
As a product owner,
I want the runtime smoke configuration and runbook scope accepted before implementation,
so that operational documentation does not expand beyond the approved adapter-safe runtime slice.

### Acceptance Criteria

1. **Given** this SDD set is still Draft
   **When** an implementation agent prepares to edit code, tests, or runbook docs
   **Then** the agent stops and asks for user acceptance.

2. **Given** the user accepts the SDD
   **When** implementation begins
   **Then** traceability records acceptance and the implementation follows `docs/06-tasks/runtime-smoke-config-and-runbook-tasks.md`.

### Notes / Assumptions

- This story maps to REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 and REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-012.

### Dependencies

- Existing `real-office-parser-runtime` implementation and traceability.

### Out Of Scope

- Product code before acceptance.

### Open Questions

- None.

## Story 2

**ID:** US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002
**Title:** Configure optional local runtime smoke checks

**Story:**
As a platform administrator,
I want a precise smoke-test configuration contract,
so that approved local runtime binaries can be checked without changing the default CI path.

### Acceptance Criteria

1. **Given** `ATLAS_RUNTIME_SMOKE_ENABLED` is absent or not `true`
   **When** smoke tests run
   **Then** they self-skip and do not require runtime binaries.

2. **Given** smoke is enabled but the command env var is missing
   **When** the relevant smoke test runs
   **Then** it self-skips with a safe missing-command reason.

3. **Given** smoke is enabled and an approved command is present
   **When** the smoke test executes
   **Then** it uses configured smoke args or defaults to `--version`.

### Notes / Assumptions

- This story maps to REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 to 004 and REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009.

### Dependencies

- Existing `ConfiguredRuntimeSmokeIT` behavior.

### Out Of Scope

- Installing runtime binaries.

### Open Questions

- None.

## Story 3

**ID:** US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003
**Title:** Capture safe runtime smoke evidence

**Story:**
As a delivery lead,
I want smoke evidence that distinguishes skip, pass, and fail without leaking local details,
so that readiness can be reviewed safely.

### Acceptance Criteria

1. **Given** a smoke check passes
   **When** evidence is recorded
   **Then** it names the runtime family and result without raw command paths.

2. **Given** a smoke check fails
   **When** diagnostics are captured
   **Then** diagnostics are bounded and sanitized before they are reported.

3. **Given** a smoke check times out or exits non-zero
   **When** the runbook interprets the result
   **Then** it provides safe troubleshooting and rollback steps.

### Notes / Assumptions

- This story maps to REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005, 006, 010, and 011.

### Dependencies

- Existing runtime output sanitizer and smoke test diagnostics.

### Out Of Scope

- Persisting smoke evidence in production audit tables.

### Open Questions

- None.

## Story 4

**ID:** US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004
**Title:** Provide a mock-safe runtime runbook

**Story:**
As an implementation or operations engineer,
I want a runbook for local runtime smoke setup and closeout,
so that approved binaries can be verified with repeatable commands and safe fixtures.

### Acceptance Criteria

1. **Given** the runbook is created
   **When** an engineer reads it
   **Then** it includes setup, required env vars, run commands, expected outcomes, troubleshooting, disable/rollback, and evidence checklist.

2. **Given** the runbook references data or fixtures
   **When** verification is performed
   **Then** it uses only mock/sample-safe inputs and forbids real company documents.

### Notes / Assumptions

- This story maps to REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007, 010, and 011.

### Dependencies

- User decision on runbook path if needed.

### Out Of Scope

- Production deployment operations.

### Open Questions

- OQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001.

## Story 5

**ID:** US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005
**Title:** Preserve adapter boundaries during smoke readiness work

**Story:**
As an architect,
I want runtime smoke readiness to stay behind adapter/runtime boundaries,
so that Atlas remains parser-neutral and product services do not become tool runners.

### Acceptance Criteria

1. **Given** implementation changes are made
   **When** seam guard tests run
   **Then** direct process/runtime references remain limited to allowed adapter/runtime scope.

2. **Given** roadmap and traceability are updated
   **When** the slice is closed
   **Then** status language states runtime smoke readiness only and does not claim production operations readiness.

### Notes / Assumptions

- This story maps to REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008, 009, and 012.

### Dependencies

- Existing `AdapterSeamGuardTest`.

### Out Of Scope

- Changing core parser/converter adapter contracts.

### Open Questions

- None.
