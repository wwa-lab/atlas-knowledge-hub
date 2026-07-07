# Detailed Design: Ask Session Citations

## Overview

This design adds session-aware Trusted Ask history and safe answer citations by extending the existing Ask domain. It uses additive backend DTOs, repositories, service methods, Flyway migration, and frontend typed rendering.

## Module Design

### Backend Domain

- `AskSession` stores one session per related question sequence.
- `AskRun` gains `sessionId`; new runs always belong to a session.
- `AskEvidence` gains citation fields used by API responses and UI.
- Citation status derivation is deterministic and based on review status, confidence, and source trace presence.

### Backend Service

- `AskService.createRun` validates optional `sessionId` and `sessionTitle`.
- When `sessionId` is absent, the service creates a session using a safe title.
- When `sessionId` is present, the service verifies same-space ownership before creating the run.
- `AskService.listSessions(spaceId)` returns bounded recent summaries.
- `AskService.getSession(sessionId)` returns session detail with ordered runs and citations.

### API / Interface Design

- Existing Ask endpoints remain compatible.
- New session endpoints return `ApiEnvelope`.
- All DTOs expose safe display fields only.

### Frontend Design

- Add TypeScript types for `ApiAskSessionSummary`, `ApiAskSessionDetail`, and richer `ApiAskEvidence`.
- Add frontend API helpers `listAskSessions` and `getAskSession`.
- Trusted Ask UI displays recent sessions beside or below the question composer depending on available width.
- Selected session detail displays answer history and citation detail.
- Current answer view continues to render immediately after ask submission.

## Validation And Safety Rules

- `sessionTitle`, `requestedBy`, citation labels, and source locators must reject or redact secret-like, URL-like, or private path-like text.
- Unknown or cross-space session ids fail safely.
- Missing source trace cannot be marked eligible.
- Generated answers remain `REVIEW_REQUIRED`.

## Testing Considerations

- Backend contract test creates a session implicitly, reuses it, lists it, and reads detail.
- Backend unit/domain tests cover citation status derivation and safe defaults.
- Frontend tests cover session/citation rendering.
- Existing Ask tests must continue to pass.

## Risks / Tradeoffs

- This slice stores safe labels, not raw source snippets; this limits preview richness but preserves data safety.
- Existing rows may lack session ids; read paths handle legacy rows while new writes create sessions.

## Open Questions

- None.
