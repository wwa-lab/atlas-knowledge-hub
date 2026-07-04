# User Stories: Full-Stack Productization

## Status

Draft for implementation. Derived from `req-to-user-story`.

## Stories

### US-FSP-001: Browse API-backed spaces

As a knowledge user, I want to see real Knowledge Spaces from the backend, so that I can enter the current product workflow from live metadata.

Acceptance criteria:

1. Given the frontend is open, when `GET /api/spaces` succeeds, then the user sees a space list from the API.
2. Given the spaces API is loading, empty, or failing, then the UI shows an explicit state.
3. Given a space card is selected, then the app loads `GET /api/spaces/{spaceId}`.

### US-FSP-002: Create a metadata-only sample batch

As a delivery lead, I want to create a safe sample batch from the browser, so that I can verify the upload-to-review loop without real company files.

Acceptance criteria:

1. Given a selected space, when the user starts sample upload, then the UI posts metadata-only inventory to `POST /api/spaces/{spaceId}/batches`.
2. Given the batch is created, then the UI loads batch files and source chunks through API calls.
3. Given creation fails, then the UI shows a safe error and does not imply a batch was created.

### US-FSP-003: Review and publish trusted Wiki

As an SME reviewer, I want to approve a file and publish it to Wiki, so that trusted knowledge becomes available downstream.

Acceptance criteria:

1. Given a review-required file with source trace, when the reviewer approves it, then `POST /api/files/{fileId}/reviews` records the review.
2. Given the file is approved, when the user publishes it, then `POST /api/files/{fileId}/publish` returns a `PUBLISHED` Wiki page.
3. Given Wiki pages are refreshed, then the published page appears in `GET /api/spaces/{spaceId}/wiki-pages`.

### US-FSP-004: Inspect graph evidence

As a knowledge consumer, I want the Graph tab to show API-backed evidence from the published file, so that I can verify relationships before trusting them.

Acceptance criteria:

1. Given a file was published, when graph projection is refreshed, then the graph API returns evidence-backed nodes or edges.
2. Given the user selects graph evidence, then source chunk id, source file, confidence, and review status are visible.
3. Given graph access fails, then the UI shows an error state instead of pretending mock data is live.

### US-FSP-005: Ask with citations

As a knowledge consumer, I want to ask a question against the selected space and see answer evidence, so that I can evaluate whether the answer is trustworthy.

Acceptance criteria:

1. Given approved/published evidence was indexed through the backend, when the user asks a question, then `POST /api/spaces/{spaceId}/ask` returns a stored Ask run.
2. Given an Ask run exists, then `GET /api/ask-runs/{runId}` returns the answer and citations.
3. Given the answer is generated, then the UI shows `REVIEW_REQUIRED` answer status and evidence rows.

### US-FSP-006: Disable unconnected features

As a user, I want unavailable controls to be clearly disabled, so that I do not mistake prototype affordances for working product features.

Acceptance criteria:

1. Given a UI feature is still mock-only or not connected, then its primary action is disabled and labeled coming soon.
2. Given the P0 flow is connected, then its actions remain enabled only when prerequisites are satisfied.
3. Given a user attempts an unavailable path, then the UI shows a non-destructive coming-soon message.

### US-FSP-007: Preserve verification layers

As a platform maintainer, I want existing E2E layers to remain intact, so that productization does not regress current acceptance gates.

Acceptance criteria:

1. Given existing commands are run, then first-layer, second-layer, provider-backed scripts retain their purpose and entrypoints.
2. Given the new UI-driven E2E runs, then it exercises browser actions instead of bypassing the frontend with API-only setup for the main loop.
3. Given verification finds skipped checks, then the final report names the skipped checks and reasons.
