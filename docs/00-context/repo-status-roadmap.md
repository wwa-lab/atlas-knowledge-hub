# Atlas Repo One-Page Status And Unified Roadmap

Last updated: 2026-07-08
Purpose: English companion to `docs/00-context/repo-status-roadmap.zh-CN.md`. The Chinese file remains the canonical one-page status entry until the repository fully standardizes bilingual roadmap maintenance.

## Current Summary

Atlas Knowledge Hub has an acceptance-ready, mock/sample-safe evidence package across the core product surfaces, plus implemented governance, Ask/Graph productization, and Wave 5 operations foundations. Wave 5 now includes delivered `manual-url-knowledge-ingest`, `deployment-monitoring-runbook`, `connector-sync-v0`, and `worker-retry-dead-letter` foundations. The current active work has moved to the `frontend-componentization` SDD gate: behavior-preserving structural extraction of the monolithic Vue `App.vue`, without `vue-router` or product behavior changes.

Atlas is still not production-ready. This repository does not yet provide production deployment automation, live monitoring dashboards, real alert channels, production SSO/OIDC, production secret manager, production distributed quota, SIEM/export, production incident tooling, or approved real-company document ingestion.

## Current Active Slice

| Field | Current Value |
|---|---|
| Wave | Wave 6 / Frontend Maintainability |
| Slice | `frontend-componentization` |
| Status | Locally verified after accepted SDD; T-001 through T-007 implemented |
| Maturity | Frontend structural hardening checkpoint; no product behavior, router, backend/API, or visual redesign change |
| Delivered | Bilingual SDD set, domain view-model helpers/tests, shell/sidebar/home/chat components, Space detail/tab-content components, settings modal/panel components, focused mock upload workflow composable, and final frontend regression/closeout evidence |
| Deferred | `vue-router`, URL semantics, deep links, browser back/forward behavior, Pinia/Vuex/global store, visual redesign, backend/API changes, provider/runtime changes, and real company data |

## Wave 5 Operations Queue

| Slice | Status | Notes |
|---|---|---|
| `manual-url-knowledge-ingest` | Delivered | Metadata-only manual URL source registration; not real external fetch, scheduled crawl, or connector sync. |
| `connector-sync-v0` | Delivered | Adapter-first mock/local connector sync foundation; no real providers, credentials, or external fetch. |
| `worker-retry-dead-letter` | Delivered | Deterministic local retry/dead-letter foundation; no production MQ, distributed worker, or scheduled background worker. |
| `deployment-monitoring-runbook` | Delivered | Documentation-only operations contract; no production monitoring side effects. |

## Wave 6 Frontend Maintainability Queue

| Slice | Status | Notes |
|---|---|---|
| `frontend-componentization` | Locally verified frontend structural checkpoint | T-001 through T-007 are implemented and locally verified. No `vue-router`, URL semantics, backend/API changes, new dependencies, visual redesign, provider calls, or real data. |

## Reading Order

| Need | Start Here |
|---|---|
| Chinese canonical repo status | `docs/00-context/repo-status-roadmap.zh-CN.md` |
| Deployment monitoring runbook | `docs/05-design/runbooks/deployment-monitoring-runbook.md` |
| Slice traceability | `docs/00-context/frontend-componentization-traceability.md` |
| SDD tasks | `docs/06-tasks/frontend-componentization-tasks.md` |
| Goal-loop workflow | `docs/00-context/agent-goal-loop-workflow.md` |

## Next Gate

Review and commit the `frontend-componentization` local checkpoint when ready. Any URL semantics, deep links, browser history, backend/API changes, provider/runtime work, dependency additions, real-data ingestion, or visual redesign must be opened as a separate accepted slice.
