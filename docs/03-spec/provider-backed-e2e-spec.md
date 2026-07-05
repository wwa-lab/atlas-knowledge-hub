# Specification: Provider-Backed E2E

## Status

Draft. Lightweight behavior source of truth for `provider-backed-e2e`. This pass defines the slice only; it does not implement scripts, adapters, tests, or package commands.

## Overview

`provider-backed-e2e` adds a third acceptance layer for an intentionally provider-backed Ask journey. Unlike first-layer mock E2E and second-layer local full-stack E2E, this layer is allowed to call a configured chat provider such as DeepSeek or GitHub Models, but only after explicit opt-in and only through the ModelAdapter boundary. The journey still uses local services and mock/sample knowledge data, proving the real Ask page can use provider-generated text without weakening Atlas data-safety rules.

## Acceptance Layer Model

| Layer | Purpose | Provider Calls | Data |
|---|---|---:|---|
| First-layer | Fast local/mock gate for implemented surfaces and backend contracts. | No | Mock/sample only |
| Second-layer | Local full-stack browser + Spring Boot API + temporary PostgreSQL gate. | No | Mock/sample only |
| Third-layer provider-backed | Opt-in local provider-backed Ask browser journey. | Yes, configured provider through ModelAdapter only | Mock/sample only |

Third-layer behavior must not be wired into first-layer, second-layer, `npm run e2e`, ordinary backend verification, or default CI.

## Scope

In scope:

- One future opt-in command that starts the local stack and executes provider-backed Ask E2E.
- Preflight checks for explicit opt-in and required configured-provider environment configuration.
- Browser journey that asks a question from the Atlas Ask page and validates source-grounded output.
- Backend path that routes provider execution through ModelAdapter.
- Safe handling for missing credentials, network errors, provider 429, provider 5xx, timeout, and malformed provider output.
- Artifact hygiene for logs, screenshots, traces, videos, reports, generated output, and git.
- Regression checks that first-layer and second-layer still pass.

Out of scope:

- Code implementation in this pass.
- Changing default first-layer/second-layer behavior.
- Real company data, real private endpoints, production auth/RBAC, cost governance, provider account administration, streaming, IDE Copilot session-token usage, and provider selection beyond DeepSeek/GitHub Models.

## Actors

| Actor | Role |
|---|---|
| Local developer | Provides local environment variables and runs the opt-in command. |
| Knowledge user | Exercises the Ask page in the browser against sample knowledge. |
| Platform engineer | Verifies adapter isolation, safe failures, and artifact hygiene. |
| Codex implementation agent | Implements future tasks strictly against this spec and the task checklist. |

## Functional Requirements

### Layer Isolation

- **FR-PBE2E-001:** Third-layer E2E must have its own command and must not run from first-layer, second-layer, default frontend E2E, default backend verification, or setup commands. (REQ-PBE2E-001, REQ-PBE2E-010)
- **FR-PBE2E-002:** Third-layer execution must require an explicit opt-in signal in addition to provider credentials. (REQ-PBE2E-001, REQ-PBE2E-008)
- **FR-PBE2E-003:** Documentation and command output must describe third-layer as local/developer acceptance, not a default CI gate. (REQ-PBE2E-011)

### Local Stack And Browser Journey

- **FR-PBE2E-004:** The future command must start or reuse the local frontend, Spring Boot backend, and PostgreSQL stack required for Ask. (REQ-PBE2E-002)
- **FR-PBE2E-005:** The journey must seed or reuse mock/sample approved knowledge evidence and must not ingest real company documents. (REQ-PBE2E-003)
- **FR-PBE2E-006:** Playwright must drive the visible Ask page, submit a question, wait for the provider-backed result, and assert answer, citations/evidence, review-required status, and safe provider status. (REQ-PBE2E-002, REQ-PBE2E-006)

### Provider Configuration

- **FR-PBE2E-007:** Provider credentials must be read only from environment variables at runtime. (REQ-PBE2E-004)
- **FR-PBE2E-008:** Preflight must fail before starting provider execution when the key is missing, opt-in is missing, or configuration is obviously invalid. (REQ-PBE2E-007, REQ-PBE2E-008)
- **FR-PBE2E-009:** Frontend source and browser-visible state must never receive raw provider credentials or raw auth headers. (REQ-PBE2E-004, REQ-PBE2E-009)

### ModelAdapter Boundary

- **FR-PBE2E-010:** Ask orchestration must invoke provider generation through the existing model service/ModelAdapter contract. (REQ-PBE2E-005)
- **FR-PBE2E-011:** Provider-specific HTTP client, endpoint, request signing, and response parsing must live only inside the configured ModelAdapter implementation or a provider adapter package. (REQ-PBE2E-005, REQ-PBE2E-012)
- **FR-PBE2E-012:** Controller, generic service, repository, vector, graph, frontend, and test orchestration layers must not contain direct provider execution logic. (REQ-PBE2E-005, REQ-PBE2E-012)

### Safe Failure Behavior

