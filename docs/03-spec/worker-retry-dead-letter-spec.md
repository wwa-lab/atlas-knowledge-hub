# Feature Specification: worker-retry-dead-letter

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07
Source stories: US-WORKER-RETRY-DEAD-LETTER-001 through US-WORKER-RETRY-DEAD-LETTER-005

## Overview

Atlas needs a local reliability contract for processing work that may fail after batch ingest, connector sync, or future async execution starts. This slice adds deterministic job attempt tracking, retry state transitions, terminal dead-letter handling, safe error snapshots, source trace preservation, inspection APIs, and a Processing Center operations UI without introducing a production queue or scheduled worker.

## Actors

- Operator: inspects failed jobs and performs safe local retry/acknowledge actions.
- Developer: verifies deterministic retry and dead-letter transitions.
- SME reviewer: uses source trace and review eligibility to understand whether failed output can be recovered.
- Platform administrator: understands retry policy limits without assuming production operations readiness.

## Functional Requirements

| ID | Spec Requirement |
|---|---|
| FR-WORKER-RETRY-DEAD-LETTER-001 | REQ-WORKER-RETRY-DEAD-LETTER-001: Represent worker jobs for batch ingest, connector sync, and future async processing without binding Atlas to a queue engine. |
| FR-WORKER-RETRY-DEAD-LETTER-002 | REQ-WORKER-RETRY-DEAD-LETTER-002: Track each job attempt with status, attempt number, timestamps, safe error snapshot, retryable classification, and source trace. |
| FR-WORKER-RETRY-DEAD-LETTER-003 | REQ-WORKER-RETRY-DEAD-LETTER-003: Apply a deterministic local retry policy with max attempts and fixed delay metadata. |
| FR-WORKER-RETRY-DEAD-LETTER-004 | REQ-WORKER-RETRY-DEAD-LETTER-004: Retryable failures before exhaustion move jobs to `WAITING_RETRY`. |
| FR-WORKER-RETRY-DEAD-LETTER-005 | REQ-WORKER-RETRY-DEAD-LETTER-005: Non-retryable failures or exhausted attempts move jobs to `DEAD_LETTERED` and create one dead-letter entry. |
| FR-WORKER-RETRY-DEAD-LETTER-006 | REQ-WORKER-RETRY-DEAD-LETTER-006: Dead-letter entries preserve safe error, attempt summary, source trace, created time, and operator status. |
| FR-WORKER-RETRY-DEAD-LETTER-007 | REQ-WORKER-RETRY-DEAD-LETTER-007: Safe error snapshots are sanitized through existing safe error patterns. |
| FR-WORKER-RETRY-DEAD-LETTER-008 | REQ-WORKER-RETRY-DEAD-LETTER-008: Source trace remains relative/mock-safe and is retained across job, attempt, and dead-letter records. |
| FR-WORKER-RETRY-DEAD-LETTER-009 | REQ-WORKER-RETRY-DEAD-LETTER-009: APIs expose failed/dead-letter list and detail through `ApiEnvelope`. |
| FR-WORKER-RETRY-DEAD-LETTER-010 | REQ-WORKER-RETRY-DEAD-LETTER-010: Manual retry and acknowledge use safe local v0 transitions. |
| FR-WORKER-RETRY-DEAD-LETTER-011 | REQ-WORKER-RETRY-DEAD-LETTER-011: Frontend renders failed/dead-letter jobs, attempts, safe error, source trace, and blocked/review states. |
| FR-WORKER-RETRY-DEAD-LETTER-012 | REQ-WORKER-RETRY-DEAD-LETTER-012: Tests cover retry, terminal failure, redaction, source trace, retry action, and acknowledge action. |
| FR-WORKER-RETRY-DEAD-LETTER-013 | REQ-WORKER-RETRY-DEAD-LETTER-013: Roadmap, traceability, tasks, and verification evidence stay aligned with implementation. |

## Status Model

Worker job status:

- `QUEUED`: job record exists and is eligible for work.
- `RUNNING`: an attempt is currently executing or being recorded.
- `WAITING_RETRY`: last attempt failed retryably and next retry metadata is available.
- `SUCCEEDED`: terminal successful state.
- `DEAD_LETTERED`: terminal failed state with a dead-letter entry.
- `ACKNOWLEDGED`: operator has acknowledged the terminal failure.

Attempt status:

- `RUNNING`
- `FAILED_RETRYABLE`
- `FAILED_TERMINAL`
- `SUCCEEDED`

Dead-letter status:

- `OPEN`
- `RETRIED`
- `ACKNOWLEDGED`

Safe error category:

