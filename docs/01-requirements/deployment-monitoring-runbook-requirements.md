# Requirements: deployment-monitoring-runbook

Status: Draft accepted by explicit goal preauthorization
Last updated: 2026-07-07
Wave: Wave 5 / Connector And Operations
Workflow tier: Tier 1 / Standard Single Slice

## Goal

Create a reusable Atlas deployment monitoring runbook that gives the team and coding agents one operational contract for deployment readiness, post-deployment verification, monitoring signals, alert severity, incident triage, rollback, safe logging, evidence retention, and closeout.

This slice does not deploy Atlas to production. It documents the checks and evidence expected before and after a mock/sample-safe delivery, while keeping real monitoring platforms, alert channels, infrastructure, secrets, and company data out of scope.

## Scope

- Pre-deployment checklist for branch hygiene, SDD gate, closeout gate, backend/frontend verification, secret/private-path scan, network/dependency scan, mock data check, adapter boundary check, and rollback readiness.
- Post-deployment verification checklist for application startup, key UI routes, key API health/status behavior when present, safe error format, and upload/Wiki/review/Ask/Graph/connector surfaces.
- Monitoring signal catalog for request outcomes, safe error categories, rate limit events, ingest status, connector sync status, worker retry/dead-letter status, review queue size, Wiki publish, Ask evidence/refusal, and graph extraction.
- Alert severity model with P0/P1/P2/P3 triggers, owners, immediate actions, escalation conditions, and closeout evidence.
- Incident triage runbooks for backend startup, frontend build, unsafe errors, stuck ingest, connector sync failures, worker dead-letter spikes, blocked review queue, Ask evidence/citation issues, graph mismatch, and suspected data exposure.
- Rollback/recovery guidance, safe logging/data handling rules, closeout evidence requirements, and CI/manual gate alignment.
- Bilingual SDD, runbook, tasks, traceability, and roadmap/discoverability updates.

## Exclusions

- No real Datadog, Grafana, Prometheus, Sentry, New Relic, CloudWatch, Azure Monitor, GCP Monitoring, PagerDuty, Opsgenie, Slack, Teams, or other live monitoring/alert integration.
- No production deployment pipeline, production infrastructure, production credentials, production secret manager, live cloud SDK, external network dependency, or paid service.
- No real logs, real incident records, real company URLs, internal endpoints, screenshots, private absolute paths, credentials, or confidential data.
- No production-grade observability SDK.
- No auth/RBAC/audit/secret-manager/provider strategy change beyond future-documentation requirements.
- No backend, frontend, database, schema, migration, or workflow script behavior change unless a future accepted slice explicitly scopes it.
- No WeKnora code, structure, assets, styles, or implementation details.

## Requirements

| ID | Priority | Requirement |
|---|---|---|
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-001 | Must | The slice must provide one clear runbook that operators and agents can execute before and after Atlas delivery without requiring a real production environment. |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-002 | Must | The pre-deployment checklist must cover repo status, branch/commit hygiene, SDD gate, closeout gate, backend verification, frontend verification, secret/private-path scan, network/dependency scan, mock-data-only review, adapter boundary review, and rollback readiness. |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-003 | Must | The post-deployment checklist must cover application startup, key UI route loading, key API health/status behavior when present, safe error envelope validation, regression checks for upload/Wiki/review/Ask/Graph/connector surfaces, and log/UI safety checks. |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-004 | Must | The monitoring signal catalog must define safe local evidence for request success/failure, safe error categories, rate limiting, ingest jobs, connector sync, worker retry/dead-letter, review queue size, Wiki publish, Ask citation/no-evidence, and graph extraction signals. |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 | Must | The alert severity model must define P0/P1/P2/P3 triggers, response owner, immediate action, escalation condition, and closeout evidence. |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-006 | Must | The incident runbooks must cover backend startup failure, frontend build failure, unsafe API errors, upload/ingest stuck state, connector sync stuck or failed, worker dead-letter spike, blocked review queue, Ask no-evidence or unsafe citation state, graph extraction mismatch, and suspected secret/private data exposure. |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-007 | Must | The rollback/recovery guidance must distinguish safe documentation rollback, local mock/sample reset, backend/frontend build rollback, migration caution, and stop conditions for production-like changes. |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-008 | Must | Safe logging and data handling rules must forbid committing raw secrets, private paths, internal endpoints, raw logs, real company data, screenshots, credentials, provider payloads, or incident exports. |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-009 | Must | Closeout evidence requirements must align local manual checks with `npm run agent:check-sdd`, `npm run agent:closeout`, `git diff --check`, and focused safety scans. |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-010 | Must | Traceability and roadmap documents must make the slice discoverable and accurately state delivered scope, deferred production observability, and residual risks. |

## Acceptance Criteria

| ID | Criteria |
|---|---|
| AC-DEPLOYMENT-MONITORING-RUNBOOK-001 | A bilingual SDD artifact set exists and passes the Atlas SDD gate. |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-002 | A bilingual deployment monitoring runbook exists and covers readiness, verification, signals, severity, incidents, rollback, data safety, and closeout. |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-003 | The runbook clearly tells agents which gates, metrics, and evidence to check before PR, push, and closeout. |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-004 | The runbook explicitly forbids secrets, private paths, internal endpoints, raw logs, real company data, real incident content, and external monitoring side effects. |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-005 | The runbook distinguishes local/mock evidence from production observability and does not claim L5 production readiness. |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-006 | Roadmap and traceability state what is delivered and what remains future work. |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-007 | `npm run agent:check-sdd -- --slice deployment-monitoring-runbook`, `npm run agent:closeout`, `git diff --check`, and focused safety scans pass. |

## Assumptions

- Existing Atlas verification commands and SDD gates are the operational enforcement layer for this slice.
- Existing backend/frontend routes and status endpoints vary by local phase; the runbook must use "when present" language rather than inventing a mandatory production health API.
- Connector and worker slices may be absent or still evolving; their signals are documented as catalog entries to apply when those surfaces exist.

## Open Questions

None blocking under the explicit goal preauthorization boundary.
