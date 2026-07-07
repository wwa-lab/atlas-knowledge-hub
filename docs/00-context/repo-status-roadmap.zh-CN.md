# Atlas Repo 一页总览与统一 Roadmap

最后更新：2026-07-07
定位：本文件是当前仓库状态、产品成熟度、slice 队列和下一步门禁的**唯一阅读入口**。

如果只想看一份文档，请看这一份。其他 roadmap、progress、traceability、acceptance report 都作为明细和证据存在，不再作为第一入口。

## 1. 一句话状态

Atlas Knowledge Hub 当前已经完成 mock/sample-safe 的 A-J 产品路线图验收证据整理，并具备真实 Vue 产品路径、API-backed E2E、second-layer local full-stack 证据和一次 opt-in provider-backed third-layer 验证。

但它还不是生产就绪产品。

当前最准确的表述是：

> Atlas 已达到“可进入用户验收评审”的 evidence package；Wave 3 Trust And Governance 的 `secret-manager-integration` 与 `rate-limit-safe-errors` 已完成 prototype foundation；Wave 4 Ask And Graph Productization 的 `ask-session-citations`、`graph-from-wiki-extraction`、`answer-review-governance` 与 `retrieval-quality-metrics` 已完成 mock/sample-safe foundation；Wave 5 Connector And Operations 的 `manual-url-knowledge-ingest` 已完成 metadata-only manual URL source registration foundation，`deployment-monitoring-runbook` 已完成 documentation-only operations contract，`connector-sync-v0` 已完成 adapter-first mock/local connector sync foundation，`worker-retry-dead-letter` 已完成 deterministic local retry/dead-letter reliability contract foundation。它们仍不是 real connector providers、OAuth/API key/cookie/service account flow、scheduled/background/webhook sync、真实 external fetch/crawl、production MQ、distributed worker cluster、exactly-once delivery、production compliance scanning、connector secret management、production approval operations、reviewer queue、answer reuse indexing、production retrieval governance、model-assisted extraction、production graph layout/optimization、manual graph editor、external graph service、production RBAC/audit automation、production secret manager、production distributed quota、真实 alerting/SLO、SSO/OIDC、SIEM/export、生产部署自动化或 production incident tooling 系统。

## 2. 目前应该如何读仓库

| 你想知道什么 | 先看哪里 | 说明 |
|---|---|---|
| 整个 repo 当前状态 | 本文件 | 唯一总览入口 |
| 产品 A-J 验收证据 | `docs/07-acceptance/product-acceptance-report.zh-CN.md` | 证明可进入用户验收评审，不等于生产就绪 |
| 内部 Beta readiness | `docs/07-acceptance/internal-beta-readiness.zh-CN.md` | L4 readiness preparation，不是 L5 production |
| 单个 slice 的执行细节 | `docs/00-context/{slice}-traceability.zh-CN.md` | 只看某个 slice 时使用 |
| SDD/task 执行模板 | `docs/00-context/slice-roadmap.zh-CN.md` | 保留为 agent 操作手册，不再作为唯一状态入口 |
| Codex goal 提示词 | `docs/00-context/codex-goal-prompts.zh-CN.md` | 复制 single-slice、master、SDD-only、implementation、closeout、resume prompts |
| 历史产品路线叙事 | `ROADMAP.zh-CN.md` | 保留 A-J 叙事和成熟度定义 |
| 长期 Wiki/Wave 计划 | `docs/00-context/wiki-foundation-goal-plan.zh-CN.md` | 作为后续 Wave 参考，不代表当前 active 状态 |
| 中断恢复流水账 | `docs/00-context/product-goal-progress.zh-CN.md` | 历史 batch ledger，不作为第一阅读入口 |

## 3. 成熟度分级

