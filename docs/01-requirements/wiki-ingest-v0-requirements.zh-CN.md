# 需求：Wiki Ingest v0

## 状态

已由用户接受。产品代码可严格依据已接受的 SDD 与任务清单推进。

## 切片契约

- **Slice:** `wiki-ingest-v0`
- **Wave:** Wave 1 / Wiki Foundation
- **目标:** 从已批准 source chunks 生成需审核的 Auto Wiki 页面候选，并将候选合并进现有 Wiki 数据模型，同时不破坏当前 API-backed Wiki 路径。
- **前置条件:** `wiki-data-model` 已完成，提供 page slug、reference、generation run、log 和 issue metadata。
- **成熟度目标:** 先达到 Auto Wiki ingest v0 的 SDD readiness；经用户接受后再实现。这不是 linkify/lint、生产 RBAC、connector sync 或生产就绪。

## 需求

| ID | 需求 | 优先级 | 阶段 |
|---|---|---|---|
| REQ-WIKI-INGEST-V0-001 | 在产品代码变更前，必须存在并接受完整双语 SDD 文档集。 | Must | SDD |
| REQ-WIKI-INGEST-V0-002 | ingest run 只能选择带 source trace、confidence、review status 的已批准 source chunks。 | Must | Backend |
| REQ-WIKI-INGEST-V0-003 | ingest run 必须生成确定性的 Wiki page candidates，包含 slug、title、page type、aliases、source refs、chunk refs、confidence 和 review-required 状态。 | Must | Backend |
| REQ-WIKI-INGEST-V0-004 | candidate generation 在 v0 必须使用确定性摘要；任何未来 model-assisted generation 必须通过 Atlas ModelAdapter，且不属于本 slice。 | Must | Adapter |
| REQ-WIKI-INGEST-V0-005 | 同一 approved evidence set 重跑必须具备幂等性：按同一 slug 更新或合并，不创建重复页面。 | Must | Backend |
| REQ-WIKI-INGEST-V0-006 | 现有手工发布的 `PUBLISHED_FILE` Wiki pages 必须保持兼容，不能被 generated candidates 静默覆盖。 | Must | Backend |
| REQ-WIKI-INGEST-V0-007 | 所有自动生成或模型辅助内容在后续 review-gate slice 批准前必须保持 `REVIEW_REQUIRED`。 | Must | Governance |
| REQ-WIKI-INGEST-V0-008 | Ingest runs 必须写入安全的 `wiki_generation_run` 和 `wiki_log_entry`，不得包含原始 source text、prompt、provider payload、secret 或私有路径。 | Must | Backend |
| REQ-WIKI-INGEST-V0-009 | failure 和 partial failure 必须用安全 run status 与 safe error summary 表示。 | Must | Backend |
| REQ-WIKI-INGEST-V0-010 | API 必须以 Atlas response envelope 暴露 start-run 与 read-run contract。 | Must | API |
| REQ-WIKI-INGEST-V0-011 | Vue Wiki 和 Processing Center surface 必须展示 generated draft/review-required ingest status，且不暗示可信发布。 | Should | Frontend |
| REQ-WIKI-INGEST-V0-012 | Parser、converter、model、vector、storage、search 行为必须继续位于 Atlas adapter 边界之后。 | Must | Architecture |
| REQ-WIKI-INGEST-V0-013 | 本 slice 只能使用 mock/sample-safe 数据，默认不得引入外部 cloud calls。 | Must | Security |
| REQ-WIKI-INGEST-V0-014 | 验证必须覆盖 backend contracts、idempotency、review-required defaulting、frontend status display、E2E regression、diff hygiene、secret/private-path scan 和 network/dependency scan。 | Must | Verification |

## 明确排除

- Linkify 与 lint rule execution。
- Refresh/retract、stale-source 处理和 protected manual edits。
- 生产 authentication、RBAC、audit retention 或 secret-manager integration。
- Connector sync、manual URL ingest 和 worker dead-letter queues。
- 真实公司文档、默认外部 provider 调用、raw prompts、raw provider logs 或机密 payload。

## 假设

- 本 run 开始前，source chunks 可以先经过 review 并被批准。
- 现有 `wiki_generation_run` 与 `wiki_log_entry` metadata 可复用来追踪 run。
- v0 写入带确定性安全摘要的 generated Markdown artifact，并且只保存安全相对 `markdownPath`。

## 已解决决策

- v0 只创建 `TOPIC` pages。`ENTITY` 和 `CONCEPT` candidates 推迟到后续 graph/wiki extraction slices。
- v0 立即写入 generated Markdown artifacts，内容使用确定性安全摘要。
- v0 confidence aggregation 使用 included chunk confidence 的最低值。
