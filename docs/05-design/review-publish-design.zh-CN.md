# 详细设计：Review Publish

## 状态

草稿。由 Review Publish architecture 推导。

## 设计范围

本设计覆盖 review queue 加固、SME review 状态转换、publish eligibility、Wiki page metadata publication 和 FE status rendering 的全栈契约。不实现 graph extraction、Ask/RAG、production auth 或 direct adapter execution。

## 模块设计

### Review Queue Module

- 从 file items、source chunks 和 review statuses 派生 queue counts。
- 返回 blocked categories：parser failure、OCR required、low confidence、missing trace、LLM-generated review-required。
- 每个 queue 返回有界 representative item metadata，且只使用安全字段：file id、file status、review status、confidence 和 trace-presence flag。Queue representative items 不得暴露 raw source paths、raw document content、private absolute paths 或 secrets。
- 仅当候选项满足最终 publish action 之前的 eligibility inputs 时，才进入 publish-ready candidates。

### Review Decision Module

- 复用 append-only review record 行为。
- 保持 `PUBLISHED` 不作为 review action result。
- 允许未来 chunk-level 扩展，而不改变 file-level history semantics。

### Publish Module

- 加载 candidate file 和 source trace metadata。
- 在 mutation 前应用全部 eligibility rules。
- 创建或更新 Wiki page metadata record。
- 将 published metadata 设置为 `PUBLISHED`。
- 使用单事务，确保 eligibility failure 或 conflict 不留下 partial result。

### Frontend Module

- Processing Center 展示 blocked counts 和 ready-to-publish count。
- Review actions 只作为允许的 workflow affordances 可见；不得暗示生产 RBAC。
- Wiki 表面展示 published status 和 source/confidence metadata。
- Global Chat trust copy 继续明确只使用 reviewed、source-traced、publishable assets。

## API / Interface Design

本切片必须使用 `docs/05-design/contracts/review-publish-API_IMPLEMENTATION_GUIDE.md` 中的 API guide。

## 校验与错误处理

| Case | Expected Handling |
|---|---|
| Candidate not approved | 使用用户安全 validation/conflict response 拒绝。 |
| Missing source trace | 拒绝，并将 `sourceTrace` 标为 blocked field。 |
| Absolute or traversal path | 拒绝；绝不回显 private absolute paths。 |
| Missing confidence | 拒绝，并保持 metadata 不变。 |
| Duplicate publish | 根据测试中记录的实现选择，返回 existing published metadata 或 conflict。 |

## UI / User Flow Design

1. 用户打开 Processing Center。
2. 用户看到 blocked queues 和 ready-to-publish content。
3. Reviewer 通过 review actions 解决 review-required items。
4. Admin 发布 eligible approved content。
5. Wiki tab 反映 published page metadata。
6. Graph/Ask copy 继续表明它们只是下游消费。

## 测试考虑

- Eligibility 和 state transitions 的 backend unit tests。
- Queues、review history、publish success、blocked publish 的 backend integration/API contract tests。
- Queue API contract tests 必须同时断言 queue counts/categories 和 representative item shape，包括 missing-trace examples 带有 `hasSourceTrace=false`。
- Processing Center counts、action states、published Wiki display、trust copy 的 frontend tests。
- Approve-to-publish happy path 和 blocked missing-trace path 的 E2E。
- 扫描 direct adapter calls、new network dependencies、secrets、private paths。

## 风险 / 取舍

- Single-item publish 保持实现可评审；bulk publish 延后，除非另行接受。
- File-level publish 对 MVP 可能足够，但细粒度 SME workflow 可能需要 chunk-level publish。
- Production RBAC 在外部暴露前需要后续加固。

## 待确认问题

- Duplicate publish 应 idempotent 还是 conflict？
- Publish 应更新 `file_item.review_status` 到 `PUBLISHED`，还是只更新 `wiki_page.review_status`？
