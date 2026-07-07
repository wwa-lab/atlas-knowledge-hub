# Atlas Repo One-Page Status And Unified Roadmap

Last updated: 2026-07-07
Purpose: English companion to `docs/00-context/repo-status-roadmap.zh-CN.md`. The Chinese file remains the canonical one-page status entry until the repository fully standardizes bilingual roadmap maintenance.

## Current Summary

Atlas Knowledge Hub has an acceptance-ready, mock/sample-safe evidence package across the core product surfaces, plus implemented governance, Ask/Graph productization, and Wave 5 operations foundations. Wave 5 now includes delivered `manual-url-knowledge-ingest`, `deployment-monitoring-runbook`, and `connector-sync-v0` foundations.

Atlas is still not production-ready. This repository does not yet provide production deployment automation, live monitoring dashboards, real alert channels, production SSO/OIDC, production secret manager, production distributed quota, SIEM/export, production incident tooling, or approved real-company document ingestion.

## Current Active Slice

| Field | Current Value |
|---|---|
| Wave | Wave 5 / Connector And Operations |
| Slice | `connector-sync-v0` |
| Status | Adapter-first mock/local connector sync foundation implemented and verified locally |
| Maturity | Prototype connector sync skeleton only; not production connector operations readiness |
| Delivered | Connector registry, connector definition/status model, sync job/run/item APIs, local fixture adapter, safe error mapping, source trace/provenance preservation, review-required artifact handoff, and Vue connector inspection surface |
| Deferred | Real providers, OAuth/API keys, scheduled/background sync, webhooks, external crawling/fetching, connector marketplace, connector secret strategy, and direct approved Wiki/Ask/Graph use |

## Wave 5 Operations Queue

| Slice | Status | Notes |
|---|---|---|
| `manual-url-knowledge-ingest` | Delivered | Metadata-only manual URL source registration; not real external fetch, scheduled crawl, or connector sync. |
| `connector-sync-v0` | Delivered | Adapter-first mock/local connector sync foundation; no real providers, credentials, or external fetch. |
| `worker-retry-dead-letter` | Future | Runbook includes worker retry/dead-letter signals as conditional operational checks. |
| `deployment-monitoring-runbook` | Delivered | Documentation-only operations contract; no production monitoring side effects. |

## Reading Order

| Need | Start Here |
|---|---|
| Chinese canonical repo status | `docs/00-context/repo-status-roadmap.zh-CN.md` |
| Deployment monitoring runbook | `docs/05-design/runbooks/deployment-monitoring-runbook.md` |
| Slice traceability | `docs/00-context/deployment-monitoring-runbook-traceability.md` |
| SDD tasks | `docs/06-tasks/deployment-monitoring-runbook-tasks.md` |
| Goal-loop workflow | `docs/00-context/agent-goal-loop-workflow.md` |

## Next Gate

Review the pushed `feat: add connector sync v0` commit on `develop-leo`. Future real connectors, credential flows, scheduled/background sync, external crawling, and production connector operations must be delivered by separate accepted slices.
