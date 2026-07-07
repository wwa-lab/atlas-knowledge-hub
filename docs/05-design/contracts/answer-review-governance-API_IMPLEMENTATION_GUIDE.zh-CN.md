# Answer Review Governance API 实现指南

最后更新：2026-07-07
Base path：`/api`

## Response Envelope

所有 endpoints 使用 `ApiEnvelope<T>`。

## 状态参考

`AnswerReviewStatus`：`REVIEW_REQUIRED`、`APPROVED`、`REJECTED`、`NEEDS_REVISION`

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

## 校验

| Field | Rule |
|---|---|
| `status` | 必填 answer governance enum。 |
| `reviewer` | 必填 safe text。 |
| `reason` | `REJECTED` 和 `NEEDS_REVISION` 必填；存在时必须为 safe text。 |
| approval | Run 必须属于 `spaceId`，状态为 `SUCCEEDED` 或 `PARTIAL_FAILED`，有 answer text 且有 eligible evidence。 |

## Error Cases

| HTTP | Code | When |
|---:|---|---|
| 400 | `VALIDATION_FAILED` | Invalid status/reviewer/reason 或 invalid approval。 |
| 404 | `NOT_FOUND` | Ask run 不存在或不属于该 space。 |

## 安全

不得返回 raw provider payloads、raw source documents、raw stack traces、private paths、internal endpoints、API keys、passwords、tokens 或 confidential content。
