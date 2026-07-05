# Traceability: Provider-Backed E2E

## Status

Implemented. `provider-backed-e2e` now has the lightweight SDD set, opt-in third-layer command, configured chat execution behind ModelAdapter, provider-backed Playwright coverage, and verification evidence. Supported configured providers are DeepSeek and GitHub Models. The real provider run remains local-key-dependent and is not executed when `ATLAS_MODEL_API_KEY` is absent.

## Slice Contract

| Field | Value |
|---|---|
| Goal | A local developer can intentionally provide a configured provider API key through environment variables, start the local Atlas stack with one command, and complete a real provider-backed Ask E2E journey in the browser using mock/sample knowledge data. |
| Slice | `provider-backed-e2e` |
| Phase | Third-layer acceptance / Phase 4+ provider integration verification |
| Scope | Opt-in third-layer E2E command, local stack orchestration, provider-backed Ask through browser, DeepSeek/GitHub Models execution behind ModelAdapter, safe failures, and artifact hygiene. |
| Exclusions | Default first-layer/second-layer changes, real company data, raw provider artifacts, production auth/RBAC, cost governance, IDE Copilot session-token usage, and provider selection beyond DeepSeek/GitHub Models. |

## Sources Read

- `README.md`
- `PROJECT_RULES.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `docs/07-acceptance/README.md`
- `docs/07-acceptance/knowledge-loop-e2e.md`
- `docs/local-runbook.md`
- `docs/local-runbook.zh-CN.md`
- `docs/01-requirements/model-adapter-requirements.md`
- `docs/03-spec/model-adapter-spec.md`
- `docs/06-tasks/model-adapter-tasks.md`
- `docs/00-context/model-adapter-traceability.md`
- `docs/01-requirements/ask-rag-requirements.md`
- `docs/03-spec/ask-rag-spec.md`
- `docs/06-tasks/ask-rag-tasks.md`
- `docs/00-context/ask-rag-traceability.md`
- `docs/01-requirements/knowledge-graph-requirements.md`
- `docs/03-spec/knowledge-graph-spec.md`
- `docs/06-tasks/knowledge-graph-tasks.md`

## Skill Application

| Skill | Applied How |
|---|---|
| `atlas-sdd-generate-all` | Used as the Atlas SDD orchestration reference for bilingual files, traceability, IDs, and quality gates. |

Full downstream SDD generation skills were intentionally not expanded because the user requested a lightweight documentation-only subset: requirements, spec, tasks, and traceability.

## SDD Artifacts

| Stage | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/provider-backed-e2e-requirements.md` | `docs/01-requirements/provider-backed-e2e-requirements.zh-CN.md` |
| Spec | `docs/03-spec/provider-backed-e2e-spec.md` | `docs/03-spec/provider-backed-e2e-spec.zh-CN.md` |
| Tasks | `docs/06-tasks/provider-backed-e2e-tasks.md` | `docs/06-tasks/provider-backed-e2e-tasks.zh-CN.md` |
| Traceability | `docs/00-context/provider-backed-e2e-traceability.md` | `docs/00-context/provider-backed-e2e-traceability.zh-CN.md` |

## Implementation Evidence

| Area | Evidence |
|---|---|
| Root command | `package.json` exposes `npm run e2e:third-layer`. |
| Frontend command | `frontend/package.json` exposes `npm --prefix frontend run e2e:third-layer`, scoped to `frontend/tests/e2e/third-layer`. |
| Local orchestration | `scripts/e2e/run-third-layer.sh` starts temporary PostgreSQL, Spring Boot API, builds frontend, runs provider-backed Playwright, scans artifacts, and cleans up services. |
| Provider adapter | `backend/src/main/java/com/atlas/metadata/adapter/ConfiguredModelAdapter.java` implements minimal DeepSeek and GitHub Models chat execution behind `ModelAdapter`. |
| Ask routing | `backend/src/main/java/com/atlas/metadata/service/AskService.java` routes `mode=configured` Ask model runs to the configured provider adapter key without exposing provider calls to controllers or Playwright. |
| E2E coverage | `frontend/tests/e2e/third-layer/provider-backed-ask.spec.ts` seeds approved sample evidence, publishes Wiki, projects graph/vector evidence, triggers Ask, verifies citations/source trace, and confirms the browser graph is live API-backed. |
| Config placeholders | `configs/atlas.example.env`, `configs/atlas.company.example.env`, and `configs/adapters.configured.example.yaml` document provider settings without real keys. |
| User-facing docs | `README.md`, `docs/07-acceptance/README.md`, `docs/07-acceptance/knowledge-loop-e2e.md`, `docs/local-runbook.md`, and `docs/local-runbook.zh-CN.md` now describe first/second/third-layer acceptance, local DeepSeek environment setup, VS Code execution, reports, cleanup, and skip/failure conditions. |