| 等级 | 含义 | 当前判断方式 |
|---|---|---|
| L0 Concept | 只有想法或文档 | 有目标和范围 |
| L1 Prototype | 静态或 mock 原型 | 原型可被直接评审 |
| L2 Vue Parity | 真实 Vue 界面达到原型行为 | 截图、交互清单、Playwright 证据 |
| L3 API-backed | Vue 产品路径连接后端 API | API contract、Vue tests、E2E |
| L4 Internal Beta Readiness | mock/sample-safe 内部试用准备 | second-layer full-stack、本地配置和 readiness 报告 |
| L5 Production-ready | 生产安全、运维、规模和治理达标 | 部署、监控、RBAC、审计、性能、回滚全部验收 |

当前整体成熟度：**L4 readiness preparation / acceptance-ready evidence package**。
当前明确不是：**L5 production-ready**。

## 4. 产品面现状

| 产品区域 | 当前成熟度 | 证据 | 主要缺口 |
|---|---|---|---|
| 首页与全局入口 | L2 Vue parity | `phase-a-product-home.png`；Phase A/B E2E | 生产级认证入口未完成 |
| 知识空间详情 | L2 Vue parity | `phase-b-space-detail.png`；Phase A/B E2E | 生产协作、权限、审计未完成 |
| 上传/批处理 | L2 Vue parity | `phase-c-upload-batch.png`；Phase C/D E2E | 真实企业文件包、杀毒/合规/大文件策略未完成 |
| 处理中心 | L2 Vue parity | `phase-d-processing-center.png`；Phase C/D E2E | worker retry、dead-letter、operator runbook 未完成 |
| LM Wiki | L2 Vue parity + L3 API-backed surface | `phase-e-lm-wiki.png`；Phase E/F/G + I4/I7 E2E | 真实 Markdown 生成、发布治理、刷新/撤回仍需加固 |
| 知识图谱 | L2 Vue parity + L3 API-backed surface | `phase-f-knowledge-graph.png`；Graph/E2E | 生产图谱质量、布局、权限过滤未完成 |
| Trusted Ask | L2 Vue parity + L3 API-backed surface | `phase-g-trusted-ask.png`；Ask/E2E | retrieval governance、answer approval、成本/额度策略未完成 |
| 设置与模型管理 | L2 Vue parity + 部分 L3 capability metadata | `phase-h-settings-administration.png`；Phase H E2E | secret manager、credential rotation、RBAC 未完成 |
| API-backed Vue 切换 | L3 API-backed | Phase I1-I3 / I4-I7 E2E | API-backed 不等于真实数据、生产认证或 provider 已验收 |
| 内部 Beta readiness | L4 readiness preparation | `internal-beta-readiness.zh-CN.md`；`npm run e2e:second-layer` | 仍未达到生产部署/监控/安全/性能完整验收 |

## 5. 验收和验证现状

已记录通过的关键验证：

- `cd frontend && npm run typecheck && npm run test && npm run build`
- `cd frontend && npm run e2e`
- `cd backend && mvn verify`
- `npm run e2e:second-layer`
- `npm run e2e:third-layer`：在用户本地批准 provider key 后通过；仍为 opt-in，不进入默认 CI 或默认验收。
- `Agent Workflow Gate`：已接入 PR/push，作为 workflow 红灯。

重要边界：

- product acceptance report 说明“可进入用户验收评审”，不表示用户已经接受。
- third-layer provider-backed 通过说明 provider path 可验证，不表示生产 provider governance 已完成。
- 当前验证使用 mock/sample-safe 数据，不代表真实公司文档摄取已获批。

## 6. 当前 active slice

| 字段 | 当前值 |
|---|---|
| Wave | Wave 5 / Connector And Operations |
| Slice | `worker-retry-dead-letter` |
| 文档状态 | 双语 SDD、tasks、traceability、API guide 与 completion report 已生成并按预授权接受 |
| 工作区状态 | Deterministic local retry/dead-letter foundation 已交付，覆盖 worker job/attempt/dead-letter model、retry policy、terminal failure classification、safe error snapshot、source trace preservation、inspection API、manual retry/ack v0 与 Vue Processing Center recovery surface |
| 成熟度 | Prototype reliability contract；不等于 production MQ、distributed worker、scheduled worker 或 exactly-once readiness |
| 前置依赖 | `connector-sync-v0`、source trace / review-required rules、safe error envelope、adapter boundary、upload/review/Wiki/Ask/Graph product surfaces |
| 下一门禁 | 推送 `develop-leo` 并进行人工 review；后续 production MQ、distributed worker cluster、scheduled production worker、real connector retry operations 与 alert automation 需独立 slice |
| 实现规则 | 不接入 Kafka/RabbitMQ/SQS/PubSub/Redis Queue、真实 scheduled worker、distributed worker、真实 connector provider、OAuth/API key/cookie/service account、真实 external API、真实公司数据、外部 network dependency、production secret、auth/RBAC/audit/secret-manager/provider behavior 或 destructive migration |

