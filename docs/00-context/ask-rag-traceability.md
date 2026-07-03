# Traceability: Ask RAG

## Status

Implemented. The slice now has backend Ask run persistence, adapter-bound retrieval and model orchestration, API endpoints, Trusted Ask UI state mapping, E2E coverage, seam guards, and verification evidence.

## Slice Contract

| Field | Value |
|---|---|
| Goal | Atlas users can ask questions in a Knowledge Space and receive source-grounded, review-aware answers over approved knowledge with visible evidence, confidence, and safe failure behavior. |
| Slice | `ask-rag` |
| Phase | 4 hardening |
| Scope | Trusted Ask UI, Ask API, adapter-bound vector retrieval, adapter-bound model answer generation, evidence bundle, review-awareness, audit records, safe errors, and verification tasks. |
| Exclusions | Real external model/vector calls, new parser/converter behavior, graph extraction, Wiki publish state machine, production SSO/RBAC, raw prompt/source retention, streaming, and provider cost governance. |
| Verification row | Phase 4 hardening: full unit + integration + E2E for the touched layer. |
| Constraints row | Preserve source trace, confidence, review status on all Markdown/metadata; LLM output stays review-required until verified; adapters only; secret-masked; mock engines; no external network calls. |

## Sources Read

- `README.md`
- `PROJECT_RULES.md`
- `AGENTS.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `docs/00-context/slice-roadmap.md`
- `docs/01-requirements/requirement.md`
- `docs/product-vision.md`
- `docs/mvp-scope.md`
- `docs/markdown-standard.md`
- `docs/review-workflow.md`
- `docs/knowledge-graph-design.md`
- `docs/03-spec/metadata-api-spec.md`
- `docs/03-spec/vector-adapter-spec.md`
- `docs/03-spec/model-adapter-spec.md`
- `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md`
- `docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md`
- `docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md`
- `frontend/public/atlas-prototype.html`
- `prototypes/index.html`

## Skill Chain Applied

| Skill | Applied How |
|---|---|
| `atlas-sdd-generate-all` | Orchestrated full bilingual SDD set and reconciliation. |
| `req-to-user-story` | Converted requirements into capability-domain stories. |
| `user-story-to-spec` | Consolidated stories into behavior source of truth. |
| `spec-to-architecture` | Derived high-level architecture and boundaries. |
| `architecture-to-design` | Produced design, data flow, data model, and API contract. |
| `design-to-tasks` | Converted design into Codex-actionable tasks. |
| `review-doc-quality` | Applied final quality gate; result recorded below. |

## SDD Artifacts

| Stage | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/ask-rag-requirements.md` | `docs/01-requirements/ask-rag-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/ask-rag-stories.md` | `docs/02-user-stories/ask-rag-stories.zh-CN.md` |
| Spec | `docs/03-spec/ask-rag-spec.md` | `docs/03-spec/ask-rag-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/ask-rag-architecture.md` | `docs/04-architecture/ask-rag-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/ask-rag-data-flow.md` | `docs/04-architecture/ask-rag-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/ask-rag-data-model.md` | `docs/04-architecture/ask-rag-data-model.zh-CN.md` |
| Design | `docs/05-design/ask-rag-design.md` | `docs/05-design/ask-rag-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/ask-rag-tasks.md` | `docs/06-tasks/ask-rag-tasks.zh-CN.md` |
| Traceability | `docs/00-context/ask-rag-traceability.md` | `docs/00-context/ask-rag-traceability.zh-CN.md` |

## API Guide Decision

API guide is included. `ask-rag` is a full-stack Phase 4 hardening slice and introduces `POST /api/spaces/{spaceId}/ask` plus `GET /api/ask-runs/{runId}`.

## Requirement Trace

| Requirement | Stories | Spec Sections | Tasks |
|---|---|---|---|
| REQ-ASKRAG-001 | US-ASKRAG-001 | Ask Request, UI Behavior | T-ASKRAG-003, T-ASKRAG-006 |
| REQ-ASKRAG-002 | US-ASKRAG-002 | Retrieval Policy | T-ASKRAG-003 |
| REQ-ASKRAG-003 | US-ASKRAG-002 | Evidence And Audit, UI Behavior | T-ASKRAG-002, T-ASKRAG-006 |
| REQ-ASKRAG-004 | US-ASKRAG-004 | Answer Generation, Constraints | T-ASKRAG-003, T-ASKRAG-008 |
| REQ-ASKRAG-005 | US-ASKRAG-002 | Evidence And Audit | T-ASKRAG-001, T-ASKRAG-002 |
| REQ-ASKRAG-006 | US-ASKRAG-005 | Answer Generation, State Model | T-ASKRAG-001, T-ASKRAG-005 |
| REQ-ASKRAG-007 | US-ASKRAG-006 | Failure Behavior, API Surface | T-ASKRAG-002, T-ASKRAG-004 |
| REQ-ASKRAG-008 | US-ASKRAG-006 | Ask Request, Failure Behavior | T-ASKRAG-002, T-ASKRAG-004 |
| REQ-ASKRAG-009 | US-ASKRAG-005 | Evidence And Audit | T-ASKRAG-001, T-ASKRAG-005 |
| REQ-ASKRAG-010 | US-ASKRAG-001 | UI Behavior | T-ASKRAG-006, T-ASKRAG-007 |
| REQ-ASKRAG-011 | US-ASKRAG-004 | Constraints, Acceptance Matrix | T-ASKRAG-007, T-ASKRAG-008 |
| REQ-ASKRAG-012 | US-ASKRAG-003 | Answer Generation, Failure Behavior | T-ASKRAG-003, T-ASKRAG-004 |
| REQ-ASKRAG-013 | US-ASKRAG-002 | Retrieval Policy, UI Behavior | T-ASKRAG-003, T-ASKRAG-006 |
| REQ-ASKRAG-014 | US-ASKRAG-006 | Acceptance Matrix | T-ASKRAG-008, T-ASKRAG-009, T-ASKRAG-010 |

