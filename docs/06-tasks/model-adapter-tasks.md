# Tasks: Model Adapter

## Status

Draft implementation checklist for `model-adapter`. This document is executable only after the SDD set is accepted. This SDD pass does not implement product code.

## Source Design

- Spec: `docs/03-spec/model-adapter-spec.md`
- Design: `docs/05-design/model-adapter-design.md`
- API guide: `docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md`
- Data model: `docs/04-architecture/model-adapter-data-model.md`

## Constraints For Every Task

- Use mock/fake model engines for automated tests.
- Keep model execution behind product-facing adapter contracts.
- Do not call real model providers, SDKs, local runtimes, command runners, or outbound HTTP clients from controller/service/repository/domain layers.
- Do not introduce external network dependencies or cloud calls.
- Preserve source trace, confidence/evidence, and review status; generated outputs default to `REVIEW_REQUIRED`.
- Mask secrets, endpoints, provider account ids, hostnames, local runtime paths, raw prompts, raw provider payloads, stack traces, and private paths.
- Do not write vectors, Ask answers, graph nodes/edges, Wiki pages, or frontend settings changes.
- Use mock/sample metadata only.

## Workstreams

| Workstream | Tasks |
|---|---|
| Domain and persistence | T-MODA-001, T-MODA-002 |
| Adapter contract | T-MODA-003, T-MODA-004 |
| Service behavior | T-MODA-005, T-MODA-006, T-MODA-007 |
| API contract | T-MODA-008 |
| Verification and guards | T-MODA-009, T-MODA-010 |

## Task Details

### T-MODA-001: Add model run domain model and migration

- **Maps to:** REQ-MODA-006, REQ-MODA-007, REQ-MODA-008, REQ-MODA-009, REQ-MODA-014; spec sections "Model Run Lifecycle", "Operation Outputs", "State Model"; data model `model_run`, `model_run_output`, `model_run_source_reference`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** None
- **Scope:** Add model type, operation type, adapter status, run status, output kind, and source reference type enums; add model run/output/source reference entities and repositories; add Flyway migration for model run evidence. Do not add vector, Ask, graph, Wiki, or provider credential tables.
- **Constraints:** Adapter boundary; mock-only test data; review-required outputs; secret/path safety.
- **Verification:**

```bash
cd backend && mvn verify
git diff --check
```

### T-MODA-002: Add model DTOs and mapping contracts

- **Maps to:** REQ-MODA-003, REQ-MODA-004, REQ-MODA-007, REQ-MODA-009; spec sections "Capability Metadata", "API / Interface Surface"; API guide response shapes.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-MODA-001
- **Scope:** Add model capability, create-run request, run response, usage response, output response, and source-reference response mapping. All API responses use `ApiEnvelope`.
- **Constraints:** Secret-masked capability summaries; no raw provider/runtime details; no raw prompt fields.
- **Verification:**

```bash
cd backend && mvn test
git diff --check
```

### T-MODA-003: Define model adapter interface and capability model

- **Maps to:** REQ-MODA-001, REQ-MODA-002, REQ-MODA-003, REQ-MODA-005; spec sections "Adapter Boundary", "Capability Metadata"; design "Adapter Contract".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-MODA-002
- **Scope:** Add product-facing `ModelAdapter` interface, capability record, request record, result record, usage record, output descriptor record, and source reference record. Include adapter key, model key, model type, supported operations, status, default marker, context limit, and masked config summary.
- **Constraints:** Product concepts only; no direct model provider call in interface; no network/client dependency.
- **Verification:**

```bash
cd backend && mvn -Dtest=ModelAdapterContractTest test
! rg -n "WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(" backend/src/main/java/com/atlas/metadata/adapter/ModelAdapter.java
```

### T-MODA-004: Add mock and configured model adapter implementations

- **Maps to:** REQ-MODA-002, REQ-MODA-005, REQ-MODA-011, REQ-MODA-013; spec sections "Adapter Boundary", "Operation Outputs"; design "Adapter Contract".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-MODA-003
- **Scope:** Add deterministic mock model adapter for chat, embedding, rerank, vision, and speech contract tests. Add a configured model adapter boundary that reports safe capability metadata; real execution remains deferred until provider topology is accepted.
- **Constraints:** Mock-only verification; no external network; no raw endpoint/credential/path/prompt/provider payload in capability or output.
- **Verification:**

```bash
cd backend && mvn -Dtest=ModelAdapterContractTest test
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src/main/java/com/atlas/metadata/adapter
```

### T-MODA-005: Implement model adapter registry and capability listing

- **Maps to:** REQ-MODA-001, REQ-MODA-002, REQ-MODA-003, REQ-MODA-004; spec sections "Adapter Boundary", "Capability Metadata"; API guide `GET /api/model-adapters`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-MODA-003, T-MODA-004
- **Scope:** Add registry resolution for explicit/default adapter/model selection, including unavailable and misconfigured statuses. Add capability listing service behavior with masked config.
- **Constraints:** Adapter boundary; secret masking; no hardcoded single implementation as the only future option.
- **Verification:**

```bash
cd backend && mvn -Dtest=ModelAdapterRegistryTest test
git diff --check
```

### T-MODA-006: Implement model run service and status mapping

- **Maps to:** REQ-MODA-006, REQ-MODA-007, REQ-MODA-008, REQ-MODA-014; spec sections "Model Run Lifecycle", "Operation Outputs", "State Model"; design "Service Behavior".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-MODA-001, T-MODA-003, T-MODA-005
- **Scope:** Implement model run creation, request validation, default resolution, adapter execution, output validation, usage summary calculation, `SUCCEEDED` / `PARTIAL_FAILED` / `FAILED` mapping, and safe adapter-fault handling.
- **Constraints:** Mock-engine execution in tests; generated output remains `REVIEW_REQUIRED`; no Ask/RAG/vector/graph/Wiki side effects.
- **Verification:**

