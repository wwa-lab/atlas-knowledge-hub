# Traceability: Model Adapter

## Status

Implemented traceability for `model-adapter`. The slice now has SDD artifacts plus backend adapter/API implementation evidence.

## Slice Contract

| Field | Value |
|---|---|
| Goal | Atlas can invoke LLM, embedding, rerank, vision, and speech model capabilities through a product-facing model adapter contract with masked configuration, mock-only verification, and review-required outputs. |
| Slice | `model-adapter` |
| Phase | 3 adapter |
| Scope | Model capability metadata, adapter contract, model run API, mock model execution, run evidence, source references, review-required output, safe errors, and guard/verification plan. |
| Exclusions | Real provider calls, Ask/RAG, vector indexing, graph extraction, Wiki publication, frontend changes, production auth/RBAC, production secret management, raw prompt storage, and external network/cloud calls. |
| Verification row | Phase 3 adapter: unit + integration tests against mock engines. |
| Constraints row | Parser/converter/model/vector/storage go only through product-facing adapters; never call a tool/provider directly; never hardcode one implementation; secret-masked; preserve trace/review status. |

## Sources Read

- `README.md`
- `PROJECT_RULES.md`
- `AGENTS.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `docs/00-context/slice-roadmap.md`
- `docs/00-context/slice-roadmap.zh-CN.md`
- `docs/01-requirements/requirement.md`
- `docs/architecture.md`
- `docs/technology-decisions.md`
- `docs/03-spec/metadata-api-spec.md`
- `docs/04-architecture/metadata-api-data-model.md`
- Existing converter/parser/storage adapter SDD documents
- FE model settings references in `frontend/public/atlas-prototype.html`, `prototypes/index.html`, `frontend/src/types.ts`, and `frontend/src/data/atlasMock.ts`

## Skill Chain Applied

| Skill | Applied How |
|---|---|
| `atlas-sdd-generate-all` | Orchestrated the complete bilingual SDD set and final consistency gate. |
| `req-to-user-story` | Requirements were converted into capability-domain user stories. |
| `user-story-to-spec` | Stories were consolidated into behavior source of truth. |
| `spec-to-architecture` | Spec was translated into adapter architecture, data flow, and data model. |
| `architecture-to-design` | Architecture was refined into design and API/adapter contract. |
| `design-to-tasks` | Design was converted into executable Codex tasks. |
| `architecture-review` | Adapter extensibility, decoupling, secret safety, and phase boundaries were checked during design. |
| `review-doc-quality` | Final SDD set was reviewed for completeness, traceability, bilingual parity, and readiness. |

## Requirement To Story To Spec To Task Matrix

| Requirement | User Stories | Spec Sections | Tasks |
|---|---|---|---|
| REQ-MODA-001 | US-MODA-001, US-MODA-005 | Adapter Boundary | T-MODA-003, T-MODA-005, T-MODA-009 |
| REQ-MODA-002 | US-MODA-001 | Adapter Boundary, Capability Metadata | T-MODA-003, T-MODA-004, T-MODA-005 |
| REQ-MODA-003 | US-MODA-001 | Capability Metadata, API / Interface Surface | T-MODA-002, T-MODA-003, T-MODA-005, T-MODA-008 |
| REQ-MODA-004 | US-MODA-001, US-MODA-004 | Capability Metadata, Validation And Failure Behavior | T-MODA-002, T-MODA-005, T-MODA-009 |
| REQ-MODA-005 | US-MODA-002 | Capability Metadata, Operation Outputs | T-MODA-003, T-MODA-004 |
| REQ-MODA-006 | US-MODA-002 | Model Run Lifecycle | T-MODA-001, T-MODA-006, T-MODA-008 |
| REQ-MODA-007 | US-MODA-002, US-MODA-003 | Operation Outputs | T-MODA-001, T-MODA-002, T-MODA-006, T-MODA-007, T-MODA-008 |
| REQ-MODA-008 | US-MODA-002, US-MODA-003 | Operation Outputs, State Model | T-MODA-001, T-MODA-006, T-MODA-007 |
| REQ-MODA-009 | US-MODA-003 | Source Reference Preservation, Operation Outputs | T-MODA-001, T-MODA-002, T-MODA-007 |
| REQ-MODA-010 | US-MODA-002, US-MODA-005 | Operation Outputs, Embedding Boundary | T-MODA-007, T-MODA-009 |
| REQ-MODA-011 | US-MODA-002, US-MODA-003 | Operation Outputs | T-MODA-004, T-MODA-007 |
| REQ-MODA-012 | US-MODA-004 | Validation And Failure Behavior | T-MODA-007, T-MODA-009 |
| REQ-MODA-013 | US-MODA-005 | Constraints, Acceptance Matrix | T-MODA-004, T-MODA-009, T-MODA-010 |
| REQ-MODA-014 | US-MODA-002, US-MODA-004 | Model Run Lifecycle, State Model | T-MODA-001, T-MODA-006, T-MODA-008, T-MODA-010 |
| REQ-MODA-015 | US-MODA-005 | Out Of Scope, Acceptance Matrix | T-MODA-009, T-MODA-010 |

## Files Created

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/model-adapter-requirements.md` | `docs/01-requirements/model-adapter-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/model-adapter-stories.md` | `docs/02-user-stories/model-adapter-stories.zh-CN.md` |
| Spec | `docs/03-spec/model-adapter-spec.md` | `docs/03-spec/model-adapter-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/model-adapter-architecture.md` | `docs/04-architecture/model-adapter-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/model-adapter-data-flow.md` | `docs/04-architecture/model-adapter-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/model-adapter-data-model.md` | `docs/04-architecture/model-adapter-data-model.zh-CN.md` |
| Design | `docs/05-design/model-adapter-design.md` | `docs/05-design/model-adapter-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/model-adapter-tasks.md` | `docs/06-tasks/model-adapter-tasks.zh-CN.md` |
| Traceability | `docs/00-context/model-adapter-traceability.md` | `docs/00-context/model-adapter-traceability.zh-CN.md` |

