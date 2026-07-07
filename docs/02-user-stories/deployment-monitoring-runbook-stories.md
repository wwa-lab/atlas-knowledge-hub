# User Stories: deployment-monitoring-runbook

Status: Draft accepted by explicit goal preauthorization
Last updated: 2026-07-07

## Story Map

| ID | Title | Requirements |
|---|---|---|
| US-DEPLOYMENT-MONITORING-RUNBOOK-001 | Execute pre-deployment readiness checks | REQ-DEPLOYMENT-MONITORING-RUNBOOK-001, REQ-DEPLOYMENT-MONITORING-RUNBOOK-002, REQ-DEPLOYMENT-MONITORING-RUNBOOK-009 |
| US-DEPLOYMENT-MONITORING-RUNBOOK-002 | Verify Atlas after delivery | REQ-DEPLOYMENT-MONITORING-RUNBOOK-003, REQ-DEPLOYMENT-MONITORING-RUNBOOK-008, REQ-DEPLOYMENT-MONITORING-RUNBOOK-009 |
| US-DEPLOYMENT-MONITORING-RUNBOOK-003 | Interpret monitoring signals and alert severity | REQ-DEPLOYMENT-MONITORING-RUNBOOK-004, REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| US-DEPLOYMENT-MONITORING-RUNBOOK-004 | Triage incidents and recover safely | REQ-DEPLOYMENT-MONITORING-RUNBOOK-006, REQ-DEPLOYMENT-MONITORING-RUNBOOK-007, REQ-DEPLOYMENT-MONITORING-RUNBOOK-008 |
| US-DEPLOYMENT-MONITORING-RUNBOOK-005 | Close out with durable evidence | REQ-DEPLOYMENT-MONITORING-RUNBOOK-009, REQ-DEPLOYMENT-MONITORING-RUNBOOK-010 |

## US-DEPLOYMENT-MONITORING-RUNBOOK-001: Execute pre-deployment readiness checks

As an Atlas delivery operator,
I want one pre-deployment checklist,
so that I can confirm branch, SDD, verification, data-safety, adapter, and rollback readiness before PR, push, or closeout.

### Acceptance Criteria

1. Given a delivery candidate, when the operator opens the runbook, then it lists repo status, branch hygiene, SDD gate, closeout gate, backend/frontend checks, safety scans, mock-only review, adapter boundary review, and rollback readiness.
2. Given a check is not applicable, when the operator records evidence, then the runbook requires a reason instead of implying the skipped check passed.
3. Given the work changes only docs, when backend/frontend checks are skipped, then the evidence explains that no backend/frontend source files changed.

### Notes / Assumptions

- This slice documents the operational contract and does not add new deployment automation.

### Dependencies

- Existing `npm run agent:check-sdd`, `npm run agent:closeout`, and repository verification commands.

### Out of Scope

- Production deployment pipeline or live monitoring integration.

### Open Questions

None.

## US-DEPLOYMENT-MONITORING-RUNBOOK-002: Verify Atlas after delivery

As an Atlas operator,
I want a post-delivery verification checklist,
so that I can confirm the app starts and key product surfaces still behave safely.

### Acceptance Criteria

1. Given a local or staging-like mock/sample-safe delivery, when verification starts, then the runbook checks application startup, key UI routes, key API health/status behavior when present, and safe error responses.
2. Given upload, Wiki, review, Ask, Graph, or connector surfaces are present, when the operator verifies the delivery, then the runbook lists expected regression checks for those surfaces.
3. Given logs or UI output are reviewed, when evidence is recorded, then it must not include raw secrets, private paths, internal endpoints, raw logs, real company data, or provider payloads.

### Notes / Assumptions

- "Health/status endpoint" checks are conditional because Atlas phases may not expose one stable production health API.

### Dependencies

- Existing local runbook, frontend checks, backend checks, and E2E paths.

### Out of Scope

- Creating a new health endpoint or observability SDK.

### Open Questions

None.

## US-DEPLOYMENT-MONITORING-RUNBOOK-003: Interpret monitoring signals and alert severity

As an Atlas delivery lead,
I want a monitoring signal catalog and severity model,
so that I can consistently classify operational concerns without connecting a real alerting system.

### Acceptance Criteria

1. Given a monitoring signal, when the operator consults the catalog, then it explains the signal, source evidence, safe collection method, owner, and action threshold.
2. Given an alert condition, when severity is assigned, then P0/P1/P2/P3 definitions include trigger examples, response owner, immediate action, escalation condition, and closeout evidence.
3. Given a signal references a future or optional surface, when the operator records evidence, then the runbook labels it as "if present" and does not require missing future infrastructure.

### Notes / Assumptions

- Metrics are cataloged as local/manual evidence expectations, not live dashboard queries.

### Dependencies

- Existing safe error, rate-limit, Ask, Graph, Wiki, connector, and workflow documentation.

### Out of Scope

- Real SLO dashboard, alert channel, or incident management tool.

### Open Questions

None.

## US-DEPLOYMENT-MONITORING-RUNBOOK-004: Triage incidents and recover safely

As an Atlas operator,
I want incident runbooks and rollback guidance,
so that I can respond to failures without leaking sensitive data or expanding scope.

### Acceptance Criteria

1. Given a listed incident scenario, when the operator opens the runbook, then it provides symptoms, first checks, immediate mitigation, escalation condition, rollback/recovery path, and safe evidence requirements.
2. Given suspected secret/private data exposure, when the incident runbook is used, then it instructs the operator to stop, avoid committing evidence, rotate exposed material outside the repository, and inspect for similar exposure.
3. Given recovery would require production infrastructure, real credentials, destructive migrations, or external services, when the operator reaches that step, then the runbook requires stopping for user decision.

### Notes / Assumptions

- Recovery guidance is conservative and documentation-first for this slice.

### Dependencies

- Atlas security/data rules and existing workflow gates.

### Out of Scope

- Performing live rollback, destructive migration, or credential rotation inside this repository.

### Open Questions

None.

## US-DEPLOYMENT-MONITORING-RUNBOOK-005: Close out with durable evidence

As a coding agent,
I want traceability, roadmap, and closeout evidence requirements,
so that the completed slice can be reviewed and resumed from durable docs.

### Acceptance Criteria

1. Given the slice is delivered, when closeout is prepared, then traceability lists SDD artifacts, runbook paths, task IDs, verification, skipped checks, residual risks, and future work.
2. Given the roadmap is read later, when the team scans Wave 5, then `deployment-monitoring-runbook` appears as delivered documentation/operations contract, not production observability.
3. Given `npm run agent:closeout` runs, when changed slices are detected, then the deployment-monitoring-runbook SDD gate passes.

### Notes / Assumptions

- This goal preauthorizes SDD acceptance because all artifacts stay inside the stated docs-only boundary.

### Dependencies

- Atlas SDD gate and closeout gate scripts.

### Out of Scope

- Adding new workflow gate behavior.

### Open Questions

None.
