# Deployment Monitoring Runbook

状态：`deployment-monitoring-runbook` operational contract 已交付
最后更新：2026-07-07
成熟度：仅 L4 readiness preparation / operations contract。不代表 L5 production observability。

## 1. Purpose

在 PR 前、push 前、本地/staging-like delivery 后和 incident triage 时使用本 runbook。它把 Atlas deployment monitoring 变成可重复执行的 checklist，同时不接入真实 monitoring platform、alert channel、production deployment pipeline 或 cloud service。

## 2. Quick Use

| Moment | Use |
|---|---|
| 编辑 delivery slice 前 | 确认 manifest、SDD scope、branch 和 stop conditions。 |
| PR 或 push 前 | 运行 pre-deployment checklist 并记录 safe evidence。 |
| Local delivery 后 | 运行 post-deployment checklist，检查 startup、UI/API behavior、safe errors 和 product surfaces。 |
| Signal 异常时 | 分配 severity，打开匹配 incident card，并记录 safe evidence。 |
| Closeout 前 | 运行 closeout evidence checklist 和 `npm run agent:closeout`。 |

## 3. Pre-Deployment Checklist

| Check | Applicability | Expected evidence |
|---|---|---|
| Repo status | Always | `git status --short` clean，或只有 scoped slice files 发生变化。 |
| Branch and commit hygiene | Always | Target branch 正确；commit message 符合 conventional style；未 stage 无关工作。 |
| Execution manifest | Tier 1+ | Manifest 存在于 `docs/00-context/execution-manifests/` 并命名 active slice。 |
| SDD gate | Tier 1+ | `npm run agent:check-sdd -- --slice <slice>` 通过；需要 skill-chain evidence 时带 report path。 |
| Closeout gate dry run | Before final report | Traceability/roadmap 更新后 `npm run agent:closeout` 通过。 |
| Backend verification | Backend files changed | `cd backend && mvn verify` 通过，或因无 backend files changed 而跳过。 |
| Frontend verification | Frontend files changed | `cd frontend && npm run typecheck && npm run test && npm run build` 通过，或因无 frontend files changed 而跳过。 |
| Workflow script verification | Workflow scripts changed | 运行受影响 npm script 和 Node syntax check。 |
| Secret/private-path scan | Always | Changed files focused scan 未发现 raw secrets、private paths、internal endpoints、real logs、real company data 或 credentials。 |
| Network/dependency scan | Always | 无 dependency manifest change 或新的 external monitoring/cloud/network integration，除非未来已接受 slice 纳入范围。 |
| Mock/sample-only review | Always | Evidence 只使用 repository-safe mock/sample data。 |
| Adapter boundary review | Adapter or operations docs changed | Product logic 仍位于 parser/converter/model/vector/storage/search/connector boundaries 后。 |
| Rollback readiness | Always | 明确需要 revert 的文件或 back out 的 commit；没有 destructive migration 或 production environment change。 |

Skipped checks 必须写明原因。不要暗示未运行命令已通过。

## 4. Post-Deployment Checklist

| Check | Applicability | Expected evidence |
|---|---|---|
| Application starts | Local delivery includes app runtime 时 | Startup command 成功，或失败被归类到 incident card。 |
| Key UI routes load | Frontend in scope | 存在时 Home、knowledge space detail、upload/processing、Wiki、Review、Graph、Ask 和 Settings surfaces 可加载。 |
| API health/status behavior | API status endpoint present | Endpoint 返回 expected safe status。如果没有 endpoint，记录 "not present in current phase"。 |
| Safe error format | Backend/API in scope | Representative errors 保持 safe envelope，不暴露 internals。 |
| Upload/ingest surface | Present | Batch/file statuses 未异常卡住，并使用 safe messages。 |
| Wiki/review/publish surface | Present | Review-required 和 publish states 保持 traceable，且不会自动 approve unverified content。 |
| Ask surface | Present | Citations/no-evidence states 安全且 evidence-aware。 |
| Graph surface | Present | Graph output 仍从 eligible reviewed/published Wiki evidence 派生。 |
| Connector/worker surface | Present | Connector sync 和 retry/dead-letter summaries 安全且有界。 |
| Logs/UI safety | Always | Evidence 只包含 summaries，不包含 raw logs、real data、private paths、internal endpoints、screenshots 或 credentials。 |

## 5. Monitoring Signal Catalog

