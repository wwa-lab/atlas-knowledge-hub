# User Stories: Model Adapter

## Status

Draft. Derived from `docs/01-requirements/model-adapter-requirements.md`. IDs are stable across EN and zh-CN copies.

## US-MODA-001: Resolve Model Capabilities Through Adapters

**Story:**
As a platform administrator,
I want Atlas to list model capabilities through a replaceable adapter registry,
so that model providers can change without product workflows depending on one SDK or provider.

## Acceptance Criteria

1. **Given** configured model adapters exist
   **When** capability metadata is requested
   **Then** Atlas returns adapter/model keys, model type, status, supported operations, default marker, context limits, and masked configuration.

2. **Given** a provider has sensitive configuration
   **When** capability metadata is returned
   **Then** raw secrets, provider endpoints, hostnames, organization identifiers, and local paths are not exposed.

3. **Given** product code outside the adapter package is scanned
   **When** seam guard checks run
   **Then** direct model SDK, provider, local runtime, and outbound client references fail verification.

## Notes / Assumptions

- Capability metadata may include safe status words such as `configured`, `not_configured`, and `mock`.
- Real provider execution is deferred; mock adapters prove the contract.

## Dependencies

- Phase 2 metadata API and existing `ApiEnvelope`.
- Existing adapter registry pattern from converter/parser slices.

## Out of Scope

- Real provider execution, credential rotation, production auth/RBAC, frontend settings edits.

## Open Questions

- OQ-MODA-001: Which real provider ships first after mock verification?

## US-MODA-002: Run Mock Model Operations With Review-Required Output

**Story:**
As a delivery lead,
I want Atlas to create mock model runs for chat, embedding, rerank, vision, or speech operations,
so that downstream Ask, vector, graph, and review workflows can integrate with a stable contract later.

## Acceptance Criteria

1. **Given** a valid model run request in `mock` mode
   **When** Atlas executes the run
   **Then** it persists a run record with adapter/model identity, operation, status, source references, safe output summary, usage summary, and timestamps.

2. **Given** a model-generated output is created
   **When** the run is completed
   **Then** the output defaults to `REVIEW_REQUIRED` and is not treated as approved knowledge.

3. **Given** an embedding operation is requested
   **When** the mock adapter returns embedding metadata
   **Then** Atlas records dimension and item counts only and does not write to a vector database.

## Notes / Assumptions

- Mock input may use safe sample text; real document content is referenced, not persisted.
- Usage summary records counts only; cost/quota policy is deferred.

## Dependencies

- Metadata entities for file items/source chunks when source references are used.

## Out of Scope

- Ask answer generation, vector indexing, graph extraction, Wiki publication, real speech/vision processing.

## Open Questions

- OQ-MODA-002: Whether raw prompt retention is ever allowed.

## US-MODA-003: Preserve Source Trace And Safe Evidence

**Story:**
As an SME reviewer,
I want model outputs to retain source references, confidence/evidence, and review status,
so that generated content can be evaluated before it affects trusted Wiki, Graph, or Ask experiences.

## Acceptance Criteria

1. **Given** model input references source chunks
   **When** Atlas returns the model run report
   **Then** the report includes source chunk references without embedding confidential raw text.

2. **Given** a mock chat, vision, speech, or rerank output is produced
   **When** the output is persisted or returned
   **Then** Atlas includes safe summaries, confidence/evidence when available, and `REVIEW_REQUIRED`.

3. **Given** a later workflow consumes model output
   **When** the output has not been reviewed
   **Then** the contract makes the review-required status observable.

## Notes / Assumptions

- Model output references are intentionally lighter than final Wiki publication records.

## Dependencies

- Review status vocabulary from metadata API.

## Out of Scope

- SME review UI changes and publish transitions.

## Open Questions

- None.

## US-MODA-004: Fail Safely Without Leaking Provider Internals

**Story:**
As a knowledge base administrator,
I want model adapter failures to return safe, bounded errors,
so that troubleshooting is possible without exposing secrets, private paths, prompts, or provider internals.

## Acceptance Criteria

1. **Given** an unknown adapter or model key is requested
   **When** Atlas validates the request
   **Then** it returns a `VALIDATION_ERROR` with a user-safe field message.

2. **Given** a configured adapter is unavailable or misconfigured
   **When** a run is requested
   **Then** Atlas records a failed run or rejects the request with no raw endpoint, credential, stack trace, local path, or provider payload.

3. **Given** a mock adapter throws an unexpected fault
   **When** the service maps the error
   **Then** API and persisted messages are sanitized and bounded.

## Notes / Assumptions

- Detailed operational diagnostics, if needed later, belong in protected server logs and Phase 4 governance, not in user-facing responses.

## Dependencies

- Existing safe error envelope and validation patterns.

## Out of Scope

- Production observability, rate limiting, cost alerts, or provider incident workflows.

## Open Questions

- OQ-MODA-003: Whether cost/quota policy belongs in a later governance slice.

## US-MODA-005: Verify The Model Adapter Contract With Mock Engines

**Story:**
As a Codex implementation agent,
I want executable model-adapter tasks with exact mock-engine verification commands,
so that implementation can complete the slice without guessing scope or calling real providers.

## Acceptance Criteria

1. **Given** the SDD is accepted
   **When** implementation starts
   **Then** each task maps to REQ IDs, spec sections, constraints, dependencies, and exact commands.

2. **Given** automated verification runs
   **When** adapter contract and API tests execute
   **Then** they use mock/fake model engines only and require no network, credentials, local daemons, or provider accounts.

3. **Given** final guard checks run
   **When** non-adapter product layers are scanned
   **Then** direct model provider/client references and raw secret patterns are absent.

## Notes / Assumptions

- Implementation must stop and report if it would diverge from the accepted spec/tasks.

## Dependencies

- Accepted SDD set for `model-adapter`.

## Out of Scope

- Product code implementation in this SDD generation pass.

## Open Questions

- None.
