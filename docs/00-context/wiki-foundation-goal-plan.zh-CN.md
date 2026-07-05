# Atlas Next-Phase Goal Plan: Wiki Foundation To Internal Beta

日期：2026-07-05
目标读者：准备在 Atlas Knowledge Hub 中继续执行 goal-mode 的 Codex / 编码 Agent
配套执行清单：`docs/00-context/execution-manifests/wiki-foundation-20260705.yaml`

## 1. 背景与判断

Atlas 当前已经具备较完整的 Vue 产品外壳、Spring Boot metadata/control-plane、adapter 契约、review/publish、graph、Ask 和多层 E2E 验证证据。对照 WeKnora 分析和 Atlas 自己的 SDD，当前主要问题不是“完全没做”，而是成熟度层级混在一起：

- L2/L3 demo 和 API-backed surface 已较完整。
- 真实知识生产内核仍偏薄，尤其缺少自维护 Wiki ingest pipeline。
- 现有 Wiki 更接近“已审核文件发布成 Wiki metadata”，还不是“从文档自动生成、维护、互链、lint、可刷新”的 Auto Wiki。
- 因此下一阶段不应先铺开 Auth、Connector、Agent、IM 或部署大工程，而应先补齐 Wiki Foundation。

本计划遵守 Atlas 项目规则：WeKnora 只作为产品体验、信息架构和工作流参考；不得复制 WeKnora 源码、项目结构、UI 资产、prompt 或实现细节。Atlas 必须继续按自己的 SDD、adapter 边界、source trace、review status 和 mock/sample 数据安全规则推进。

## 2. 已确认基线

执行前 Codex 必须读取：

