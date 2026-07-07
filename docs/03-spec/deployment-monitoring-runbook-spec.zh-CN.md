# Specification: deployment-monitoring-runbook

状态：已由本次 goal 预授权接受为草案
最后更新：2026-07-07
Source stories: US-DEPLOYMENT-MONITORING-RUNBOOK-001 through US-DEPLOYMENT-MONITORING-RUNBOOK-005

## Overview

Atlas 需要一份 operations contract，支撑从本地原型和工程 slices 走向更可重复的交付。本 slice 以文档形式创建这份契约：一份双语 deployment monitoring runbook，并由 SDD、task、traceability 和 roadmap evidence 支撑。

本 slice 明确不是生产部署或 live observability integration。它定义团队和 agents 应该检查什么、哪些 evidence 可以安全留存，以及哪些 stop conditions 需要人工决策。

## Actors

- Delivery operator：执行部署前后 checklist。
- Coding agent：在 closeout 时遵守 gate 和 evidence expectations。
- Delivery lead：分配 severity 并决定 escalation。
- Security reviewer：审查 safe logging、scans 和 suspected exposure handling。
- Product reviewer：在存在相关 surface 时检查 upload、Wiki、review、Ask、Graph 和 connector surfaces。

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

已接受的 runbook 必须包含：

1. Purpose and maturity statement。
2. Pre-deployment checklist。
3. Post-deployment checklist。
4. Monitoring signal catalog。
5. Alert severity model。
6. Incident triage runbooks。
7. Rollback and recovery guidance。
8. Safe logging and data handling。
9. Closeout evidence requirements。
10. CI/manual gate alignment。

## Pre-Deployment Behavior

Runbook 必须要求 operator 检查：

- clean 或明确 scoped 的 `git status --short`；
- target branch 和 commit hygiene；
- execution manifest 和 active slice；
- `npm run agent:check-sdd -- --slice deployment-monitoring-runbook`；
- final report 前的 `npm run agent:closeout`；
- backend files 改动时的 backend verification；
- frontend files 改动时的 frontend verification；
- workflow scripts 改动时的 workflow-script verification；
- focused secret/private-path/real-data scan；
- focused network/dependency scan；
- mock/sample-only evidence；
- adapter boundary preservation；
- rollback readiness 和 stop conditions。

## Post-Deployment Behavior

Runbook 必须定义以下检查：

- application 使用 documented local path 启动；
- frontend in scope 时 key UI routes load；
- 若存在关键 API health/status endpoints，则其行为符合预期；
- API errors 保持 safe envelope semantics；
- 存在时 upload、ingest、Wiki、review、Ask、Graph、connector 和 worker surfaces 不回归；
- logs 和 UI evidence 不包含 forbidden data。

## Monitoring Signal Catalog

Catalog 必须把每个 signal 定义为安全的 local/manual evidence，而不是真实 dashboard integration。Signals 包含：

- request success/failure rate；
- safe error category distribution；
- rate limit events；
- ingest job status；
- 存在时 connector sync run status；
- 存在时 worker retry/dead-letter status；
- review-required queue size；
- Wiki publish success/failure；
- 存在时 Ask citation status 和 no-evidence refusal signals；
- 存在时 graph extraction success/failure。

## Alert Severity Model

Severity definitions：

- P0：active 或 suspected data exposure、unsafe error leakage，或可能破坏 trusted knowledge evidence 的 delivery。
- P1：核心本地 delivery path 不可用，或关键 API-backed workflow 被阻塞。
- P2：product surface 降级、batch/connector/worker path 延迟，或 quality signal 低于阈值但没有 exposure。
- P3：documentation、evidence 或 non-blocking signal drift，应在下次 delivery 前修复。

## Incident Coverage

Runbook 必须包含以下 incident triage cards：

- backend fails to start；
- frontend build fails；
- API returns unsafe errors；
- upload/ingest stuck；
- connector sync stuck or failed；
- worker dead-letter spike；
- review queue blocked；
- Ask returns no evidence or unsafe citation state；
- graph extraction output mismatch；
- suspected secret/private data exposure。

## Safety Rules

Runbook examples 和 evidence 不得包含 raw secrets、tokens、passwords、private local paths、internal endpoints、raw stack traces、raw logs、company documents、screenshots、provider payloads 或 real incident exports。

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