## 7. Slice / Wave 队列

### 已完成的产品化基线

| 组 | 状态 | 备注 |
|---|---|---|
| Product Goal A-J | 已完成验收证据整理 | acceptance-ready，不是 production-ready |
| Phase I API-backed Vue cutover | 已完成 L3 API-backed checkpoint | spaces/batches/files/chunks/review/Wiki/Graph/Ask/model metadata |
| Provider-backed third-layer E2E | 已在 opt-in 本地批准 key 下通过 | 不进入默认 CI |

### 已完成的 Wiki Foundation / Runtime 基线

| Wave | Slice | 状态 | 注意 |
|---|---|---|---|
| Wave 1 | `wiki-data-model` | ✅ 完成 | Wiki Foundation 数据模型底座，不等于完整 Auto Wiki |
| Wave 1 | `wiki-ingest-v0` | ✅ 完成 | deterministic-only review-required candidate generation |
| Wave 1 | `wiki-linkify-lint` | ✅ 完成 | linkify/lint base，不等于 review-gate 或生产治理 |
| Wave 2 | `real-office-parser-runtime` | ✅ 完成 | runtime 在 adapter boundary 后；默认 CI mock-safe |
| Wave 3 | `runtime-smoke-config-and-runbook` | ✅ 完成 | command-level readiness，不等于 production ops |
| Wave 3 | `auth-space-rbac` | ✅ 完成 | 不等于 production SSO/OIDC readiness |

### 当前和下一批治理切片

| 顺序 | Slice | 状态 | 下一步 |
|---|---|---|---|
| 1 | `audit-log-foundation` | ✅ prototype audit foundation 已实现 | 保留 deferred emitters 与 production audit/compliance 缺口 |
| 2 | `secret-manager-integration` | ✅ prototype secret-reference foundation 已实现并通过本地 closeout verification | Production secret manager storage、rotation 与 policy automation 保持为 future work |
| 3 | `rate-limit-safe-errors` | ✅ prototype rate-limit/safe-error foundation 已实现 | Production distributed quota、alerting/SLO dashboard、SSO/OIDC、SIEM/export 与 production secret manager 保留为未来工作 |
| 4 | `ask-session-citations` | ✅ session-scoped Trusted Ask citation snapshot foundation 已实现 | Answer governance 与 production prompt/cost/quota controls 保留为未来工作 |
| 5 | `graph-from-wiki-extraction` | ✅ prototype Wiki-derived Graph foundation 已实现并通过本地 backend/frontend/Graph E2E verification | Model-assisted graph extraction、production graph layout/optimization、manual graph editor 与 external graph services 保留为未来工作 |
| 6 | `answer-review-governance` | ✅ prototype answer-review governance foundation 已实现并通过本地 backend/frontend verification | Production approval operations、reviewer queues、answer reuse indexing 与 production RBAC/audit automation 保留为未来工作 |
| 7 | `retrieval-quality-metrics` | ✅ mock/sample-safe retrieval quality metrics foundation 已实现 | Online evaluation、provider/model changes 与 production cost/quota controls 保留为未来工作 |
| 8 | `manual-url-knowledge-ingest` | ✅ metadata-only manual URL source registration foundation 已实现并通过 verification | Connector sync、scheduled/recursive crawl、真实 external fetch、production compliance scanning、connector secrets 与 approved Wiki/Ask/Graph 使用未审核内容保留为未来工作 |
| 9 | `deployment-monitoring-runbook` | ✅ documentation-only deployment monitoring operations contract 已交付 | Real monitoring platforms、alert channels、production deployment automation、production health/SLO dashboards、connector/worker automation 与 production incident tooling 保留为未来工作 |
| 10 | `connector-sync-v0` | ✅ adapter-first mock/local connector sync foundation 已实现并通过本地验证 | Real providers、OAuth/API keys、scheduled/background sync、webhooks、external network fetch、connector marketplace、connector secrets 与 direct approved Wiki/Ask/Graph use 保留为未来工作 |
| 11 | `worker-retry-dead-letter` | ✅ deterministic local retry/dead-letter foundation 已实现并通过本地验证 | Production MQ、distributed worker cluster、scheduled production worker、exactly-once semantics、alert automation 与 real connector retry operations 保留为未来工作 |

