# 需求：retrieval-quality-metrics

状态：已由本次 goal 预授权接受
最后更新：2026-07-07

## 目标

为 Trusted Ask 以及相邻的 Wiki/Graph 检索界面增加确定性、mock-safe 的检索质量指标，让 Atlas 团队能够查看回答质量信号，同时不暴露 raw source content、secrets、private paths、raw prompts、raw provider payloads，也不声称已经具备生产级治理。

## 范围

- 为 retrieval runs、answers、citations、evidence coverage、citation health、review eligibility、confidence 和 no-evidence refusal behavior 计算并暴露安全指标。
- 保持现有 Ask、Graph、Wiki、review/publish、safe error 和 authorization 语义不变。
- 优先使用现有 Spring Boot、PostgreSQL、Flyway、DTO、mapper、service、controller、Vue、TypeScript 和测试模式。
- 仅使用 mock/sample-safe 的确定性数据。

## 范围外

- `answer-review-governance`、`graph-from-wiki-extraction` 和新的 `ask-session-citations` 行为。
- Provider/model/vector adapter 策略变更。
- 真实 analytics、observability vendor、LLM provider、embedding provider、cloud service、公司数据或生产 A/B evaluation。
- Raw source content、raw prompts、provider responses、stack traces、internal endpoints、private paths、secrets、billing controls、human rating workflows 或 ML reranker tuning。

## 需求

| ID | 需求 | 验收 |
|---|---|---|
| REQ-RETRIEVAL-QUALITY-METRICS-001 | Atlas 必须基于已存储的 run 与 evidence metadata，为 Ask runs 计算确定性 retrieval quality metrics。 | AC-RETRIEVAL-QUALITY-METRICS-001：测试能用同一组 mock-safe run 与 evidence records 每次计算出相同结果。 |
| REQ-RETRIEVAL-QUALITY-METRICS-002 | Metrics 必须区分 evidence coverage、citation health、confidence、review eligibility 和 no-evidence refusal behavior。 | AC-RETRIEVAL-QUALITY-METRICS-002：API 与 UI 将这些类别作为独立安全字段暴露。 |
| REQ-RETRIEVAL-QUALITY-METRICS-003 | Metrics 必须保留 source trace、review status、confidence、citation 和 evidence metadata，但不暴露 raw source content。 | AC-RETRIEVAL-QUALITY-METRICS-003：DTO 只包含 IDs、statuses、counts、scores、page/section labels 和 safe summaries。 |
| REQ-RETRIEVAL-QUALITY-METRICS-004 | Review eligibility 必须保守且确定性。 | AC-RETRIEVAL-QUALITY-METRICS-004：missing evidence、review-required evidence、low confidence、unhealthy citations 或 failed/no-evidence runs 不会被报告为 trusted review-eligible。 |
| REQ-RETRIEVAL-QUALITY-METRICS-005 | Metrics APIs 必须使用 Atlas envelope 和 safe error handling。 | AC-RETRIEVAL-QUALITY-METRICS-005：后端 contract tests 验证成功与安全失败 shape。 |
| REQ-RETRIEVAL-QUALITY-METRICS-006 | 前端界面必须展示安全质量信号，且不暗示生产级 retrieval governance。 | AC-RETRIEVAL-QUALITY-METRICS-006：Trusted Ask、Graph、Wiki 或 diagnostic UI 展示保守质量标签，且不显示 raw internals。 |
| REQ-RETRIEVAL-QUALITY-METRICS-007 | 实现不得回归现有 Ask、Graph、Wiki、review/publish、auth、rate-limit、safe-error 或 adapter 行为。 | AC-RETRIEVAL-QUALITY-METRICS-007：现有验证与聚焦测试通过。 |
| REQ-RETRIEVAL-QUALITY-METRICS-008 | 必须记录 traceability、roadmap、SDD gate evidence、verification evidence、commit 和 push。 | AC-RETRIEVAL-QUALITY-METRICS-008：Closeout 包含 docs changed、code changed、tests changed、verification、residual risks、commit hash 和 push target。 |

## 假设

- 本切片属于 Wave 4 / Ask And Graph Productization，并保持 prototype-safe。
- Metrics 是本地质量指标，不是最终生产 retrieval governance。
- 现有 Ask evidence snapshots 足够支持本切片；不需要新的 provider calls。

## 依赖

- 已有 `ask-rag`、`knowledge-graph`、`review-publish`、`wiki-data-model`、`wiki-ingest-v0`、`wiki-linkify-lint` 和 `rate-limit-safe-errors` 基础。

## 开放问题

在本次 goal 的预授权边界内没有阻塞问题。
