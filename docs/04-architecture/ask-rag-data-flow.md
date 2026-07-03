# Data Flow: Ask RAG

## Status

Draft. Companion to `docs/04-architecture/ask-rag-architecture.md`.

## Primary Ask Flow

```text
User submits question
  -> Frontend validates non-empty question
  -> POST /api/spaces/{spaceId}/ask
  -> Ask API validates scope, policy, limit, filters
  -> Ask service creates REQUESTED run
  -> Vector query returns bounded evidence
  -> If no approved evidence under policy: NO_EVIDENCE response
  -> If evidence exists: model run generates mock answer from references
  -> Ask service persists answer evidence and final status
  -> API returns answer payload
  -> Frontend renders answer, confidence, evidence, review warning
```

## Review Policy Branch

| Policy | Evidence Behavior | Answer Behavior |
|---|---|---|
| `APPROVED_ONLY` | Only `APPROVED` or `PUBLISHED` evidence can be used. | If none exists, return no-evidence and skip model generation. |
| `INCLUDE_REVIEW_REQUIRED` | Approved and review-required evidence can be returned, with clear flags. | Generated answer remains `REVIEW_REQUIRED` and UI shows warning. |

## Failure Flow

```text
Invalid request
  -> VALIDATION_ERROR envelope
  -> no adapter execution

Unknown space
  -> NOT_FOUND envelope
  -> no adapter execution

Vector adapter unavailable
  -> FAILED Ask run with safe error
  -> no model generation

Model adapter unavailable after evidence
  -> FAILED or PARTIAL_FAILED Ask run
  -> evidence retained, safe no-answer shown
```

## Data Preservation Rules

- Source chunks are read-only in this flow.
- File item, Wiki page, graph node, graph edge, and review state are not mutated.
- Ask run records may reference model run and vector evidence ids, but do not copy raw source text.
- Safe question and answer summaries are bounded.

## Verification Touchpoints

| Flow | Verification |
|---|---|
| Approved evidence success | Backend integration and frontend E2E assert answer and evidence. |
| No approved evidence | Unit, API contract, and E2E assert no model generation and no-answer state. |
| Review-required inclusion | Unit/API/UI tests assert warning and separated evidence. |
| Adapter failure | Unit/API tests assert sanitized safe message. |
| Data immutability | Integration tests assert source/Wiki/graph/review records unchanged. |