- `README.md`
- `PROJECT_RULES.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `ROADMAP.md`
- `ROADMAP.zh-CN.md`
- `docs/07-acceptance/product-acceptance-report.zh-CN.md`
- `docs/07-acceptance/internal-beta-readiness.zh-CN.md`
- `docs/00-context/full-stack-productization-traceability.zh-CN.md`
- `docs/03-spec/full-stack-productization-spec.zh-CN.md`
- `docs/06-tasks/full-stack-productization-tasks.zh-CN.md`

WeKnora 参考材料只读使用。执行时由用户或调用方提供 `WEKNORA_ANALYSIS_DIR`，指向本地 WeKnora 分析目录：

- `${WEKNORA_ANALYSIS_DIR}/01-product-function-map.md`
- `${WEKNORA_ANALYSIS_DIR}/04-fe-be-interaction-flows.md`
- `${WEKNORA_ANALYSIS_DIR}/05-internal-kms-adaptation.md`
- `${WEKNORA_ANALYSIS_DIR}/07-internal-kms-mvp-spec.md`
- `${WEKNORA_ANALYSIS_DIR}/sdd/mvp-v1/03-spec.md`
- `${WEKNORA_ANALYSIS_DIR}/sdd/mvp-v1/06-data-model.md`
- `${WEKNORA_ANALYSIS_DIR}/sdd/mvp-v1/09-tasks.md`

当前仓库参考实现：

- `backend/src/main/java/com/atlas/metadata/domain/WikiPage.java`
- `backend/src/main/java/com/atlas/metadata/service/ReviewPublishService.java`
- `backend/src/main/java/com/atlas/metadata/service/IngestionService.java`
- `backend/src/main/java/com/atlas/metadata/adapter/LocalPdfTextParserAdapter.java`
- `backend/src/main/resources/db/migration/V1__init_schema.sql`
- `frontend/src/App.vue`
- `frontend/src/api.ts`
- `frontend/src/types.ts`

注意：`docs/00-context/next-phase-plan.zh-CN.md` 当前在本地工作区中是未跟踪文件。Codex 可以参考它，但不得把它当作唯一 source of truth，也不要修改它，除非用户明确要求。

## 3. 强制 SDD Skill 使用规则

这次 handoff 的关键风险是：只写“生成或更新完整双语 SDD”还不够。后续交给 Codex、Claude Code 或其他 agent 时，必须把 `PROJECT_RULES.md` 中的项目本地 SDD skill chain 明确写成执行步骤、阻断条件和 completion report 证据。

生成或实质更新任何 slice SDD 前，执行 agent 必须先读取并遵守：

- `.agents/skills/atlas-sdd-generate-all/SKILL.md`
- `.agents/skills/req-to-user-story/SKILL.md`
- `.agents/skills/user-story-to-spec/SKILL.md`
- `.agents/skills/spec-to-architecture/SKILL.md`
- `.agents/skills/architecture-to-design/SKILL.md`
- `.agents/skills/design-to-tasks/SKILL.md`
- `.agents/skills/review-doc-quality/SKILL.md`

完整 SDD 生成必须按以下项目本地 skill chain 执行，而不是临时手写全部文档：

1. `atlas-sdd-generate-all`
2. `req-to-user-story`
3. `user-story-to-spec`
4. `spec-to-architecture`
5. `architecture-to-design`
6. `design-to-tasks`
7. `review-doc-quality`

如果 slice 会引入或实质改变架构、adapter 边界、后端/API contract、持久化、安全或 data flow，还必须读取并使用 `.agents/skills/architecture-review/SKILL.md`。`review-code-against-design` 只在实现完成后使用，不属于初始 SDD 生成链。

如果执行 agent 无法访问这些 skill 文件、没有按链条执行、或准备绕过 skill chain 直接写 SDD 文档，必须停下并报告阻塞原因。每个 slice 的 completion report 必须明确包含：

- `SDD skill chain used: yes/no`
- 已读取的 skill 文件列表。
- 若跳过某个 required skill，说明原因和风险。
- `review-doc-quality` 是否已用于最终 SDD set。

## 4. 总体执行路线

本路线分为 6 个 Wave。Wave 0 用来校准成熟度和执行边界；Wave 1 补 Auto Wiki 的数据和最小生成底座；Wave 2 做 Wiki 可维护性；Wave 3 做内部试用必需的信任与治理；Wave 4 产品化 Ask/Graph；Wave 5 扩展来源与运维。

| Wave | 目标 | 准入条件 | 退出条件 |
|---|---|---|---|
| Wave 0 | 成熟度与计划校准 | 当前仓库 A-J 验收证据存在 | gap matrix 明确，下一 slice 被用户接受 |
| Wave 1 | Wiki Foundation | 现有 L3 API-backed surface 不回退 | Wiki 数据底座、最小 ingest、linkify/lint、真实 parser runtime 可分步完成 |
| Wave 2 | Wiki Hardening | Wave 1.1-1.3 完成；Auto Wiki 能产生 review-required 页面 | Wiki 可刷新、可回滚、可审计、可处理质量问题 |
| Wave 3 | Trust And Governance | 核心知识闭环能在 mock/sample 或批准测试包跑通 | SSO/RBAC/audit/secret/rate-limit 达到受控内部试用底线 |
| Wave 4 | Ask And Graph Productization | 已有可信 Wiki 和治理底座 | Ask/Graph 具备会话、引用快照、审核、质量指标 |
| Wave 5 | Connector And Operations | 内部试用路径稳定 | 来源扩展、worker recovery、部署监控和 runbook 成体系 |

### Wave 0：成熟度与计划校准

目标：把“任务完成、demo 可看、API-backed、内部试用、生产可用”重新拆清楚。

最小输出：

- 若尚未存在正式计划，更新本文件或新增对应英文 companion。
- 给出 WeKnora-inspired MVP 能力到 Atlas 当前落地的 gap matrix。
- 明确当前可宣称的是：mock/sample-safe acceptance-ready，不是 production-ready。

完成标准：

- 用户能从计划中看出每个下一步 slice 为什么排在这里。
- 不再用“已实现”模糊表示所有成熟度。

### Wave 1：Wiki Foundation

目标：让 Atlas 从“能展示/发布 Wiki metadata”升级为“具备 Auto Wiki 的数据底座和最小维护能力”。

#### Slice 1.1：`wiki-data-model`

这是第一优先级。

状态（2026-07-05）：已完成。该 slice 已通过双语 SDD 接受门，完成 additive Wiki data model、最小支撑表、read APIs、backend contract/repository 覆盖、Vue Wiki metadata 展示和完整验证。成熟度仅为 Wiki Foundation 数据底座；不代表 Auto Wiki ingest、linkify/lint、生产 RBAC、connector 或运维能力完成。

下一推荐 slice：`wiki-ingest-v0`。进入前仍需先生成或更新完整双语 SDD，并经用户接受。

范围：

- 扩展 `wiki_page` 数据模型：
  - `slug`
  - `page_type`
  - `aliases`
  - `source_refs`
  - `chunk_refs`
  - `in_links`
  - `out_links`
  - `version`
  - `source_mode`
  - `refresh_policy`
- 新增 Wiki 支撑表：
  - `wiki_folder`
  - `wiki_generation_run`
  - `wiki_log_entry`
  - `wiki_page_issue`
- 后端 API：
  - Wiki page list/detail by id
  - Wiki page lookup by slug
  - folder list/tree
  - log entries list
  - issues list
- 前端 Wiki tab：
  - 展示 `slug`、`page_type`、aliases、source/chunk refs、in/out links、version。
  - 保留当前 API-backed Wiki surface，不退回纯 mock。
- 回归兼容：
  - 现有 `publishFile` 仍可把 approved file 发布为 Wiki page。
  - 现有 `listWikiPages` 与前端 E2E 不能破坏。

排除：

- 不实现真实 Wiki ingest pipeline。
- 不实现 linkify 和 lint 规则引擎。
- 不实现生产 auth/RBAC。
- 不引入真实公司数据、外部 cloud call 或新模型 provider。

#### Slice 1.2：`wiki-ingest-v0`

目标：从 approved source chunks 生成最小 Wiki 页面候选，并合并进 Wiki page。

范围：

- `wiki_pending_op` 或 `wiki_generation_run` 驱动的最小 ingest run。
- Map：source document / approved chunks -> summary candidate + entity/concept candidate。
- Reduce：按 slug 合并，更新 existing page，保留 source_refs/chunk_refs。
- 所有 LLM 调用必须走 `model-adapter`。
- 自动生成内容默认 `REVIEW_REQUIRED` 或 draft，不直接 trusted publish。
- 幂等：同一 source 重跑不重复建页。

排除：

- 不做复杂并行 worker。
- 不做 production prompt tuning。
- 不做 Connector sync。

#### Slice 1.3：`wiki-linkify-lint`

目标：让 Wiki 页面有自动互链和基础质量检查。

范围：

- Linkify：根据 slug/aliases 注入 `[[wiki-link]]`。
- 跳过 fenced code block、inline code、已有 wiki link、Markdown link、image、YAML frontmatter。
- 每个 target slug 每页最多 linkify 一次。
- Lint：orphan page、broken link、stale source ref、empty/thin content。
- issues 写入 `wiki_page_issue`。
- Processing Center 展示 issue 统计。

#### Slice 1.4：`real-office-parser-runtime`

目标：把现有 PDF/ZIP-of-PDF 闭环扩展到 internal converter/parser runtime。

范围：

- `trinity-office` 通过 converter adapter。
- `document-normalize` 通过 parser adapter。
- Office -> PDF -> Markdown + images。
- 状态映射、失败恢复、unsupported 文件报告。

排除：

- 不接外部云 parser。
- 不引入真实公司资料。

### Wave 2：Wiki Hardening

目标：让 Wiki 从“能生成页面”进一步变成“可维护、可刷新、可审计、可恢复”的知识层。

#### Slice 2.1：`wiki-index-log`

范围：

- 结构化 Wiki index overview：
  - 按 folder、page_type、更新时间、review status 聚合。
  - 显示 Wiki coverage、orphan count、broken link count、stale source count。
- append-only `wiki_log_entry`：
  - 记录 generation、manual edit、refresh、retract、publish、lint run。
  - 每条 log 绑定 space、page、run、actor、safe summary。
- Vue Wiki/Processing Center：
  - 展示 Wiki health summary。
  - 展示最近 log entries。

验收：

- API 可读取 Wiki index summary 和 log entries。
- Log 不包含 raw secret、私有路径、真实文档正文或 provider payload。
- second-layer E2E 能看到 API-backed Wiki health/log surface。

#### Slice 2.2：`wiki-refresh-retract`

范围：

- `refresh`：重新处理指定 source 或 page，按 slug 合并。
- `retract`：source 删除或失效后，从 affected pages 移除 source_refs/chunk_refs，必要时标记 stale。
- tombstone / stale marker：
  - 防止已撤回 source 被 in-flight job 写回。
  - 保留可审计记录。
- 冲突策略：
  - manual edited section 默认 protect。
  - AI generated section 可 refresh。

验收：

- 同一 source refresh 不产生重复页面。
- retract 后 Ask/Graph/Wiki 不再把 stale source 作为 trusted evidence。
- conflict/protect 行为有后端单元测试和 UI 状态提示。

#### Slice 2.3：`wiki-review-gate`

范围：

- 自动生成 Wiki page 默认进入 review-required/draft。
- Knowledge Manager 可 approve、request changes、archive。
- 大段自动重写、低置信、缺 source refs、lint issue 未清除时阻止 trusted publish。
- Wiki review actions 写入 audit/log。

验收：

- 未审核 Wiki 不进入 trusted Ask/Graph 默认 evidence。
- 审核状态变更保留 source trace、confidence、review status。
- UI 明确区分 generated draft、review-required、published、archived。

#### Slice 2.4：`knowledge-graph-quality`

范围：

- Graph projection 从 approved/published Wiki page + source chunks 生成节点/边。
- 增强 edge quality：
  - 每条边必须有 evidence reference。
  - 低置信边进入 review-required。
  - 支持 Graph edge review action。
- Graph quality summary：
  - no-evidence edge count。
  - stale evidence count。
  - review-required relationship count。

验收：

- Graph 不再只是 deterministic metadata projection，而能消费 Wiki page relationships。
- Graph detail 展示 Wiki page evidence 与 source chunk evidence。
- Graph projection/audit tests 覆盖 excluded evidence 和 unsafe state。

Wave 2 退出标准：

- Wiki 页面可被生成、刷新、撤回、审核和发布。
- Wiki 健康问题能被检测、展示、追踪。
- Wiki 与 Graph/Ask 的 trusted boundary 一致。

### Wave 3：Trust And Governance

目标：为受控内部试用补齐安全、权限、审计和凭证治理底线。Wave 3 不是为了“做漂亮登录页”，而是为了防止内部试用时数据、权限和密钥边界失控。

#### Slice 3.1：`auth-space-rbac`

范围：

- 公司 SSO/OIDC 抽象接口和本地 mock auth profile。
- 当前用户上下文：user、memberships、active space、roles。
- Space membership 和 role matrix：
  - Viewer
  - Editor
  - Knowledge Manager
  - Space Owner
  - Auditor
  - Platform Admin
- 后端 RBAC guard 覆盖核心 API：
  - space
  - batch/file/chunk
  - review/publish
  - wiki
  - graph
  - ask
  - model/storage/vector/parser settings
- 前端只做权限感知 UI，不作为真实安全边界。

验收：

- Viewer 调写接口返回 403。
- 无权限访问不泄露资源存在性或敏感 detail。
- E2E 覆盖普通用户、Knowledge Manager、Space Owner 的关键路径差异。

#### Slice 3.2：`audit-log-foundation`

范围：

- 审计事件模型：
  - actor、role、space、action、target_type、target_id、result、request_id、safe metadata、created_at。
- 覆盖关键事件：
  - login/logout/switch space。
  - upload/parse/retry/reparse。
  - review/publish/wiki refresh/retract。
  - graph projection/review。
  - ask run。
  - settings/model credential state change。
  - permission denied。
- API 和 UI：
  - Space Owner/Auditor 可看 space audit。
  - Platform Admin 可看 system audit。

验收：

- 所有关键写操作有 audit event。
- audit metadata 不含 raw prompt、raw provider response、secret、私有路径或真实正文。
- 权限拒绝也可审计。

#### Slice 3.3：`secret-manager-integration`

范围：

- 将 model/storage/vector/parser runtime secret 从普通配置中隔离。
- API 只返回 masked/configured 状态。
- 允许 save/rotate/clear，但不回显 raw value。
- 本地开发继续支持 `.env`，但不作为生产 secret model。

验收：

- 后端测试证明 raw secret 不出现在 API response、logs、audit、Playwright artifacts。
- 前端设置页只能看到 masked/configured。
- 旧 model configuration flow 不回退。

#### Slice 3.4：`rate-limit-safe-errors`

范围：

- 对 Ask、upload、parser/converter trigger、model run、settings 写操作加基础 rate limit。
- 统一安全错误码和用户可理解消息。
- 避免 stack trace、SQL、private path、provider diagnostics 外泄。

验收：

- API contract tests 覆盖 401/403/409/422/429/500 safe envelope。
- 前端对 rate-limited 和 permission-denied 有明确状态。

Wave 3 退出标准：

- 受控内部用户可以按角色使用系统。
- 密钥不会通过 API/UI/log/audit 泄露。
- 关键行为可追踪，权限拒绝可解释。

### Wave 4：Ask And Graph Productization

目标：把 Ask/Graph 从“API-backed surface”推进到“可信知识使用体验”。核心是 citation 可复现、答案可审核、图谱关系有质量边界。

#### Slice 4.1：`ask-session-citations`

范围：

- Chat session/message 数据模型。
- Space/KB scoped Ask session list/detail。
- SSE 或可恢复 streaming contract。
- stop generation。
- Citation snapshot：
  - answer 保存当时使用的 source chunk/page/version。
  - citation 可跳转到授权可见的 source evidence。
- No-evidence fallback，不编造答案。

验收：

- Ask 历史可回放，引用不因 source 后续变化而丢失解释。
- Viewer 只能看到自己有权限的 session/citation。
- provider-backed third-layer 仍为 opt-in，不进入默认 CI。

#### Slice 4.2：`answer-review-governance`

范围：

- Ask answer 默认 `REVIEW_REQUIRED`。
- 支持 answer review queue：
  - approve
  - reject
  - request clarification
  - mark insufficient evidence
- 高风险问题或低证据答案强制进入 review gate。
- 记录 model run、cost/token estimate、evidence count、review action。

验收：

- 未审核答案不得变成可复用 trusted knowledge。
- 审核动作可审计。
- UI 明确标出答案状态和证据不足原因。

#### Slice 4.3：`graph-from-wiki-extraction`

范围：

- 从 approved Wiki pages 中抽取 entity/concept/relation candidates。
- 第一版可 deterministic + pattern based；未来模型抽取必须走 adapter。
- 关系默认 review-required，证据齐全后可 approve。
- Graph layout 仍可轻量，但要支持 search/filter/detail。

验收：

- 新图谱关系能追溯到 Wiki page section 和 source chunk。
- 缺 evidence 的候选关系被排除或进入 review-required。
- 不引入外部图数据库作为硬依赖。

#### Slice 4.4：`retrieval-quality-metrics`

范围：

- 记录 retrieval run summary：
  - query
  - candidate count
  - eligible evidence count
  - excluded evidence reasons
  - top scores
  - answer outcome
- Processing Center 增加 Ask readiness：
  - no approved evidence
  - stale index
  - low citation confidence
  - provider unavailable
- 基础 eval fixtures。

验收：

- 用户能解释“为什么 Ask 没答上”。
- 开发者能用 fixture 检查 retrieval regression。

Wave 4 退出标准：

- Ask 可以作为可信知识使用入口，而不是一次性 demo 问答。
- Graph 关系可解释、可审核、可追踪。

### Wave 5：Connector And Operations

目标：扩展知识来源，并把系统从“本地可跑”推进到“受控环境可运维”。这波次必须在安全和治理底座之后做，否则 connector 和运维会放大风险。

#### Slice 5.1：`manual-url-knowledge-ingest`

范围：

- Manual Markdown knowledge。
- URL ingest v0。
- URL ingest 必须有 SSRF 防护：
  - 禁止内网地址。
  - 禁止 file scheme。
  - 限制 redirect。
  - 限制大小和 content type。
- 生成 Document/SourceChunk，并进入同一 review/Wiki pipeline。

验收：

- 手工知识和 URL 知识都可进入 review-required 流程。
- SSRF 单元测试覆盖内网、localhost、metadata endpoint、非法 scheme。

#### Slice 5.2：`connector-sync-v0`

范围：

- Connector 数据模型：
  - type
  - name
  - config without raw secret
  - credential configured state
  - status
  - owner
- 第一版只做一个低风险 connector 类型，例如 static URL list 或 approved local fixture connector。
- 手动 sync。
- Sync job/log。
- Sync 结果创建或更新 Document/SourceChunk。

验收：

- Connector credential 不回显。
- Sync failures 可见且可审计。
- Connector 输出进入同一 review/Wiki/Ask/Graph 边界。

#### Slice 5.3：`worker-retry-dead-letter`

范围：

- 为 parse/wiki/connector/model-heavy jobs 增加统一 job state：
  - queued
  - running
  - succeeded
  - partial_failed
  - failed
  - dead_letter
  - cancelled
- retry policy：
  - max attempts
  - retryable vs non-retryable reason
  - safe failure summary
- operator view：
  - retry
  - cancel
  - inspect safe logs

验收：

- 单文档失败不拖垮 batch。
- 超过 retry 上限进入 dead-letter。
- 操作员可以看到安全原因，不看到敏感原文或 secret。

#### Slice 5.4：`deployment-monitoring-runbook`

范围：

- 本地/staging/production 配置分层。
- Docker 或等价部署拓扑文档。
- Health checks：
  - frontend
  - backend
  - database
  - parser/converter runtime
  - model adapter configured state
- SLO 草案：
  - upload accepted latency
  - parse job completion
  - Ask response first-token/first-result
  - error rate
- Alert/rollback runbook。

验收：

- 新环境可以按 runbook 启动并跑 smoke test。
- 故障时有 rollback 和数据安全说明。
- 不把真实 secret 写进仓库。

Wave 5 退出标准：

- 系统具备受控来源扩展能力。
- 运维团队能部署、监控、排障和回滚。
- 产品可以进入真正内部 beta 候选，而不是仅 mock/sample readiness。

## 5. 给 Codex 的 Goal 提示词

本节给两类提示词：

- **Master Goal Prompt**：把 Wave 0-5 全部交给 Codex 作为长期 goal-mode 执行计划，让它按队列逐 slice 推进。
- **Single Slice Prompt Template**：当你希望一次只让 Codex 做一个 slice 时，用这个模板替换 slice 信息。

建议第一片仍然从 `wiki-data-model` 开始，但 master prompt 不能只写这一片。原因：没有 slug/page_type/source_refs/chunk_refs/in_links/out_links/version，后面的 ingest、linkify、lint 都会变成临时字段或重复返工；同时 Codex 也必须知道后续 Wave 2-5 的完整方向，避免第一片设计得过窄。

### Master Goal Prompt：可直接粘贴给 Codex

```text
使用 goal-driven SDD 模式执行 Atlas Knowledge Hub 下一阶段路线图。请读取并遵守：

