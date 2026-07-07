# Design: worker-retry-dead-letter

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07

## Overview

The design adds a small backend domain for local retry/dead-letter state and extends the Processing Center UI with an operations panel. It follows existing Spring Boot API envelope patterns and Vue mock/API-backed data patterns.

## Backend Design

### Domain

- `WorkerJob`: owns job identity, job type, subject, status, retry metadata, safe latest error, and source trace.
- `WorkerJobAttempt`: append-only attempt history for a job.
- `DeadLetterEntry`: terminal failure inspection and operator action record.
- `LocalRetryPolicy`: deterministic max attempts and delay calculation.
- `WorkerJobService`: transition coordinator; controllers delegate all workflow logic to it.

### API

- `GET /api/worker-jobs/failed`
- `GET /api/worker-jobs/{jobId}`
- `GET /api/dead-letter-entries`
- `GET /api/dead-letter-entries/{entryId}`
- `POST /api/dead-letter-entries/{entryId}/retry`
- `POST /api/dead-letter-entries/{entryId}/acknowledge`

### Error Handling

- Use existing `SafeErrorSanitizer` for all stored and returned safe messages.
- Use existing safe codes where applicable: `VALIDATION_FAILED`, `NOT_FOUND`, `CONFLICT`, `SAFE_SYSTEM_ERROR`.
- Worker-specific safe categories remain domain fields, not a competing API error envelope.

### Seed / Fixture Behavior

The backend may seed one mock dead-letter scenario for local UI/API inspection if needed by existing product tests. Seed data must use sample-safe IDs and relative source traces only.

## Frontend Design

### Processing Center Surface

The UI adds an operations section or panel with:

- Failed/dead-letter job list.
- Status and safe category badges.
- Attempt timeline.
- Source trace panel.
- Safe error snapshot panel.
- Review eligibility / blocked state indicator.
- Retry and acknowledge controls when backend supports the action.

### UI Rules

- Do not show raw stack traces, private paths, raw exception names, internal endpoints, tokens, cookies, or API keys.
- Empty source trace shows a safe unavailable state.
- Acknowledged entries remain visible as resolved operations records.
- Retry action updates the selected detail predictably.

## Test Design

Backend:

- Retryable failure before max attempts.
- Retry exhaustion creates one dead-letter entry.
- Non-retryable failure creates one dead-letter entry.
- Unsafe error text is redacted.
- Manual retry and acknowledge transitions are safe.
- API contract tests cover list/detail/actions and safe errors.

Frontend:

- Type checks for worker/dead-letter models.
- Component/data tests verify list/detail mapping and safe display.
- Build verifies no runtime type drift.

## Risks / Tradeoffs

- Local metadata does not prove production worker recovery.
- Manual retry does not dispatch real work; it records a safe local recovery foundation.
- Operator action metadata is not a production audit log.

## Open Questions

None blocking under the preauthorized local v0 scope.
