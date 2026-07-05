# Runtime Smoke Config And Runbook - API Implementation Guide

## Status

Accepted and implemented on 2026-07-05. Runtime smoke readiness only; not production operations readiness.

## Overview

This slice introduces no new public API endpoint and no request/response schema changes. The "contract" is the local runtime smoke configuration contract, the documented Spring adapter runtime property distinction, and the verification behavior that implementation must preserve.

## Public API Changes

None.

## Configuration Contract

### Optional Smoke Test Environment Variables

| Variable | Required to execute smoke? | Default / Behavior | Raw value allowed in committed evidence? |
|---|---|---|---|
| `ATLAS_RUNTIME_SMOKE_ENABLED` | Yes | any value other than `true` self-skips | variable name only |
| `ATLAS_TRINITY_OFFICE_COMMAND` | Yes for Trinity smoke | missing self-skips Trinity smoke | no |
| `ATLAS_TRINITY_OFFICE_SMOKE_ARGS` | No | defaults to `--version` | avoid machine-specific values |
| `ATLAS_DOCUMENT_NORMALIZE_COMMAND` | Yes for Document Normalize smoke | missing self-skips Document Normalize smoke | no |
| `ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS` | No | defaults to `--version` | avoid machine-specific values |

### Spring Adapter Runtime Properties

These properties belong to configured adapter execution, not smoke-only command checks:

| Property | Default | Raw value allowed in API/docs? |
|---|---|---|
| `atlas.runtime.trinity-office.enabled` | false | property name only |
| `atlas.runtime.trinity-office.command` | blank | no raw value |
| `atlas.runtime.document-normalize.enabled` | false | property name only |
| `atlas.runtime.document-normalize.command` | blank | no raw value |

## Error / Outcome Reference

| Outcome | Meaning | Required reporting |
|---|---|---|
| `SKIPPED_DISABLED` | smoke enable flag absent or not `true` | safe skip summary |
| `SKIPPED_MISSING_COMMAND` | smoke enabled but command env var missing | safe missing-command summary |
| `PASSED` | command exits zero and no timeout | runtime family pass summary, no raw command |
| `FAILED` | command exits non-zero | sanitized diagnostic only |
| `TIMED_OUT` | command did not finish in allowed timeout | safe timeout summary |
| `BLOCKED` | approved local binary not available or not approved | skipped check with reason |

## Endpoint Reference

No endpoint reference applies.

## State Reference

```text
DRAFT -> ACCEPTED -> IMPLEMENTED_WITH_SKIP_EVIDENCE
                    -> IMPLEMENTED_WITH_APPROVED_PASS_EVIDENCE
```

## Concurrency

Not applicable. No persistent API mutation is introduced.

## Integration Dependencies

- Local approved `trinity-office` command, if Trinity smoke pass evidence is requested.
- Local approved `document-normalize` command, if Document Normalize smoke pass evidence is requested.
- Default verification must not depend on either command.

## Contract Test Expectations

- `ConfiguredRuntimeSmokeIT` self-skips when `ATLAS_RUNTIME_SMOKE_ENABLED` is not true.
- `ConfiguredRuntimeSmokeIT` self-skips when the relevant command env var is missing.
- Diagnostics are sanitized and do not contain the raw command value.
- `AdapterSeamGuardTest` continues to pass.
- `mvn verify` passes without local runtime binaries.