- docs/00-context/wiki-foundation-goal-plan.zh-CN.md
- docs/00-context/execution-manifests/wiki-foundation-20260705.yaml
- README.md
- PROJECT_RULES.md
- DEVELOPMENT_STANDARDS.md
- docs/00-context/sdd-profile.md
- ROADMAP.md
- ROADMAP.zh-CN.md
- docs/07-acceptance/product-acceptance-report.zh-CN.md

Mandatory SDD Skill Usage:
- Before writing or updating any SDD artifact, read and follow `.agents/skills/atlas-sdd-generate-all/SKILL.md`.
- Use the project-local SDD skill chain in order: `atlas-sdd-generate-all` -> `req-to-user-story` -> `user-story-to-spec` -> `spec-to-architecture` -> `architecture-to-design` -> `design-to-tasks` -> `review-doc-quality`.
- Read the corresponding skill files under `.agents/skills/` before using them.
- If the slice changes architecture, adapter boundaries, backend/API contracts, persistence, security, or data flow, also read and use `.agents/skills/architecture-review/SKILL.md`.
- Do not hand-write all SDD docs in one pass. If the skill files are missing, unavailable, or not used, stop and report before generating SDD documents.

Goal: 按 Wave 0-5 将 Atlas 从当前 L2/L3 API-backed 骨架推进到以 Auto Wiki 为核心、具备治理、可信 Ask/Graph、Connector 和运维能力的内部 KMS 候选产品。

