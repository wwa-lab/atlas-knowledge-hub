# Requirements: Provider-Backed E2E

## Status

Draft. Lightweight SDD-only slice. No implementation code is included in this pass.

## Slice Contract

| Field | Value |
|---|---|
| Goal | A local developer can intentionally provide a configured provider API key through environment variables, start the local Atlas stack with one command, and complete a real provider-backed Ask E2E journey in the browser using mock/sample knowledge data. |
| Slice | `provider-backed-e2e` |
| Phase | Third-layer acceptance / Phase 4+ provider integration verification |
| Scope | Opt-in third-layer E2E command, local service orchestration, provider-backed Ask through the page, ModelAdapter-bound DeepSeek/GitHub Models call path, safe failure behavior, and secret/log/artifact hygiene gates. |
| Exclusions | Product implementation in this documentation pass, default first-layer or second-layer E2E changes, real company documents, real provider keys in source/docs/logs/screenshots/traces/git, production auth/RBAC, provider cost governance, IDE Copilot session-token usage, and provider selection beyond DeepSeek/GitHub Models. |
| Source of truth | This requirements file, `docs/03-spec/provider-backed-e2e-spec.md`, and `docs/06-tasks/provider-backed-e2e-tasks.md`. |

## In Scope

- Define a third-layer provider-backed E2E gate that is separate from existing first-layer mock E2E and second-layer local full-stack E2E.
- Require explicit opt-in before any real provider call can happen.
- Use the existing local frontend, Spring Boot API, PostgreSQL, vector/Ask flow, and mock/sample knowledge data.
- Allow provider credentials only through environment variables in the developer's shell or local untracked environment file.
- Route all provider calls through the ModelAdapter boundary.
- Verify the browser-visible Ask journey against provider-generated output and source citations.
- Fail safely when the key is missing, network is unavailable, provider rate limits, or provider 5xx errors occur.
- Add verification tasks for logs, screenshots, traces, reports, and git diff hygiene.

## Out Of Scope

- Implementing code or scripts in this pass.
- Adding provider-backed checks to `npm run e2e:first-layer`, `npm run e2e:second-layer`, `npm run e2e`, or default CI.
- Replacing mock Ask behavior in first-layer or second-layer tests.
- Using real company documents, screenshots, exports, or private endpoints.
- Persisting raw prompts, raw provider responses, raw auth headers, or raw secrets.
- Adding a production secret manager, quota system, billing controls, or provider administration UI.

## Requirements

| ID | Requirement | Priority | Source / Rationale |
|---|---|---|---|
| REQ-PBE2E-001 | Atlas must define provider-backed E2E as a third-layer acceptance gate that is opt-in and never part of first-layer, second-layer, default frontend E2E, or default backend verification. | Must | Acceptance layering, data safety |
| REQ-PBE2E-002 | A future implementation must provide a one-command local path that starts the required local services and runs the browser Ask journey with provider-backed generation. | Must | User goal |
| REQ-PBE2E-003 | The third-layer journey must continue to use mock/sample knowledge data only and must not read real company documents or private local files. | Must | Project data rules |
| REQ-PBE2E-004 | Provider API credentials must come only from environment variables and must never be committed, echoed, logged, screenshotted, traced, or returned by API/UI responses. | Must | Security/data standards |
| REQ-PBE2E-005 | Any provider execution must occur behind the product-facing ModelAdapter boundary; Ask, controller, frontend, vector, graph, repository, and generic service layers must not call provider APIs directly. | Must | Adapter boundary rules |
| REQ-PBE2E-006 | Provider-backed Ask must preserve the Ask/RAG trust model: scoped Knowledge Space, approved/sample evidence, citations, confidence or safe status, and generated output marked review-required unless future policy changes it. | Must | Ask RAG spec |
| REQ-PBE2E-007 | Missing key, invalid configuration, network failure, provider 429, provider 5xx, timeout, and malformed provider output must fail safely with sanitized user-facing messages and no unsafe artifact leakage. | Must | Reliability and secret safety |
| REQ-PBE2E-008 | The third-layer command must include preflight checks that stop before starting provider execution when the required opt-in flag or required environment variables are missing. | Must | Prevent accidental provider calls |
| REQ-PBE2E-009 | Test reports, Playwright screenshots, videos, traces, backend logs, frontend console output, and generated samples must be scanned or constrained so secrets, raw provider responses, auth headers, private paths, and real company data do not enter git. | Must | Artifact hygiene |
| REQ-PBE2E-010 | First-layer and second-layer E2E behavior must remain unchanged and must pass after the third-layer slice is implemented. | Must | Regression protection |
| REQ-PBE2E-011 | Provider-backed E2E must be documented as local/developer acceptance, not as a default CI requirement, because it depends on network and developer-owned provider credentials. | Must | Repeatability and cost control |
| REQ-PBE2E-012 | Verification tasks must include explicit diff hygiene, secret/private-path scans, and checks that provider-specific code is isolated to adapter implementation and provider-backed E2E setup. | Must | Quality gates |

## Acceptance

- This SDD pass creates bilingual requirements, spec, tasks, and traceability documents only.
- Requirement IDs and task IDs match across English and Simplified Chinese copies.
- The spec clearly separates first-layer, second-layer, and third-layer acceptance.
- The tasks are scoped as future implementation work and preserve ModelAdapter, secret, mock/sample data, and safe-failure constraints.
- No implementation code, scripts, or package commands are changed in this pass.

## Open Questions

| ID | Question | Default For This SDD |
|---|---|---|
| OQ-PBE2E-001 | What exact environment variable name should the implementation use for the provider key? | Use `ATLAS_MODEL_API_KEY` for the configured provider key. |
| OQ-PBE2E-002 | Should provider-backed E2E keep Playwright traces/videos disabled by default or allow sanitized trace retention on failure? | Disable or aggressively sanitize by default; implementation must not retain secrets. |
| OQ-PBE2E-003 | Should third-layer E2E run in CI for a protected branch with managed secrets later? | Deferred. This SDD treats it as local opt-in only. |