## API Guide Decision

API guide is included. `model-adapter` is a Phase 3 backend/API adapter contract slice, and `docs/00-context/slice-roadmap.md` states that adapter contract is required per adapter slice.

## Implementation Evidence

| Task Range | Evidence |
|---|---|
| T-MODA-001 | Added model run, output, and source reference persistence with Flyway migration `backend/src/main/resources/db/migration/V7__model_adapter.sql`. |
| T-MODA-002 | Added model capability/run/source/output DTOs and `ModelMapper`; responses stay inside `ApiEnvelope`. |
| T-MODA-003, T-MODA-004 | Added `ModelAdapter` contract, deterministic `MockModelAdapter`, and safe `ConfiguredModelAdapter` placeholder. |
| T-MODA-005 | Added `ModelAdapterRegistry` for explicit/default model selection, compatibility validation, and capability listing. |
| T-MODA-006, T-MODA-007 | Added `ModelService` run lifecycle, source trace persistence, output validation, review-required enforcement, usage summary, and safe adapter-fault handling. |
| T-MODA-008 | Added `GET /api/model-adapters`, `POST /api/model-runs`, and `GET /api/model-runs/{runId}` in `ModelController`. |
| T-MODA-009 | Extended `AdapterSeamGuardTest` for model provider/runtime markers, outbound client bans, vector write bans, and secret/private-path scans. |
| T-MODA-010 | Verified with model unit, service, seam, API integration, and full backend verification commands. |

## Verification Evidence

| Command | Result |
|---|---|
| `cd backend && mvn -Dtest=ModelAdapterContractTest,ModelAdapterRegistryTest,ModelSummaryCalculatorTest,ModelDomainInvariantTest test` | Pass |
| `cd backend && mvn -Dtest=ModelServiceTest,ModelSummaryCalculatorTest,ModelDomainInvariantTest test` | Pass |
| `cd backend && mvn -Dtest=AdapterSeamGuardTest test` | Pass |
| `cd backend && mvn -Dit.test=ModelApiContractIT verify` | Pass |
| `cd backend && mvn verify` | Pass |
| `git diff --check` | Pass |
| Model provider/client/vector seam grep over controller/service/repository/domain | Pass |
| Secret, private path, and private key grep over backend source plus model SDD docs | Pass |

## Review-Doc-Quality Gate

| Check | Result |
|---|---|
| English and Chinese copies exist for every touched artifact | Pass |
| REQ/US/T IDs match across languages | Pass |
| Requirements map to stories/spec/tasks | Pass |
| Tasks are actionable for Codex | Pass |
| Phase discipline preserved | Pass |
| Mock-only/no-network/adapter/secret-masked constraints explicit | Pass |
| API guide inclusion/omission recorded | Pass, included |
| Open questions explicit | Pass |

## Residual Risks

- Real provider selection is unresolved and intentionally deferred.
- Raw prompt retention is a product/security decision and is not enabled by this SDD.
- Usage/cost governance is deferred to a future governance/hardening slice.

## Recommended Codex Handoff Command

```text
Implement the model-adapter slice strictly against docs/03-spec/model-adapter-spec.md and docs/06-tasks/model-adapter-tasks.md: complete every task in ID order, respect the stated Constraints and Verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```
