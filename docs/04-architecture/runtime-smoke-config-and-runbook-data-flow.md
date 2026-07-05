# Data Flow: Runtime Smoke Config And Runbook

## Status

Accepted and implemented on 2026-07-05. Runtime smoke readiness only; not production operations readiness.

## Overview

This slice moves only smoke configuration, local command outcome, and safe documentation evidence. It does not move source documents, generated Markdown, Wiki pages, graph edges, Ask citations, secrets, or production audit records.

## Flow 1: Default Verification With Smoke Disabled

```text
┌────────────────────────┐
│ Developer / CI command │
│ cd backend && mvn verify│
└────────────┬───────────┘
             ▼
┌────────────────────────┐
│ Optional smoke IT      │
│ reads enable env var   │
└────────────┬───────────┘
             ▼
┌────────────────────────┐
│ enable != true         │
│ JUnit assumption skip  │
└────────────┬───────────┘
             ▼
┌────────────────────────┐
│ Safe evidence          │
│ skipped by design      │
└────────────────────────┘
```

### Data Objects

| Object | Source | Destination | Safety rule |
|---|---|---|---|
| Enable flag | Local environment | Smoke test assumption | Do not persist. |
| Skip reason | JUnit result | Verification summary | Safe to report as generic status. |

## Flow 2: Approved Runtime Command Smoke

```text
┌─────────────────────────────┐
│ Platform administrator      │
│ sets local env outside repo  │
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ Focused smoke test          │
│ reads command + args env    │
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ Runtime executor            │
│ bounded local command check │
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ Sanitizer                   │
│ masks paths/hosts/secrets   │
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ Safe closeout evidence      │
│ pass/fail/timeout summary   │
└─────────────────────────────┘
```

### Field Mapping

| Input | Processing | Output |
|---|---|---|
| `ATLAS_RUNTIME_SMOKE_ENABLED` | Exact case-insensitive `true` check | enabled or skipped |
| command env var | Treated as local sensitive path | never copied to evidence |
| smoke args env var | Split into command args; defaults to `--version` | generic args behavior only |
| stdout/stderr | Bounded capture and sanitization | safe diagnostic summary |
| exit code / timeout | Result classification | pass, fail, or timeout |

## Flow 3: Documentation Closeout

```text
┌─────────────────────────┐
│ Verification result     │
│ skip/pass/fail/timeout  │
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│ Safety scan             │
│ secret/path/network     │
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│ Traceability            │
│ safe evidence only      │
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│ Roadmap                 │
│ smoke readiness status  │
└─────────────────────────┘
```

## Validation Rules

| Rule | Edge case | Expected result |
|---|---|---|
| Smoke disabled unless enable flag is `true` | Env var absent | Skip. |
| Smoke disabled unless enable flag is `true` | Env var `TRUE` | Execute if command is present. |
| Smoke disabled unless enable flag is `true` | Env var `yes` | Skip. |
| Command value is sensitive | Command contains a local path | Do not include raw value in docs/report. |
| Diagnostics are sanitized | Output contains path/host/token-like value | Mask before reporting. |
| Default CI remains mock-safe | No runtime binaries installed | `mvn verify` still passes with skips. |

## Data Lifecycle

1. Smoke env vars are set outside the repository.
2. Test code reads env vars at runtime.
3. Local command output is bounded and sanitized.
4. Only safe status summaries enter traceability/final response.
5. Raw env values and diagnostics are discarded.

## Exclusions

- No source document content enters this flow.
- No raw command value is persisted.
- No production audit event is created.
- No external network call is introduced by this slice.
