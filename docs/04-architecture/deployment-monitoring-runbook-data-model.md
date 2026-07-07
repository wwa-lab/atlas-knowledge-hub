# Data Model: deployment-monitoring-runbook

Status: Draft accepted by explicit goal preauthorization
Last updated: 2026-07-07

## Overview

This slice adds no database tables, DTOs, migrations, or persisted runtime entities. It defines a documentation data model for runbook evidence and monitoring signals.

## Documentation Entities

### Deployment Check

| Field | Description | Requirement |
|---|---|---|
| name | Human-readable check name. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-002, REQ-DEPLOYMENT-MONITORING-RUNBOOK-003 |
| phase | `pre-deployment`, `post-deployment`, or `closeout`. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-002, REQ-DEPLOYMENT-MONITORING-RUNBOOK-003 |
| applicability | Always, changed-files-only, or if-present. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-003 |
| command_or_method | Safe command or manual inspection method. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-009 |
| expected_evidence | Pass/fail/skipped summary and safe artifact pointers. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-009 |
| safety_notes | Forbidden data and scan expectations. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-008 |

### Monitoring Signal

| Field | Description | Requirement |
|---|---|---|
| signal | Stable signal name. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-004 |
| source | Existing command, test, API response, UI state, or manual check. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-004 |
| safe_collection | How to collect without raw logs or sensitive data. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-008 |
| healthy_pattern | Expected local/mock pattern. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-004 |
| action_threshold | When the signal becomes P0/P1/P2/P3. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| owner | First response owner. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |

### Alert Severity

| Field | Description | Requirement |
|---|---|---|
| severity | P0, P1, P2, or P3. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| trigger_examples | Concrete examples. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| response_owner | Role responsible for first response. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| immediate_action | First safe action. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| escalation_condition | When to stop or escalate. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| closeout_evidence | Safe evidence required. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |

### Incident Runbook

| Field | Description | Requirement |
|---|---|---|
| scenario | Incident name. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-006 |
| severity_default | Default initial severity. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| symptoms | Safe symptoms to inspect. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-006 |
| first_checks | First non-destructive checks. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-006 |
| mitigation | Immediate safe mitigation. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-006 |
| rollback_recovery | Safe rollback or recovery path. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-007 |
| stop_condition | When user decision is required. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-007 |
| evidence | Safe evidence summary. | REQ-DEPLOYMENT-MONITORING-RUNBOOK-008 |

## Persistence

No persistence is added. Evidence remains in traceability, final reports, and safe command summaries. Generated reports, raw logs, screenshots, and real incident exports must not be committed.

## API Guide Decision

No API implementation guide is created for this slice because no backend/API behavior is implemented or changed. The decision is recorded in traceability.

## Task Mapping

T-DEPLOYMENT-MONITORING-RUNBOOK-001 through T-DEPLOYMENT-MONITORING-RUNBOOK-008 use this documentation model.
