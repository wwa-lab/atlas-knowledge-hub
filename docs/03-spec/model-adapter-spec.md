# Specification: Model Adapter

## Status

Draft. Phase 3 adapter slice. Behavior source of truth for `model-adapter`. Derived from `docs/02-user-stories/model-adapter-stories.md`.

## Source Documents

- `docs/01-requirements/model-adapter-requirements.md`
- `docs/02-user-stories/model-adapter-stories.md`
- `docs/01-requirements/requirement.md`
- `docs/00-context/slice-roadmap.md`
- `docs/03-spec/metadata-api-spec.md`
- Existing converter/parser adapter specs and storage adapter SDD for adapter contract shape
- FE model settings baseline: `frontend/public/atlas-prototype.html`, `prototypes/index.html`

## Scope

Atlas must support model-provider capability discovery and mock model execution through a product-facing model adapter contract. The slice includes model capabilities, model run lifecycle, mock operation outputs, source trace references, review-required output status, usage summaries, safe error handling, adapter seam guards, and internal API/adapter contracts. It does not implement real model provider calls, Ask/RAG, vector indexing, graph extraction, Wiki publication, frontend UI changes, production secret management, or production auth/RBAC.

## Constraints

- **Adapter only:** product workflow code depends on model adapter interfaces and registry contracts, not provider SDKs, HTTP clients, local runtimes, or model CLIs directly (REQ-MODA-001).
- **No single hardcoded implementation:** built-in/mock models may mirror the FE baseline, but real providers remain replaceable (REQ-MODA-002).
- **Mock-engine verification:** automated tests use mock/fake model adapters and require no credentials, network, local model daemon, or provider account (REQ-MODA-013).
- **Secret/endpoint safety:** raw provider secrets, endpoints, hostnames, private paths, provider payloads, raw prompts, and stack traces are not returned or persisted (REQ-MODA-004, REQ-MODA-012).
- **Trace/review preservation:** model outputs retain source references and default to `REVIEW_REQUIRED`; they do not become approved knowledge (REQ-MODA-008, REQ-MODA-009).
- **Boundary discipline:** embedding metadata does not write vectors; chat output does not become Ask/RAG; model output does not publish Wiki or graph data (REQ-MODA-010, REQ-MODA-015).

## Actors

| Actor | Role |
|---|---|
| Platform administrator | Reviews masked model capabilities and provider readiness. |
| Delivery lead | Triggers or inspects mock model runs for workflow integration evidence. |
| SME reviewer | Uses source references and review status to evaluate generated output in later slices. |
| Codex implementation agent | Implements strictly against this spec and task checklist after SDD acceptance. |

## Functional Requirements

### Adapter Boundary

- **FR-MODA-001:** The model workflow must resolve a model adapter through a registry/capability contract before any model operation starts. (US-MODA-001)
- **FR-MODA-002:** Non-adapter product layers must not reference provider SDKs, local runtime commands, direct model HTTP clients, or provider-specific execution APIs. (US-MODA-001, US-MODA-005)
- **FR-MODA-003:** The adapter registry must support explicit model selection, default model selection per model type, unavailable status, and misconfigured status. (US-MODA-001, US-MODA-004)

### Capability Metadata

- **FR-MODA-004:** Capability metadata must include adapter key, model key, display name, provider family, model type, supported operations, default marker, status, context limit, and masked configuration summary. (US-MODA-001)
- **FR-MODA-005:** Capability metadata must not expose raw provider endpoints, local runtime paths, organization identifiers, credentials, hostnames, or private configuration values. (US-MODA-001, US-MODA-004)
- **FR-MODA-006:** Model types must include `CHAT`, `EMBEDDING`, `RERANK`, `VISION`, and `SPEECH`; unsupported operations fail validation before adapter execution. (US-MODA-002)

### Model Run Lifecycle

