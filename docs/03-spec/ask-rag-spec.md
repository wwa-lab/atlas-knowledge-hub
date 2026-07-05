# Specification: Ask RAG

## Status

Draft. Phase 4 hardening slice. Behavior source of truth for `ask-rag`.

## Overview

Ask RAG lets a user ask a question inside a Knowledge Space and receive a source-grounded answer from approved evidence. The slice hardens trust boundaries by keeping retrieval and generation behind adapters, refusing unsupported answers, preserving source trace and review status, and recording safe audit evidence.

## Source Stories

| Story | Capability |
|---|---|
| US-ASKRAG-001 | Scoped Ask UI and request behavior |
| US-ASKRAG-002 | Source-grounded answer and evidence display |
| US-ASKRAG-003 | No-evidence refusal behavior |
| US-ASKRAG-004 | Adapter-bound orchestration |
| US-ASKRAG-005 | Review-required output and audit evidence |
| US-ASKRAG-006 | Validation and safe failure behavior |

## Scope

In scope:

- Trusted Ask UI behavior for a selected Knowledge Space.
- `POST /api/spaces/{spaceId}/ask` and related read endpoint for Ask run evidence.
- Ask request validation and review policy enforcement.
- Vector evidence query through the vector adapter/service contract.
- Model answer generation through the model adapter/service contract.
- Ask run and evidence records preserving source trace, confidence, review status, score, model run id, safe answer summary, and requested-by.
- No-evidence, partial-evidence, review-required, validation, and adapter-failure states.
- Unit, integration, API contract, E2E, seam guard, dependency/network, and secret/private-path verification.

Out of scope:

- Real external model/vector provider calls, streaming, raw prompt retention, answer publication to Wiki, graph extraction, parser/converter/storage behavior, complete production SSO/RBAC setup, provider cost governance, and cross-space chat history.

## Constraints

- Default retrieval policy is `APPROVED_ONLY`.
- `INCLUDE_REVIEW_REQUIRED` is allowed only as an explicit request policy and must be visibly marked.
- Ask never mutates source chunk, file, Wiki, graph, or review status.
- Generated answer output is `REVIEW_REQUIRED`.
- Product layers do not call vector DBs, model providers, SDKs, CLIs, or external HTTP clients directly.
- Mock engines are required for automated verification; no network or credentials are required.
- Secrets, private paths, raw prompts, confidential source text, provider payloads, and stack traces are not returned or persisted.

## Actors

| Actor | Role |
|---|---|
| Knowledge user | Asks questions and reads trusted answers. |
| SME reviewer | Verifies answer evidence and review-required warnings. |
| Delivery lead | Uses Ask audit evidence to judge knowledge readiness. |
| Platform administrator | Verifies adapter health and safe configuration. |
| Codex implementation agent | Implements tasks strictly against this spec and `docs/06-tasks/ask-rag-tasks.md`. |

## Functional Requirements

### Ask Request

- **FR-ASKRAG-001:** Ask requests are scoped to one `spaceId`. (US-ASKRAG-001, REQ-ASKRAG-001)
- **FR-ASKRAG-002:** Request fields include `question`, `requestedBy`, optional `reviewPolicy`, optional `limit`, optional `filters`, and optional `mode`; `mode=mock` is used in automated tests. (US-ASKRAG-006, REQ-ASKRAG-008)
- **FR-ASKRAG-003:** Empty, oversized, unsafe, or cross-scope requests fail validation before adapter execution. (US-ASKRAG-006, REQ-ASKRAG-008)

### Retrieval Policy

- **FR-ASKRAG-004:** The default review policy is `APPROVED_ONLY`. (US-ASKRAG-002, REQ-ASKRAG-002)
- **FR-ASKRAG-005:** Review-required evidence is included only when `reviewPolicy=INCLUDE_REVIEW_REQUIRED` and is marked in the response and UI. (US-ASKRAG-002, REQ-ASKRAG-013)
- **FR-ASKRAG-006:** Retrieval results are bounded, sorted by score descending, and include stable tie-break by source chunk id. (US-ASKRAG-002, REQ-ASKRAG-003)

### Answer Generation

- **FR-ASKRAG-007:** If no approved evidence is available under the selected policy, Ask returns `NO_APPROVED_EVIDENCE` and does not call the model adapter for answer generation. (US-ASKRAG-003, REQ-ASKRAG-012)
- **FR-ASKRAG-008:** If evidence is available, Ask calls the model adapter with safe product references and bounded context descriptors, not raw provider payloads or private paths. (US-ASKRAG-004, REQ-ASKRAG-004)
- **FR-ASKRAG-009:** The generated answer response includes answer text or safe no-answer message, confidence, answer review status, safe message, and evidence references. (US-ASKRAG-002, REQ-ASKRAG-003)
- **FR-ASKRAG-010:** Generated answer review status is always `REVIEW_REQUIRED` until a future review workflow verifies it. (US-ASKRAG-005, REQ-ASKRAG-006)

### Evidence And Audit

