# Specification: deployment-monitoring-runbook

Status: Draft accepted by explicit goal preauthorization
Last updated: 2026-07-07
Source stories: US-DEPLOYMENT-MONITORING-RUNBOOK-001 through US-DEPLOYMENT-MONITORING-RUNBOOK-005

## Overview

Atlas needs an operations contract for moving from local prototype and engineering slices toward more repeatable delivery. This slice creates that contract as documentation: one bilingual deployment monitoring runbook, backed by SDD, task, traceability, and roadmap evidence.

The slice is intentionally not a production deployment or live observability integration. It defines what teams and agents should check, which evidence is safe to keep, and which stop conditions require human decision.

## Actors

- Delivery operator: runs the pre/post delivery checklists.
- Coding agent: follows gate and evidence expectations during closeout.
- Delivery lead: assigns severity and decides escalation.
- Security reviewer: reviews safe logging, scans, and suspected exposure handling.
- Product reviewer: checks user-facing upload, Wiki, review, Ask, Graph, and connector surfaces when present.

## Functional Requirements

| ID | Spec Requirement |
|---|---|
| FR-DEPLOYMENT-MONITORING-RUNBOOK-001 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-001: Provide one runbook that can be executed without real production infrastructure. |
| FR-DEPLOYMENT-MONITORING-RUNBOOK-002 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-002: Define pre-deployment checks for repository, SDD, closeout, backend/frontend verification, safety scans, mock data, adapter boundaries, and rollback readiness. |
| FR-DEPLOYMENT-MONITORING-RUNBOOK-003 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-003: Define post-deployment checks for app startup, UI routes, API status behavior when present, safe errors, surface regressions, and logs/UI safety. |
| FR-DEPLOYMENT-MONITORING-RUNBOOK-004 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-004: Define a monitoring signal catalog for request outcomes, safe errors, rate limits, ingest, connector sync, worker retry/dead-letter, review queue, Wiki publish, Ask, and Graph. |
| FR-DEPLOYMENT-MONITORING-RUNBOOK-005 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005: Define P0/P1/P2/P3 severity with triggers, owners, actions, escalation, and evidence. |
| FR-DEPLOYMENT-MONITORING-RUNBOOK-006 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-006: Provide incident runbooks for the required failure scenarios. |
| FR-DEPLOYMENT-MONITORING-RUNBOOK-007 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-007: Provide rollback and recovery guidance with stop conditions for production-like actions. |
| FR-DEPLOYMENT-MONITORING-RUNBOOK-008 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-008: Define safe logging and data handling rules that forbid sensitive committed evidence. |
| FR-DEPLOYMENT-MONITORING-RUNBOOK-009 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-009: Align manual evidence with SDD gate, closeout gate, diff hygiene, and focused safety scans. |
| FR-DEPLOYMENT-MONITORING-RUNBOOK-010 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-010: Update traceability and roadmap/discoverability docs with delivered scope and deferred work. |

## Runbook Contract

The accepted runbook must include these sections:

1. Purpose and maturity statement.
2. Pre-deployment checklist.
3. Post-deployment checklist.
4. Monitoring signal catalog.
5. Alert severity model.
6. Incident triage runbooks.
7. Rollback and recovery guidance.
8. Safe logging and data handling.
9. Closeout evidence requirements.
10. CI/manual gate alignment.

## Pre-Deployment Behavior

The runbook must instruct the operator to check:

- clean or intentionally scoped `git status --short`;
- target branch and commit hygiene;
- execution manifest and active slice;
- `npm run agent:check-sdd -- --slice deployment-monitoring-runbook`;
- `npm run agent:closeout` before final report;
- backend verification when backend files changed;
- frontend verification when frontend files changed;
- workflow-script verification when workflow scripts changed;
- focused secret/private-path/real-data scan;
- focused network/dependency scan;
- mock/sample-only evidence;
- adapter boundary preservation;
- rollback readiness and stop conditions.

## Post-Deployment Behavior

The runbook must define checks for:

- application starts using the documented local path;
- key UI routes load when frontend is in scope;
- key API health/status endpoints behave as expected when such endpoints exist;
- API errors keep safe envelope semantics;
- upload, ingest, Wiki, review, Ask, Graph, connector, and worker surfaces do not regress when present;
- logs and UI evidence exclude forbidden data.

## Monitoring Signal Catalog

The catalog must define each signal as safe local/manual evidence, not as a real dashboard integration. Signals include:

- request success/failure rate;
- safe error category distribution;
- rate limit events;
- ingest job status;
- connector sync run status when present;
- worker retry/dead-letter status when present;
- review-required queue size;
- Wiki publish success/failure;
- Ask citation status and no-evidence refusal signals when present;
- graph extraction success/failure when present.

## Alert Severity Model

Severity definitions:

- P0: active or suspected data exposure, unsafe error leakage, or delivery that could corrupt trusted knowledge evidence.
- P1: core local delivery path unavailable or key API-backed workflow blocked.
- P2: degraded product surface, delayed batch/connector/worker path, or quality signal below threshold without exposure.
- P3: documentation, evidence, or non-blocking signal drift that should be fixed before the next delivery.

## Incident Coverage

The runbook must include incident triage cards for:

- backend fails to start;
- frontend build fails;
- API returns unsafe errors;
- upload/ingest stuck;
- connector sync stuck or failed;
- worker dead-letter spike;
- review queue blocked;
- Ask returns no evidence or unsafe citation state;
- graph extraction output mismatch;
- suspected secret/private data exposure.

## Safety Rules

Runbook examples and evidence must not contain raw secrets, tokens, passwords, private local paths, internal endpoints, raw stack traces, raw logs, company documents, screenshots, provider payloads, or real incident exports.

## Acceptance Mapping

| Acceptance | Spec Coverage |
|---|---|
| AC-DEPLOYMENT-MONITORING-RUNBOOK-001 | FR-DEPLOYMENT-MONITORING-RUNBOOK-001, FR-DEPLOYMENT-MONITORING-RUNBOOK-009 |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-002 | FR-DEPLOYMENT-MONITORING-RUNBOOK-001 through FR-DEPLOYMENT-MONITORING-RUNBOOK-008 |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-003 | FR-DEPLOYMENT-MONITORING-RUNBOOK-002, FR-DEPLOYMENT-MONITORING-RUNBOOK-003, FR-DEPLOYMENT-MONITORING-RUNBOOK-009 |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-004 | FR-DEPLOYMENT-MONITORING-RUNBOOK-008 |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-005 | FR-DEPLOYMENT-MONITORING-RUNBOOK-001, FR-DEPLOYMENT-MONITORING-RUNBOOK-004, FR-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-006 | FR-DEPLOYMENT-MONITORING-RUNBOOK-010 |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-007 | FR-DEPLOYMENT-MONITORING-RUNBOOK-009 |

## Task Mapping

T-DEPLOYMENT-MONITORING-RUNBOOK-001 through T-DEPLOYMENT-MONITORING-RUNBOOK-008 implement this specification.

## Open Questions

None blocking under the explicit goal preauthorization boundary.
