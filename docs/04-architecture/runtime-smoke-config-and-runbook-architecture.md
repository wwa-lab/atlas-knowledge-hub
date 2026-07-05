# System Architecture: Runtime Smoke Config And Runbook

## Status

Accepted and implemented on 2026-07-05. Runtime smoke readiness only; not production operations readiness.

## Overview

- **Architecture summary:** This slice is an operational documentation and verification layer around the existing real-runtime adapter implementation. It does not add a new runtime engine, product API, database table, or frontend surface.
- **Design objective:** Keep optional real-runtime smoke checks deliberate, safe, and non-blocking for default CI.
- **Architectural style:** Adapter-preserving operational readiness layer.

## Source Specification

- **Feature / System name:** Runtime Smoke Config And Runbook
- **Scope summary:** Define smoke env vars, runbook outcome interpretation, safe evidence, and adapter-boundary verification for configured local `trinity-office` and `document-normalize` smoke checks.

## Architectural Drivers

### Key Functional Drivers

- Optional smoke checks must self-skip unless explicitly enabled.
- Approved local commands must never be committed or exposed raw.
- Smoke-only env vars and Spring adapter runtime properties must be documented separately.
- Runtime invocation remains inside adapter/runtime implementation and test scope.

### Key Non-Functional Drivers

- Default CI remains mock-safe.
- Diagnostics remain bounded and sanitized.
- Evidence language must distinguish smoke readiness from production readiness.

### Constraints And Assumptions

- Real runtime binaries are optional local dependencies, not repository dependencies.
- This slice does not add a public API.
- This slice does not add persistence.
- This slice does not replace adapter contract tests.

## System Context

| Actor / System | Role |
|---|---|
| Platform administrator | Sets approved local smoke env vars outside the repository. |
| Backend test suite | Runs optional smoke tests with JUnit self-skip behavior. |
| Runtime adapter implementation | Owns actual configured runtime execution behind converter/parser adapters. |
| Traceability / roadmap docs | Store safe readiness evidence and maturity language. |

## High-Level Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│  Platform Administrator                                      │
│  Provides approved local env vars outside the repository      │
└──────────────────────────────┬───────────────────────────────┘
                               │ local shell env, not committed
                               ▼
┌──────────────────────────────────────────────────────────────┐
│  Backend Verification                                         │
│  mvn verify · focused smoke IT · seam guard · safety scans     │
└──────────────────────────────┬───────────────────────────────┘
                               │ optional command smoke only
                               ▼
┌──────────────────────────────────────────────────────────────┐
│  Smoke Test Boundary                                          │
│  self-skip assumptions · safe args default · sanitized output  │
└──────────────────────────────┬───────────────────────────────┘
                               │ allowed direct process check
                               ▼
┌──────────────────────────────────────────────────────────────┐
│  Adapter Runtime Boundary                                     │
│  converter runtime · parser runtime · sanitizer · seam guard   │
└──────────────────────────────┬───────────────────────────────┘
                               │ safe evidence only
                               ▼
