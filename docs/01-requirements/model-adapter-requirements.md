# Requirements: Model Adapter

## Status

Draft. Phase 3 adapter slice. SDD only; no product code is implemented in this pass.

## Slice Contract

- **Goal:** Atlas can invoke LLM, embedding, rerank, vision, and speech model capabilities through a product-facing model adapter contract with masked configuration, mock-only verification, and review-required outputs.
- **Slice:** `model-adapter`
- **Phase:** 3 adapter
- **Sources:** `README.md`, `PROJECT_RULES.md`, `DEVELOPMENT_STANDARDS.md`, `docs/00-context/sdd-profile.md`, `docs/00-context/slice-roadmap.md`, `docs/01-requirements/requirement.md`, `docs/architecture.md`, `docs/technology-decisions.md`, `docs/03-spec/metadata-api-spec.md`, `docs/04-architecture/metadata-api-data-model.md`, existing converter/parser adapter SDD, storage adapter SDD, and FE model settings baseline in `frontend/public/atlas-prototype.html` / `prototypes/index.html`.
- **Verification row:** Phase 3 adapter requires unit + integration tests against mock engines.
- **Hard constraints:** Parser/converter/model/vector/storage must go only through product-facing adapters; tools/providers must never be called directly; one implementation must not be hardcoded; secrets and private endpoints must be masked/status-only; source trace, confidence, and review status must be preserved.

## In Scope

- Product-facing model adapter contract and registry for chat, embedding, rerank, vision, and speech capability classes.
- Capability metadata for built-in/mock model providers, including adapter key, model key, model type, default marker, status, supported operations, context limits, and masked configuration summary.
- Internal model-run API contract for mock execution of model operations with source references and review-required output metadata.
- Model run evidence records with request purpose, input reference, source chunk references, output summary, confidence/evidence, review status, safe message, and usage summary.
- Secret and endpoint masking rules for provider configuration and errors.
- Mock model adapter implementation and contract tests; no real model provider calls.
- Adapter seam guards that forbid model SDK/client/provider names in controller/service/repository/domain layers.

## Exclusions

- Real LLM, embedding, rerank, vision, speech, GitHub Models, Copilot, OpenAI-compatible, Ollama, or custom provider execution.
- Production secret manager integration, credential rotation, BYOK, cost controls, quotas, streaming responses, rate limiting, and provider account administration.
- Ask/RAG answer generation, vector indexing, graph extraction, Markdown normalization, Wiki publication, OCR/transcription pipelines, frontend UI changes, production auth/RBAC, and production audit policy beyond run evidence.
- Raw prompt/document storage, raw provider logs, raw credentials, private endpoints, real company content, external cloud calls, or external network dependencies.

## Requirements

| ID | Requirement | Priority | Source / Rationale |
|---|---|---|---|
| REQ-MODA-001 | Product workflow code must resolve model behavior through a model adapter registry and must not call model SDKs, provider HTTP APIs, local model runtimes, or outbound clients directly outside adapter scope. | Must | Adapter Standards, REQ-PROD-041, REQ-PROD-058 |
| REQ-MODA-002 | Model providers must be represented as replaceable adapters; no single provider such as Ollama, DeepSeek, GitHub Models, Copilot, or an OpenAI-compatible API may be hardcoded as the only implementation. | Must | REQ-PROD-057, REQ-PROD-058, REQ-PROD-061 |
| REQ-MODA-003 | Capability metadata must expose adapter key, model key, display name, model type, default marker, status, supported operations, context limits, and masked configuration summary. | Must | REQ-PROD-055, REQ-PROD-056, REQ-PROD-057, REQ-PROD-061 |
| REQ-MODA-004 | Capability metadata and API responses must never expose raw secrets, provider endpoints, organization identifiers, hostnames, local runtime paths, credentials, or private model configuration. | Must | REQ-PROD-049, REQ-PROD-058, Security/Data Standards |
| REQ-MODA-005 | The adapter contract must support chat, embedding, rerank, vision, and speech capability classes as product concepts, while allowing unsupported operations to fail safely. | Must | REQ-PROD-057 |
| REQ-MODA-006 | A model run must accept a purpose, operation type, model selector, input reference or safe mock input, optional source chunk references, requested-by field, and mode; automated tests use `mock` mode. | Must | Phase 3 verification row |
| REQ-MODA-007 | Model run output must include operation-specific safe output descriptors, confidence/evidence when available, review status, usage summary, adapter/model identity, and a sanitized message. | Must | Trace/review preservation |
| REQ-MODA-008 | Any LLM-generated or model-generated content must default to `REVIEW_REQUIRED` unless an accepted deterministic validation or SME review explicitly changes it in a later slice. | Must | REQ-PROD-026, REQ-PROD-041, Trace And Review |
| REQ-MODA-009 | Model run evidence must preserve source trace references when inputs come from file items, source chunks, Wiki pages, graph nodes, or Ask context. | Must | REQ-PROD-040, REQ-PROD-042 |
| REQ-MODA-010 | Embedding outputs must expose vector dimension and item counts as metadata only; this slice must not write to a vector database or create indexes. | Must | Vector adapter boundary |
| REQ-MODA-011 | Chat/vision/speech/rerank mock outputs must be safe summaries or references and must not store raw confidential document text or raw prompt payloads. | Must | Security/Data Standards |
| REQ-MODA-012 | Provider failures and validation errors must return user-safe messages with no raw provider payload, stack trace, secret, private endpoint, local path, or confidential input text. | Must | REQ-PROD-077 |
| REQ-MODA-013 | Automated verification must use mock/fake model engines only and must not require external network access, model credentials, local model daemons, or real provider accounts. | Must | Phase 3 verification row |
| REQ-MODA-014 | Model run records must support terminal statuses and summaries for success, partial failure, validation failure, unavailable adapter, and safe adapter faults. | Must | Adapter Standards |
| REQ-MODA-015 | The model adapter slice must not implement Ask/RAG, vector indexing, graph extraction, Wiki publication, production auth/RBAC, or frontend settings changes. | Must | Phase discipline |

## Acceptance

- Complete bilingual SDD artifacts exist for requirements, stories, spec, architecture, data flow, data model, design, API guide, tasks, and traceability.
- REQ/US/T IDs match across English and Simplified Chinese copies.
- Tasks map to requirement IDs and spec sections and include exact verification commands.
- API guide is included because this Phase 3 adapter slice defines internal API/adapter contracts.
- No product code is changed in this SDD pass.

## Open Questions

| ID | Question | Impact |
|---|---|---|
| OQ-MODA-001 | Which real provider should be enabled first after mock verification: local runtime, GitHub/Copilot catalog, DeepSeek-compatible API, or another internal gateway? | Deployment detail; does not block the mock adapter contract. |
| OQ-MODA-002 | Should raw prompts ever be persisted, or should Atlas persist only prompt references, source references, and safe summaries? | This SDD commits to references and safe summaries only. Confirm before implementation if raw prompt retention is required. |
| OQ-MODA-003 | Should model run usage/cost policy be part of this slice or deferred to Phase 4 governance? | This SDD records usage counts only and defers cost/quota policy. |
