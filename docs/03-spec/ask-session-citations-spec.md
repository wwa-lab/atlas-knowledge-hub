# Feature Specification: Ask Session Citations

> **Source stories:** US-ASK-SESSION-CITATIONS-001 through US-ASK-SESSION-CITATIONS-004  
> **Spec status:** Accepted by autonomous preauthorization after SDD gate  
> **Last updated:** 2026-07-07

## Overview

Ask Session Citations adds durable session grouping and richer answer citation snapshots to the existing Trusted Ask API. The slice preserves current Ask create/read behavior while adding session list/detail APIs and UI history/detail display.

## Actors

- **Knowledge user:** Asks questions and reviews answer evidence.
- **SME reviewer:** Inspects citation safety, review status, and source trace before trusting an answer.
- **Atlas maintainer:** Verifies contract compatibility and safety constraints.

## Scope

### In Scope

- Ask session persistence scoped by Knowledge Space.
- Backward-compatible Ask run creation with optional `sessionId` and `sessionTitle`.
- Session summaries and session detail APIs.
- Citation snapshot fields on Ask evidence responses.
- UI display for recent sessions, selected session answer history, and citation detail.
- Backend unit/integration/API contract tests and frontend type/unit/build tests.

### Out Of Scope

- Answer review governance workflow.
- Retrieval quality metrics.
- Graph extraction from Wiki.
- Provider/model adapter strategy changes.
- Auth/RBAC/audit/secret/rate-limit semantics changes.
- Production prompt tuning, cost guards, quotas, or billing controls.

## Functional Requirements

### Session Lifecycle

- **FR-01:** The Ask API shall create an `ask_session` when a request omits `sessionId`.
- **FR-02:** The Ask API shall attach a run to an existing session when `sessionId` belongs to the request Knowledge Space.
- **FR-03:** The Ask API shall reject cross-space or unknown session ids with a safe validation/not-found response.
- **FR-04:** Session title shall be a safe display string derived from request `sessionTitle` or the first question.

### Answer And Citation Contract

- **FR-05:** `AskRunResponse` shall include `sessionId`, `sessionTitle`, and existing answer fields.
- **FR-06:** `AskEvidenceResponse` shall retain existing evidence fields and add `citationId`, `evidenceLabel`, `sourceLocator`, `citationStatus`, `reviewEligible`, and `excludedReason`.
- **FR-07:** Approved or published evidence shall be `reviewEligible=true` with `citationStatus=ELIGIBLE`.
- **FR-08:** Review-required evidence included by explicit policy shall be `reviewEligible=false` with `citationStatus=REVIEW_REQUIRED`.
- **FR-09:** Evidence missing source trace shall not be treated as an eligible citation.

### Session Read APIs

- **FR-10:** `GET /api/spaces/{spaceId}/ask-sessions` shall return recent session summaries ordered by most recent update.
- **FR-11:** `GET /api/ask-sessions/{sessionId}` shall return session metadata and ordered Ask runs with citations.
- **FR-12:** Session APIs shall use the existing `ApiEnvelope` and safe error handling patterns.

### Frontend Behavior

- **FR-13:** Trusted Ask UI shall show recent session summaries for the selected Knowledge Space.
- **FR-14:** Trusted Ask UI shall allow selecting a session and show ordered answer history.
- **FR-15:** Citation detail shall show safe labels, locator, confidence, review status, and review eligibility.
- **FR-16:** UI error states shall use safe fallback messages and shall not display raw internal errors.

## Non-Functional Requirements

- **Security:** No raw secrets, tokens, private paths, internal endpoints, raw source content, or raw stack traces may be returned in session or citation responses.
- **Compatibility:** Existing Ask create/read clients must continue to work without new request fields.
- **Traceability:** Citation snapshots must preserve source chunk id, file item id, source label, page/section locator, confidence, review status, score, and creation time when available.
- **Review safety:** Generated answers remain `REVIEW_REQUIRED`; unreviewed evidence is marked or excluded.
- **No external calls:** This slice must not add provider, vector, model, parser, storage, or search calls outside existing adapter boundaries.

## API Surface

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/spaces/{spaceId}/ask` | Create Ask run, creating or reusing a session. |
| GET | `/api/ask-runs/{runId}` | Read one Ask run with session and citations. |
| GET | `/api/spaces/{spaceId}/ask-sessions` | List safe session summaries for a Knowledge Space. |
| GET | `/api/ask-sessions/{sessionId}` | Read one session with answer history and citations. |

## Acceptance Matrix

| Requirement | Observable Check |
|---|---|
| REQ-ASK-SESSION-CITATIONS-001 | API contract test creates a run without `sessionId` and receives generated session metadata. |
| REQ-ASK-SESSION-CITATIONS-002 | Existing Ask API and frontend tests continue to pass. |
| REQ-ASK-SESSION-CITATIONS-004 | Contract tests assert citation source chunk, page/section, label, locator, confidence, and review status. |
| REQ-ASK-SESSION-CITATIONS-005 | Contract tests assert `reviewEligible` and `citationStatus` for approved and review-required evidence. |
| REQ-ASK-SESSION-CITATIONS-007 | Contract tests assert session list response ordering and safe summary shape. |
| REQ-ASK-SESSION-CITATIONS-010 | Frontend tests assert session/citation labels render. |

## Risks

- Existing Ask test fixtures may need additive fields; keep fields optional in TypeScript where needed.
- Session title generation must remain safe without storing raw unsafe question text as a display label.

## Open Questions

- None.
