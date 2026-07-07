# User Stories: Answer Review Governance

Last updated: 2026-07-07

## US-ANSWER-REVIEW-GOVERNANCE-001: See Answer Trust State

As a Trusted Ask user,  
I want every answer to show whether it is review-required, approved, rejected, or needs revision,  
so that I do not mistake a generated draft for trusted reusable knowledge.

### Acceptance Criteria

1. Given a generated Ask answer, when I read the response, then its status is `REVIEW_REQUIRED` unless a reviewer has changed it.
2. Given a reviewed answer, when I read the response, then the response includes reviewer-safe metadata and a plain trust label.
3. Given a non-approved answer, when I view it in the UI, then the UI warns that it is not approved reusable knowledge.

## US-ANSWER-REVIEW-GOVERNANCE-002: Review An Answer Safely

As an SME reviewer,  
I want to mark an Ask answer as approved, rejected, or needs revision with a reason,  
so that downstream users can understand whether and why the answer may be reused.

### Acceptance Criteria

1. Given an existing Ask run, when I submit a valid review status and reason, then the status and metadata are persisted.
2. Given an approval attempt on an answer without answer text or evidence, when I submit the review, then the system rejects the approval safely.
3. Given unsafe reviewer or reason text, when I submit the review, then the request fails before persistence and does not leak raw input.

## US-ANSWER-REVIEW-GOVERNANCE-003: Preserve Evidence And Source Trace

As a delivery lead,  
I want answer governance updates to preserve citations, source trace, confidence, and model run metadata,  
so that answer review does not erase the evidence needed for trust decisions.

### Acceptance Criteria

1. Given a reviewed answer, when I fetch the Ask run, then citations and evidence review statuses are still present.
2. Given a rejected or needs-revision answer, when I inspect it, then evidence remains visible but the answer is not reusable.
3. Given existing Ask behavior, when this slice is implemented, then retrieval and citation display continue to work.

## US-ANSWER-REVIEW-GOVERNANCE-004: Avoid Unsafe Reuse

As a knowledge governance owner,  
I want only approved answer governance states to be reusable,  
so that unreviewed or rejected generated content does not become trusted knowledge.

### Acceptance Criteria

1. Given `REVIEW_REQUIRED`, `REJECTED`, or `NEEDS_REVISION`, when the API returns the answer, then `answerReusable` is false.
2. Given `APPROVED` with answer text and eligible evidence, when the API returns the answer, then `answerReusable` is true.
3. Given no-evidence or failed Ask runs, when reviewed, then they cannot be marked approved reusable knowledge.

## Dependencies

- Existing Ask run persistence and API.
- Existing vector/model adapter boundaries.
- Existing Trusted Ask frontend surface.

## Out Of Scope

Production assignment queues, notifications, legal approval, `ask-session-citations`, retrieval metrics, graph extraction, and provider strategy changes.