- `NONE`
- `VALIDATION`
- `CONNECTOR_UNAVAILABLE`
- `UNSUPPORTED_SOURCE`
- `SOURCE_UNREADABLE`
- `RATE_LIMITED`
- `SAFE_SYSTEM`

## Retry Policy

- `maxAttempts`: 3.
- Delay schedule in seconds: attempt 1 failure -> 30, attempt 2 failure -> 120.
- A retryable failure with remaining attempts transitions to `WAITING_RETRY` and sets `nextRetryAt`.
- A non-retryable failure transitions directly to `DEAD_LETTERED`.
- A retryable failure at max attempts transitions to `DEAD_LETTERED`.
- The policy only records scheduling metadata; it does not run a scheduler or dispatch a real worker.

## Main Flow

1. A local workflow creates a worker job for batch ingest, connector sync, or future async processing.
2. The backend records an attempt as `RUNNING`.
3. The caller records success, retryable failure, or non-retryable failure.
4. For retryable failure before exhaustion, the job enters `WAITING_RETRY`.
5. For terminal failure, the job enters `DEAD_LETTERED` and exactly one dead-letter entry is created.
6. The operations API lists and returns dead-letter entries with attempts and source trace.
7. The frontend Processing Center shows failed/dead-letter jobs and safe details.
8. Operators may request local retry or acknowledge on `OPEN` entries.

## Manual Actions

Manual retry:

- Allowed only for `OPEN` dead-letter entries.
- Marks the entry `RETRIED`.
- Creates or resets the associated job into `WAITING_RETRY` with deterministic retry metadata.
- Repeated retry requests for the same entry return the same predictable state and do not create duplicate open entries.

Manual acknowledge:

- Allowed for `OPEN` or already `ACKNOWLEDGED` entries.
- Marks the entry and job `ACKNOWLEDGED`.
- Repeated acknowledge requests are idempotent.

## API Acceptance Matrix

| Check | Requirement | Observable result |
|---|---|---|
| AC-WORKER-RETRY-DEAD-LETTER-001 | REQ-WORKER-RETRY-DEAD-LETTER-001, 002 | Job detail includes job type, status, attempts, source trace, and created/updated timestamps. |
| AC-WORKER-RETRY-DEAD-LETTER-002 | REQ-WORKER-RETRY-DEAD-LETTER-003, 004 | Retryable failure before max attempts returns `WAITING_RETRY` and next retry metadata. |
| AC-WORKER-RETRY-DEAD-LETTER-003 | REQ-WORKER-RETRY-DEAD-LETTER-005, 006 | Terminal failure creates exactly one `OPEN` dead-letter entry. |
| AC-WORKER-RETRY-DEAD-LETTER-004 | REQ-WORKER-RETRY-DEAD-LETTER-007 | Unsafe error strings are redacted from persisted snapshots and API responses. |
| AC-WORKER-RETRY-DEAD-LETTER-005 | REQ-WORKER-RETRY-DEAD-LETTER-008 | Source trace remains relative/mock-safe in job, attempt, dead-letter, and UI detail. |
| AC-WORKER-RETRY-DEAD-LETTER-006 | REQ-WORKER-RETRY-DEAD-LETTER-009 | List/detail APIs use `ApiEnvelope` and safe error semantics. |
| AC-WORKER-RETRY-DEAD-LETTER-007 | REQ-WORKER-RETRY-DEAD-LETTER-010 | Manual retry and acknowledge transitions are deterministic and idempotent where specified. |
| AC-WORKER-RETRY-DEAD-LETTER-008 | REQ-WORKER-RETRY-DEAD-LETTER-011 | UI displays failed/dead-letter list and detail without raw internals. |
| AC-WORKER-RETRY-DEAD-LETTER-009 | REQ-WORKER-RETRY-DEAD-LETTER-012, 013 | Backend/frontend tests, scans, SDD gate, and closeout gate pass. |

## Edge States

- Unknown dead-letter or job IDs return safe `NOT_FOUND`.
- Invalid action state returns safe `CONFLICT`.
- Empty source trace renders as unavailable, not trusted.
- Retry after acknowledgement is rejected with safe `CONFLICT`.
- Unsafe error input is sanitized before persistence and before response mapping.

## Non-Functional Requirements

- Security: no raw exception, stack trace, secret, token, cookie, API key, private path, internal endpoint, or real source content in API or UI.
- Reliability: local deterministic state transitions are testable and repeatable.
- Auditability foundation: operator action metadata is captured locally but does not claim production audit readiness.
- Environment: no external queue, no scheduler, no external network, no real provider credential.

## Open Questions

None blocking under the preauthorized local v0 scope.
