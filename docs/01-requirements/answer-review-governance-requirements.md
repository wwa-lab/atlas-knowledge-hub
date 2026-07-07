# Requirements: Answer Review Governance

Last updated: 2026-07-07
Slice: `answer-review-governance`
Wave: Wave 4 / Ask And Graph Productization
Status: Accepted by goal preauthorization for implementation

## Goal

Trusted Ask answers must expose a dedicated governance state so users can distinguish model-generated draft answers from reviewed answers, rejected answers, and answers that need revision. The slice must preserve Ask evidence, source trace, confidence, and existing review/publish behavior while staying mock/sample-safe.

## Scope

- Define an Ask-answer-specific status model: `REVIEW_REQUIRED`, `APPROVED`, `REJECTED`, `NEEDS_REVISION`.
- Persist and expose review metadata: reviewer, reason, reviewed timestamp, reusable-knowledge eligibility, and reviewer-safe display fields.
- Add a stable API contract for creating and reading answer governance state.
- Update Trusted Ask UI to show the governance status and avoid implying unreviewed answers are trusted.
- Add backend unit tests, backend API contract tests, frontend tests, and traceability evidence.

## Out Of Scope

- `ask-session-citations`, retrieval quality metrics, graph extraction, provider/model adapter strategy changes, production approval workflow, legal/compliance sign-off, notifications, assignment queues, SLA, real company data, and external cloud/provider calls.
- Reusing `REJECTED`, `REVIEW_REQUIRED`, low-confidence, missing-source-trace, or `NEEDS_REVISION` answers as approved reusable knowledge.
- Changing production auth/RBAC/audit/secret/rate-limit semantics.

## Requirements

| ID | Requirement | Priority | Acceptance |
|---|---|---|---|
| REQ-ANSWER-REVIEW-GOVERNANCE-001 | Ask answers must carry an answer-specific governance status separate from source document and evidence review status. | Must | API responses distinguish `REVIEW_REQUIRED`, `APPROVED`, `REJECTED`, and `NEEDS_REVISION`. |
| REQ-ANSWER-REVIEW-GOVERNANCE-002 | Newly generated model answers must default to `REVIEW_REQUIRED`. | Must | Create Ask run returns a review-required answer even when generation succeeds. |
| REQ-ANSWER-REVIEW-GOVERNANCE-003 | Review actions must persist reviewer, reason, reviewed timestamp, and safe reuse eligibility. | Must | Review API response and read API response expose safe metadata only. |
| REQ-ANSWER-REVIEW-GOVERNANCE-004 | Only approved answers with eligible evidence can be marked reusable knowledge. | Must | Rejected, review-required, needs-revision, no-evidence, missing-answer, or missing-evidence answers are not reusable. |
| REQ-ANSWER-REVIEW-GOVERNANCE-005 | Answer governance must preserve Ask evidence, source trace, confidence, and model run metadata. | Must | Review updates do not mutate evidence rows or hide citations. |
| REQ-ANSWER-REVIEW-GOVERNANCE-006 | API and UI fields must be reviewer-safe. | Must | Responses do not expose raw secrets, private paths, raw provider payloads, raw stack traces, internal endpoints, or raw source documents. |
| REQ-ANSWER-REVIEW-GOVERNANCE-007 | Trusted Ask UI must clearly state whether an answer is reviewed, rejected, needs revision, or review-required. | Must | UI tests verify governance label, reason, reuse hint, and citations. |
| REQ-ANSWER-REVIEW-GOVERNANCE-008 | Existing Ask and citation behavior must not regress. | Must | Existing Ask tests and API-backed frontend tests still pass. |

## Assumptions

- Existing prototype auth path policy remains the authorization boundary; this slice adds no production RBAC semantics.
- `NEEDS_REVISION` is the Ask-answer governance term for revision-needed answers and does not replace the document review `NEED_FIX` state.
- Review reasons are stored as user-safe text and validated/sanitized at the service boundary.

## Verification

- `cd backend && mvn verify`
- `cd frontend && npm run typecheck`
- `cd frontend && npm run test`
- `cd frontend && npm run build`
- `npm run agent:check-sdd -- --slice answer-review-governance`
- `npm run agent:closeout`
- `git diff --check`