### 后续产品化方向

| Wave | 方向 | 代表 slices |
|---|---|---|
| Wave 2 / Wiki Hardening | Wiki 可维护、可刷新、可撤回、可审核 | `wiki-index-log`、`wiki-refresh-retract`、`wiki-review-gate`、`knowledge-graph-quality` |
| Wave 4 / Ask And Graph Productization | Ask/Graph 的会话、引用、审核和质量指标 | `ask-session-citations`、`answer-review-governance`、`graph-from-wiki-extraction`、`retrieval-quality-metrics` |
| Wave 5 / Connector And Operations | 来源扩展、worker recovery、部署监控 | `manual-url-knowledge-ingest`、`connector-sync-v0`、`worker-retry-dead-letter`、`deployment-monitoring-runbook` |

## 8. 生产就绪缺口

当前阻止 L5 production-ready 的主要缺口：

| 优先级 | 缺口 | 建议承接方式 |
|---|---|---|
| P0 | 生产 authentication / authorization / rate limiting / audit policy 未完整验收 | Wave 3 governance slices |
| P0 | 真实公司文档摄取未获批 | 独立数据安全和受控测试包流程 |
| P1 | 大批量性能、production worker automation、operator runbook automation 未验收 | Wave 5 operations slice |
| P1 | 生产部署、监控、SLO、alert、rollback runbook 未验收 | deployment-monitoring-runbook |
| P2 | provider-backed E2E 不属于默认 CI，费用/额度/失败重试策略未定 | provider governance / retrieval quality slices |

## 9. 当前建议执行顺序

1. 推送 `worker-retry-dead-letter` 到 `develop-leo` 并进行人工 review。
2. 按 Wave 5 方向继续 production observability / deployment automation、real connector operations 或 production worker automation 后续切片，但必须先生成并接受各自 SDD。
3. 根据治理优先级继续 production secret-manager hardening、production quota/observability 或其他 operations 后续切片。

不要同时推进多个 slice。Master roadmap 可以管理队列，但执行必须一次一个 slice。

## 10. 旧文档如何处理

为了避免继续出现多个 roadmap 互相竞争，后续约定如下：

- 本文件是总览和当前状态入口。
- `ROADMAP.zh-CN.md` 保留产品 A-J 叙事和成熟度定义。
- `docs/00-context/slice-roadmap.zh-CN.md` 保留 slice backlog、SDD loop 和 prompt 模板。
- `docs/00-context/product-goal-progress.zh-CN.md` 保留 batch 恢复流水账。
- `docs/00-context/wiki-foundation-goal-plan.zh-CN.md` 保留 Wave 0-5 长期计划。
- `docs/00-context/*-traceability.zh-CN.md` 保留单 slice 证据。
- 状态冲突时，以本文件和最新 traceability / acceptance evidence 为准。

## 11. 下一次更新本文件的时机

需要更新本文件的情况：

- active slice 状态变化。
- SDD 草案被接受或退回修改。
- slice 实现完成。
- roadmap/wave 顺序变化。
- 验收证据新增或发现缺口。
- 任何文档宣称 readiness、acceptance 或 production 状态发生变化。
