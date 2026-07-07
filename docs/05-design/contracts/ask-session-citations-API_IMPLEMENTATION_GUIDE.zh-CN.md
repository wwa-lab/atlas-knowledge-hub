# Ask Session Citations - API 实现指南

Date: 2026-07-07  
Base path: `/api`  
Backend stack: Spring Boot, PostgreSQL, Flyway  
Response envelope: `ApiEnvelope<T>`

## Endpoints

### POST `/spaces/{spaceId}/ask`

在现有 request body 上增加可选 session fields。

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

兼容性：`sessionId` 和 `sessionTitle` 均为可选。

### GET `/ask-runs/{runId}`

返回带 session 和 citation fields 的单个 run。

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

返回 recent session summaries。

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

返回带 ordered Ask runs 的单个 session。

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

- `sessionTitle` 使用与 questions 和 requestedBy 相同的 safe text boundary。
- 创建 run 时，`sessionId` 必须属于请求 Knowledge Space。
- Citation safe labels 不得包含 raw secrets、raw endpoints、private paths 或 raw source content。

## Error Handling

使用现有 safe errors：

- 跨 space session 或 unsafe fields 返回 `VALIDATION_ERROR`。
- 读取 unknown session 返回 `NOT_FOUND`。
- Vector/model failures 复用现有 Ask failure behavior。

## Contract Tests

- 无 session 创建 ask -> 返回生成的 session。
- 使用 session 创建第二次 ask -> 返回同一 session。
- List sessions -> 包含 summary。
- Get session -> 包含 ordered runs 和 citation metadata。
- Existing get run -> 同时包含旧字段和新字段。