- **FR-PBE2E-013:** Missing key and missing opt-in must return a skipped/preflight-failed result, not a partially started provider run. (REQ-PBE2E-007, REQ-PBE2E-008)
- **FR-PBE2E-014:** Network failure, timeout, provider 429, provider 5xx, and malformed provider output must produce sanitized failure states in API, UI, and test output. (REQ-PBE2E-007)
- **FR-PBE2E-015:** Safe failures must preserve enough high-level status for troubleshooting without exposing secrets, raw provider responses, raw request headers, stack traces, private paths, or confidential source text. (REQ-PBE2E-007, REQ-PBE2E-009)

### Artifact Hygiene

- **FR-PBE2E-016:** Provider-backed E2E must disable or sanitize Playwright trace/video/screenshot retention unless the implementation can prove no secrets or raw provider payloads are captured. (REQ-PBE2E-009)
- **FR-PBE2E-017:** Backend logs, frontend console logs, reports, generated output, and git diff must be scanned for secret-like values, private absolute paths, raw provider payload markers, and real company data markers. (REQ-PBE2E-009, REQ-PBE2E-012)
- **FR-PBE2E-018:** First-layer and second-layer verification must remain part of implementation close-out. (REQ-PBE2E-010)

## State Model

Provider-backed E2E run status:

```text
PREFLIGHT -> SKIPPED_MISSING_OPT_IN
          -> SKIPPED_MISSING_KEY
          -> STARTING_LOCAL_STACK -> SEEDING_SAMPLE_EVIDENCE
          -> ASKING_PROVIDER -> SUCCEEDED
                            -> FAILED_SAFE
```

Provider call outcome:

| Outcome | Expected Behavior |
|---|---|
| Missing opt-in | Stop before stack/provider execution; report skipped. |
| Missing key | Stop before provider execution; report skipped or preflight failed. |
| Network failure / timeout | Show sanitized safe error; no raw request/response material. |
| Provider 429 | Show sanitized rate-limit state; no raw provider payload. |
| Provider 5xx | Show sanitized provider-unavailable state; no raw provider payload. |
| Malformed provider output | Mark run failed safe; no raw payload persisted. |
| Success | Browser shows answer, citations, source evidence, review-required status, and safe provider status. |

## API / Interface Surface

This lightweight SDD does not introduce a new API guide. Future implementation may extend the existing model-adapter and ask-rag contracts, but any provider-specific endpoint or configuration behavior must be represented in an accepted SDD/API guide before code changes if a new backend API is required.

Expected implementation surfaces:

| Surface | Expected Behavior |
|---|---|
| E2E shell script or npm command | Opt-in third-layer local orchestration. |
| Configured ModelAdapter | Reads environment config server-side, calls DeepSeek or GitHub Models, returns sanitized adapter result. |
| Ask service | Uses model service/ModelAdapter only; preserves Ask/RAG evidence and review status. |
| Playwright provider-backed spec | Drives browser Ask page with sample evidence. |
| Safety scans | Verify no secrets, raw provider responses, private paths, or real data entered artifacts or git. |

## Acceptance Matrix

| Check | Requirements | Observable Result |
|---|---|---|
| AC-PBE2E-01 | REQ-PBE2E-001, 010, 011 | Third-layer has its own opt-in command and is absent from first-layer, second-layer, default frontend E2E, default backend verification, and CI. |
| AC-PBE2E-02 | REQ-PBE2E-002, 003, 006 | Browser Ask journey uses sample approved evidence and displays provider-backed answer with citations and review-required status. |
| AC-PBE2E-03 | REQ-PBE2E-004, 008 | Missing opt-in or missing key stops before provider execution. |
| AC-PBE2E-04 | REQ-PBE2E-005, 012 | Provider-specific execution is isolated to ModelAdapter/provider adapter code; seam checks block direct calls elsewhere. |
| AC-PBE2E-05 | REQ-PBE2E-007 | Missing key, network failure, 429, 5xx, timeout, and malformed output fail safely with sanitized messages. |
| AC-PBE2E-06 | REQ-PBE2E-009, 012 | Logs, screenshots, traces, reports, generated outputs, and git diff contain no raw secrets, raw provider responses, auth headers, private paths, or real company data. |
| AC-PBE2E-07 | REQ-PBE2E-010 | `npm run e2e:first-layer` and `npm run e2e:second-layer` still pass after implementation. |

## Verification Plan For Future Implementation

Future implementation must include at minimum:

```bash
npm run e2e:first-layer
npm run e2e:second-layer
# future opt-in command, name to be finalized
git diff --check
```

It must also include focused scans over provider-backed E2E docs, scripts, backend adapter code, frontend artifacts, Playwright output, backend logs, and generated sample output.

## Open Questions

- OQ-PBE2E-001: Exact environment variable name for the provider key.
- OQ-PBE2E-002: Trace/video retention policy for third-layer failures.
- OQ-PBE2E-003: Whether protected CI with managed secrets is ever desired.
