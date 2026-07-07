# Requirements: Ask Session Citations

## Status

Draft accepted by the autonomous goal preauthorization when the SDD gate passes.

## Slice Contract

| Field | Value |
|---|---|
| Slice | `ask-session-citations` |
| Wave | Wave 4 / Ask And Graph Productization |
| Goal | Preserve session-scoped Trusted Ask question, answer, citation snapshot, source trace, review eligibility, and safe evidence metadata for user inspection and later governance slices. |
| Phase | Phase 4 productization over existing Ask API-backed behavior |
| In scope | Ask sessions, session-scoped Ask answer records, safe citation snapshots, evidence labels, source locators, review eligibility, API read/write contracts, Trusted Ask session history/detail UI, tests, traceability, roadmap evidence. |
| Out of scope | Answer review governance, retrieval quality metrics, graph extraction, provider/model strategy changes, real company data, external cloud calls, auth/RBAC/audit/secret/rate-limit semantic changes, production prompt/cost/quota controls. |

## Requirements

| ID | Priority | Requirement | Acceptance |
|---|---|---|---|
| REQ-ASK-SESSION-CITATIONS-001 | Must | Atlas must group Trusted Ask runs into an Ask session scoped to a Knowledge Space. | A user can create an Ask run with an existing `sessionId` or receive a generated session when omitted. |
| REQ-ASK-SESSION-CITATIONS-002 | Must | Existing `POST /api/spaces/{spaceId}/ask` and `GET /api/ask-runs/{runId}` behavior must remain backward compatible. | Existing frontend and API tests can still create and read Ask runs without a session field. |
| REQ-ASK-SESSION-CITATIONS-003 | Must | Each answer response must expose session metadata. | Ask run responses include `sessionId`, session title, and session timestamps. |
| REQ-ASK-SESSION-CITATIONS-004 | Must | Each answer citation must preserve a safe snapshot of source trace fields. | Citation DTOs include source chunk id, file item id, source file label, page, section, locator, confidence, review status, and score. |
| REQ-ASK-SESSION-CITATIONS-005 | Must | Citations must expose review eligibility without treating unreviewed evidence as trusted. | Citations include `reviewEligible`, `citationStatus`, and `excludedReason`; review-required evidence is clearly marked. |
| REQ-ASK-SESSION-CITATIONS-006 | Must | Citations must not expose raw secrets, private paths, internal endpoints, raw source documents, raw provider payloads, or raw stack traces. | Safe text validation/sanitization is applied to session titles, evidence labels, source labels, and safe messages. |
| REQ-ASK-SESSION-CITATIONS-007 | Must | Users must be able to list recent Ask sessions for a Knowledge Space. | `GET /api/spaces/{spaceId}/ask-sessions` returns safe session summaries with answer counts and latest run state. |
| REQ-ASK-SESSION-CITATIONS-008 | Must | Users must be able to read a session with its answer history and citations. | `GET /api/ask-sessions/{sessionId}` returns session metadata and ordered Ask runs with citation snapshots. |
| REQ-ASK-SESSION-CITATIONS-009 | Must | Low-confidence, missing-source-trace, or review-required evidence must be excluded or clearly marked according to Ask policy. | Approved-only mode returns trusted citations only; include-review-required mode marks citations as `REVIEW_REQUIRED`. |
| REQ-ASK-SESSION-CITATIONS-010 | Should | Trusted Ask UI should show session-aware answer history and current answer citation detail. | The Ask surface shows recent session rows, selected session answer history, and citation detail with labels and review state. |
| REQ-ASK-SESSION-CITATIONS-011 | Must | API contract tests must cover session creation, session reuse, citation metadata, and session read behavior. | Backend integration tests verify create/read/list contracts and safe citation fields. |
| REQ-ASK-SESSION-CITATIONS-012 | Must | Traceability and roadmap docs must record implementation evidence and residual risks. | Slice traceability and roadmap status files are updated before closeout. |

## Assumptions

- Existing `AskRun` and `AskEvidence` are the correct foundation for answer and citation snapshots.
- Ask session persistence is additive and does not change provider, vector, auth, RBAC, audit, secret, or rate-limit behavior.
- Session title may be generated from the first safe question when the request does not provide one.

## Constraints

- Mock/sample-safe data only.
- No new external network calls or provider dependencies.
- Citation labels and source locators must be safe display strings, not raw document content.
- LLM-generated answers remain `REVIEW_REQUIRED` until a later governance slice approves them.