Execution mode:
- 一次只执行一个 slice，不要把多个 slice 的代码混在一次改动里。
- 每个 slice 都先生成或更新完整双语 SDD：requirements、stories、spec、architecture、data-flow、data-model、design、API guide、tasks、traceability。
- 生成或更新 SDD 时必须显式使用上面的项目本地 skill chain；不要只按普通 Markdown 写作方式生成文档。
- SDD 被用户接受前，不要写产品代码；如果用户明确要求继续实现，再按该 slice 的 tasks 顺序编码。
- 每个 slice 完成后更新 traceability、roadmap/progress 状态和验证证据，再进入下一个 slice。
- 如果某个 slice 的实现会改变已接受行为，先更新对应 spec/design/tasks，不要静默绕过。

Wave / Slice Queue:
1. Wave 0: maturity-and-plan-calibration
   - next-phase-gap-matrix
   - readiness-language-cleanup
2. Wave 1: wiki-foundation
   - wiki-data-model
   - wiki-ingest-v0
   - wiki-linkify-lint
   - real-office-parser-runtime
3. Wave 2: wiki-hardening
   - wiki-index-log
   - wiki-refresh-retract
   - wiki-review-gate
   - knowledge-graph-quality
4. Wave 3: trust-and-governance
   - auth-space-rbac
   - audit-log-foundation
   - secret-manager-integration
   - rate-limit-safe-errors
