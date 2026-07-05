# Runbook: Runtime Smoke Config And Runbook

## Status

Accepted and implemented for command-level runtime smoke readiness. This runbook does not establish production operations readiness.

## Purpose

This runbook explains how to verify optional local `trinity-office` and `document-normalize` runtime command health checks without changing Atlas' default mock-safe CI path. It is scoped to approved local smoke checks only.

## Maturity Statement

- **Included:** optional command-level smoke checks, safe evidence capture, skip/pass/fail interpretation, and disable/rollback instructions.
- **Not included:** production deployment monitoring, SLOs, alerting, secret manager integration, real company document processing, or sample document conversion/parser smoke.

## Prerequisites And Approvals

Before running an approved local smoke check:

1. Confirm the runtime binary is approved for local smoke verification.
2. Install or expose the command outside this repository.
3. Set command environment variables only in the local shell/session used for the smoke run.
4. Do not commit command paths, local env files, shell history, raw diagnostics, screenshots, or machine-specific logs.
5. Use only mock/sample-safe commands and arguments. Do not use real company documents.

## Configuration Matrix

### Optional Smoke Test Environment Variables

| Variable | Purpose | Default / Behavior | Evidence rule |
|---|---|---|---|
| `ATLAS_RUNTIME_SMOKE_ENABLED` | Enables optional smoke tests | Any value other than case-insensitive `true` self-skips | Variable name can be reported. |
| `ATLAS_TRINITY_OFFICE_COMMAND` | Approved Trinity command for smoke test | Missing value self-skips Trinity smoke | Raw value must not be reported. |
| `ATLAS_TRINITY_OFFICE_SMOKE_ARGS` | Trinity command args for smoke test | Defaults to `--version` | Avoid machine-specific values in evidence. |
| `ATLAS_DOCUMENT_NORMALIZE_COMMAND` | Approved Document Normalize command for smoke test | Missing value self-skips Document Normalize smoke | Raw value must not be reported. |
| `ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS` | Document Normalize command args for smoke test | Defaults to `--version` | Avoid machine-specific values in evidence. |

### Spring Adapter Runtime Properties

These properties are for configured adapter execution, not for the optional command-health smoke test:

| Property | Default | Scope |
|---|---|---|
| `atlas.runtime.trinity-office.enabled` | `false` | Backend configured converter adapter runtime. |
| `atlas.runtime.trinity-office.command` | blank | Backend configured converter adapter runtime. |
| `atlas.runtime.document-normalize.enabled` | `false` | Backend configured parser adapter runtime. |
| `atlas.runtime.document-normalize.command` | blank | Backend configured parser adapter runtime. |

### Distinction Table

| Purpose | Smoke env vars | Spring adapter runtime properties |
|---|---|---|
| Command-health smoke | Required only for focused smoke tests | Not required |
| Configured adapter execution | Not sufficient by itself | Required when running configured adapter mode |
| Default CI | Unset; smoke tests self-skip | Disabled by default |

## Default Skip Verification

Use this check when no approved local runtime binaries are available:

```bash
unset ATLAS_RUNTIME_SMOKE_ENABLED
unset ATLAS_TRINITY_OFFICE_COMMAND
unset ATLAS_TRINITY_OFFICE_SMOKE_ARGS
unset ATLAS_DOCUMENT_NORMALIZE_COMMAND
unset ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS
cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test
```

Expected result:

- The focused test command exits successfully.
- Both optional runtime smoke tests are skipped by design.
- The absence of real runtime binaries is not treated as product failure.

Safe evidence wording:

```text
ConfiguredRuntimeSmokeIT passed with optional runtime smoke tests skipped because approved local runtime env vars were absent.
```

## Approved Local Smoke Verification

Run this only after local binary approval. Use placeholders here; set real values only in your local shell and do not record them.

```bash
export ATLAS_RUNTIME_SMOKE_ENABLED=true
export ATLAS_TRINITY_OFFICE_COMMAND="<approved-trinity-command>"
export ATLAS_TRINITY_OFFICE_SMOKE_ARGS="--version"
export ATLAS_DOCUMENT_NORMALIZE_COMMAND="<approved-document-normalize-command>"
export ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS="--version"
cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test
```

Expected result:

- Each configured runtime command exits zero.
- No timeout is reported.
- Sanitized diagnostics do not include the raw command value.

Safe evidence wording:

```text
ConfiguredRuntimeSmokeIT passed for approved local Trinity and Document Normalize command-health smoke checks; raw command values were not recorded.
```

If only one runtime is approved, set only that command and record the other runtime as skipped with reason.

## Outcome Matrix

| Outcome | Meaning | Action |
|---|---|---|
| Skipped by disabled smoke | `ATLAS_RUNTIME_SMOKE_ENABLED` is absent or not `true` | Accept for default CI; record skip evidence. |
| Skipped by missing command | Smoke enabled but one command env var is missing | Accept only when that runtime is not approved locally; record reason. |
| Passed | Approved command exits zero and does not time out | Record safe pass evidence without raw command values. |
| Failed | Approved command exits non-zero | Record sanitized summary; do not paste raw output. |
| Timed out | Command does not finish in the allowed time | Record safe timeout summary and disable smoke before ordinary verification. |
| Unsafe diagnostic | Output contains private path, host, credential-like value, or raw local details | Sanitize or discard evidence before closeout. |
| Blocked | Approved local binary is not available | Skip approved-pass evidence with reason; do not fabricate success. |

## Troubleshooting

| Symptom | Likely cause | Safe next step |
|---|---|---|
| All smoke tests skipped | Smoke enable flag is absent or not `true` | This is expected for default CI. |
| One runtime skipped | Command env var missing | Confirm whether that runtime was approved for local smoke. |
| Non-zero exit | Binary unavailable, incompatible args, or runtime error | Run local diagnosis outside committed evidence; record only sanitized summary. |
| Timeout | Command hangs or starts a long-running process | Disable smoke env vars, use `--version`, and keep ordinary verification mock-safe. |
| Diagnostic includes local path or host | Runtime emitted machine details | Do not paste raw output; use sanitized summary only. |

## Disable And Rollback

To return to the default mock-safe state:

```bash
unset ATLAS_RUNTIME_SMOKE_ENABLED
unset ATLAS_TRINITY_OFFICE_COMMAND
unset ATLAS_TRINITY_OFFICE_SMOKE_ARGS
unset ATLAS_DOCUMENT_NORMALIZE_COMMAND
unset ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS
cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test
```

Expected rollback result:

- Optional runtime smoke tests self-skip.
- Default backend verification can run without real binaries.

## Closeout Evidence Checklist

Before closing this slice or a local smoke check, record:

- Whether SDD acceptance was recorded before implementation.
- Whether `ConfiguredRuntimeSmokeIT` passed with skips or approved pass evidence.
- Whether `AdapterSeamGuardTest` passed.
- Whether `mvn verify` passed.
- Whether `git diff --check` passed.
- Whether focused secret/private-path scan passed.
- Whether focused network/dependency scan passed.
- Whether approved local runtime pass evidence was skipped, and why.
- Residual risks, especially command-level smoke limitations.

## Safety Bans

Do not commit, paste, or attach:

- real company documents;
- raw command paths or local binary paths;
- raw stdout/stderr diagnostics;
- secrets, credentials, or provider logs;
- private absolute paths;
- internal hostnames or endpoints;
- shell history;
- local env files;
- screenshots that reveal local paths, credentials, or confidential content.

## Next Scope Boundary

A future accepted slice may add mock/sample-safe document processing smoke. That work must define fixtures, expected converter/parser output, evidence handling, and verification before implementation.
