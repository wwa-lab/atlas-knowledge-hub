# Data Flow: Knowledge Graph

## Overview

This flow describes how approved Atlas knowledge becomes graph data and how users inspect it without exposing unreviewed or unsafe source material.

## Projection Flow

1. Read approved/published Wiki pages and source chunks for a knowledge space.
2. Validate eligibility: review status, source trace presence, confidence policy, safe relative source references.
3. Pass eligible descriptors to the graph projection boundary.
4. Create or refresh stable graph nodes and evidence-backed edges.
5. Persist projection run summary, item outcomes, graph records, and audit evidence.
6. Exclude unsafe/unapproved records with safe reason codes.

## Query Flow

1. Frontend requests graph view with type/search/review/evidence filters.
2. Backend validates access, pagination/limits, and filter values.
3. Query service loads bounded nodes/edges and counts.
4. Response maps graph records to safe DTOs with evidence summaries.
5. Frontend renders graph, empty/error/unauthorized states, and selected detail.

## Review Flow

1. SME selects a graph edge with evidence.
2. SME submits review action and comment.
3. Backend validates authorization, target edge, action, and comment safety.
4. Graph edge review status is updated.
5. Append-only review/audit record is written.

## State Transitions

```text
ProjectionRun: REQUESTED -> RUNNING -> SUCCEEDED
ProjectionRun: REQUESTED -> RUNNING -> PARTIAL_FAILED
ProjectionRun: REQUESTED -> RUNNING -> FAILED

GraphEdge: REVIEW_REQUIRED -> APPROVED
GraphEdge: REVIEW_REQUIRED -> NEED_FIX
GraphEdge: REVIEW_REQUIRED -> OCR_REQUIRED
GraphEdge: APPROVED -> PUBLISHED
```

## Error And Exclusion Codes

| Code | Meaning |
|---|---|
| `UNAPPROVED_SOURCE` | Source is not approved/published. |
| `MISSING_SOURCE_TRACE` | Required trace reference is absent. |
| `LOW_CONFIDENCE_UNAPPROVED` | Confidence is low and no SME approval exists. |
| `UNSAFE_REFERENCE` | Path/reference violates safety rules. |
| `NO_EVIDENCE` | Candidate edge lacks source evidence. |
| `ADAPTER_MISCONFIGURED` | Projection boundary is not configured safely. |

## Verification Hooks

- Unit tests for eligibility and exclusion logic.
- Integration tests for projection run persistence and item outcomes.
- API tests for graph view/detail/review action flows.
- E2E tests for Graph tab inspectable evidence behavior.