- **FR-MODA-007:** A model run request must include operation type, requested-by identity, mode, purpose, optional adapter/model selector, and either a safe mock input or an input reference. (US-MODA-002)
- **FR-MODA-008:** A model run may reference source chunks, file items, Wiki pages, graph nodes, or a future Ask context by id; referenced source text is not copied into the run record. (US-MODA-003)
- **FR-MODA-009:** The service must validate operation/model compatibility, mode, input reference shape, source reference scope, and safe text limits before adapter execution. (US-MODA-002, US-MODA-004)
- **FR-MODA-010:** Run statuses are `REQUESTED`, `RUNNING`, `SUCCEEDED`, `PARTIAL_FAILED`, and `FAILED`. (US-MODA-002, US-MODA-004)
- **FR-MODA-011:** Completed run reports must include adapter/model identity, operation type, status, safe message, usage summary, outputs, source references, timestamps, and review status. (US-MODA-002, US-MODA-003)

### Operation Outputs

- **FR-MODA-012:** Chat output returns a safe generated summary/reference and review status, not an approved answer for Ask. (US-MODA-002, US-MODA-003)
- **FR-MODA-013:** Embedding output returns embedding dimension, item count, and output reference metadata only; vectors are not returned in API JSON and are not written to a vector database. (US-MODA-002)
- **FR-MODA-014:** Rerank output returns ordered item ids and scores for safe mock inputs/references only. (US-MODA-002)
- **FR-MODA-015:** Vision output returns a safe description/reference and evidence metadata only; no raw image bytes or confidential screenshots are persisted. (US-MODA-002, US-MODA-003)
- **FR-MODA-016:** Speech output returns a safe transcript summary/reference only; real transcription is out of scope. (US-MODA-002)
- **FR-MODA-017:** All generated outputs default to `REVIEW_REQUIRED` and preserve confidence/evidence when available. (US-MODA-003)

### Validation And Failure Behavior

- **FR-MODA-018:** Unknown adapter key, unknown model key, incompatible operation/model type, invalid mode, unsafe input reference, oversized safe input, or cross-scope source references fail with `VALIDATION_ERROR`. (US-MODA-004)
- **FR-MODA-019:** Unavailable or misconfigured adapters fail safely without exposing raw provider configuration or private runtime details. (US-MODA-004)
- **FR-MODA-020:** Unexpected adapter faults return sanitized, bounded safe messages and do not persist raw prompts, provider payloads, stack traces, endpoints, credentials, or private paths. (US-MODA-004)

## Non-Functional Requirements

| Category | Requirement |
|---|---|
| Security | No raw provider secrets, endpoints, credentials, private paths, raw prompts, raw provider payloads, or confidential document text in responses, persisted summaries, or test fixtures. |
| Reliability | Mock-engine tests cover capability listing, default resolution, each supported model type, incompatible operation rejection, unavailable adapter, adapter fault, and safe error handling. |
| Extensibility | New provider families and model types can be added through adapter capability metadata without changing product workflow callers. |
| Auditability | Model run records preserve adapter/model identity, operation, requested-by, timestamps, source references, usage summary, output review status, and safe messages. |
| Data safety | Mock/sample data only; no real company documents, screenshots, credentials, private endpoints, or external cloud calls. |

## Workflow

```text
+---------------------------+
| Model run request          |
| operation + references     |
+-------------+-------------+
              |
              v
+---------------------------+        invalid selector/input
| Validate request and scope |------------------------------+
+-------------+-------------+                              |
              | valid                                      v
              v                                    +----------------+
+---------------------------+                      | Safe error     |
| Resolve model adapter      |-- unavailable -----> | no raw leak    |
+-------------+-------------+                      +----------------+
              |
              v
+---------------------------+
| Execute model adapter      |
| mock/fake engine in tests  |
+-------------+-------------+
              |
              v
+---------------------------+
| Validate safe output       |
| set REVIEW_REQUIRED        |
+-------------+-------------+
              |
              v
+---------------------------+
| Persist run evidence       |
| return API envelope        |
+---------------------------+
```

## State Model

### Model Run Status

`REQUESTED -> RUNNING -> SUCCEEDED | PARTIAL_FAILED | FAILED`

- `REQUESTED -> RUNNING`: request validation passed and adapter resolved.
- `RUNNING -> SUCCEEDED`: all requested mock operation outputs were produced safely.
- `RUNNING -> PARTIAL_FAILED`: at least one output succeeded and at least one failed or was skipped.
- `RUNNING -> FAILED`: adapter-level failure, validation failure after creation, or no output succeeded.

