# Tasks: Runtime Smoke Config And Runbook

## Status

Accepted and implemented on 2026-07-05. Runtime smoke readiness only; not production operations readiness.

## Overview

This task list creates an implementation-ready path for runtime smoke configuration and runbook evidence. It is intentionally documentation/test focused and does not add public APIs, persistence, frontend UI, or production operations.

## Source Design

- **System name:** Runtime Smoke Config And Runbook
- **Design scope summary:** Bilingual runbook, smoke env var contract, safe evidence checklist, optional focused smoke verification, seam guard preservation, and maturity-qualified roadmap/traceability updates.

## Workstreams

- **Acceptance and SDD closeout:** record user acceptance before implementation.
- **Runbook documentation:** create the bilingual runbook with outcome matrix and safety bans.
- **Verification:** preserve smoke self-skip/pass behavior, run backend checks, run safety scans.
- **Context status:** update traceability and roadmaps after implementation.

## Task Breakdown By Domain

### Documentation / Runbook

- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001: Record SDD acceptance.
- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002: Create bilingual runbook.
- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003: Add env var and Spring property distinction.
- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004: Add outcome and troubleshooting matrix.

### Backend Verification

- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005: Verify default smoke self-skip.
- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006: Verify optional approved local smoke path when available.
- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007: Preserve seam guard and adapter boundaries.

### Safety / Closeout

- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008: Run full verification and safety scans.
- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009: Update traceability and roadmap maturity status.

## Task Details

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001: Record SDD acceptance

- **Objective:** Prevent implementation before user acceptance.
- **Scope:** Update traceability from Draft to Accepted only after the user explicitly accepts this SDD.
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-012
- **Dependencies:** None
- **Owner type:** product / implementation
- **Priority:** Must
- **Verification:** Traceability contains acceptance record before implementation changes.

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002: Create bilingual runtime smoke runbook

- **Objective:** Provide the durable operator guide for local runtime smoke checks.
- **Scope:** Create English and Simplified Chinese runbook files under the accepted runbook path. Include purpose, maturity statement, prerequisites, commands, outcome matrix, troubleshooting, disable/rollback, evidence checklist, and safety bans.
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-010, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-011
- **Dependencies:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001
- **Owner type:** platform / docs
- **Priority:** Must
- **Verification:** Runbook files exist; scan finds no real company data, raw commands, private paths, secrets, screenshots, or provider logs.

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003: Document configuration distinctions

- **Objective:** Prevent confusion between smoke-test env vars and Spring adapter runtime properties.
- **Scope:** Add side-by-side env/property tables and safe-value rules to the runbook.
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003
- **Dependencies:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002
- **Owner type:** platform
- **Priority:** Must
- **Verification:** Runbook lists all required smoke env vars and Spring properties exactly as specified in the API guide.

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004: Add outcome and troubleshooting matrix

- **Objective:** Make smoke results easy to interpret without leaking local details.
- **Scope:** Document skip, missing command, pass, non-zero exit, timeout, unsafe diagnostic, blocked approval, troubleshooting, and rollback/disable actions.
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-010
- **Dependencies:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002
- **Owner type:** platform / security
- **Priority:** Must
- **Verification:** Outcome matrix covers every status in the spec and contains no raw command/path examples.

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005: Verify default smoke self-skip

- **Objective:** Prove ordinary verification remains mock-safe.
- **Scope:** Run focused smoke tests without approved smoke env vars and record skip evidence.
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009
- **Dependencies:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001
- **Owner type:** QA / backend
- **Priority:** Must
- **Verification:** `cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test` passes with optional smoke tests skipped.

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006: Verify approved local smoke path when available

- **Objective:** Capture safe pass/fail evidence for approved local runtime binaries.
- **Scope:** If approved local binaries are available, run focused smoke with env vars set outside the repository. If unavailable, record the check as skipped with reason.
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-011
- **Dependencies:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005
- **Owner type:** platform / QA
- **Priority:** Should
- **Verification:** Focused smoke pass/fail evidence is safe, or traceability states approved binaries were unavailable.

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007: Preserve seam guard and adapter boundaries

