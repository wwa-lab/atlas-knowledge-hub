# Data Flow: Model Adapter

## Status

Draft. Phase 3 adapter slice. Companion to `docs/04-architecture/model-adapter-architecture.md`.

## Scope

This document describes runtime data movement for model capability listing and mock model runs. It covers product metadata, adapter calls, safe output validation, and run evidence persistence. Real provider execution, vector writes, Ask/RAG, graph extraction, Wiki publication, frontend changes, and production secret management are out of scope.

## Flow 1: Capability Listing

```text
Admin / test
  -> GET /api/model-adapters
  -> ModelController
  -> ModelService.listCapabilities
  -> ModelAdapterRegistry.capabilities
  -> ModelAdapter.capability
  -> masked ModelCapabilityResponse
  -> ApiEnvelope.ok(data)
```

Rules:

- Capability responses expose only masked/status fields.
- Capability listing must not call real model providers to discover live state.
- Default model markers are product policy, not provider hardcoding.

## Flow 2: Create Model Run

```text
Internal workflow / future UI
  -> POST /api/model-runs
  -> validate operation, mode, selector, input reference, source references
  -> resolve adapter/model default
  -> persist model_run as REQUESTED
  -> mark RUNNING
  -> ModelAdapter.execute(request)
  -> validate output descriptors and usage summary
  -> persist outputs + source references
  -> complete run as SUCCEEDED / PARTIAL_FAILED / FAILED
  -> return ModelRunResponse
```

Failure branches:

- Invalid request: reject before run creation with `VALIDATION_ERROR`.
- Unknown adapter/model: reject before adapter execution.
- Adapter unavailable/misconfigured: fail safely with no raw provider detail.
- Adapter fault: persist/return a bounded safe message only.

## Flow 3: Source Reference Preservation

```text
file_item/source_chunk/wiki/graph/ask reference ids
  -> request sourceReferences[]
  -> service validates known scope where available
  -> adapter receives references, not raw document text
  -> output records keep sourceReferences[]
  -> reviewStatus remains REVIEW_REQUIRED
```

Rules:

- Source references are ids and safe labels, not raw document content.
- Model output must remain review-required until another accepted slice changes it.
- Confidence/evidence values are copied only when valid `[0,1]`.

## Flow 4: Embedding Boundary

```text
embedding model run
  -> mock adapter returns dimension + itemCount + outputReference
  -> service validates dimension/itemCount
  -> persist metadata only
  -> no vector database write
```

Rules:

- Vectors are not returned in JSON.
- Vector indexing belongs to `vector-adapter`, not this slice.

## State Transitions

```text
REQUESTED
   |
   v
RUNNING
   |---- all outputs valid ------------------> SUCCEEDED
   |---- some outputs valid, some failed ----> PARTIAL_FAILED
   |---- no outputs or adapter fault --------> FAILED
```

## Error Cascade

| Stage | Error | Outcome |
|---|---|---|
| Request validation | Invalid operation, mode, selector, source reference, or safe input | `400 VALIDATION_ERROR`; no adapter call |
| Adapter resolution | Unknown key or no default | `400 VALIDATION_ERROR`; no provider leak |
| Capability status | Unavailable/misconfigured adapter | safe failed run or validation error |
| Adapter execution | Unexpected fault | `FAILED`; safe message only |
| Output validation | Invalid confidence, usage, output kind, or unsafe summary | `PARTIAL_FAILED` or `FAILED`; offending output rejected |

## Verification Hooks

- Contract tests validate mock model adapter request/result shape.
- API contract tests validate envelope, status mapping, review status, and safe output fields.
- Seam guard scans non-adapter product layers for provider/client/runtime references.
- Secret scans cover backend model-adapter code and all `model-adapter` SDD docs.
