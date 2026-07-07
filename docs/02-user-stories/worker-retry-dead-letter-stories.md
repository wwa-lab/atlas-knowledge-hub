# User Stories: worker-retry-dead-letter

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07

## User Story 1

**Title:** Inspect failed processing jobs safely

**Story:**
As an Atlas operator,
I want to inspect failed worker jobs and their safe error snapshots,
so that I can understand processing reliability issues without seeing secrets or raw internals.

### Acceptance Criteria

1. **Given** a failed job exists
   **When** the operator opens the dead-letter list
   **Then** the job appears with job type, status, safe error category, source trace, created time, and operator status.
2. **Given** a failure contained unsafe text
   **When** the API and UI expose the failure
   **Then** raw stack traces, private paths, tokens, cookies, API keys, and internal endpoints are absent.

### Notes / Assumptions
- Operator access is represented through existing local/mock-safe API behavior; this slice does not change production RBAC.

### Dependencies
- Existing `ApiEnvelope` and safe error patterns.

### Out of Scope
- Production alerting, SIEM, SSO/OIDC, and audit policy changes.

### Open Questions
- None blocking under the preauthorized local v0 scope.

## User Story 2

**Title:** Retry retryable failures deterministically

**Story:**
As an Atlas developer,
I want retryable failures to follow a deterministic local policy,
so that batch ingest, connector sync, and future async processing can be tested predictably.

### Acceptance Criteria

1. **Given** a retryable failure occurs before max attempts
   **When** the backend records the failure
   **Then** the job attempt count increments and the job enters `WAITING_RETRY` with `nextRetryAt` and `retryDelaySeconds`.
2. **Given** retry attempts are exhausted
   **When** the backend records the next failure
   **Then** the job becomes `DEAD_LETTERED` and creates one dead-letter entry.

### Notes / Assumptions
- The v0 policy uses a fixed local delay list and does not schedule production workers.

### Dependencies
- Worker job and attempt persistence.

### Out of Scope
- Real timers, distributed retry, external queue visibility, and exactly-once guarantees.

### Open Questions
- None.

## User Story 3

**Title:** Preserve source trace through dead-letter handling

**Story:**
As an SME reviewer,
I want failed processing records to preserve source trace,
so that I can map failures back to the originating file, connector item, or batch context.

### Acceptance Criteria

1. **Given** a job has source trace metadata
   **When** attempts and dead-letter records are created
   **Then** the records retain relative/mock-safe source identifiers and review eligibility.
2. **Given** a source trace is incomplete
   **When** the UI renders the dead-letter detail
   **Then** it shows a safe unavailable state rather than inventing a trusted source.

### Notes / Assumptions
- Source trace is stored as JSON-like text for local v0 and remains safe relative metadata.

### Dependencies
- Markdown standard and connector sync source trace conventions.

### Out of Scope
- Real document payload preview and raw source content.

### Open Questions
- None.

## User Story 4

**Title:** Retry or acknowledge dead-letter entries locally

**Story:**
As an Atlas operator,
I want to manually retry or acknowledge a dead-letter entry,
so that I can exercise a safe local recovery foundation before production operations exist.

### Acceptance Criteria

1. **Given** a dead-letter entry is open
   **When** the operator requests retry
   **Then** the backend creates a predictable retry job state and marks the entry `RETRIED` without duplicating active retries.
2. **Given** a dead-letter entry is open
   **When** the operator acknowledges it
   **Then** the entry becomes `ACKNOWLEDGED` and further acknowledge requests are idempotent.

### Notes / Assumptions
- Manual actions are local v0 transitions only and do not dispatch a real worker.

### Dependencies
- Dead-letter API and state model.

### Out of Scope
- Production authorization, audit retention, escalation workflow, or incident management.

### Open Questions
- None.

## User Story 5

**Title:** Verify and document reliability boundaries

**Story:**
As an Atlas maintainer,
I want SDD, tests, roadmap, and traceability to describe retry/dead-letter limits,
so that future production operations work starts from a clear contract.

### Acceptance Criteria

1. **Given** the slice is complete
   **When** closeout runs
   **Then** SDD, tasks, traceability, roadmap, and verification evidence match the implemented behavior.
2. **Given** scans run over changed files
   **When** the slice closes
   **Then** no secrets, private paths, real data, or new external network dependencies are introduced.

### Notes / Assumptions
- This slice may update roadmap status but must not claim production readiness.

### Dependencies
- Goal-loop closeout gate.

### Out of Scope
- Production deployment monitoring runbook.

### Open Questions
- None.
