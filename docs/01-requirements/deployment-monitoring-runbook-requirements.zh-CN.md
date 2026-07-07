# Requirements: deployment-monitoring-runbook

状态：已由本次 goal 预授权接受为草案
最后更新：2026-07-07
Wave：Wave 5 / Connector And Operations
Workflow tier：Tier 1 / Standard Single Slice

## 目标

创建一份可复用的 Atlas deployment monitoring runbook，让团队和 coding agent 在部署准备、部署后验证、监控信号、告警分级、事故分诊、回滚、安全日志、证据留存和 closeout 上使用同一份 operational contract。

本 slice 不把 Atlas 部署到生产。它只定义 mock/sample-safe 交付前后的检查和证据要求，并明确排除真实监控平台、告警渠道、基础设施、secret 和公司真实数据。

## 范围

- 部署前检查：branch hygiene、SDD gate、closeout gate、backend verification、frontend verification、secret/private-path scan、network/dependency scan、mock data check、adapter boundary check 和 rollback readiness。
- 部署后检查：application startup、关键 UI route、存在时的关键 API health/status 行为、safe error format、upload/Wiki/review/Ask/Graph/connector surfaces 回归，以及 logs/UI 安全。
- 监控信号目录：request outcomes、safe error categories、rate limit events、ingest status、connector sync status、worker retry/dead-letter status、review queue size、Wiki publish、Ask evidence/refusal 和 graph extraction。
- 告警分级模型：P0/P1/P2/P3 triggers、owner、immediate action、escalation condition 和 closeout evidence。
- 事故分诊 runbooks：backend startup failure、frontend build failure、unsafe errors、stuck ingest、connector sync failures、worker dead-letter spikes、blocked review queue、Ask evidence/citation issues、graph mismatch 和 suspected data exposure。
- Rollback/recovery guidance、safe logging/data handling rules、closeout evidence requirements 和 CI/manual gate alignment。
- 双语 SDD、runbook、tasks、traceability 以及 roadmap/discoverability 更新。

## 排除项

- 不接入真实 Datadog、Grafana、Prometheus、Sentry、New Relic、CloudWatch、Azure Monitor、GCP Monitoring、PagerDuty、Opsgenie、Slack、Teams 或其他实时监控/告警系统。
- 不创建生产部署 pipeline、生产基础设施、生产凭据、生产 secret manager、live cloud SDK、外部网络依赖或付费服务。
- 不提交真实 logs、真实事故记录、真实公司 URL、internal endpoints、screenshots、private absolute paths、credentials 或 confidential data。
- 不实现 production-grade observability SDK。
- 不改变 auth/RBAC/audit/secret-manager/provider strategy，除非只是文档中的未来要求。
- 不改 backend、frontend、database、schema、migration 或 workflow script 行为，除非未来独立接受的 slice 明确纳入范围。
- 不复制 WeKnora 的代码、结构、资产、样式或实现细节。

## Requirements

| ID | Priority | Requirement |
|---|---|---|
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-001 | Must | 本 slice 必须提供一份清晰 runbook，让 operators 和 agents 能在不需要真实生产环境的情况下执行 Atlas 交付前后检查。 |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-002 | Must | 部署前 checklist 必须覆盖 repo status、branch/commit hygiene、SDD gate、closeout gate、backend verification、frontend verification、secret/private-path scan、network/dependency scan、mock-data-only review、adapter boundary review 和 rollback readiness。 |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-003 | Must | 部署后 checklist 必须覆盖 application startup、key UI route loading、存在时的 key API health/status behavior、safe error envelope validation、upload/Wiki/review/Ask/Graph/connector surfaces 回归，以及 log/UI safety checks。 |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-004 | Must | Monitoring signal catalog 必须定义 request success/failure、safe error categories、rate limiting、ingest jobs、connector sync、worker retry/dead-letter、review queue size、Wiki publish、Ask citation/no-evidence 和 graph extraction signals 的安全本地证据。 |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 | Must | Alert severity model 必须定义 P0/P1/P2/P3 triggers、response owner、immediate action、escalation condition 和 closeout evidence。 |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-006 | Must | Incident runbooks 必须覆盖 backend startup failure、frontend build failure、unsafe API errors、upload/ingest stuck state、connector sync stuck or failed、worker dead-letter spike、blocked review queue、Ask no-evidence or unsafe citation state、graph extraction mismatch 和 suspected secret/private data exposure。 |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-007 | Must | Rollback/recovery guidance 必须区分 safe documentation rollback、local mock/sample reset、backend/frontend build rollback、migration caution 和 production-like changes 的 stop conditions。 |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-008 | Must | Safe logging and data handling rules 必须禁止提交 raw secrets、private paths、internal endpoints、raw logs、real company data、screenshots、credentials、provider payloads 或 incident exports。 |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-009 | Must | Closeout evidence requirements 必须让 local manual checks 对齐 `npm run agent:check-sdd`、`npm run agent:closeout`、`git diff --check` 和 focused safety scans。 |
| REQ-DEPLOYMENT-MONITORING-RUNBOOK-010 | Must | Traceability 和 roadmap documents 必须让本 slice 可发现，并准确说明已交付范围、延期的 production observability 和残留风险。 |

## Acceptance Criteria

| ID | Criteria |
|---|---|
| AC-DEPLOYMENT-MONITORING-RUNBOOK-001 | 双语 SDD artifact set 存在并通过 Atlas SDD gate。 |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-002 | 双语 deployment monitoring runbook 存在，并覆盖 readiness、verification、signals、severity、incidents、rollback、data safety 和 closeout。 |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-003 | Runbook 清楚说明每次 PR、push、closeout 前后应该检查哪些 gates、metrics 和 evidence。 |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-004 | Runbook 明确禁止 secrets、private paths、internal endpoints、raw logs、real company data、real incident content 和 external monitoring side effects。 |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-005 | Runbook 区分 local/mock evidence 与 production observability，且不宣称 L5 production readiness。 |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-006 | Roadmap 和 traceability 说明已交付内容与未来工作。 |
| AC-DEPLOYMENT-MONITORING-RUNBOOK-007 | `npm run agent:check-sdd -- --slice deployment-monitoring-runbook`、`npm run agent:closeout`、`git diff --check` 和 focused safety scans 通过。 |

## 假设

- 现有 Atlas verification commands 和 SDD gates 是本 slice 的 operational enforcement layer。
- 现有 backend/frontend routes 和 status endpoints 会随本地阶段变化；runbook 必须使用 "when present" 语言，而不是虚构一个强制生产 health API。
- Connector 和 worker slices 可能不存在或仍在演进；其 signals 作为 catalog entries 记录，等相关 surface 存在时应用。

## Open Questions

None blocking under the explicit goal preauthorization boundary.