┌──────────────────────────────────────────────────────────────┐
│  Documentation Evidence                                       │
│  runbook · traceability · roadmap maturity statement           │
└──────────────────────────────────────────────────────────────┘
```

## Layer Summary

- **Documentation layer:** owns the runbook and evidence checklist.
- **Verification layer:** owns default backend verification, focused smoke execution, seam guard, and scans.
- **Smoke-test layer:** owns optional env var interpretation and self-skip behavior.
- **Adapter/runtime layer:** remains the only allowed product runtime execution boundary.

## Component Breakdown

### Backend / Verification Components

- **Optional runtime smoke test:** checks command health only when enabled and configured.
- **Runtime output sanitizer:** masks diagnostics before evidence is reported.
- **Adapter seam guard:** keeps direct runtime/process references out of product layers.

### Configuration / Administration Modules

- **Smoke env var contract:** controls test-only local runtime checks.
- **Spring adapter runtime properties:** controls configured adapter execution in the backend runtime and is documented separately from smoke env vars.

### Monitoring / Audit Modules

- **Traceability evidence:** records safe command-independent results and skipped checks.
- **Roadmap maturity language:** prevents smoke readiness from becoming a production-readiness claim.

### Integration Adapters

- **`trinity-office` adapter boundary:** existing converter runtime boundary.
- **`document-normalize` adapter boundary:** existing parser runtime boundary.

## Data Architecture

### Conceptual Entities

| Entity | Description | Persistence |
|---|---|---|
| Smoke configuration | Local env var inputs for optional smoke checks | Not persisted |
| Smoke outcome | Safe skip/pass/fail/timeout evidence | Documentation only |
| Runbook | Operator-facing smoke workflow | Markdown docs |
| Roadmap status | Maturity-qualified slice state | Markdown docs |

### State / Status Models

- Smoke readiness: `Draft -> Accepted -> Implemented with skip evidence -> Implemented with approved pass evidence`
- Smoke execution: `Disabled -> Skipped | Enabled missing command -> Skipped | Enabled command -> Passed | Failed | Timed out`

### Persistence Responsibilities

No database or API persistence is introduced by this slice.

## Integration Architecture

| Integration | Pattern | Data exchanged |
|---|---|---|
| Optional local runtime command | Local process smoke only in allowed test/runtime scope | Exit code and bounded diagnostics |
| Runtime adapter configuration | Spring server-side properties | Enabled flag and command configuration, never raw in product responses |
| Documentation evidence | Manual update during closeout | Safe summaries, skipped checks, residual risks |

## Workflow / Runtime Architecture

### Request Flow

There is no user-facing API request flow. The operator runs local verification commands after SDD acceptance and implementation.

### Execution Flow

1. Default backend verification runs with smoke disabled and skipped.
2. Optional focused smoke runs only when env vars explicitly enable it.
3. The smoke test executes command health checks using approved local command values.
4. The output sanitizer removes sensitive diagnostics.
5. Traceability records only safe evidence.

### Failure And Retry Handling

- Missing enable flag: skip, not failure.
- Missing command: skip, not failure.
- Non-zero exit: focused smoke failure, not default CI failure.
- Timeout: focused smoke failure with safe timeout summary.
- Unsafe diagnostic: treat as failed evidence until sanitized.

## API / Interface Boundaries

### Major Inbound Interfaces

None. No new public API is introduced.

### Internal Module Boundaries

- Smoke tests may use the local runtime executor for command health checks.
- Product services and controllers must continue to use converter/parser adapters only.

### Outbound Integrations

| Target | Protocol | Triggered by |
|---|---|---|
| Approved local runtime command | Local process | Optional smoke test only |

## Deployment / Environment Considerations

- Supported environment for this slice: local developer/operator machine with approved binaries.
- Default CI must not set smoke env vars.
- Local command env vars are treated as sensitive and never committed.
- Production deployment health checks are deferred to a later operations slice.

## Security / Reliability / Observability

### Secret Protection

Raw command paths, private paths, credentials, shell history, and raw diagnostics must not be committed or echoed in reports.

### Reliability

Self-skip behavior prevents missing optional binaries from breaking default verification.

### Monitoring / Logging

Evidence is documentation-based in this slice. Production logging and audit are out of scope.

## Risks / Tradeoffs

| # | Risk / Tradeoff | Notes |
|---|---|---|
| 1 | Command health smoke is weaker than document processing smoke | Keeps this slice safe and small; later sample fixture smoke can be added by accepted scope. |
| 2 | Env var command values are local paths | Runbook must treat them as sensitive and avoid raw evidence. |
| 3 | Default skip can hide local runtime drift | Closeout must name whether approved pass evidence was run or skipped. |

## Open Questions

1. OQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001: final runbook path.
2. OQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002: whether future sample processing smoke should be added.
