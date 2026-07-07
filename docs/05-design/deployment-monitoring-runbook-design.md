# Design: deployment-monitoring-runbook

Status: Draft accepted by explicit goal preauthorization
Last updated: 2026-07-07

## Design Scope

Create a bilingual, executable runbook and supporting SDD evidence for REQ-DEPLOYMENT-MONITORING-RUNBOOK-001 through REQ-DEPLOYMENT-MONITORING-RUNBOOK-010. No application code, workflow scripts, package scripts, CI jobs, API guides, or external integrations are changed.

## Runbook Placement

Primary runbook paths:

- `docs/05-design/runbooks/deployment-monitoring-runbook.md`
- `docs/05-design/runbooks/deployment-monitoring-runbook.zh-CN.md`

The runbook lives under design because it is the detailed operational design artifact for this slice. Traceability and roadmap documents point to it for discoverability.

## Content Design

The runbook is structured for repeated use:

1. **Purpose and status**: states L4 readiness preparation / operations contract only.
2. **Quick use table**: tells operators which section to use before PR, before push, after delivery, and during incident triage.
3. **Pre-deployment checklist**: command/method, expected evidence, applicability, and stop conditions.
4. **Post-deployment checklist**: startup, UI, API, safe errors, product surface regressions, and log/UI safety.
5. **Monitoring signal catalog**: signal, source, safe collection, healthy pattern, threshold, owner.
6. **Severity model**: P0/P1/P2/P3 with response and closeout evidence.
7. **Incident runbooks**: scenario cards with symptoms, first checks, mitigation, escalation, rollback/recovery, and safe evidence.
8. **Rollback and recovery**: documentation rollback, local mock reset, build rollback, migration caution, and stop conditions.
9. **Safe logging/data handling**: forbidden committed evidence and safe summary pattern.
10. **Closeout evidence**: exact command evidence and skipped-check rules.

## Interaction Design

This is a documentation interaction, not UI interaction:

- Operators scan tables first, then jump to incident cards.
- Agents use task IDs and checklist sections as closeout anchors.
- Reviewers use traceability to confirm delivered/deferred scope.
- Future slices can add implementation without rewriting this runbook by adding signal sources and evidence examples.

## Safety Design

- Examples avoid real endpoints, real logs, private paths, and secrets.
- Evidence is recorded as bounded summaries and command pass/fail/skipped results.
- Any suspected exposure is P0 and stops ordinary closeout.
- "If present" wording is used for optional connector/worker/API-health surfaces.

## Verification Design

| Check | Purpose |
|---|---|
| `npm run agent:check-sdd -- --slice deployment-monitoring-runbook --report docs/00-context/deployment-monitoring-runbook-sdd-completion-report.md` | Confirms bilingual SDD artifacts and skill-chain evidence. |
| `npm run agent:closeout` | Runs the same changed-slice workflow gate used by CI. |
| `git diff --check` | Confirms whitespace hygiene. |
| Focused secret/private-path/real-data scan | Confirms changed docs do not introduce forbidden evidence. |
| Focused network/dependency scan | Confirms no external monitoring dependencies or live endpoint instructions were introduced. |

## Deferred Work

- Real monitoring platform integration.
- Real alert routing.
- Production deployment pipeline.
- Production health endpoint/SLO dashboard design.
- Worker queue/dead-letter implementation.
- Connector sync operational automation.
- Production incident tooling.

## Task Mapping

T-DEPLOYMENT-MONITORING-RUNBOOK-001 through T-DEPLOYMENT-MONITORING-RUNBOOK-008 implement this design and verify AC-DEPLOYMENT-MONITORING-RUNBOOK-001 through AC-DEPLOYMENT-MONITORING-RUNBOOK-007.