### Model Output Review Status

| Outcome | Review Status | Notes |
|---|---|---|
| Any generated chat/vision/speech output | `REVIEW_REQUIRED` | SME review or deterministic validation in later slice may change it. |
| Embedding metadata | `REVIEW_REQUIRED` | Embedding vectors are not trusted knowledge by themselves. |
| Rerank output | `REVIEW_REQUIRED` | Ranking evidence is advisory until validated by a consuming workflow. |
| Failed output | `REVIEW_REQUIRED` | Safe error only; no trusted content. |

## Validation Rules

- `operationType` must be one of `CHAT`, `EMBEDDING`, `RERANK`, `VISION`, `SPEECH`.
- `mode` must be `mock` or `configured`; automated tests use `mock`.
- `adapterKey` and `modelKey` must resolve to a registered capability, or be omitted for default resolution.
- The selected model type must support the requested operation.
- Safe mock input is bounded and may not contain raw credentials, private paths, or real company content.
- Input references must be relative product references, not local paths, URLs, or provider payloads.
- Source references must not cross known workspace/batch scope when a scope is provided.
- Usage counts, when present, must be non-negative.
- Confidence/evidence values, when present, must be in `[0,1]`.
- Safe messages and safe output summaries are bounded and sanitized.

## API / Interface Surface

The full contract lives in `docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md`.

| Interface | Behavior |
|---|---|
| `GET /api/model-adapters` | Lists configured model adapter/model capabilities with masked configuration. |
| `POST /api/model-runs` | Executes a mock/configured model operation through the adapter contract and records run evidence. |
| `GET /api/model-runs/{runId}` | Returns model run summary, source references, usage summary, and safe outputs. |
| Internal model adapter interface | Executes model operations behind product-facing contracts; mock/fake implementations are required for tests. |

## Acceptance Matrix

| Check | Requirement | Observable Result |
|---|---|---|
| AC-MODA-01 | REQ-MODA-001, REQ-MODA-013 | Guard tests fail on direct model provider/client references outside adapter implementation. |
| AC-MODA-02 | REQ-MODA-002, REQ-MODA-003, REQ-MODA-004 | Capability endpoint returns replaceable masked model metadata for supported model types. |
| AC-MODA-03 | REQ-MODA-005, REQ-MODA-006, REQ-MODA-007 | Mock model run succeeds for supported operation/model combinations and returns safe output descriptors. |
| AC-MODA-04 | REQ-MODA-008, REQ-MODA-009 | Outputs default to `REVIEW_REQUIRED` and preserve source references without raw source text. |
| AC-MODA-05 | REQ-MODA-010 | Embedding run returns dimension/count metadata only and does not call vector database code. |
| AC-MODA-06 | REQ-MODA-011, REQ-MODA-012 | Error messages and outputs contain no raw prompt, secret, endpoint, private path, provider payload, or stack trace. |
| AC-MODA-07 | REQ-MODA-013 | `cd backend && mvn verify` passes using mock/fake model adapters only. |
| AC-MODA-08 | REQ-MODA-014 | Run status and summary correctly represent success, partial failure, failure, and unavailable adapter cases. |
| AC-MODA-09 | REQ-MODA-015 | No Ask/RAG, vector write, graph extraction, Wiki publish, frontend, or production auth/RBAC behavior is introduced. |

## Out Of Scope

- Real model provider calls, provider SDK wiring, local runtime invocation, streaming, credential rotation, and production secret manager integration.
- Ask/RAG answering, vector index writes, graph extraction, Markdown normalization, Wiki publication, OCR/transcription pipelines, and frontend UI.
- Production auth/RBAC, rate limiting, cost/quota policy, and provider account administration.

## Open Questions

| ID | Question | Impact |
|---|---|---|
| OQ-MODA-001 | First real provider after mock implementation. | Affects adapter implementation choice, not the model adapter contract. |
| OQ-MODA-002 | Raw prompt retention policy. | This spec commits to references and safe summaries only. |
| OQ-MODA-003 | Usage/cost governance timing. | This spec records usage counts only and defers quota/cost policy. |
