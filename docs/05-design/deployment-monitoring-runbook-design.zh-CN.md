# Design: deployment-monitoring-runbook

状态：已由本次 goal 预授权接受为草案
最后更新：2026-07-07

## Design Scope

为 REQ-DEPLOYMENT-MONITORING-RUNBOOK-001 through REQ-DEPLOYMENT-MONITORING-RUNBOOK-010 创建双语、可执行 runbook 和 supporting SDD evidence。不改变 application code、workflow scripts、package scripts、CI jobs、API guides 或 external integrations。

## Runbook Placement

Primary runbook paths：

- `docs/05-design/runbooks/deployment-monitoring-runbook.md`
- `docs/05-design/runbooks/deployment-monitoring-runbook.zh-CN.md`

Runbook 放在 design 下，因为它是本 slice 的 detailed operational design artifact。Traceability 和 roadmap documents 会指向它，保证可发现。

## Content Design

Runbook 面向重复执行组织：

1. **Purpose and status**：声明仅为 L4 readiness preparation / operations contract。
2. **Quick use table**：告诉 operator 在 PR 前、push 前、delivery 后、incident triage 时使用哪个 section。
3. **Pre-deployment checklist**：command/method、expected evidence、applicability 和 stop conditions。
4. **Post-deployment checklist**：startup、UI、API、safe errors、product surface regressions 和 log/UI safety。
5. **Monitoring signal catalog**：signal、source、safe collection、healthy pattern、threshold、owner。
6. **Severity model**：P0/P1/P2/P3 以及 response 和 closeout evidence。
7. **Incident runbooks**：scenario cards，包含 symptoms、first checks、mitigation、escalation、rollback/recovery 和 safe evidence。
8. **Rollback and recovery**：documentation rollback、local mock reset、build rollback、migration caution 和 stop conditions。
9. **Safe logging/data handling**：forbidden committed evidence 和 safe summary pattern。
10. **Closeout evidence**：exact command evidence 和 skipped-check rules。

## Interaction Design

这是 documentation interaction，不是 UI interaction：

- Operators 先扫描 tables，再跳到 incident cards。
- Agents 使用 task IDs 和 checklist sections 作为 closeout anchors。
- Reviewers 使用 traceability 确认 delivered/deferred scope。
- Future slices 可以通过增加 signal sources 和 evidence examples 来扩展实现，而不重写 runbook。

## Safety Design

- Examples 避免 real endpoints、real logs、private paths 和 secrets。
- Evidence 记录为 bounded summaries 和 command pass/fail/skipped results。
- 任何 suspected exposure 都是 P0，并停止 ordinary closeout。
- Optional connector/worker/API-health surfaces 使用 "if present"。

## Verification Design

| Check | Purpose |
|---|---|
| `npm run agent:check-sdd -- --slice deployment-monitoring-runbook --report docs/00-context/deployment-monitoring-runbook-sdd-completion-report.md` | 确认 bilingual SDD artifacts 和 skill-chain evidence。 |
| `npm run agent:closeout` | 运行与 CI 相同的 changed-slice workflow gate。 |
| `git diff --check` | 确认 whitespace hygiene。 |
| Focused secret/private-path/real-data scan | 确认 changed docs 没有引入 forbidden evidence。 |
| Focused network/dependency scan | 确认没有引入 external monitoring dependencies 或 live endpoint instructions。 |

## Deferred Work

- Real monitoring platform integration。
- Real alert routing。
- Production deployment pipeline。
- Production health endpoint/SLO dashboard design。
- Worker queue/dead-letter implementation。
- Connector sync operational automation。
- Production incident tooling。

## Task Mapping

T-DEPLOYMENT-MONITORING-RUNBOOK-001 through T-DEPLOYMENT-MONITORING-RUNBOOK-008 implement this design and verify AC-DEPLOYMENT-MONITORING-RUNBOOK-001 through AC-DEPLOYMENT-MONITORING-RUNBOOK-007.
