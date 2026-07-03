# 实现任务：知识图谱

## 状态

已实现。Phase 4 hardening。验证证据记录在 `docs/00-context/knowledge-graph-traceability.zh-CN.md`。

## 验证行

来自 `docs/00-context/slice-roadmap.md`：Phase 4 hardening 要求对所触层做完整单元 + 集成 + E2E；所有 Markdown/元数据保留 source trace、confidence、review status；LLM output 在验证前保持 review-required；引入新端点时 API guide 必需。

## 任务详情

| ID | 任务 | Owner | 优先级 | 依赖 | 映射 | 验证 |
|---|---|---|---:|---|---|---|
| T-KG-001 | 确认入口门禁：Phase 2 metadata API 与所需 Phase 3 adapters 已实现；若 `review-publish` 不可用，仅创建 approved/published test fixtures 并记录依赖。任何产品行为不得绕过 review status。 | backend | Must | None | REQ-KG-001 / Spec S1 | 静态审阅 docs 与 fixtures；`git diff --check`。 |
| T-KG-002 | 添加 additive Flyway migration 与 projection runs、projection items、audit records、graph evidence additive fields 的实体/repositories。只使用 mock/sample seed。 | backend | Must | T-KG-001 | REQ-KG-004、REQ-KG-010 / Spec S3、S6 | `cd backend && mvn verify`；集成测试校验 Flyway migration。 |
| T-KG-003 | 实现 graph projection eligibility service：仅 approved/published、必须有 source trace、保留 confidence/review、为 skipped items 返回安全 reason codes。 | backend | Must | T-KG-002 | REQ-KG-001、REQ-KG-009 / Spec S1 | 每个 exclusion code 的单元测试；`cd backend && mvn verify`。 |
| T-KG-004 | 实现产品面 `GraphProjectionAdapter` 与 deterministic adapter；添加 seam guard，确保 graph/model/vector engine 名称只允许在 adapter/worker 边界后出现。 | backend | Must | T-KG-003 | REQ-KG-007、REQ-KG-011 / Spec S2 | Adapter contract tests 与 seam guard tests：`cd backend && mvn verify`。 |
| T-KG-005 | 实现 `GraphProjectionService`，包含幂等 node/edge 创建、partial failure、脱敏错误、run/item summaries 与 audit records。 | backend | Must | T-KG-004 | REQ-KG-004、REQ-KG-010、REQ-KG-011 / Spec S2、S6 | 单元 + 集成测试：`cd backend && mvn verify`。 |
| T-KG-006 | 实现 graph DTOs 与 query service，支持有界 graph views、node details、filters、counts、evidence summaries，且不泄露 raw body/secret/path。 | backend | Must | T-KG-005 | REQ-KG-002、REQ-KG-003、REQ-KG-005、REQ-KG-011 / Spec S3、S4 | API/service tests：`cd backend && mvn verify`。 |
| T-KG-007 | 按 API guide 实现 graph endpoints，包含后端 auth/RBAC、validation、安全 `400/401/403/404` envelope，并审计访问失败。 | backend/security | Must | T-KG-006 | REQ-KG-005、REQ-KG-006、REQ-KG-010 / Spec S4、S6 | Contract tests：`cd backend && mvn verify`。 |
| T-KG-008 | 实现 graph edge review action endpoint 与 append-only review/audit 行为。 | backend | Must | T-KG-007 | REQ-KG-004、REQ-KG-010 / Spec S4、S6 | 集成测试：`cd backend && mvn verify`。 |
| T-KG-009 | 添加前端 graph API client/store/types，包含 typed envelope handling、loading/error/unauthorized/empty states、无外部网络/CDN 依赖、无 raw secret fields。 | frontend | Must | T-KG-007 | REQ-KG-005、REQ-KG-008、REQ-KG-011 / Spec S5 | `cd frontend && npm run typecheck && npm run test`。 |
| T-KG-010 | 将 Graph tab 从 mock-only 数据路径切换为 API-backed graph view，同时保留 prototype canvas、search/filter、legend、hover/focus、selected detail 和 evidence panel 行为。 | frontend | Must | T-KG-009 | REQ-KG-008、REQ-KG-004 / Spec S5 | `cd frontend && npm run typecheck && npm run test && npm run build`。 |
| T-KG-011 | 添加前端 E2E：打开 Graph、过滤/搜索、选择 evidence-backed node/edge、看到 source trace/confidence/review status，并覆盖 unauthorized/empty states。 | QA/frontend | Must | T-KG-010 | REQ-KG-008、REQ-KG-012 / Spec Acceptance Matrix | `cd frontend && npm run e2e`；`npm run e2e:loop:mock`。 |
| T-KG-012 | 运行安全/数据扫描：无新增外部网络调用/依赖，无 raw secrets/private paths，graph responses 或前端 fixtures 中无 raw vectors/prompts/confidential bodies。 | security | Must | T-KG-011 | REQ-KG-006、REQ-KG-011、REQ-KG-012 / Spec S6 | `git diff --check`；`rg -n "(api[_-]?key|password|secret|token|/Users/|/home/|jdbc:|https?://)" docs backend frontend prototypes`。 |
| T-KG-013 | 完成收尾 review：核对 SDD/API/tasks 一致性，运行完整 backend/frontend checks，并用实现证据和 deferred work 更新 traceability。 | QA | Must | T-KG-012 | REQ-KG-012 / Spec Acceptance Matrix | `cd backend && mvn verify`；`cd frontend && npm run typecheck && npm run test && npm run build && npm run e2e`；`npm run e2e:loop:mock`；`git diff --check`。 |

