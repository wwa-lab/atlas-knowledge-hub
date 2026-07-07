# User Stories: deployment-monitoring-runbook

状态：已由本次 goal 预授权接受为草案
最后更新：2026-07-07

## Story Map

| ID | Title | Requirements |
|---|---|---|
| US-DEPLOYMENT-MONITORING-RUNBOOK-001 | 执行部署前 readiness checks | REQ-DEPLOYMENT-MONITORING-RUNBOOK-001, REQ-DEPLOYMENT-MONITORING-RUNBOOK-002, REQ-DEPLOYMENT-MONITORING-RUNBOOK-009 |
| US-DEPLOYMENT-MONITORING-RUNBOOK-002 | 交付后验证 Atlas | REQ-DEPLOYMENT-MONITORING-RUNBOOK-003, REQ-DEPLOYMENT-MONITORING-RUNBOOK-008, REQ-DEPLOYMENT-MONITORING-RUNBOOK-009 |
| US-DEPLOYMENT-MONITORING-RUNBOOK-003 | 解读 monitoring signals 和 alert severity | REQ-DEPLOYMENT-MONITORING-RUNBOOK-004, REQ-DEPLOYMENT-MONITORING-RUNBOOK-005 |
| US-DEPLOYMENT-MONITORING-RUNBOOK-004 | 分诊 incidents 并安全恢复 | REQ-DEPLOYMENT-MONITORING-RUNBOOK-006, REQ-DEPLOYMENT-MONITORING-RUNBOOK-007, REQ-DEPLOYMENT-MONITORING-RUNBOOK-008 |
| US-DEPLOYMENT-MONITORING-RUNBOOK-005 | 使用 durable evidence close out | REQ-DEPLOYMENT-MONITORING-RUNBOOK-009, REQ-DEPLOYMENT-MONITORING-RUNBOOK-010 |

## US-DEPLOYMENT-MONITORING-RUNBOOK-001: 执行部署前 readiness checks

As an Atlas delivery operator,
I want one pre-deployment checklist,
so that I can confirm branch, SDD, verification, data-safety, adapter, and rollback readiness before PR, push, or closeout.

### Acceptance Criteria

1. Given 一个 delivery candidate, when operator 打开 runbook, then 它列出 repo status、branch hygiene、SDD gate、closeout gate、backend/frontend checks、safety scans、mock-only review、adapter boundary review 和 rollback readiness。
2. Given 某个 check 不适用, when operator 记录 evidence, then runbook 要求写明原因，而不是暗示 skipped check 已通过。
3. Given 本次只改 docs, when backend/frontend checks 被跳过, then evidence 说明 backend/frontend source files 没有改动。

### Notes / Assumptions

- 本 slice 定义 operational contract，不新增部署自动化。

### Dependencies

- 现有 `npm run agent:check-sdd`、`npm run agent:closeout` 和仓库 verification commands。

### Out of Scope

- Production deployment pipeline 或 live monitoring integration。

### Open Questions

None.

## US-DEPLOYMENT-MONITORING-RUNBOOK-002: 交付后验证 Atlas

As an Atlas operator,
I want a post-delivery verification checklist,
so that I can confirm the app starts and key product surfaces still behave safely.

### Acceptance Criteria

1. Given local 或 staging-like mock/sample-safe delivery, when verification starts, then runbook 检查 application startup、key UI routes、存在时的 key API health/status behavior 以及 safe error responses。
2. Given upload、Wiki、review、Ask、Graph 或 connector surfaces 存在, when operator 验证 delivery, then runbook 列出这些 surfaces 的 expected regression checks。
3. Given logs 或 UI output 被检查, when evidence 被记录, then 不得包含 raw secrets、private paths、internal endpoints、raw logs、real company data 或 provider payloads。

### Notes / Assumptions

- "Health/status endpoint" 是条件性检查，因为 Atlas 各阶段可能没有稳定生产 health API。

### Dependencies

- Existing local runbook、frontend checks、backend checks 和 E2E paths。

### Out of Scope

- 创建新的 health endpoint 或 observability SDK。

### Open Questions

None.

## US-DEPLOYMENT-MONITORING-RUNBOOK-003: 解读 monitoring signals 和 alert severity

As an Atlas delivery lead,
I want a monitoring signal catalog and severity model,
so that I can consistently classify operational concerns without connecting a real alerting system.

### Acceptance Criteria

1. Given 一个 monitoring signal, when operator 查阅 catalog, then 它说明 signal、source evidence、safe collection method、owner 和 action threshold。
2. Given 一个 alert condition, when severity 被分级, then P0/P1/P2/P3 definitions 包含 trigger examples、response owner、immediate action、escalation condition 和 closeout evidence。
3. Given 某个 signal 引用 future 或 optional surface, when operator 记录 evidence, then runbook 将其标为 "if present"，且不要求缺失的未来基础设施。

### Notes / Assumptions

- Metrics 被定义为 local/manual evidence expectations，而不是 live dashboard queries。

### Dependencies

- Existing safe error、rate-limit、Ask、Graph、Wiki、connector 和 workflow documentation。

### Out of Scope

- Real SLO dashboard、alert channel 或 incident management tool。

### Open Questions

None.

## US-DEPLOYMENT-MONITORING-RUNBOOK-004: 分诊 incidents 并安全恢复

As an Atlas operator,
I want incident runbooks and rollback guidance,
so that I can respond to failures without leaking sensitive data or expanding scope.

### Acceptance Criteria

1. Given 一个列出的 incident scenario, when operator 打开 runbook, then 它提供 symptoms、first checks、immediate mitigation、escalation condition、rollback/recovery path 和 safe evidence requirements。
2. Given suspected secret/private data exposure, when 使用 incident runbook, then 它要求停止、避免提交 evidence、在仓库外轮换 exposed material，并检查类似 exposure。
3. Given recovery 需要 production infrastructure、real credentials、destructive migrations 或 external services, when operator 到达该步骤, then runbook 要求停止并请求用户决策。

### Notes / Assumptions

- 本 slice 的 recovery guidance 保守且 documentation-first。

### Dependencies

- Atlas security/data rules 和 existing workflow gates。

### Out of Scope

- 在本仓库内执行 live rollback、destructive migration 或 credential rotation。

### Open Questions

None.

## US-DEPLOYMENT-MONITORING-RUNBOOK-005: 使用 durable evidence close out

As a coding agent,
I want traceability, roadmap, and closeout evidence requirements,
so that the completed slice can be reviewed and resumed from durable docs.

### Acceptance Criteria

1. Given slice 已交付, when 准备 closeout, then traceability 列出 SDD artifacts、runbook paths、task IDs、verification、skipped checks、residual risks 和 future work。
2. Given 以后阅读 roadmap, when team 扫描 Wave 5, then `deployment-monitoring-runbook` 显示为已交付 documentation/operations contract，而不是 production observability。
3. Given `npm run agent:closeout` runs, when changed slices are detected, then deployment-monitoring-runbook SDD gate passes。

### Notes / Assumptions

- 本 goal 预授权 SDD acceptance，因为所有 artifacts 都限制在明确 docs-only boundary 内。

### Dependencies

- Atlas SDD gate 和 closeout gate scripts。

### Out of Scope

- 新增 workflow gate behavior。

### Open Questions

None.