```bash
cd backend && mvn -Dtest=ModelServiceTest,ModelSummaryCalculatorTest test
git diff --check
```

`ModelServiceTest` must include direct assertions for `SUCCEEDED`, `PARTIAL_FAILED`, `FAILED`, and unavailable/misconfigured adapter status mapping.

### T-MODA-007: Implement source reference and output persistence

- **Maps to:** REQ-MODA-007, REQ-MODA-008, REQ-MODA-009, REQ-MODA-010, REQ-MODA-011, REQ-MODA-012; spec sections "Operation Outputs", "Validation And Failure Behavior"; design "Data Design".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-MODA-006
- **Scope:** Persist validated source references and output descriptors. Reject unsafe references, invalid confidence, negative usage counts, invalid embedding dimensions, raw vector payloads, oversized summaries, and output statuses other than `REVIEW_REQUIRED`.
- **Constraints:** Preserve source trace and review status; secret/prompt/provider masking; no raw vector storage and no vector database write.
- **Verification:**

```bash
cd backend && mvn -Dtest=ModelDomainInvariantTest,ModelServiceTest test
git diff --check
```

### T-MODA-008: Add model REST API endpoints

- **Maps to:** REQ-MODA-003, REQ-MODA-006, REQ-MODA-007, REQ-MODA-014; spec section "API / Interface Surface"; API guide all endpoints.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-MODA-005, T-MODA-006, T-MODA-007
- **Scope:** Add controller endpoints `GET /api/model-adapters`, `POST /api/model-runs`, and `GET /api/model-runs/{runId}`. Responses must use `ApiEnvelope` and match the API guide.
- **Constraints:** Internal-only; no auth/RBAC implementation; no raw prompts, provider payloads, vectors, or credentials.
- **Verification:**

```bash
cd backend && mvn -Dit.test=ModelApiContractIT verify
git diff --check
```

### T-MODA-009: Extend adapter seam and safety guards

- **Maps to:** REQ-MODA-001, REQ-MODA-004, REQ-MODA-012, REQ-MODA-013, REQ-MODA-015; spec sections "Adapter Boundary", "Validation And Failure Behavior"; design "Testing Considerations".
- **Owner type:** QA/backend
- **Priority:** Must
- **Dependencies:** T-MODA-003, T-MODA-004, T-MODA-008
- **Scope:** Extend guard tests so model provider names, local runtime markers, command runners, outbound network clients, and vector database write paths are forbidden in non-adapter product layers. Add secret/private-path/raw-prompt scans for model implementation and docs.
- **Constraints:** No direct model provider calls outside adapter; no vector writes; secret-masked output only.
- **Verification:**

```bash
cd backend && mvn -Dtest=AdapterSeamGuardTest test
! rg -n "OpenAI|Ollama|DeepSeek|GitHub Models|Copilot|WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(|pgvector|Milvus|Qdrant" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/model-adapter-requirements.md docs/02-user-stories/model-adapter-stories.md docs/03-spec/model-adapter-spec.md docs/04-architecture/model-adapter-architecture.md docs/04-architecture/model-adapter-data-flow.md docs/04-architecture/model-adapter-data-model.md docs/05-design/model-adapter-design.md docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/model-adapter-tasks.md
```

### T-MODA-010: Run final model-adapter verification

- **Maps to:** REQ-MODA-013, REQ-MODA-014, REQ-MODA-015; spec acceptance matrix AC-MODA-01 through AC-MODA-09.
- **Owner type:** QA/backend
- **Priority:** Must
- **Dependencies:** T-MODA-001 through T-MODA-009
- **Scope:** Run the complete verification set, review the diff, and update traceability with evidence if implementation changes docs/status later.
- **Constraints:** Mock-only; adapter only; no external network/cloud calls from product code; secret-masked; review-required output; no vector/Ask/graph/Wiki/frontend side effects.
- **Verification:**

```bash
cd backend && mvn verify
git diff --check
! rg -n "OpenAI|Ollama|DeepSeek|GitHub Models|Copilot|WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(|pgvector|Milvus|Qdrant" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/model-adapter-requirements.md docs/02-user-stories/model-adapter-stories.md docs/03-spec/model-adapter-spec.md docs/04-architecture/model-adapter-architecture.md docs/04-architecture/model-adapter-data-flow.md docs/04-architecture/model-adapter-data-model.md docs/05-design/model-adapter-design.md docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/model-adapter-tasks.md
```

## Dependency Plan

- Critical path: T-MODA-001 -> T-MODA-002 -> T-MODA-003 -> T-MODA-004 -> T-MODA-005 -> T-MODA-006 -> T-MODA-007 -> T-MODA-008 -> T-MODA-009 -> T-MODA-010
- Parallel opportunities after T-MODA-003: adapter contract tests, DTO mapper tests, and summary calculator tests may be built alongside service tests.

## Open Questions / Risks

- OQ-MODA-001: First real provider is deferred; the mock contract must not depend on it.
- OQ-MODA-002: This task set stores references and safe summaries only; raw prompt retention would require an accepted SDD change.
- OQ-MODA-003: Usage/cost governance is deferred; only counts are in scope.
- R-MODA-003: Embedding outputs must not bypass `vector-adapter`; T-MODA-009 must guard non-adapter vector writes.