| Signal | Source | Safe collection | Healthy pattern | Action threshold |
|---|---|---|---|---|
| Request success/failure rate | API tests、local smoke、E2E | 统计 pass/fail summaries，不记录 raw logs | Expected tests pass | Key API path 不可用为 P1；非关键 path degraded 为 P2。 |
| Safe error category distribution | API contract tests | 只统计 safe code counts | Known validation/auth/not-found/rate-limit errors 映射到 safe codes | Unsafe internals leak 为 P0；mapping drift 为 P2。 |
| Rate limit events | Rate-limit tests or API responses | 统计 `RATE_LIMITED` responses 和 retry hints | Deterministic local behavior | Unexpected local throttling 阻塞 key flow 为 P2；doc-only drift 为 P3。 |
| Ingest job status | Batch/ingest UI or API when present | Status counts 和 safe error summaries | Jobs finish 或明确 require review | 所有 ingest stuck 为 P1；partial backlog grows 为 P2。 |
| Connector sync run status | Connector APIs/UI when present | Run status counts 和 safe summaries | Sync completes 或 fails safely | Connector sync 阻塞 release path 为 P1；degraded source 为 P2。 |
| Worker retry/dead-letter status | Worker evidence when present | 只记录 retry/dead-letter counts | 无 unexplained spike | Spike 阻塞 processing 为 P1；bounded known failures 为 P2。 |
| Review-required queue size | Review UI/API | 只记录 count，不记录 raw documents | Queue visible and actionable | Blocked 为 P2；stale count only 为 P3。 |
| Wiki publish success/failure | Wiki/review tests | Publish counts 和 safe failures | Approved/source-traced content publishes | Publish path unavailable 为 P1；partial failure 为 P2。 |
| Ask citation/no-evidence | Ask tests/UI | Citation health/no-evidence counts | No-evidence refusal safe；citations have source trace | Unsafe citation 为 P0；unexpected no-evidence spike 为 P2。 |
| Graph extraction success/failure | Graph tests/UI | Node/edge counts 和 safe mismatch summaries | Output matches eligible Wiki evidence | Graph path unavailable 为 P1；mismatch requiring review 为 P2。 |

## 6. Alert Severity Model

| Severity | Trigger examples | Owner | Immediate action | Escalation condition | Closeout evidence |
|---|---|---|---|---|---|
| P0 | Suspected secret/private data exposure；unsafe API error leakage；trusted evidence corruption | Security reviewer + delivery lead | 停止 closeout，避免提交 evidence，检查 changed files，必要时在仓库外轮换 exposed material | 任何 confirmed exposure 或无法证明安全 | Safe summary、affected file list、remediation、scan results、可复用时的 lesson/prevention |
| P1 | Backend 无法启动；frontend build 阻塞 release；核心 API-backed upload/Wiki/Ask/Graph path 不可用 | Delivery lead + owning engineer | 本地复现，隔离 changed slice，安全时回滚 docs/code change | Scoped fix 后仍失败，或需要 production credentials/infrastructure | Command summary、root cause、fix or rollback、skipped checks |
| P2 | Connector sync degraded；worker dead-letter spike；review queue blocked；graph/Ask quality signal abnormal | Owning engineer + product reviewer | 确认 scope，分类 affected surface，记录 safe summaries，future slice 时 defer | 阻塞 acceptance 或升级为 P1 | Signal summary、decision、needed follow-up slice |
| P3 | Documentation drift；缺少 evidence reason；stale roadmap wording | Slice owner | 更新 docs，重跑 gates | 跨 slices 重复或造成错误 readiness claim | Updated docs and gate result |

## 7. Incident Runbooks

### Backend fails to start

- Default severity: P1.
- First checks: 使用的 backend command、Java/Maven availability、database/Testcontainers requirement、recent backend/migration changes。
- Mitigation: 用 scoped command 重跑；检查 bounded error summary；不要把 raw stack traces 粘进 docs。
- Escalate: 需要 real credentials、destructive migration，或 repeated failure 根因不清。
- Evidence: command、pass/fail summary、changed backend files if any、skipped checks。

### Frontend build fails

- Default severity: P1.
- First checks: `cd frontend && npm run typecheck && npm run test && npm run build`、dependency manifest changes、generated output。
- Mitigation: 修复 scoped TypeScript/test/build issue，或回滚当前 slice frontend change。
- Escalate: 需要 dependency update 或 active slice 之外的 broad UI regression。
- Evidence: failing command summary 和 fixed file list。

### API returns unsafe errors

- Default severity: P0.
- First checks: safe error code、message、path、correlation metadata，以及是否缺少 raw exception/secret/private path/internal endpoint。
- Mitigation: 停止 closeout，检查 safe error boundary，在 owning slice 添加或更新 contract test。
- Escalate: 任何 confirmed sensitive leakage。
- Evidence: 只记录 sanitized summary；如果 raw response 含敏感内容，绝不提交。

### Upload or ingest stuck

- Default severity: P2；core delivery 被阻塞时为 P1。
- First checks: batch status counts、review-required queue、safe error summaries、adapter status。
- Mitigation: 确认当前 slice 是否触及 ingest；适用时重跑 existing ingest tests。
- Escalate: stuck state 阻塞 acceptance 或暗示 data loss。
- Evidence: status counts 和 safe summaries。

