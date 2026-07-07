# Requirements: worker-retry-dead-letter

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07
Wave: Wave 5 / Connector And Operations

## Goal

Establish Atlas' local, deterministic, observable retry and dead-letter reliability contract for batch ingest, connector sync, and future async processing without introducing a production queue, distributed worker cluster, scheduled worker, real connector, external API, real credentials, or company data.

## Scope

In scope:

- Worker job, job attempt, retry policy, retry schedule metadata, terminal failure classification, dead-letter entry, safe error snapshot, source trace, operator action metadata, and review/processing eligibility.
- Local deterministic retry transitions that can be exercised through backend services and API contract tests.
- Safe dead-letter inspection APIs and a frontend Processing Center operations surface.
- Manual retry and acknowledge as local v0 state transitions only.
- Redaction through the existing safe error pattern.

Out of scope:

- Kafka, RabbitMQ, SQS, PubSub, Redis Queue, real scheduled workers, distributed worker clusters, exactly-once delivery, saga orchestration, real connector/provider calls, production auth/RBAC/audit/secret-manager changes, destructive migrations, raw stack traces, raw exception messages, private paths, tokens, cookies, API keys, and real company data.

## Requirements

| ID | Requirement | Priority |
|---|---|---|
| REQ-WORKER-RETRY-DEAD-LETTER-001 | Atlas must define a worker job model that can represent batch ingest, connector sync, and future async processing jobs without binding to one queue engine. | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-002 | Each worker job must track attempts with attempt number, status, timestamps, safe error snapshot, retryable classification, and source trace. | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-003 | Retry policy must be deterministic and local, with a documented max attempt count and fixed delay schedule suitable for tests. | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-004 | Retryable failures must increment attempts and move jobs to a predictable retry-waiting state with next retry metadata. | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-005 | Non-retryable failures or retry exhaustion must transition jobs to terminal failed state and create one dead-letter entry. | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-006 | Dead-letter entries must preserve safe error code/category, safe message, attempt summary, source trace, created time, and operator-facing status. | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-007 | Error snapshots must reuse existing safe error redaction patterns and must not expose raw exceptions, stack traces, secrets, private paths, internal endpoints, tokens, cookies, API keys, or real source content. | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-008 | Source trace must be retained from originating batch, file, connector item, or future job metadata and remain relative/mock-safe. | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-009 | Backend APIs must expose failed/dead-letter job list and detail inspection through the existing `ApiEnvelope` pattern. | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-010 | Manual retry and acknowledge endpoints may be implemented only as safe, local, predictable v0 transitions with tests. | Should |
| REQ-WORKER-RETRY-DEAD-LETTER-011 | The frontend Processing Center or operations surface must show failed/dead-letter jobs, attempts, safe error, source trace, and review/blocked states without raw internals. | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-012 | Tests must cover retry transitions, terminal failure, dead-letter creation, safe redaction, source trace, manual retry, and acknowledge behavior. | Must |
| REQ-WORKER-RETRY-DEAD-LETTER-013 | Documentation, roadmap, traceability, and closeout evidence must record scope, exclusions, verification, and residual production gaps. | Must |

## Constraints

- Use mock/sample data only.
- Keep parser, converter, connector, model, vector, storage, and search execution behind adapter boundaries.
- Do not introduce external network dependencies or provider SDK calls.
- Do not mutate production auth, RBAC, audit, secret-manager, or provider strategy.
- Use additive, non-destructive persistence changes only.

## Acceptance

- Retryable failures produce a new attempt and next retry metadata.
- Terminal failures produce exactly one dead-letter entry.
- Dead-letter detail exposes safe error, attempt summary, source trace, created time, and operator status.
- Manual retry/acknowledge are safe and predictable if implemented.
- Backend and frontend verification plus `npm run agent:closeout` pass before completion.
