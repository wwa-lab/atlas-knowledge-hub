# Architecture: deployment-monitoring-runbook

状态：已由本次 goal 预授权接受为草案
最后更新：2026-07-07

## Architectural Drivers

- REQ-DEPLOYMENT-MONITORING-RUNBOOK-001 through REQ-DEPLOYMENT-MONITORING-RUNBOOK-010 要求一份 durable operations contract，而不是 runtime infrastructure。
- Runbook 必须对齐现有 local verification、SDD gates、safe error rules、adapter boundaries 和 mock/sample-safe evidence。
- 本 slice 不得创建真实 monitoring stack、alert channel、deployment pipeline、production SDK 或 cloud dependency。

## System Context

```text
Delivery Goal / PR / Push / Closeout
  |
  v
Execution Manifest + SDD Artifacts
  |
  v
Deployment Monitoring Runbook
  |
  +--> Pre-deployment checks
  +--> Post-deployment checks
  +--> Signal catalog and severity model
  +--> Incident triage and rollback guidance
  +--> Safe evidence and closeout requirements
  |
  v
Traceability + Roadmap + Local Gate Evidence
```

## Component Ownership

| Component | Owner | Responsibility |
|---|---|---|
| SDD artifacts | Docs/SDD | 定义 requirements、stories、spec、architecture、data flow、data model、design、tasks 和 traceability。 |
| Runbook | Docs/Operations | 为团队和 agents 提供可执行 operational guidance。 |
| Existing workflow gates | Scripts/CI | 强制 SDD completeness、diff hygiene、workflow scans 和 changed-slice checks。 |
| Existing backend/frontend checks | Implementation layers | 基于 changed files 条件执行；本 slice 不修改它们。 |
| Signal catalog | Docs/Operations | 命名 signals 和 safe evidence expectations，但不连接真实 metrics systems。 |
| Incident guidance | Docs/Operations/Security | 定义 triage、mitigation、escalation 和 evidence rules。 |

## Boundaries

- 不新增 backend controllers、health endpoints、metrics emitters、alert dispatchers、queues 或 migrations。
- 不新增 frontend runtime behavior。
- 不修改 `npm run agent:closeout`、CI workflow 或 package scripts。
- Parser、converter、model、vector、storage、search、connector 和 worker concerns 仍保持在其 documented adapter 或 future-worker boundaries 后。
- Runbook 可以引用 existing commands 和 "if present" surfaces，但不得把 future work 写成 current requirements。

## Architecture Decisions

| Decision | Rationale |
|---|---|
| Document-only operations contract | 满足 slice goal，同时避免 production deployment 和 external service side effects。 |
| Conditional checks for endpoints and worker/connector surfaces | Atlas slices 独立演进；runbook 必须在未来 surface 尚不存在时仍可用。 |
| Evidence-first closeout | Atlas goal loops 依赖 durable docs 和 command evidence 恢复，而不是聊天记忆。 |
| Safe summaries over raw logs | 保护数据安全，并避免提交敏感 operational artifacts。 |

## Risks And Mitigations

| Risk | Mitigation |
|---|---|
| Runbook 可能过度宣称 production readiness | 每个 artifact 都声明这是 operations contract，不是 L5 production observability。 |
| Operators 可能把 raw logs 粘进 evidence | Safe logging section 禁止 raw logs，并要求 bounded summaries。 |
| Future connector/worker signals 可能漂移 | Catalog 将 optional signals 标为 "if present"，traceability 记录 deferred implementation。 |
| Manual checks 可能被静默跳过 | Closeout evidence 要求 skipped checks 写明原因。 |

## Architecture Review Result

Architecture review required: no separate code architecture review, because this slice is documentation-only and does not change runtime architecture, API contracts, persistence, security behavior, or external integrations.

Review-doc-quality result: Ready for implementation under the explicit preauthorization boundary; no critical or major findings.
