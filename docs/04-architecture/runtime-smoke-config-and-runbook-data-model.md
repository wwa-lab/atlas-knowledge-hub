# Data Model: Runtime Smoke Config And Runbook

## Status

Accepted and implemented on 2026-07-05. Runtime smoke readiness only; not production operations readiness.

## Overview

This slice does not introduce database entities or schema changes. The data model is a documentation/configuration model for optional smoke checks and safe evidence.

## Entity Relationship Diagram

```text
┌──────────────────────┐       produces       ┌──────────────────────┐
│ SmokeConfiguration   │ ───────────────────▶ │ SmokeOutcome         │
│ local env only       │                      │ safe summary only    │
└──────────┬───────────┘                      └──────────┬───────────┘
           │ documented by                              │ recorded in
           ▼                                            ▼
┌──────────────────────┐                      ┌──────────────────────┐
│ RuntimeSmokeRunbook  │                      │ TraceabilityStatus   │
│ markdown doc         │                      │ markdown doc         │
└──────────────────────┘                      └──────────────────────┘
```

## Entity Definitions

### SmokeConfiguration

- **Persistence:** none; local process environment only.
- **Fields:**
  - `enabledFlag`: maps to `ATLAS_RUNTIME_SMOKE_ENABLED`; exact `true` enables smoke.
  - `runtimeFamily`: `trinity-office` or `document-normalize`.
  - `commandEnvVar`: one of the command env var names; value is sensitive and must not be persisted.
  - `argsEnvVar`: one of the args env var names; value may be local and must not be committed.
  - `defaultArgs`: `--version` when args env var is absent.

### SmokeOutcome

- **Persistence:** safe documentation evidence only.
- **Fields:**
  - `runtimeFamily`: runtime family label.
  - `status`: `SKIPPED`, `PASSED`, `FAILED`, `TIMED_OUT`, or `BLOCKED`.
  - `safeReason`: sanitized reason or generic skip message.
  - `verificationCommand`: generic command name, not raw local command path.
  - `rawCommandPath`: forbidden.
  - `rawDiagnostic`: forbidden.

### RuntimeSmokeRunbook

- **Persistence:** Markdown documentation after acceptance and implementation.
- **Fields / sections:**
  - prerequisites
  - env var matrix
  - default skip verification
  - approved local smoke verification
  - outcome matrix
  - troubleshooting
  - disable/rollback
  - evidence checklist
  - safety bans

### TraceabilityStatus

- **Persistence:** Markdown documentation.
- **Fields / sections:**
  - SDD acceptance state
  - SDD skill chain evidence
  - changed docs/code summary
  - verification evidence
  - skipped checks
  - residual risks
  - maturity statement

## State Models

### SmokeConfiguration State

```text
UNCONFIGURED
  ├─ enable flag absent/not true ─▶ DISABLED
  └─ enable flag true
       ├─ command missing ───────▶ ENABLED_MISSING_COMMAND
       └─ command present ───────▶ ENABLED_WITH_COMMAND
```

### SmokeOutcome State

```text
DISABLED ───────────────▶ SKIPPED
ENABLED_MISSING_COMMAND ─▶ SKIPPED
ENABLED_WITH_COMMAND ────▶ PASSED
ENABLED_WITH_COMMAND ────▶ FAILED
ENABLED_WITH_COMMAND ────▶ TIMED_OUT
```

## Configuration Entities

| Name | Owner | Type | Validation | Raw value allowed in docs? |
|---|---|---|---|---|
| `ATLAS_RUNTIME_SMOKE_ENABLED` | Operator | string/boolean-like | case-insensitive `true` only | Yes, name only |
| `ATLAS_TRINITY_OFFICE_COMMAND` | Operator | local command | non-blank to execute | No raw value |
| `ATLAS_TRINITY_OFFICE_SMOKE_ARGS` | Operator | arg string | optional; split on whitespace | Avoid raw local values |
| `ATLAS_DOCUMENT_NORMALIZE_COMMAND` | Operator | local command | non-blank to execute | No raw value |
| `ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS` | Operator | arg string | optional; split on whitespace | Avoid raw local values |

## Audit Entities

None introduced. Production audit for runtime operations is out of scope.

## Field Mapping

| Smoke test input | Safe evidence field |
|---|---|
| enable flag absent | `status=SKIPPED`, `safeReason=runtime smoke disabled` |
| command env var missing | `status=SKIPPED`, `safeReason=<runtime family> command missing` |
| command exits zero | `status=PASSED`, `safeReason=approved local smoke passed` |
| command exits non-zero | `status=FAILED`, sanitized diagnostic summary |
| command times out | `status=TIMED_OUT`, safe timeout summary |