## Gate Note

`docs/00-context/slice-roadmap.md` previously marked `ask-rag` as gated by Phase 3. Implementation proceeded after the repository already contained verified vector and model adapter slices, and the ask-rag implementation remains mock-only and adapter-bound.

## Implementation Evidence

| Area | Evidence |
|---|---|
| Domain and persistence | `AskRun`, `AskEvidence`, `AskRunRepository`, `AskEvidenceRepository`, and `V8__ask_rag.sql` add Ask audit records and evidence snapshots without mutating source files, chunks, Wiki, graph, or review state. |
| Service orchestration | `AskService` validates safe questions, defaults to `APPROVED_ONLY`, supports explicit `INCLUDE_REVIEW_REQUIRED`, retrieves evidence through `VectorService`, generates answers through `ModelService`, and keeps generated answers `REVIEW_REQUIRED`. |
| API surface | `AskController` exposes `POST /api/spaces/{spaceId}/ask` and `GET /api/ask-runs/{runId}` using `ApiEnvelope` and safe error behavior. |
| Frontend state mapping | `frontend/public/atlas-prototype.html`, `prototypes/index.html`, `frontend/src/types.ts`, and `frontend/src/data/atlasMock.ts` expose answered, loading, no-evidence, review-warning, and safe-error states. |
| E2E coverage | `frontend/tests/e2e/phase1-smoke.spec.ts` verifies the Ask tab, answer state, evidence, review-required warning, no-evidence state, and safe-error state. |
| Seam guard | `AdapterSeamGuardTest` scans ask-rag docs and product layers for direct provider/client references, private paths, and secret-like material. |

## Verification Evidence

```bash
cd backend && mvn -Dtest=AskDomainInvariantTest,AskRequestValidationTest,AskServiceTest,AskSummaryCalculatorTest,AskStateImmutabilityTest test
cd backend && mvn -Dtest=AdapterSeamGuardTest test
cd backend && mvn -Dit.test=AskApiContractIT verify
cd frontend && npm run typecheck
cd frontend && npm run test
cd frontend && npm run build
cd frontend && npm run e2e
```

## Review-Doc-Quality Gate

| Check | Result |
|---|---|
| English and Chinese files exist for every touched artifact | Pass |
| REQ/US/T IDs match across languages | Pass |
| Requirements map to stories/spec/tasks | Pass |
| Tasks are actionable for Codex | Pass |
| Phase discipline and implementation gate are explicit | Pass |
| Adapter, mock-only, no-network, secret-masked, trace/review constraints are explicit | Pass |
| API guide inclusion decision recorded | Pass |
| Open questions are explicit | Pass |
| Implementation evidence and verification commands recorded | Pass |

## Documentation Verification Plan

```bash
git diff --check
for f in docs/01-requirements/ask-rag-requirements.md docs/01-requirements/ask-rag-requirements.zh-CN.md docs/02-user-stories/ask-rag-stories.md docs/02-user-stories/ask-rag-stories.zh-CN.md docs/03-spec/ask-rag-spec.md docs/03-spec/ask-rag-spec.zh-CN.md docs/04-architecture/ask-rag-architecture.md docs/04-architecture/ask-rag-architecture.zh-CN.md docs/04-architecture/ask-rag-data-flow.md docs/04-architecture/ask-rag-data-flow.zh-CN.md docs/04-architecture/ask-rag-data-model.md docs/04-architecture/ask-rag-data-model.zh-CN.md docs/05-design/ask-rag-design.md docs/05-design/ask-rag-design.zh-CN.md docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.zh-CN.md docs/06-tasks/ask-rag-tasks.md docs/06-tasks/ask-rag-tasks.zh-CN.md docs/00-context/ask-rag-traceability.md docs/00-context/ask-rag-traceability.zh-CN.md; do test -s "$f" || exit 1; done
! rg -n "T[O]DO|T[B]D|to be determine[d]|implementation will decid[e]|grep late[r]" docs/01-requirements/ask-rag-requirements.md docs/02-user-stories/ask-rag-stories.md docs/03-spec/ask-rag-spec.md docs/04-architecture/ask-rag-architecture.md docs/04-architecture/ask-rag-data-flow.md docs/04-architecture/ask-rag-data-model.md docs/05-design/ask-rag-design.md docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/ask-rag-tasks.md docs/00-context/ask-rag-traceability.md
```

## Residual Risks

- Production RBAC and role-specific evidence visibility remain open product decisions.
- Reranking and answer review queue integration are deferred unless explicitly added.

## Recommended Codex Handoff Command

```text
Implement the ask-rag slice strictly against docs/03-spec/ask-rag-spec.md and docs/06-tasks/ask-rag-tasks.md: complete every task in ID order, respect the stated Constraints and Verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```
