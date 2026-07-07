# 数据流：Answer Review Governance

最后更新：2026-07-07

## 创建 Ask Answer

```text
Question request
  → AskService validates request
  → VectorService returns eligible evidence
  → ModelService returns safe generated output
  → AskRun.complete(..., answerReviewStatus=REVIEW_REQUIRED)
  → AskMapper returns answer governance + evidence citations
```

## 审核 Answer

```text
Review request
  → AskController receives /api/spaces/{spaceId}/ask/{runId}/review-actions
  → AskService loads AskRun and AskEvidence
  → Validate run belongs to space
  → Validate status, reviewer, reason, and approval eligibility
  → AskRun.reviewAnswer(status, reviewer, reason, reviewedAt)
  → AskMapper computes answerReusable and safe label
  → API returns updated AskRunResponse
```

## 字段流

| Input | Domain | Response |
|---|---|---|
| `status` | `AskRun.answerReviewStatus` | `answerReviewStatus` |
| `reviewer` | `AskRun.answerReviewedBy` | `answerReviewedBy` |
| `reason` | `AskRun.answerReviewReason` | `answerReviewReason` |
| service clock | `AskRun.answerReviewedAt` | `answerReviewedAt` |
| eligible evidence count + answer text | computed | `answerReusable` |

## 安全文本流

Reviewer 和 reason 字符串经过 service validation。包含 secret markers、URLs、JDBC strings、private absolute paths 或 network paths 的值会在持久化前以安全 `VALIDATION_FAILED` fields 拒绝。

## 保留规则

审核 answer 只更新 `ask_run` 上的 answer governance fields。它不更新 `ask_evidence`、source chunks、files、Wiki pages、graph nodes、model outputs 或 adapter configuration。