## API Guide Decision

No API guide is included in this lightweight SDD pass. The slice is currently an acceptance/documentation slice. If future implementation introduces new backend API behavior beyond existing model-adapter or ask-rag contracts, an API guide or accepted contract update must be added before code changes.

## Requirement Trace

| Requirement | Spec Sections | Tasks |
|---|---|---|
| REQ-PBE2E-001 | Layer Isolation, Acceptance Layer Model | T-PBE2E-001, T-PBE2E-008 |
| REQ-PBE2E-002 | Local Stack And Browser Journey | T-PBE2E-001, T-PBE2E-005 |
| REQ-PBE2E-003 | Local Stack And Browser Journey, Scope | T-PBE2E-005 |
| REQ-PBE2E-004 | Provider Configuration, Artifact Hygiene | T-PBE2E-002, T-PBE2E-003, T-PBE2E-007 |
| REQ-PBE2E-005 | ModelAdapter Boundary | T-PBE2E-003, T-PBE2E-004 |
| REQ-PBE2E-006 | Local Stack And Browser Journey, ModelAdapter Boundary | T-PBE2E-003, T-PBE2E-004, T-PBE2E-005 |
| REQ-PBE2E-007 | Safe Failure Behavior, State Model | T-PBE2E-002, T-PBE2E-006 |
| REQ-PBE2E-008 | Provider Configuration, Safe Failure Behavior | T-PBE2E-001, T-PBE2E-002, T-PBE2E-006 |
| REQ-PBE2E-009 | Artifact Hygiene | T-PBE2E-005, T-PBE2E-007 |
| REQ-PBE2E-010 | Layer Isolation, Acceptance Matrix | T-PBE2E-001, T-PBE2E-008 |
| REQ-PBE2E-011 | Layer Isolation, Acceptance Layer Model | T-PBE2E-001, T-PBE2E-008 |
| REQ-PBE2E-012 | ModelAdapter Boundary, Artifact Hygiene | T-PBE2E-003, T-PBE2E-004, T-PBE2E-007, T-PBE2E-008 |

## Key Decisions

| Decision | Rationale |
|---|---|
| Third-layer is opt-in only. | Prevents accidental network/provider calls, cost, credential dependency, and nondeterministic default tests. |
| First-layer and second-layer remain mock/sample-only. | Preserves existing local reliability and acceptance guarantees. |
| Provider key comes only from environment variables. | Keeps real credentials out of source, docs, logs, screenshots, traces, and git. |
| Provider calls stay behind ModelAdapter. | Preserves Atlas provider replaceability and Ask/RAG adapter boundaries. |
| Mock/sample knowledge data remains mandatory. | Provider-backed generation should test integration path, not expose real company documents. |

## Review-Doc-Quality Gate

| Check | Result |
|---|---|
| English and Chinese copies exist for every touched artifact | Pass |
| REQ/T IDs match across languages | Pass |
| Requirements map to spec/tasks | Pass |
| Tasks are actionable for future Codex implementation | Pass |
| Phase and acceptance-layer discipline are explicit | Pass |
| Adapter, opt-in, environment-only key, mock/sample data, and safe-failure constraints are explicit | Pass |
| API guide inclusion/omission recorded | Omitted and recorded |

## Verification Evidence

```bash
npm --prefix frontend run typecheck
mvn -f backend/pom.xml verify
npm run e2e:first-layer
npm run e2e:second-layer
if [ -n "${ATLAS_MODEL_API_KEY:-}" ]; then npm run e2e:third-layer; fi
npm run e2e:third-layer  # preflight verified clear failure when ATLAS_MODEL_API_KEY is absent
git diff --check
rg -n "<focused-doc-secret-private-path-pattern>" README.md docs/07-acceptance/README.md docs/07-acceptance/knowledge-loop-e2e.md docs/local-runbook.md docs/local-runbook.zh-CN.md docs/00-context/provider-backed-e2e-traceability.md docs/00-context/provider-backed-e2e-traceability.zh-CN.md
```

## Residual Risks

- Full third-layer provider execution is network- and credential-dependent; it must be run locally only when `ATLAS_MODEL_API_KEY` is present.
- Provider latency, quota, 429, and 5xx behavior are sanitized by the adapter but still depend on configured provider availability.
- Third-layer must not become a default deterministic gate without a separate accepted CI/secret-management design.
- User docs intentionally show placeholders only; operators must source real keys from approved local secret handling and keep `.env` or shell exports out of git.

## Recommended Codex Handoff Command

```text
Implement the provider-backed-e2e slice strictly against docs/03-spec/provider-backed-e2e-spec.md and docs/06-tasks/provider-backed-e2e-tasks.md. Keep it opt-in, use mock/sample knowledge data only, route provider calls through ModelAdapter, keep DeepSeek credentials environment-only, and do not change first-layer or second-layer defaults.
```