- **Objective:** Ensure runtime smoke readiness does not leak direct runtime calls into product layers.
- **Scope:** Run seam guard and avoid product-layer runtime references.
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008
- **Dependencies:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001
- **Owner type:** backend / architecture
- **Priority:** Must
- **Verification:** `cd backend && mvn -Dtest=AdapterSeamGuardTest test` passes.

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008: Run full verification and safety scans

- **Objective:** Close the slice with concrete evidence.
- **Scope:** Run backend verification, diff hygiene, focused secret/private-path scan, and focused network/dependency scan over changed files.
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-011
- **Dependencies:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 to T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007
- **Owner type:** QA / security
- **Priority:** Must
- **Verification:** `cd backend && mvn verify`, `git diff --check`, focused secret/private-path scan, and focused network/dependency scan are reported.

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009: Update traceability and roadmap

- **Objective:** Keep durable context current without overstating readiness.
- **Scope:** Update traceability, slice roadmap, and product roadmap after implementation with changed docs/code, verification, skipped checks, residual risks, and next slice.
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-012
- **Dependencies:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008
- **Owner type:** docs / product
- **Priority:** Must
- **Verification:** Roadmap and traceability state runtime smoke readiness only, not production operations readiness.

## Dependency Plan

- **Critical path:** T-001 -> T-002 -> T-003/T-004 -> T-005/T-007 -> T-008 -> T-009
- **Parallel workstreams:** T-003 and T-004 can run together after T-002. T-005 and T-007 can run after acceptance.
- **Optional branch:** T-006 runs only when approved local binaries are available.

## Risks / Blockers

- Approved local binaries may not be available; pass evidence can be skipped but must be reported.
- Runbook path may be adjusted by user before implementation.
- A later sample document smoke would need a separate accepted scope.

## Implementation Result

All tasks T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 through T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009 are complete for command-level runtime smoke readiness.

| Task | Result |
|---|---|
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 | User acceptance was recorded in traceability before runbook implementation. |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 | Bilingual runbooks were created under `docs/00-context/runbooks/`. |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003 | Runbooks include smoke env var and Spring adapter property distinction tables. |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004 | Runbooks include outcome, troubleshooting, disable/rollback, and safety-ban matrices. |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005 | Default self-skip verification passed: 2 focused smoke tests skipped by design. |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006 | Approved local pass evidence was skipped because no approved local runtime command values were provided for this run. |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007 | Seam guard verification passed: 3 tests passed. |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008 | Backend verification, diff hygiene, and focused safety scans passed. |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009 | Traceability and roadmap status were updated with runtime smoke readiness language only. |

## Verification Evidence

| Check | Result |
|---|---|
| `cd backend && env -u ... mvn -Dtest=ConfiguredRuntimeSmokeIT test` | Passed: 2 tests run, 0 failures, 0 errors, 2 skipped. |
| `cd backend && mvn -Dtest=AdapterSeamGuardTest test` | Passed: 3 tests run, 0 failures, 0 errors. |
| `cd backend && mvn verify` | Passed: 120 unit tests and 48 integration tests; 2 optional runtime smoke tests skipped by design. |
| `npm run agent:check-sdd -- --slice runtime-smoke-config-and-runbook` | Passed with non-blocking companion-doc/report warnings. |
| `git diff --check` | Passed. |
| Focused file existence check | Passed for 20 bilingual SDD files and 2 bilingual runbook files. |
| Focused REQ/US/T bilingual ID parity check | Passed. |
| Focused deferred-decision marker scan | Passed. |
| Focused secret/private-path scan | Passed. |
| Focused network/dependency scan | Passed. |

## Required Verification Commands After Implementation

```bash
cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test
cd backend && mvn -Dtest=AdapterSeamGuardTest test
cd backend && mvn verify
git diff --check
```

Also run focused secret/private-path and network/dependency scans over changed files. Frontend checks are not required unless implementation touches frontend files.

## Recommended Codex Handoff After Acceptance

```text
Implement the runtime-smoke-config-and-runbook slice strictly against docs/03-spec/runtime-smoke-config-and-runbook-spec.md and docs/06-tasks/runtime-smoke-config-and-runbook-tasks.md: create the bilingual runbook, preserve optional smoke self-skip behavior, record safe evidence only, keep default CI mock-safe, do not add public APIs or product runtime behavior, and stop if implementation would diverge from the accepted spec.
```