### Connector sync stuck or failed

- Default severity: P2.
- First checks: connector run status、item status counts、safe error category、存在时的 mock/local fixture state。
- Mitigation: 隔离 connector-specific failure，并把 external systems 排除在 evidence 外。
- Escalate: sync failure 阻塞 release goal，或需要 real source credentials。
- Evidence: 只记录 run ID 或 count summary，不记录 source content。

### Worker dead-letter spike

- Default severity: P2；processing 被阻塞时为 P1。
- First checks: retry counts、dead-letter counts、存在时的 recent queue/worker changes。
- Mitigation: 暂停进一步变更，并分类 failed item categories。
- Escalate: repeated unknown failures 或 evidence of data loss。
- Evidence: counts 和 safe category summaries。

### Review queue blocked

- Default severity: P2.
- First checks: review-required count、reviewer action availability、publish eligibility、RBAC safe error。
- Mitigation: 适用时验证 review workflow 和 publish gates。
- Escalate: approved content 无法 publish，或 unreviewed content 出现在 trusted surfaces。
- Evidence: counts 和 state summaries。

### Ask no evidence or unsafe citation state

- Default severity: P2；unsafe citation leakage 为 P0。
- First checks: no-evidence refusal、citation status、source trace presence、review policy。
- Mitigation: 确认 Ask 返回 safe refusal，且不会 invent evidence。
- Escalate: citation 泄露 private data，或 answer bypasses evidence policy。
- Evidence: 只记录 safe status 和 counts。

### Graph extraction output mismatch

- Default severity: P2.
- First checks: eligible Wiki inputs、node/edge counts、evidence references、review status、confidence threshold。
- Mitigation: 如果 graph code changed，则重跑 graph tests；否则记录 future follow-up。
- Escalate: graph 把 unreviewed 或 source-less knowledge 显示为 trusted。
- Evidence: count/mismatch summary only。

### Suspected secret or private data exposure

- Default severity: P0.
- First checks: 只检查 changed files；不要把 suspected value 复制到 reports。
- Mitigation: stop、移除 exposure、如果是真实 secret 则在仓库外 rotate、扫描类似 patterns、可复用时更新 prevention artifact。
- Escalate: 任何 real credential、company data、internal endpoint 或 private path 已 commit 或 push。
- Evidence: safe category description、affected file path、remediation、scan result。

## 8. Rollback And Recovery

| Case | Safe recovery |
|---|---|
| Docs-only issue | Revert 或 patch affected docs，并重跑 SDD/closeout gates。 |
| Local mock/sample state issue | 使用 local runbook reset generated local outputs；不要提交 generated artifacts。 |
| Backend/frontend build issue | 在 active slice 内修复，或回滚 active slice change。 |
| Migration issue | 停止，除非未来 accepted slice 明确覆盖 migration repair。 |
| Production-like infrastructure issue | 停止并请求用户决策。 |
| Secret/private data exposure | 停止、修复、真实 secret 在仓库外 rotate，并在继续前重跑 scans。 |

## 9. Safe Logging And Data Handling

Never commit:

- raw secrets、API keys、passwords、tokens、credentials 或 provider payloads；
- private local paths 或 internal endpoints；
- raw stack traces、raw logs、generated reports、screenshots、traces 或 incident exports；
- real company documents、real customer content 或 confidential screenshots；
- `.env` files 或 shell-history exports。

使用这个 safe evidence pattern：

```text
Check: <command or manual check>
Result: pass/fail/skipped
Summary: <bounded non-sensitive summary>
Skipped reason: <only when skipped>
Follow-up: <none or issue/slice>
```

## 10. Closeout Evidence

报告 complete 前：

1. 运行 `git status --short`。
2. 运行 `npm run agent:check-sdd -- --slice deployment-monitoring-runbook --report docs/00-context/deployment-monitoring-runbook-sdd-completion-report.md`。
3. 运行 `git diff --check`。
4. 对 changed files 运行 focused secret/private-path/real-data scan。
5. 对 changed files 运行 focused network/dependency scan。
6. 运行 `npm run agent:closeout`。
7. 确认 backend/frontend verification 仅因无 backend/frontend files changed 而跳过。
8. Review diff，确认无 scope creep 和 readiness overclaiming。
9. 只 commit 和 push active slice files。

## 11. CI And Manual Gate Alignment

- Local `npm run agent:closeout` 运行的 changed SDD slice workflow gate 与 GitHub Actions Agent Workflow Gate 形态一致。
- CI gate 不替代 manual product-surface checks；它验证 SDD、workflow docs、diff hygiene 和 safety scans。
- Runbook closeout 绿色表示本 documentation slice 已交付，不表示 Atlas 已具备 production monitoring。
