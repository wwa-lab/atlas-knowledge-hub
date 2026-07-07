# Answer Review Governance API Implementation Guide

Last updated: 2026-07-07
Base path: `/api`

## Response Envelope

All endpoints use `ApiEnvelope<T>`.

## Status Reference

`AnswerReviewStatus`: `REVIEW_REQUIRED`, `APPROVED`, `REJECTED`, `NEEDS_REVISION`

## Endpoint Summary

| Operation | Method | Endpoint |
|---|---|---|
| Create Ask run | POST | `/spaces/{spaceId}/ask` |
| Read Ask run | GET | `/ask-runs/{runId}` |
| Review Ask answer | POST | `/spaces/{spaceId}/ask/{runId}/review-actions` |

## Review Ask Answer

### Request

```json
{
  "status": "APPROVED",
  "reviewer": "sme.alex",
  "reason": "Answer is supported by approved evidence."
}
```

### Response Data

```json
{
  "runId": "ask-123",
  "spaceId": "ibm-i-modernization",
  "status": "SUCCEEDED",
  "answerReviewStatus": "APPROVED",
  "answerReviewLabel": "Approved answer",
  "answerReviewReason": "Answer is supported by approved evidence.",
  "answerReviewedBy": "sme.alex",
  "answerReviewedAt": "2026-07-07T00:00:00Z",
  "answerReusable": true,
  "evidence": [
    {
      "sourceType": "WIKI_PAGE",
      "citationStatus": "ELIGIBLE"
    }
  ]
}
```

## Validation

| Field | Rule |
|---|---|
| `status` | Required answer governance enum. |
| `reviewer` | Required safe text. |
| `reason` | Required for `REJECTED` and `NEEDS_REVISION`; safe text when present. |
| approval | Run must belong to `spaceId`, be `SUCCEEDED` or `PARTIAL_FAILED`, have answer text, and have eligible evidence. |

## Error Cases

| HTTP | Code | When |
|---:|---|---|
| 400 | `VALIDATION_FAILED` | Invalid status/reviewer/reason or invalid approval. |
| 404 | `NOT_FOUND` | Ask run does not exist or does not belong to the space. |

## Safety

Do not return raw provider payloads, raw source documents, raw stack traces, private paths, internal endpoints, API keys, passwords, tokens, or confidential content.
