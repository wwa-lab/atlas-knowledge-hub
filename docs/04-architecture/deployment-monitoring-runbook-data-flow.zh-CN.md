# Data Flow: deployment-monitoring-runbook

状态：已由本次 goal 预授权接受为草案
最后更新：2026-07-07

## Overview

本 slice 建模 operational evidence flow，而不是 runtime telemetry ingestion。Evidence 来自现有 commands、local/manual checks、safe summaries 和 traceability updates。

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

覆盖 requirements：REQ-DEPLOYMENT-MONITORING-RUNBOOK-001, REQ-DEPLOYMENT-MONITORING-RUNBOOK-002, REQ-DEPLOYMENT-MONITORING-RUNBOOK-009。

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

覆盖 requirements：REQ-DEPLOYMENT-MONITORING-RUNBOOK-003, REQ-DEPLOYMENT-MONITORING-RUNBOOK-008, REQ-DEPLOYMENT-MONITORING-RUNBOOK-009。

## Monitoring Signal Flow

```text
Local command, test, API response, or manual inspection
  -> safe bounded summary
  -> signal catalog classification
  -> severity model
  -> triage card when action is needed
  -> closeout or incident evidence
```

覆盖 requirements：REQ-DEPLOYMENT-MONITORING-RUNBOOK-004, REQ-DEPLOYMENT-MONITORING-RUNBOOK-005, REQ-DEPLOYMENT-MONITORING-RUNBOOK-006。

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

覆盖 requirements：REQ-DEPLOYMENT-MONITORING-RUNBOOK-005, REQ-DEPLOYMENT-MONITORING-RUNBOOK-006, REQ-DEPLOYMENT-MONITORING-RUNBOOK-007, REQ-DEPLOYMENT-MONITORING-RUNBOOK-008。

## Safe Evidence Filter

所有 evidence 在提交前都经过这个 filter：

1. 用 bounded summaries 替代 raw logs。
2. 移除 secrets、tokens、passwords、credentials 和 provider payloads。
3. 移除 private local paths 和 internal endpoints。
4. 移除 real company data、screenshots 和 incident exports。
5. 在安全时保留 command names 和 pass/fail/skipped status。
6. Skipped checks 必须记录原因。

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
