# Design: Ask RAG

## Status

Draft. Derived from `docs/04-architecture/ask-rag-architecture.md` using `architecture-to-design`.

## Design Scope

This design covers the Trusted Ask frontend states, backend Ask API, application orchestration, persistence, adapter boundaries, validation, error handling, and verification plan for the `ask-rag` slice.

## Module Design

### Frontend Trusted Ask

- Render within the Knowledge Space Ask tab.
- Keep the current prototype shape: title, policy note, input row, answer panel, confidence label, source rows, approved Wiki pages, evidence chunks, and review-required count.
- Add data-driven UI states: idle, loading, answered, no evidence, review-required evidence included, and safe error.
- Evidence rows expose stable selectors for E2E tests.
- The UI does not claim production RBAC or production model connectivity.

### Backend Ask API

- `AskController` owns request/response envelope shape.
- `AskService` owns validation, retrieval policy, adapter orchestration, persistence, and state transitions.
- `AskMapper` maps entities and adapter results to safe DTOs.
- `AskSummaryCalculator` or equivalent pure helper computes status and evidence summaries.

### Adapter Integration

- Use vector query behavior through the existing vector service/adapter boundary.
- Use model run behavior through the existing model service/adapter boundary.
- Pass product references and safe descriptors, not raw source text or raw prompts.
- Automated verification uses mock adapters only.

### Persistence

- Add `AskRun` and `AskEvidence` logical entities and repositories.
- Add a Flyway migration only during implementation.
- Reference existing source chunks and model runs by id.
- Do not mutate source chunk, file item, Wiki page, graph, or review records.

## API / Interface Design

The API guide is required because this slice adds backend endpoints. Full payloads live in `docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md`.

| Endpoint | Purpose |
|---|---|
| `POST /api/spaces/{spaceId}/ask` | Run one scoped Ask request and return answer/evidence/no-evidence payload. |
| `GET /api/ask-runs/{runId}` | Read persisted Ask run and evidence detail. |

## Validation And Error Handling

| Case | Behavior |
|---|---|
| Blank question | `400 VALIDATION_ERROR`; no adapter execution. |
| Oversized question | `400 VALIDATION_ERROR`; no adapter execution. |
| Unknown space | `404 NOT_FOUND`; no adapter execution. |
| Invalid policy or limit | `400 VALIDATION_ERROR`; no adapter execution. |
| No approved evidence | `200` with `status=NO_EVIDENCE`; no model generation. |
| Vector failure | Safe failed response and persisted safe error. |
| Model failure | Safe failed or partial response; evidence retained if available. |

## UI / User Flow Design

1. User opens a Knowledge Space and selects Trusted Ask.
2. User enters a question and submits.
3. UI shows loading state.
4. Successful answer shows answer text, confidence, review status, and evidence rows.
5. No-evidence state explains approved evidence is required.
6. Review-required inclusion state shows warning and separated evidence.
7. Safe error state shows a bounded message and keeps the previous successful answer, if any, visually distinct.

## Security / Audit / Reliability Design

- Store safe summaries and references only.
- Do not store raw prompt, source text, vector values, provider payloads, secrets, private endpoints, or stack traces.
- Use append-only Ask run/evidence records for audit.
- Keep generated answer status `REVIEW_REQUIRED`.
- Add seam guard coverage for direct provider/vector calls outside adapter packages.

## Testing Considerations

- Backend unit tests for validation, policy branching, no-evidence refusal, status transitions, and safe error masking.
- Backend integration/API tests for envelope shape, persistence, and state immutability.
- Frontend unit/component tests for UI state mapping and evidence rendering.
- Playwright E2E for answer success, no-evidence, and review-required warning.
- `git diff --check`, dependency/network scan, and secret/private-path scan.

## Risks / Tradeoffs

- First implementation may use mock query and mock model answers only; this is intentional for Phase 4 hardening in this repository.
- Reranking is deferred unless product accepts it as part of this slice.
- Production auth/RBAC is represented only by scope validation and audit fields unless a separate security slice accepts full enforcement.

## Open Questions

- OQ-ASKRAG-001: Role policy for review-required evidence.
- OQ-ASKRAG-002: Future review queue integration.
- OQ-ASKRAG-003: Reranking timing.
