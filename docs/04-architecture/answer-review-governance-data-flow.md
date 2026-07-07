# Data Flow: Answer Review Governance

Last updated: 2026-07-07

## Create Ask Answer

```text
Question request
  → AskService validates request
  → VectorService returns eligible evidence
  → ModelService returns safe generated output
  → AskRun.complete(..., answerReviewStatus=REVIEW_REQUIRED)
  → AskMapper returns answer governance + evidence citations
```

## Review Answer

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

## Field Flow

| Input | Domain | Response |
|---|---|---|
| `status` | `AskRun.answerReviewStatus` | `answerReviewStatus` |
| `reviewer` | `AskRun.answerReviewedBy` | `answerReviewedBy` |
| `reason` | `AskRun.answerReviewReason` | `answerReviewReason` |
| service clock | `AskRun.answerReviewedAt` | `answerReviewedAt` |
| eligible evidence count + answer text | computed | `answerReusable` |

## Safe Text Flow

Reviewer and reason strings pass through service validation. Values containing secret markers, URLs, JDBC strings, private absolute paths, or network paths are rejected with safe `VALIDATION_FAILED` fields before persistence.

## Preservation Rule

Reviewing an answer updates only answer governance fields on `ask_run`. It does not update `ask_evidence`, source chunks, files, Wiki pages, graph nodes, model outputs, or adapter configuration.