5. Wave 4: ask-and-graph-productization
   - ask-session-citations
   - answer-review-governance
   - graph-from-wiki-extraction
   - retrieval-quality-metrics
6. Wave 5: connector-and-operations
   - manual-url-knowledge-ingest
   - connector-sync-v0
   - worker-retry-dead-letter
   - deployment-monitoring-runbook

Current recommended starting slice:
- Start with Wave 1 / Slice wiki-data-model unless the user explicitly chooses another slice.
- Do not jump directly to wiki-ingest-v0, linkify/lint, auth, connector, or operations before the required prerequisite slice is complete.

Sources:
- docs/00-context/wiki-foundation-goal-plan.zh-CN.md
- docs/00-context/execution-manifests/wiki-foundation-20260705.yaml
- README.md
- PROJECT_RULES.md
- DEVELOPMENT_STANDARDS.md
- docs/00-context/sdd-profile.md
- ROADMAP.zh-CN.md
- docs/07-acceptance/product-acceptance-report.zh-CN.md
- docs/03-spec/full-stack-productization-spec.zh-CN.md
- docs/06-tasks/full-stack-productization-tasks.zh-CN.md
- WeKnora analysis path: ${WEKNORA_ANALYSIS_DIR}

Global Acceptance:
- 每个 slice 的 SDD 双语文档齐全，REQ/US/T IDs 跨语言一致。
- 每个 slice 的实现只改任务要求的文件，不扩大范围。
- 现有 API-backed product path 不回退。
- Wiki/Graph/Ask 始终保留 source trace、confidence、review status。
- parser/converter/model/vector/storage/search 始终经过 Atlas adapter 边界。
- readiness 语言必须匹配证据：不要把 demo/API-backed/mock-safe 说成 production-ready。

