# Tasks: Provider-Backed E2E

## Status

Draft implementation checklist for future work. This documentation pass does not implement code, scripts, tests, or package commands.

## Source Spec

- Requirements: `docs/01-requirements/provider-backed-e2e-requirements.md`
- Spec: `docs/03-spec/provider-backed-e2e-spec.md`
- Traceability: `docs/00-context/provider-backed-e2e-traceability.md`

## Constraints For Every Task

- Third-layer provider-backed E2E must be opt-in and isolated from first-layer, second-layer, default frontend E2E, backend verification, setup, and default CI.
- Continue using mock/sample knowledge data only.
- Provider API key must come only from environment variables.
- Provider execution must stay behind the ModelAdapter boundary.
- Do not log, persist, screenshot, trace, return, or commit raw keys, raw auth headers, raw provider responses, private paths, or real company data.
- Missing key, network failure, provider 429, provider 5xx, timeout, and malformed output must fail safely.
- First-layer and second-layer must continue to pass.

## Workstreams

| Workstream | Tasks |
|---|---|
| Preflight and local orchestration | T-PBE2E-001, T-PBE2E-002 |
| Provider adapter path | T-PBE2E-003, T-PBE2E-004 |
| Browser E2E | T-PBE2E-005 |
| Safe failures and hygiene | T-PBE2E-006, T-PBE2E-007 |
| Regression and documentation close-out | T-PBE2E-008 |

## Task Details

### T-PBE2E-001: Define Third-Layer Command Contract

- **Maps to:** REQ-PBE2E-001, REQ-PBE2E-002, REQ-PBE2E-008, REQ-PBE2E-011; spec sections `Layer Isolation`, `Local Stack And Browser Journey`.
- **Owner type:** QA/devex
- **Priority:** Must
- **Dependencies:** None
- **Scope:** Add a future opt-in command name and orchestration contract for provider-backed E2E. The command must not be invoked by first-layer, second-layer, default `npm run e2e`, setup, backend verify, or default CI.
- **Constraints:** Opt-in only; no provider execution without explicit opt-in and key preflight.
- **Verification:**
  ```bash
  npm run e2e:first-layer
  npm run e2e:second-layer
  git diff --check
  ```

### T-PBE2E-002: Add Preflight Environment Checks

- **Maps to:** REQ-PBE2E-004, REQ-PBE2E-007, REQ-PBE2E-008; spec sections `Provider Configuration`, `Safe Failure Behavior`.
- **Owner type:** QA/devex
- **Priority:** Must
- **Dependencies:** T-PBE2E-001
- **Scope:** Validate explicit opt-in, required DeepSeek environment variable, safe local mode, and sample-data-only mode before starting provider execution. Missing key or opt-in must stop before provider calls.
- **Constraints:** Do not echo secret values; print only configured/missing status.
- **Verification:**
  ```bash
  git diff --check
  # Implementation must include a missing-key preflight test that proves no provider call starts.
  ```

### T-PBE2E-003: Implement Configured Provider Behind ModelAdapter

- **Maps to:** REQ-PBE2E-004, REQ-PBE2E-005, REQ-PBE2E-006, REQ-PBE2E-012; spec sections `ModelAdapter Boundary`, `Provider Configuration`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-PBE2E-002
- **Scope:** Add or extend a configured-provider ModelAdapter implementation that reads runtime environment configuration server-side, calls DeepSeek or GitHub Models, normalizes output into the existing model result shape, and preserves review-required output status.
- **Constraints:** Provider-specific client/request/response parsing stays inside adapter implementation; no raw provider payload persistence.
- **Verification:**
  ```bash
  cd backend && mvn -Dtest=ModelAdapterContractTest,AdapterSeamGuardTest test
  git diff --check
  ```

### T-PBE2E-004: Keep Ask Orchestration Adapter-Bound

- **Maps to:** REQ-PBE2E-005, REQ-PBE2E-006, REQ-PBE2E-012; spec sections `ModelAdapter Boundary`, `Local Stack And Browser Journey`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-PBE2E-003
- **Scope:** Ensure Ask uses the existing model service/ModelAdapter path for provider-backed generation while preserving scoped sample evidence, citations, confidence or safe status, and review-required output.
- **Constraints:** Ask service, controllers, repositories, graph, vector, and frontend must not directly reference provider execution logic.
- **Verification:**
  ```bash
  cd backend && mvn -Dtest=AskServiceTest,AskApiContractIT,AdapterSeamGuardTest test
  ```

