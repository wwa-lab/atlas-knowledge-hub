# User Stories: Ask Session Citations

## User Story 1

**ID:** US-ASK-SESSION-CITATIONS-001  
**Title:** Create and continue an Ask session

**Story:**  
As a knowledge user,  
I want each Trusted Ask question to belong to a visible session,  
so that I can return to a sequence of related answers and evidence.

## Acceptance Criteria

1. **Given** I ask a question without a `sessionId`  
   **When** the Ask API accepts the request  
   **Then** it creates a session for the Knowledge Space and returns the session id.
2. **Given** I ask another question with an existing session id  
   **When** the session belongs to the same Knowledge Space  
   **Then** the new answer is attached to that session.
3. **Given** a session id from another Knowledge Space  
   **When** I use it in an Ask request  
   **Then** the API returns a safe validation error.

## Notes / Assumptions

- Session creation remains local and deterministic.

## Dependencies

- Existing `ask-rag` Ask create/read API.

## Out of Scope

- Collaboration, sharing, permissions, and production session retention policy.

## Open Questions

- None.

---

## User Story 2

**ID:** US-ASK-SESSION-CITATIONS-002  
**Title:** Inspect answer citation snapshots

**Story:**  
As a reviewer or knowledge user,  
I want each answer citation to show source trace, confidence, and review status,  
so that I can judge whether the answer is safe to rely on.

## Acceptance Criteria

1. **Given** an answer has eligible evidence  
   **When** I read the Ask run or session  
   **Then** each citation includes safe label, source chunk, file id, page/section locator, confidence, review status, and score.
2. **Given** evidence is review-required and explicitly included  
   **When** the citation is returned  
   **Then** it is marked `REVIEW_REQUIRED` and `reviewEligible=false`.
3. **Given** a citation source contains unsafe text  
   **When** the citation snapshot is created  
   **Then** unsafe raw values are rejected or redacted into safe labels.

## Notes / Assumptions

- Citation snapshots extend existing `AskEvidence` records.

## Dependencies

- Existing vector query result fields and review-aware retrieval policy.

## Out of Scope

- Storing raw source snippets or provider payloads.

## Open Questions

- None.

---

## User Story 3

**ID:** US-ASK-SESSION-CITATIONS-003  
**Title:** Review session history in the UI

**Story:**  
As a knowledge user,  
I want the Trusted Ask screen to show session history and citation detail,  
so that I can revisit previous answers without losing their evidence context.

## Acceptance Criteria

1. **Given** recent sessions exist for a Knowledge Space  
   **When** I open Trusted Ask  
   **Then** I can see session summaries with title, answer count, latest status, and time.
2. **Given** I select a session  
   **When** its detail loads  
   **Then** I can inspect ordered answers and citations.
3. **Given** session APIs fail safely  
   **When** the UI catches the error  
   **Then** it shows a safe fallback message without exposing internals.

## Notes / Assumptions

- The UI remains compatible with mock E2E fixtures.

## Dependencies

- Session list/detail APIs and frontend API client.

## Out of Scope

- Full conversation composer redesign.

## Open Questions

- None.

---

## User Story 4

**ID:** US-ASK-SESSION-CITATIONS-004  
**Title:** Preserve existing Ask behavior

**Story:**  
As an Atlas maintainer,  
I want existing Ask tests and flows to continue working,  
so that session citations do not regress current product behavior.

## Acceptance Criteria

1. **Given** existing code posts an Ask request without session fields  
   **When** it runs against the updated API  
   **Then** the request still succeeds.
2. **Given** existing UI reads `evidence` from an Ask run  
   **When** richer citation fields are added  
   **Then** existing fields remain present and typed.
3. **Given** backend verification runs  
   **When** contract tests execute  
   **Then** both legacy create/read and new session read behavior pass.

## Notes / Assumptions

- Additive fields are preferred over renaming existing response fields.

## Dependencies

- Existing AskService and frontend API surfaces.

## Out of Scope

- Removing or versioning the existing Ask endpoints.

## Open Questions

- None.