Verification:
- cd backend && mvn verify
- cd frontend && npm run typecheck && npm run test && npm run build
- cd frontend && npm run e2e
- npm run e2e:second-layer
- git diff --check
- focused secret/private-path scan
- focused network/dependency scan
Constraints:
- 不复制 WeKnora 源码、结构、UI、prompt 或专有实现细节。
- 所有 parser/converter/model/vector/storage 行为继续经过 Atlas adapter 边界。
- 只使用 mock/sample-safe 数据。
- 不展示或提交 raw secret、真实公司数据、私有路径、provider log。
- 如果 SDD 与现有实现冲突，先报告并更新 SDD，不要静默绕过。
Completion report:
- 列出 docs changed、code changed、verification run、skipped checks、residual risks。
- 明确 `SDD skill chain used: yes/no`，列出已读取的 `.agents/skills/*/SKILL.md`。
- 明确当前完成到哪个 Wave/Slice、成熟度等级和下一推荐 slice。
```

### Single Slice Prompt Template：一次执行一个 slice 时使用

```text
使用 goal-driven SDD 模式执行 Atlas Knowledge Hub 下一阶段路线图中的一个 slice。

Slice: <从 Wave / Slice Queue 选择一个 slug>
Wave: <Wave 0-5 名称>
Goal: <该 slice 的用户可见结果和工程结果>
Scope: <只列本 slice 内行为>
Exclusions: <明确排除后续 slice 的行为>
Sources:
- docs/00-context/wiki-foundation-goal-plan.zh-CN.md
- docs/00-context/execution-manifests/wiki-foundation-20260705.yaml
- README.md
- PROJECT_RULES.md
- DEVELOPMENT_STANDARDS.md
- docs/00-context/sdd-profile.md
- ROADMAP.zh-CN.md
- docs/07-acceptance/product-acceptance-report.zh-CN.md
- WeKnora analysis path: ${WEKNORA_ANALYSIS_DIR}
Mandatory SDD Skill Usage:
- Before writing or updating any SDD artifact, read `.agents/skills/atlas-sdd-generate-all/SKILL.md`.
- Use the project-local chain: `atlas-sdd-generate-all` -> `req-to-user-story` -> `user-story-to-spec` -> `spec-to-architecture` -> `architecture-to-design` -> `design-to-tasks` -> `review-doc-quality`.
- If architecture, adapter boundaries, backend/API contracts, persistence, security, or data flow change, also use `.agents/skills/architecture-review/SKILL.md`.
- If these skill files are unavailable or you cannot confirm the chain was used, stop and report instead of generating SDD.
Execution:
- 先生成或更新完整双语 SDD 文档集。
- SDD 生成必须使用项目本地 skill chain，不要临时手写全部文档。
- SDD 被用户接受前，不要写产品代码。
- 用户接受后，按 docs/06-tasks/<slice>-tasks.md 的任务 ID 顺序实现。
- 如果实现需要偏离 spec，停下并报告，不要静默重设计。
Acceptance:
- SDD 双语文档齐全，IDs 一致。
- 实现严格映射到本 slice tasks。
- 不破坏现有 API-backed product path。
- source trace、confidence、review status、adapter boundaries 和 data safety 不回退。
Verification:
- cd backend && mvn verify
- cd frontend && npm run typecheck && npm run test && npm run build
- cd frontend && npm run e2e
- npm run e2e:second-layer
- git diff --check
- focused secret/private-path scan
- focused network/dependency scan
Completion report:
- docs changed
- code changed
- SDD skill chain used and skill files read
- verification run
- skipped checks with reasons
- residual risks
- next recommended slice
```

### 首个 Slice 的推荐填充

如果你要让 Codex 从第一片开始，把上面的模板填成：

```text
Slice: wiki-data-model
Wave: Wave 1 / wiki-foundation
Goal: 将 Atlas Wiki 从“发布 metadata 的 API-backed surface”升级为 Auto Wiki 可用的数据模型底座。
Scope:
- 扩展 wiki_page 数据模型：slug、page_type、aliases、source_refs、chunk_refs、in_links、out_links、version、source_mode、refresh_policy。
- 新增 wiki_folder、wiki_generation_run、wiki_log_entry、wiki_page_issue 的最小表结构、domain、repository、DTO/API。
- 保持现有 publish/list Wiki API 和 Vue Wiki 页面不回退。
- Vue Wiki tab 展示新增字段中的可见信息。
Exclusions:
- 不实现 Wiki ingest pipeline。
- 不实现 linkify/lint 规则引擎。
- 不实现生产 auth/RBAC。
- 不接真实公司文档、外部 cloud provider 或新模型服务。
```

## 6. 实施顺序

全波次按 `Wave / Slice Queue` 顺序推进。每个 slice 都走同一个闭环：

1. 读取必读文档和 manifest。
2. 读取 `.agents/skills/atlas-sdd-generate-all/SKILL.md` 和项目本地 SDD skill chain；如果缺失或无法使用，停下报告。
3. 检查是否已有当前 slice 的 SDD；没有则按 skill chain 创建双语文档。
4. 先产出 SDD，并在任务清单中明确每个 task 的 verification。
5. 用户接受 SDD 后，按当前 slice 的 tasks 顺序实现。
6. 只改当前 slice 需要的后端、前端、测试和文档。
7. 增加当前 slice 需要的 unit、integration、API contract、E2E 覆盖。
8. 跑当前 slice 要求的验证命令。
9. 运行完整验证。
10. 更新 traceability、slice roadmap 或 product progress 中对应状态。

第一片 `wiki-data-model` 的实现顺序更具体：

1. 读取并使用项目本地 SDD skill chain，生成或更新 `wiki-data-model` 双语 SDD。
2. 用户接受后实现数据库 migration。
3. 实现 Wiki page/folder/generation/log/issue domain、repository、DTO、mapper、service、controller。
4. 更新 Vue API client/types/Wiki tab。
5. 增加后端 contract tests、前端 unit/E2E 覆盖。
6. 跑 backend、frontend、E2E、second-layer、diff、scan。

## 7. Stop Conditions

遇到以下情况必须停下并报告，不要继续编码：

- 必读文档缺失或相互冲突。
- 项目本地 SDD skill 文件缺失、无法访问，或执行 agent 无法确认已按 skill chain 使用。
- 执行 agent 准备绕过 `.agents/skills/atlas-sdd-generate-all/` 直接手写完整 SDD set。
- 需要修改生产 auth/RBAC/security model 才能继续。
- 需要外部网络调用或真实 provider 凭据。
- 需要读取或提交真实公司文档、截图、日志、凭据。
- Flyway migration 会破坏现有 seeded mock/sample 数据或现有 E2E。
- SDD 未经用户接受但任务已进入产品代码实现阶段。
- WeKnora 参考材料无法区分“产品意图”和“实现细节”。

## 8. Definition Of Done

每个 slice 完成时都必须满足：

- SDD：该 slice 的双语 requirements/stories/spec/architecture/data-flow/data-model/design/API guide/tasks/traceability 已创建或更新。
- Skill chain：completion report 记录 `SDD skill chain used: yes`，列出已读取的 `.agents/skills/*/SKILL.md`，并说明 `review-doc-quality` 已执行或跳过原因。
- Traceability：REQ/US/T IDs 一致，任务映射到 spec 行为和验证命令。
- 实现：改动严格限于当前 slice，不混入后续 slice。
- 验证：该 slice 要求的 backend、frontend、E2E、second-layer、diff、scan 已运行，或清楚说明跳过原因。
- 状态：roadmap/progress/traceability 反映成熟度，不夸大 readiness。
- 报告：列出 changed files、verification、skipped checks、residual risks、next recommended slice。

`wiki-data-model` 作为第一片还应满足：

- SDD：双语 requirements/stories/spec/architecture/data-flow/data-model/design/API guide/tasks/traceability 已创建或更新。
- Skill chain：确认 `atlas-sdd-generate-all` 和下游 SDD skills 已用于 SDD 生成。
- 数据库：新增 migration 可通过 `mvn verify`，旧 API 不破。
- 后端：Wiki page 新字段和最小 folder/log/issues 读取 API 有 contract tests。
- 前端：Wiki tab 显示新增 metadata，主路径仍为真实 Vue + API-backed surface。
- 验证：backend、frontend、E2E、second-layer、diff、scan 均运行，或清楚说明跳过原因。
- 报告：不得宣称 Auto Wiki ingest 已完成；只能宣称 Wiki Foundation 数据底座完成。
