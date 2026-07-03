# Architecture: Model Adapter

## Status

Draft. Phase 3 adapter slice. Derived from `docs/03-spec/model-adapter-spec.md`.

## Overview

Model-adapter extends the metadata control plane with a replaceable model-provider integration seam. The backend owns capability metadata, model run lifecycle, validation, run evidence, review-required output status, and user-safe reporting. Model execution is isolated behind product-facing adapter contracts; mock/fake adapters are mandatory for verification, and real provider execution remains deferred behind the same seam.

## Architectural Drivers

| Driver | Impact |
|---|---|
| Adapter neutrality | Product services resolve model providers through registry/capability contracts. |
| Secret safety | Capability and run responses expose status-only configuration and safe summaries, never raw provider secrets or endpoints. |
| Review-required output | Model-generated content remains untrusted until SME review or deterministic validation in a later slice. |
| Mock-only verification | Unit/integration tests use fake model engines and no external network, provider account, or local daemon. |
| Boundary discipline | Model output does not implement Ask/RAG, vector indexing, graph extraction, or Wiki publication. |

## Existing Metadata Context

The current backend already uses `ApiEnvelope` for success/error responses (`backend/src/main/java/com/atlas/metadata/dto/ApiEnvelope.java:4`), product-facing parser adapter contracts (`backend/src/main/java/com/atlas/metadata/adapter/ParserAdapter.java:4`), adapter registries (`backend/src/main/java/com/atlas/metadata/service/ParserAdapterRegistry.java:13`), and relative-path safety (`backend/src/main/java/com/atlas/metadata/validation/RelativePathValidator.java:10`). Model-adapter should follow these patterns while keeping provider-specific logic inside the adapter package.

## System Context

| Boundary | Responsibility |
|---|---|
| Frontend | Out of scope for this slice. Existing settings UI is a product reference only. |
| Backend API / metadata control plane | Owns model capability endpoints, model run lifecycle, validation, run reports, and persistence. |
| Model adapter seam | Encapsulates provider-specific model behavior and returns Atlas product concepts. |
| Model provider / worker | External to product workflow. Real runtime calls are deferred behind the adapter. |
| PostgreSQL metadata | Stores model run evidence, output descriptors, source references, and safe summaries. |

## High-Level Architecture

```text
+------------------------------------------------------------+
| Users / agents                                             |
| Platform admin, delivery lead, SME reviewer, Codex          |
+------------------------------+-----------------------------+
                               |
                               | REST / JSON
                               v
+------------------------------------------------------------+
| Spring Boot metadata API                                   |
| Model capability endpoint, model run endpoint, reports       |
+------------------------------+-----------------------------+
                               |
                               v
+------------------------------------------------------------+
| Model application service                                  |
| Request validation, default resolution, status mapping,      |
| safe error handling, evidence persistence                   |
+------------------------------+-----------------------------+
                               |
                   product-facing adapter interface
                               v
+------------------------------+        +---------------------+
| Model adapter registry       |        | Model adapters       |
| capability + default policy  |------->| mock provider in CI  |
+------------------------------+        | configured boundary  |
                                        +----------+----------+
                                                   |
                                                   | provider/worker boundary
                                                   v
                                        +---------------------+
                                        | Model provider       |
                                        | outside product flow |
                                        +---------------------+
                               |
                               v
+------------------------------------------------------------+
| PostgreSQL metadata                                         |
| model_run, model_run_output, model_run_source_reference      |
+------------------------------------------------------------+
```

## Component Breakdown

### Backend API

- **Model adapter capability API:** lists model capabilities and masked configuration.
- **Model run API:** creates mock/configured model runs and returns reports.
- **Metadata/source reference reuse:** file item and source chunk metadata remain the source reference surface; this slice does not duplicate raw source text.

### Application Services

- **Model run service:** validates requests, resolves adapter/model defaults, executes mock/configured mode, maps outputs, persists run evidence, and builds reports.
- **Model summary calculator:** derives operation output counts, failed/skipped counts, and usage summaries.
- **Model adapter registry:** owns default selection per model type and unavailable/misconfigured behavior.
- **Safety helpers:** reuse safe response, safe error, and relative reference validation patterns.

### Integration Adapters

- **ModelAdapter contract:** accepts Atlas model operation requests and returns Atlas model results.
- **MockModelAdapter:** deterministic fake implementation for contract/API tests.
- **ConfiguredModelAdapter boundary:** safe placeholder for future provider execution. It may know provider details, but product layers must not.

### Persistence

- Add `model_run` for execution evidence and lifecycle status.
- Add `model_run_output` for safe operation outputs, confidence/evidence, review status, and output references.
- Add `model_run_source_reference` for source chunk/file/wiki/graph/Ask references.
- Do not create vector index rows, Ask answers, graph nodes, or Wiki pages in this slice.

## State And Status Strategy

Model run status:

```text
REQUESTED -> RUNNING -> SUCCEEDED
                     -> PARTIAL_FAILED
                     -> FAILED
```

Output review strategy:

- Generated chat, vision, speech, and rerank summaries default to `REVIEW_REQUIRED`.
- Embedding outputs record dimension/item counts and references only.
- No model output is automatically `APPROVED` or `PUBLISHED`.
- Confidence/evidence is preserved when available and remains advisory.

## API / Interface Boundaries

| Interface | Consumer | Purpose |
|---|---|---|
| `GET /api/model-adapters` | Admin / implementation tests | List masked model capabilities. |
| `POST /api/model-runs` | Internal workflow / future UI | Start a model run through the model adapter contract. |
| `GET /api/model-runs/{runId}` | Delivery lead / future UI | Read model run report, safe outputs, source references, and usage summary. |
| `ModelAdapter` | Model service | Execute model work behind product-facing interface. |

## Security / Reliability / Observability

- Capability responses are masked/status-only.
- Provider errors are bounded and sanitized before persistence or response.
- Input references are product references, not local paths, URLs, or raw provider payloads.
- Raw prompts, provider payloads, provider endpoints, credentials, private paths, and stack traces are never stored in user-facing fields.
- Mock engines are mandatory for automated verification.
- Seam guard scans non-adapter product layers for direct model provider, SDK, command runner, and outbound client references.

## Architecture Review Notes

- **Extensibility:** The registry + capability model prevents provider lock-in.
- **Decoupling:** Product code depends on Atlas operation/result concepts, not provider SDK payloads.
- **Phase discipline:** Ask/RAG, vector writes, graph derivation, Wiki publishing, and frontend UI remain explicitly outside this architecture.
- **Risk:** Existing seam guard currently contains parser/vector/storage/provider terms; implementation must extend it intentionally for model provider terms.

## Risks / Tradeoffs

| ID | Risk / Tradeoff | Mitigation |
|---|---|---|
| R-MODA-001 | Real provider topology is not finalized. | Keep configured execution behind adapter; mock contract remains stable. |
| R-MODA-002 | Raw prompts or provider payloads could leak confidential content. | Persist references and safe summaries only; sanitize errors and outputs. |
| R-MODA-003 | Embedding output could accidentally bypass vector adapter. | Return dimension/count metadata only; vector writes are forbidden in this slice. |
| R-MODA-004 | Model output could be treated as trusted Ask/Wiki content too early. | Default every generated output to `REVIEW_REQUIRED`; publishing is out of scope. |

## Open Questions

- OQ-MODA-001: First real provider after mock implementation.
- OQ-MODA-002: Raw prompt retention policy.
- OQ-MODA-003: Usage/cost governance timing.
