# Data Flow: deployment-monitoring-runbook

Status: Draft accepted by explicit goal preauthorization
Last updated: 2026-07-07

## Overview

This slice models operational evidence flow, not runtime telemetry ingestion. Evidence is gathered from existing commands, local/manual checks, safe summaries, and traceability updates.

## Pre-Deployment Evidence Flow

```text
Goal prompt
  -> execution manifest
  -> required docs and SDD artifacts
  -> pre-deployment checklist
  -> SDD gate
  -> applicable backend/frontend/script checks
  -> safety scans
  -> rollback readiness statement
  -> traceability evidence
```

Covered requirements: REQ-DEPLOYMENT-MONITORING-RUNBOOK-001, REQ-DEPLOYMENT-MONITORING-RUNBOOK-002, REQ-DEPLOYMENT-MONITORING-RUNBOOK-009.

## Post-Deployment Evidence Flow

```text
Delivery candidate
  -> documented startup path
  -> UI route checks when frontend is in scope
  -> API status/health checks when present
  -> safe error validation
  -> product-surface regression checks
  -> safe log/UI scan
  -> closeout evidence
```

Covered requirements: REQ-DEPLOYMENT-MONITORING-RUNBOOK-003, REQ-DEPLOYMENT-MONITORING-RUNBOOK-008, REQ-DEPLOYMENT-MONITORING-RUNBOOK-009.

## Monitoring Signal Flow

```text
Local command, test, API response, or manual inspection
  -> safe bounded summary
  -> signal catalog classification
  -> severity model
  -> triage card when action is needed
  -> closeout or incident evidence
```

Covered requirements: REQ-DEPLOYMENT-MONITORING-RUNBOOK-004, REQ-DEPLOYMENT-MONITORING-RUNBOOK-005, REQ-DEPLOYMENT-MONITORING-RUNBOOK-006.

## Incident Triage Flow

```text
Symptom observed
  -> classify severity
  -> run first checks
  -> choose immediate mitigation
  -> decide escalation condition
  -> rollback/recover if safe
  -> record safe evidence
  -> update traceability or lessons when reusable
```

Covered requirements: REQ-DEPLOYMENT-MONITORING-RUNBOOK-005, REQ-DEPLOYMENT-MONITORING-RUNBOOK-006, REQ-DEPLOYMENT-MONITORING-RUNBOOK-007, REQ-DEPLOYMENT-MONITORING-RUNBOOK-008.

## Safe Evidence Filter

All evidence flows through this filter before being committed:

1. Replace raw logs with bounded summaries.
2. Remove secrets, tokens, passwords, credentials, and provider payloads.
3. Remove private local paths and internal endpoints.
4. Remove real company data, screenshots, and incident exports.
5. Keep command names and pass/fail/skipped status when safe.
6. Record skipped checks with reasons.

## State Model

```text
NOT_STARTED
  -> READY_FOR_SDD_GATE
  -> SDD_GATE_PASSED
  -> RUNBOOK_UPDATED
  -> VERIFICATION_RUNNING
  -> VERIFIED
  -> CLOSED_OUT
  -> COMMITTED_AND_PUSHED

Any state -> BLOCKED when a stop condition is hit.
```

## Verification Mapping

| Evidence flow | Acceptance |
|---|---|
| Pre-deployment flow | AC-DEPLOYMENT-MONITORING-RUNBOOK-001, AC-DEPLOYMENT-MONITORING-RUNBOOK-003 |
| Post-deployment flow | AC-DEPLOYMENT-MONITORING-RUNBOOK-002, AC-DEPLOYMENT-MONITORING-RUNBOOK-003 |
| Monitoring signal flow | AC-DEPLOYMENT-MONITORING-RUNBOOK-002, AC-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| Incident triage flow | AC-DEPLOYMENT-MONITORING-RUNBOOK-002, AC-DEPLOYMENT-MONITORING-RUNBOOK-004 |
| Safe evidence filter | AC-DEPLOYMENT-MONITORING-RUNBOOK-004, AC-DEPLOYMENT-MONITORING-RUNBOOK-007 |
