# Design: Answer Review Governance

Last updated: 2026-07-07

## Design Scope

Implement answer governance for existing Trusted Ask runs using additive backend, API, and frontend changes. The design does not add production workflow queues, notification, legal sign-off, provider changes, or answer reuse indexing.

## Backend Design

| Module | Design |
|---|---|
| `AnswerReviewStatus` enum | Dedicated enum for Ask answer governance. |
| `AskRun` | Store status, reason, reviewer, and reviewed timestamp; default generated answers to `REVIEW_REQUIRED`. |
| `ReviewAskAnswerRequest` | Request DTO with `status`, `reviewer`, `reason`. |
| `AskService.reviewAnswer` | Validate safe text, validate run belongs to space, reject invalid approvals, update governance fields. |
| `AskMapper` | Add reviewer-safe metadata, `answerReviewLabel`, and `answerReusable`. |
| Flyway | Add columns and status constraint. |

## API Design

- `POST /api/spaces/{spaceId}/ask/{runId}/review-actions`
- Request body:

```json
{
  "status": "APPROVED",
  "reviewer": "sme.alex",
  "reason": "Answer is supported by approved evidence."
}
```

- Response: existing `AskRunResponse` plus governance fields.
- Validation errors use existing `ApiEnvelope` safe error shape.

## Frontend Design

| Area | Design |
|---|---|
| Types | Add `ApiAnswerReviewStatus`; keep `ApiReviewStatus` for evidence/document state. |
| API client | Add `reviewAskAnswer(spaceId, runId, payload)`. |
| Trusted Ask answer computed state | Derive label/reuse hint/reason from answer governance fields. |
| Product Ask panel | Show answer status, governance label, reviewer-safe reason, reusable hint, and citations. |
| Space Ask tab | Show the same governance hint for API-backed Ask results. |

## Validation And Error Handling

- `status` is required.
- `reviewer` is required and safe.
- `reason` is required for `REJECTED` and `NEEDS_REVISION`.
- Approval requires terminal `SUCCEEDED` or `PARTIAL_FAILED`, answer text, and evidence.
- Unsafe text fails with `VALIDATION_FAILED` and safe field messages.

## Testing Design

- Backend domain test: generated answer defaults to review-required; review transitions persist metadata.
- Backend service test: invalid approvals and unsafe reasons fail before persistence.
- Backend integration test: review API updates status, preserves citations, and returns safe fields.
- Frontend tests: Trusted Ask displays governance label/reason/reuse hint; approved and rejected states are distinguishable.

## Residual Risk

Future answer reuse and retrieval quality metrics are intentionally deferred; this slice only creates the safe governance contract needed by those later slices.