## 依赖计划

关键路径：T-KG-001 -> T-KG-002 -> T-KG-003 -> T-KG-004 -> T-KG-005 -> T-KG-006 -> T-KG-007 -> T-KG-009 -> T-KG-010 -> T-KG-011 -> T-KG-012 -> T-KG-013。

T-KG-008 可在 T-KG-007 后与前端工作并行。

## Codex 约束

- Phase 4 全栈 hardening，但不得直连外部 graph/model/vector/parser/storage engine。
- 所有 Markdown/metadata 保留 source trace、confidence 和 review status。
- LLM output 在验证前保持 review-required。
- Secret 只允许 masked/status-only；禁止 raw credentials、private paths、internal endpoints、raw vectors 和 raw prompts。
- 仅使用 mock/sample data。

## 待确认问题

- requirements 中的 OQ-KG-001 到 OQ-KG-003 保持可见；若阻塞实现，停止并报告 spec/task mismatch，不得静默重设计。

## Code-Against-Design Review 补充

评审日期：2026-07-03。

- 已应用后端验收修正：missing-source-trace candidates 现在以 `MISSING_SOURCE_TRACE` 跳过；无效 graph edge review body 返回 `400` validation envelope；space-scoped graph auth failures 写入 `ACCESS_DENIED` audit record。
- 已应用前端验收修正：Vue `[data-tab="graph"]` surface 现在承载 API-backed graph 数据路径，并包含 search、node/edge filters、evidence-only toggle、SVG canvas、legend、hover/focus affordances、node/edge selection、source-trace evidence detail、empty state、fallback state 和 unauthorized state。
- 已应用验证修正：`frontend/tests/e2e/knowledge-graph.spec.ts` 在真实 `[data-tab="graph"]` surface 上断言 API-backed graph data、search/filter behavior、node 和 edge selection、source trace/confidence/review status、unauthorized state 与 empty state。
- 防复发要求：后续 T-KG-010/T-KG-011 close-out 必须继续把 E2E 断言放在 spec 命名的真实用户界面上，而不是相邻 transition panel 或仅检查 prototype SVG 的 smoke test。
