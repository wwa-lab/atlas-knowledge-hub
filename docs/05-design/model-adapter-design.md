# Design: Model Adapter

## Status

Draft. Phase 3 adapter slice. Implementation design for `model-adapter`.

## Overview

The design adds a model adapter seam to the Spring Boot metadata control plane. It mirrors the existing converter/parser adapter pattern while keeping provider execution, credentials, and raw prompts outside product layers.

## Source Architecture

- Spec: `docs/03-spec/model-adapter-spec.md`
- Architecture: `docs/04-architecture/model-adapter-architecture.md`
- Data flow: `docs/04-architecture/model-adapter-data-flow.md`
- Data model: `docs/04-architecture/model-adapter-data-model.md`

## Design Assumptions

- Backend stack remains Java + Spring Boot.
- API responses use `ApiEnvelope`, matching `backend/src/main/java/com/atlas/metadata/dto/ApiEnvelope.java:4`.
- Adapter contracts live in the backend adapter package, following the parser adapter pattern at `backend/src/main/java/com/atlas/metadata/adapter/ParserAdapter.java:4`.
- Tests use mock/fake model adapters only.

## Design Scope

In scope: domain enums/entities, DTOs/mappers, adapter interface, mock/configured adapter boundary, registry, service, REST controller, Flyway migration, tests, seam guard updates, and secret/path scans.

Out of scope: real provider calls, Ask/RAG, vector writes, graph/Wiki publication, frontend changes, production auth/RBAC, and production credential storage.

## Module Design

### Domain And Persistence

- Add model-specific enums for adapter status, model type, operation type, run status, output kind, and source reference type.
- Add `model_run`, `model_run_output`, and `model_run_source_reference` entities.
- Preserve append-oriented evidence semantics: run records are completed with terminal status and safe message; outputs are review-required by default.
- Store input/output references and safe summaries, not raw prompts, raw provider payloads, raw vectors, or confidential text.

### Adapter Contract

Conceptual interface:

```text
ModelAdapter
  capability() -> List<ModelCapability>
  execute(ModelRequest) -> ModelResult
```

Design rules:

- Request/result types use Atlas product concepts: operation, model type, source references, usage summary, output descriptors.
- No provider SDK payloads leak into controller/service/domain/repository layers.
- Capability metadata is masked/status-only.
- Configured real execution may be a safe placeholder until provider topology is accepted.

### Registry And Default Selection

- Registry lists all capabilities and sorts defaults first.
- Omitted model selector resolves to the default model for the requested operation/model type.
- Unknown selectors raise validation errors before adapter execution.
- Unavailable/misconfigured adapters return safe status or safe failed run behavior.

### Service Behavior

- Validate request fields, mode, operation/model compatibility, source references, safe input length, confidence, and usage counts.
- Persist `REQUESTED`, transition to `RUNNING`, execute adapter, validate outputs, then complete terminal status.
- Map partial output failures to `PARTIAL_FAILED`.
- Sanitize safe messages before persistence and response.

### REST API

- `GET /api/model-adapters`
- `POST /api/model-runs`
- `GET /api/model-runs/{runId}`

All responses use `ApiEnvelope`; create returns `201`.

## Data Design

The data model follows `docs/04-architecture/model-adapter-data-model.md`.

Important invariants:

- `review_status` defaults to `REVIEW_REQUIRED`.
- Embedding stores dimension/count metadata only.
- Source references are product ids and labels, not raw source text.
- No endpoint returns raw vectors, provider endpoints, credentials, local paths, or raw prompts.

## Workflow / Execution Design

1. API receives a model run request.
2. Service validates operation, mode, selectors, safe input/reference, and source references.
3. Registry resolves adapter/model default.
4. Service persists run as `REQUESTED`, marks `RUNNING`.
5. Adapter returns mock result.
6. Service validates output descriptors and usage summary.
7. Service persists outputs/source references and completes run.
8. API returns the run report.

## Validation And Error Handling

| Rule | Failure |
|---|---|
| Unsupported operation or incompatible model type | `400 VALIDATION_ERROR` |
| Unknown adapter/model key | `400 VALIDATION_ERROR` |
| Unsafe input reference or raw path/URL | `400 VALIDATION_ERROR` |
| Oversized safe input | `400 VALIDATION_ERROR` |
| Unavailable adapter | safe failed run or `400 VALIDATION_ERROR` |
| Adapter fault | `FAILED` with sanitized safe message |
| Invalid output confidence/usage/dimension | reject output; `PARTIAL_FAILED` or `FAILED` |

## Testing Considerations

- Unit tests for adapter contract and output validation.
- Unit tests for registry default resolution.
- Unit tests for summary/status calculation.
- Service tests for success, partial failure, unavailable adapter, invalid selector, unsafe references, and safe error handling.
- API contract tests for endpoints and envelope shapes.
- Seam guard tests for direct provider/client references outside adapter scope.

## Risks / Tradeoffs

| ID | Risk | Design Response |
|---|---|---|
| DT-MODA-001 | Provider choice may change after SDD acceptance. | Keep provider detail behind `ModelAdapter`; mock contract is stable. |
| DT-MODA-002 | Raw prompts are useful for debugging but unsafe. | Persist references and safe summaries only; raise OQ-MODA-002 for product decision. |
| DT-MODA-003 | Embeddings may tempt direct vector writes. | Explicit task and guard coverage prohibit vector database calls. |
| DT-MODA-004 | Existing seam guard must evolve for model terms. | Add a dedicated task to update forbidden references intentionally. |

## Open Questions

- OQ-MODA-001: First real provider after mock implementation.
- OQ-MODA-002: Raw prompt retention policy.
- OQ-MODA-003: Usage/cost governance timing.
