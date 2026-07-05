# 用户故事：Wiki Ingest v0

## 状态

供用户接受的草稿。

## Story Map

| Story ID | Requirement IDs | Title |
|---|---|---|
| US-WIKI-INGEST-V0-001 | REQ-WIKI-INGEST-V0-001 | 实现前接受 SDD |
| US-WIKI-INGEST-V0-002 | REQ-WIKI-INGEST-V0-002, 003, 007 | 生成需审核 Wiki candidates |
| US-WIKI-INGEST-V0-003 | REQ-WIKI-INGEST-V0-004, 012, 013 | 保留 adapter 与安全边界 |
| US-WIKI-INGEST-V0-004 | REQ-WIKI-INGEST-V0-005, 006 | 幂等合并 candidates |
| US-WIKI-INGEST-V0-005 | REQ-WIKI-INGEST-V0-008, 009, 010 | 查看安全 ingest run evidence |
| US-WIKI-INGEST-V0-006 | REQ-WIKI-INGEST-V0-011, 014 | 展示并验证 ingest status |

## User Story US-WIKI-INGEST-V0-001

**Title:** 实现前接受 SDD

**Story:**  
作为产品负责人，  
我希望在代码变更前接受 Wiki ingest scope 和 tasks，  
以便团队不会在错误 slice 中意外实现 linkify、review gate、connector 或生产治理能力。

### Acceptance Criteria

1. **Given** 本 slice 被选中  
   **When** agent 准备交付 artifacts  
   **Then** 所有双语 SDD 文件存在，且 REQ、US、T IDs 一致。

2. **Given** SDD 尚未被接受  
   **When** 考虑实现  
   **Then** 产品代码变更仍被阻止。

### Notes / Assumptions

- `wiki-data-model` 已完成，是本 slice 的前置基础。

### Dependencies

- `docs/00-context/wiki-data-model-traceability.md`

### Out of Scope

- 用户接受前的产品代码实现。

### Open Questions

- None.

## User Story US-WIKI-INGEST-V0-002

**Title:** 生成需审核 Wiki candidates

**Story:**  
作为知识管理员，  
我希望 Atlas 从已批准 chunks 创建 draft Wiki candidates，  
以便已审核的来源证据开始成为 durable Wiki pages。

### Acceptance Criteria

1. **Given** 存在带 source trace 的 approved chunks  
   **When** Wiki ingest run 开始  
   **Then** Atlas 创建或更新带 slug、title、source refs、chunk refs、confidence 和 `REVIEW_REQUIRED` 的 Wiki candidates。

2. **Given** 某个 chunk 未批准或缺少 trace  
   **When** run 选择输入  
   **Then** 该 chunk 被排除，并计入 safe run summary。

3. **Given** 生成了 body text  
   **When** 页面被存储  
   **Then** Atlas 写入带安全相对路径的 generated Markdown artifact，并且该页面在后续 review-gate slice 批准前保持不可信。

### Notes / Assumptions

- v0 使用确定性 candidate text 即可。

### Dependencies

- Source chunk review state 与 Wiki data model fields。

### Out of Scope

- Trusted publish、editorial workflow、linkify、lint 和 refresh/retract。

### Open Questions

- None.

## User Story US-WIKI-INGEST-V0-003

**Title:** 保留 adapter 与安全边界

**Story:**  
作为架构负责人，  
我希望 v0 ingest 保持 deterministic，并将任何未来 model-assisted 行为放在 Atlas adapters 之后，  
以便产品保持 provider-neutral 并符合内部数据边界。

### Acceptance Criteria

1. **Given** deterministic mode 已启用  
   **When** run 生成 candidates  
   **Then** 不直接调用 model、vector、parser、converter、storage 或 search engine。

2. **Given** 用户请求 future model-assisted mode  
   **When** v0 评估该请求  
   **Then** 该模式在本 slice 中被拒绝或禁用，并将未来要求记录在 ModelAdapter 边界之后。

3. **Given** run logs 被写入  
   **When** 用户查看  
   **Then** 不暴露 raw prompt、source text、provider response、secret 或 private path。

### Notes / Assumptions

- v0 只使用 deterministic generation。

### Dependencies

- 后续 slice 启用 model-assisted generation 时依赖 Future ModelAdapter boundary。

### Out of Scope

- 新 provider setup 或生产 secret manager。

### Open Questions

- None.

## User Story US-WIKI-INGEST-V0-004

**Title:** 幂等合并 candidates

**Story:**  
作为交付负责人，  
我希望对同一 evidence 的重复 ingest run 更新同一个 Wiki slug，  
以便 retry 和 rerun 不创建重复页面。

### Acceptance Criteria

1. **Given** 同一 space 已存在 candidate slug  
   **When** run 发出同一 slug  
   **Then** Atlas 更新或合并现有 review-required candidate。

2. **Given** 现有页面是 `PUBLISHED_FILE` 且可信  
   **When** generated candidate 命中冲突 slug  
   **Then** Atlas 保留可信页面并记录 safe issue 或 conflict outcome。

3. **Given** 同一 approved chunk set 被处理两次  
   **When** 两次 run 完成  
   **Then** 第二次 run 后 Wiki page count 不增加。

### Notes / Assumptions

- Generated slugs 按 space scope。

### Dependencies

- `wiki_page` `(space_id, slug)` uniqueness。

### Out of Scope

- Manual conflict-resolution UI。

### Open Questions

- None.

## User Story US-WIKI-INGEST-V0-005

**Title:** 查看安全 ingest run evidence

**Story:**  
作为 SME reviewer，  
我希望看到安全 run summaries、counts 和 issues，  
以便理解某个 candidate page 为什么存在或为什么失败，而不会看到敏感 payload。

### Acceptance Criteria

1. **Given** run 成功  
   **When** 请求 run detail  
   **Then** 展示 created/updated page IDs、input reference counts、excluded counts 和 safe summary。

2. **Given** run 部分失败  
   **When** 请求 run detail  
   **Then** 展示 `PARTIAL_FAILED` 和 safe error summaries only。

3. **Given** 存在 run logs  
   **When** 列表展示  
   **Then** 包含 safe lifecycle events，且没有 raw source content。

### Notes / Assumptions

- 现有 `wiki_generation_run` 和 `wiki_log_entry` 对 v0 已足够。

### Dependencies

- Wiki data model support tables。

### Out of Scope

- 长期 audit retention policy。

### Open Questions

- None.

## User Story US-WIKI-INGEST-V0-006

**Title:** 展示并验证 ingest status

**Story:**  
作为知识使用者，  
我希望 generated candidates 被明确标为 review-required，  
以免把 draft Wiki 内容误认为可信已发布知识。

### Acceptance Criteria

1. **Given** 存在 generated candidates  
   **When** Wiki view 渲染  
   **Then** 它们被明确标为 generated 和 review-required。

2. **Given** 不存在 candidates  
   **When** UI 加载  
   **Then** 现有 API-backed Wiki pages 和 sample-safe fallback 行为保持不变。

3. **Given** 运行 verification  
   **When** slice close-out  
   **Then** backend、frontend、E2E、safety、diff checks 被报告，或明确说明跳过原因。

### Notes / Assumptions

- UI change 仅展示 status；v0 不引入 editor 或 review action。

### Dependencies

- API run 和 page metadata responses。

### Out of Scope

- Review-gate actions 和 page editor。

### Open Questions

- None.