- **FR-ASKRAG-011:** Ask run records preserve space id, status, question summary, requested-by, review policy, result limit, vector query reference, model run reference, timestamps, safe answer summary, and safe error. (US-ASKRAG-005, REQ-ASKRAG-009)
- **FR-ASKRAG-012:** Evidence records preserve source chunk id, file item id, source file, page, section, score, confidence, review status, and safe excerpt label when available. (US-ASKRAG-002, REQ-ASKRAG-005)
- **FR-ASKRAG-013:** Ask records must not store raw source text, raw prompts, raw vectors, provider payloads, secrets, private endpoints, private paths, or stack traces. (US-ASKRAG-005, REQ-ASKRAG-009)

### UI Behavior

- **FR-ASKRAG-014:** The Ask tab presents question input, submit affordance, answer panel, confidence badge, evidence list, policy note, review-required warning, no-evidence state, loading state, and safe error state. (US-ASKRAG-001, REQ-ASKRAG-010)
- **FR-ASKRAG-015:** Evidence rows show source file and page or section, score or confidence, and review status. (US-ASKRAG-002, REQ-ASKRAG-003)
- **FR-ASKRAG-016:** UI behavior remains aligned with the current FE baseline and is responsive without text overlap. (US-ASKRAG-001, REQ-ASKRAG-010)

### Failure Behavior

- **FR-ASKRAG-017:** Validation errors return `VALIDATION_ERROR`; unknown space returns `NOT_FOUND`; no evidence returns a successful no-answer payload with `status=NO_EVIDENCE`; adapter failure returns `FAILED` with a sanitized safe message. (US-ASKRAG-006, REQ-ASKRAG-007)
- **FR-ASKRAG-018:** Partial retrieval/model outcomes preserve successful evidence and mark the Ask run `PARTIAL_FAILED` only when a safe partial answer can be displayed. (US-ASKRAG-006, REQ-ASKRAG-007)

## State Model

Ask run status:

```text
REQUESTED -> RETRIEVING -> NO_EVIDENCE
                     \-> GENERATING -> SUCCEEDED
                                  \-> PARTIAL_FAILED
                                  \-> FAILED
```

Answer review status:

| Output | Review Status |
|---|---|
| Generated answer | `REVIEW_REQUIRED` |
| No-evidence response | `REVIEW_REQUIRED` |
| Failed run safe message | `REVIEW_REQUIRED` |

## API / Interface Surface

Full request/response details live in `docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md`.

| Interface | Behavior |
|---|---|
| `POST /api/spaces/{spaceId}/ask` | Validates question, retrieves evidence, optionally generates answer through model adapter, stores safe Ask run evidence, and returns answer payload. |
| `GET /api/ask-runs/{runId}` | Returns Ask run status, safe answer summary, evidence list, review policy, model run reference, and safe error. |
| Internal vector query interface | Returns bounded source evidence with score, confidence, and review status. |
| Internal model run interface | Generates safe mock answer text from evidence references and marks output review-required. |

## Acceptance Matrix

| Check | Requirements | Observable Result |
|---|---|---|
| AC-ASKRAG-01 | REQ-ASKRAG-001, 010 | UI and API scope all Ask requests to one Knowledge Space. |
| AC-ASKRAG-02 | REQ-ASKRAG-002, 013 | Approved-only is default; review-required evidence appears only by explicit policy and is flagged. |
| AC-ASKRAG-03 | REQ-ASKRAG-003, 005 | Answer payload and Ask run detail include source references, score/confidence, review status, and model/vector references. |
| AC-ASKRAG-04 | REQ-ASKRAG-004, 011 | Seam guard blocks direct model/vector provider calls outside adapter packages; tests use mock adapters. |
| AC-ASKRAG-05 | REQ-ASKRAG-006, 009 | Generated output is `REVIEW_REQUIRED`; source/Wiki/graph/review states are unchanged. |
| AC-ASKRAG-06 | REQ-ASKRAG-007, 008, 012 | Invalid/no-evidence/adapter-failure cases return safe envelopes and no raw internals. |
| AC-ASKRAG-07 | REQ-ASKRAG-014 | Task verification includes exact unit, integration, API, E2E, seam, diff, network, and secret scan commands. |

## Open Questions

- OQ-ASKRAG-001: Role-specific visibility of review-required evidence.
- OQ-ASKRAG-002: Future path for turning Ask answers into review queue items.
- OQ-ASKRAG-003: Whether reranking is a separate follow-up.

## Product Goal Batch 3 Vue Parity Addendum

Product Goal Batch 3 extends Trusted Ask into the real Vue product shell without invoking real model or vector providers.

| Phase | Vue Product Acceptance |
|---|---|
| Phase G Trusted Ask | The real Vue global Chat surface provides multi-Knowledge-Space context selection, a question input, model selector, answer panel, evidence citations, review-required warning, and no-approved-evidence refusal state using mock/sample-safe data. |

This addendum changes only the Vue product surface maturity. It does not add production RAG optimization, raw prompt persistence, raw vector/provider payload storage, real external model calls, or new backend/API behavior.
