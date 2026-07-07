# Data Model: deployment-monitoring-runbook

状态：已由本次 goal 预授权接受为草案
最后更新：2026-07-07

## Overview

本 slice 不新增 database tables、DTOs、migrations 或 persisted runtime entities。它定义的是 runbook evidence 和 monitoring signals 的文档数据模型。

## Documentation Entities

### Deployment Check

| Field | Description | Requirement |
|---|---|---|
| name | Human-readable check name。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-002, REQ-DEPLOYMENT-MONITORING-RUNBOOK-003 |
| phase | `pre-deployment`、`post-deployment` 或 `closeout`。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-002, REQ-DEPLOYMENT-MONITORING-RUNBOOK-003 |
| applicability | Always、changed-files-only 或 if-present。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-003 |
| command_or_method | Safe command 或 manual inspection method。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-009 |
| expected_evidence | Pass/fail/skipped summary 和 safe artifact pointers。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-009 |
| safety_notes | Forbidden data 和 scan expectations。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-008 |

### Monitoring Signal

| Field | Description | Requirement |
|---|---|---|
| signal | Stable signal name。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-004 |
| source | Existing command、test、API response、UI state 或 manual check。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-004 |
| safe_collection | 如何在没有 raw logs 或 sensitive data 的情况下收集。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-008 |
| healthy_pattern | Expected local/mock pattern。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-004 |
| action_threshold | Signal 何时成为 P0/P1/P2/P3。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| owner | First response owner。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |

### Alert Severity

| Field | Description | Requirement |
|---|---|---|
| severity | P0、P1、P2 或 P3。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| trigger_examples | Concrete examples。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| response_owner | First response 的角色。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| immediate_action | First safe action。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| escalation_condition | 何时 stop 或 escalate。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| closeout_evidence | Required safe evidence。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |

### Incident Runbook

| Field | Description | Requirement |
|---|---|---|
| scenario | Incident name。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-006 |
| severity_default | Default initial severity。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| symptoms | 可检查的 safe symptoms。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-006 |
| first_checks | First non-destructive checks。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-006 |
| mitigation | Immediate safe mitigation。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-006 |
| rollback_recovery | Safe rollback 或 recovery path。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-007 |
| stop_condition | 何时需要 user decision。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-007 |
| evidence | Safe evidence summary。 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-008 |

## Persistence

不新增 persistence。Evidence 保留在 traceability、final reports 和 safe command summaries 中。Generated reports、raw logs、screenshots 和 real incident exports 不得提交。

## API Guide Decision

本 slice 不创建 API implementation guide，因为没有实现或改变 backend/API behavior。该决定记录在 traceability。

## Task Mapping

T-DEPLOYMENT-MONITORING-RUNBOOK-001 through T-DEPLOYMENT-MONITORING-RUNBOOK-008 use this documentation model.
