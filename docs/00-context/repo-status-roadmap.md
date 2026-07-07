# Atlas Repo One-Page Status And Unified Roadmap

Last updated: 2026-07-07
Purpose: English companion to `docs/00-context/repo-status-roadmap.zh-CN.md`. The Chinese file remains the canonical one-page status entry until the repository fully standardizes bilingual roadmap maintenance.

## Current Summary

Atlas Knowledge Hub has an acceptance-ready, mock/sample-safe evidence package across the core product surfaces, plus implemented governance and Ask/Graph productization foundations. Wave 5 now includes the delivered `manual-url-knowledge-ingest` metadata-only source registration foundation and the active `deployment-monitoring-runbook` documentation-only operational contract.

Atlas is still not production-ready. This repository does not yet provide production deployment automation, live monitoring dashboards, real alert channels, production SSO/OIDC, production secret manager, production distributed quota, SIEM/export, production incident tooling, or approved real-company document ingestion.

## Current Active Slice

| Field | Current Value |
|---|---|
| Wave | Wave 5 / Connector And Operations |
| Slice | `deployment-monitoring-runbook` |
| Status | Documentation-only deployment monitoring runbook and SDD evidence package delivered locally |
| Maturity | L4 readiness preparation / operations contract only; not L5 production observability |
| Delivered | Pre-deployment checklist, post-deployment checklist, monitoring signal catalog, alert severity model, incident triage runbooks, rollback/recovery guidance, safe logging/data handling rules, and closeout evidence requirements |
| Deferred | Real monitoring platforms, alert channels, production deployment automation, production health/SLO dashboards, connector/worker automation, and production incident tooling |

## Wave 5 Operations Queue

| Slice | Status | Notes |
|---|---|---|
| `manual-url-knowledge-ingest` | Delivered | Metadata-only manual URL source registration; not real external fetch, scheduled crawl, or connector sync. |
| `connector-sync-v0` | Future / if present in the active branch | Runbook includes connector sync signals as conditional operational checks. |
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

Review the pushed `docs: add deployment monitoring runbook` commit on `develop-leo`. Future production observability, deployment automation, alert routing, and incident tooling must be delivered by separate accepted slices.
