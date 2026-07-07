# Atlas Repo One-Page Status And Unified Roadmap

Last updated: 2026-07-07
Purpose: English companion to `docs/00-context/repo-status-roadmap.zh-CN.md`. The Chinese file remains the canonical one-page status entry until the repository fully standardizes bilingual roadmap maintenance.

## Current Summary

Atlas Knowledge Hub has an acceptance-ready, mock/sample-safe evidence package across the core product surfaces, plus implemented governance, Ask/Graph productization, and Wave 5 operations foundations. Wave 5 now includes delivered `manual-url-knowledge-ingest`, `deployment-monitoring-runbook`, `connector-sync-v0`, and `worker-retry-dead-letter` foundations.

Atlas is still not production-ready. This repository does not yet provide production deployment automation, live monitoring dashboards, real alert channels, production SSO/OIDC, production secret manager, production distributed quota, SIEM/export, production incident tooling, or approved real-company document ingestion.

## Current Active Slice

| Field | Current Value |
|---|---|
| Wave | Wave 5 / Connector And Operations |
| Slice | `worker-retry-dead-letter` |
| Status | Local deterministic worker retry and dead-letter foundation implemented and verified locally |
| Maturity | Prototype reliability contract only; not production distributed queue or scheduled worker readiness |
| Delivered | Worker job/attempt/dead-letter models, deterministic retry policy, terminal failure classification, safe error snapshots, source trace preservation, inspection APIs, manual retry/acknowledge v0, and Vue Processing Center recovery surface |
| Deferred | Production MQ, distributed worker clusters, scheduled production workers, exactly-once guarantees, real connector/API calls, production alerting, and production operator automation |

## Wave 5 Operations Queue

| Slice | Status | Notes |
|---|---|---|
| `manual-url-knowledge-ingest` | Delivered | Metadata-only manual URL source registration; not real external fetch, scheduled crawl, or connector sync. |
| `connector-sync-v0` | Delivered | Adapter-first mock/local connector sync foundation; no real providers, credentials, or external fetch. |
| `worker-retry-dead-letter` | Delivered | Deterministic local retry/dead-letter foundation; no production MQ, distributed worker, or scheduled background worker. |
| `deployment-monitoring-runbook` | Delivered | Documentation-only operations contract; no production monitoring side effects. |

## Reading Order

| Need | Start Here |
|---|---|
| Chinese canonical repo status | `docs/00-context/repo-status-roadmap.zh-CN.md` |
| Deployment monitoring runbook | `docs/05-design/runbooks/deployment-monitoring-runbook.md` |
| Slice traceability | `docs/00-context/worker-retry-dead-letter-traceability.md` |
| SDD tasks | `docs/06-tasks/worker-retry-dead-letter-tasks.md` |
| Goal-loop workflow | `docs/00-context/agent-goal-loop-workflow.md` |

## Next Gate

Review the pushed `feat: add worker retry and dead letter handling` commit on `develop-leo`. Future production MQ, distributed worker clusters, scheduled production workers, exactly-once semantics, alert automation, and real connector retry operations must be delivered by separate accepted slices.