### T-PBE2E-005: Add Provider-Backed Playwright Ask Journey

- **Maps to:** REQ-PBE2E-002, REQ-PBE2E-003, REQ-PBE2E-006, REQ-PBE2E-009; spec sections `Local Stack And Browser Journey`, `Artifact Hygiene`.
- **Owner type:** QA/frontend
- **Priority:** Must
- **Dependencies:** T-PBE2E-004
- **Scope:** Add an opt-in Playwright spec that drives the visible Ask page with sample approved evidence, submits a stable sample question, and asserts provider-backed answer, citations/evidence, review-required status, and safe provider status.
- **Constraints:** Use mock/sample knowledge only; disable or sanitize traces, videos, screenshots, console logs, and reports.
- **Verification:**
  ```bash
  # future opt-in provider-backed E2E command
  git diff --check
  ```

### T-PBE2E-006: Add Safe Failure Coverage

- **Maps to:** REQ-PBE2E-007, REQ-PBE2E-008, REQ-PBE2E-009; spec sections `Safe Failure Behavior`, `State Model`.
- **Owner type:** QA/backend
- **Priority:** Must
- **Dependencies:** T-PBE2E-003, T-PBE2E-005
- **Scope:** Cover missing opt-in, missing key, network failure, timeout, provider 429, provider 5xx, and malformed provider output. Each case must fail safely and expose only sanitized status.
- **Constraints:** Tests must not require real failed provider calls when failure can be simulated at adapter boundary.
- **Verification:**
  ```bash
  cd backend && mvn -Dtest=ModelAdapterContractTest,AskServiceTest test
  # future provider-backed E2E failure-mode command or targeted test
  git diff --check
  ```

### T-PBE2E-007: Add Artifact And Secret Hygiene Gates

- **Maps to:** REQ-PBE2E-004, REQ-PBE2E-009, REQ-PBE2E-012; spec sections `Artifact Hygiene`, `Acceptance Matrix`.
- **Owner type:** security/QA
- **Priority:** Must
- **Dependencies:** T-PBE2E-005, T-PBE2E-006
- **Scope:** Add scans or constraints for provider-backed docs, scripts, backend adapter code, frontend artifacts, Playwright reports, test results, backend logs, generated samples, and git diff.
- **Constraints:** No raw secrets, raw auth headers, raw provider responses, private absolute paths, or real company data.
- **Verification:**
  ```bash
  git diff --check
  rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" docs backend frontend scripts samples || true
  ```

### T-PBE2E-008: Run Regression And Close Out

- **Maps to:** REQ-PBE2E-001, REQ-PBE2E-010, REQ-PBE2E-011, REQ-PBE2E-012; spec sections `Acceptance Matrix`, `Verification Plan For Future Implementation`.
- **Owner type:** QA/docs
- **Priority:** Must
- **Dependencies:** T-PBE2E-001 through T-PBE2E-007
- **Scope:** Prove first-layer and second-layer still pass, run provider-backed opt-in E2E, run hygiene scans, review diff, and update traceability with implementation evidence and residual risks.
- **Constraints:** Name any skipped check with reason; do not imply unrun checks passed.
- **Verification:**
  ```bash
  npm run e2e:first-layer
  npm run e2e:second-layer
  # future opt-in provider-backed E2E command
  git diff --check
  ```

## Dependency Plan

Critical path: T-PBE2E-001 -> T-PBE2E-002 -> T-PBE2E-003 -> T-PBE2E-004 -> T-PBE2E-005 -> T-PBE2E-006 -> T-PBE2E-007 -> T-PBE2E-008.

T-PBE2E-006 failure simulations may be developed alongside T-PBE2E-005 after the adapter shape is stable.

## Open Questions

- OQ-PBE2E-001: Exact environment variable name for the provider key.
- OQ-PBE2E-002: Trace/video retention policy for third-layer failures.
- OQ-PBE2E-003: Whether protected CI with managed secrets is ever desired.
