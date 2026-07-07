# Ask Session Citations - API Implementation Guide

Date: 2026-07-07  
Base path: `/api`  
Backend stack: Spring Boot, PostgreSQL, Flyway  
Response envelope: `ApiEnvelope<T>`

## Endpoints

### POST `/spaces/{spaceId}/ask`

Extends existing request body with optional session fields.

```json
{
  "question": "What evidence supports the published Wiki page?",
  "requestedBy": "p0-browser",
  "reviewPolicy": "APPROVED_ONLY",
  "limit": 3,
  "mode": "mock",
  "sessionId": "ask-session-123",
  "sessionTitle": "Published Wiki evidence",
  "filters": {
    "fileItemIds": ["file-001"],
    "sourceTypes": ["pdf"]
  }
}
```

Compatibility: `sessionId` and `sessionTitle` are optional.

### GET `/ask-runs/{runId}`

Returns one run with session and citation fields.

```json
{
  "success": true,
  "data": {
    "runId": "ask-1",
    "sessionId": "ask-session-1",
    "sessionTitle": "Published Wiki evidence",
    "spaceId": "ibm-i-modernization",
    "question": "What evidence exists?",
    "status": "SUCCEEDED",
    "answerReviewStatus": "REVIEW_REQUIRED",
    "evidence": [
      {
        "evidenceId": "ask-ev-1",
        "citationId": "ask-cite-1",
        "sourceChunkId": "chunk-1",
        "fileItemId": "file-1",
        "sourceFile": "BRD_Methodology.pdf",
        "page": 12,
        "section": "Source Trace",
        "evidenceLabel": "BRD_Methodology.pdf page 12",
        "sourceLocator": "page 12 / Source Trace / chunk chunk-1",
        "reviewStatus": "APPROVED",
        "confidence": 0.93,
        "score": 0.88,
        "citationStatus": "ELIGIBLE",
        "reviewEligible": true,
        "excludedReason": null
      }
    ]
  }
}
```

### GET `/spaces/{spaceId}/ask-sessions`

Returns recent session summaries.

```json
{
  "success": true,
  "data": [
    {
      "sessionId": "ask-session-1",
      "spaceId": "ibm-i-modernization",
      "title": "Published Wiki evidence",
      "createdBy": "p0-browser",
      "answerCount": 2,
      "latestRunStatus": "SUCCEEDED",
      "latestAnswerReviewStatus": "REVIEW_REQUIRED",
      "createdAt": "2026-07-07T06:00:00Z",
      "updatedAt": "2026-07-07T06:05:00Z"
    }
  ]
}
```

### GET `/ask-sessions/{sessionId}`

Returns one session with ordered Ask runs.

```json
{
  "success": true,
  "data": {
    "sessionId": "ask-session-1",
    "spaceId": "ibm-i-modernization",
    "title": "Published Wiki evidence",
    "createdBy": "p0-browser",
    "runs": []
  }
}
```

## Validation

- `sessionTitle` uses the same safe text boundary as questions and requestedBy.
- `sessionId` must belong to the requested Knowledge Space when creating a run.
- Citation safe labels must not include raw secrets, raw endpoints, private paths, or raw source content.

## Error Handling

Use existing safe errors:

- `VALIDATION_ERROR` for cross-space session or unsafe fields.
- `NOT_FOUND` for unknown session on read.
- Existing Ask failure behavior for vector/model failures.

## Contract Tests

- Create ask without session -> generated session returned.
- Create second ask with session -> same session returned.
- List sessions -> includes summary.
- Get session -> includes ordered runs and citation metadata.
- Existing get run -> includes old and new fields.
