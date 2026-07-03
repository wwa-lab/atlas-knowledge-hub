# 用户故事：Review Publish

## 状态

草稿。由 `review-publish` requirements 通过 `req-to-user-story` 推导。

## US-REVIEW-PUBLISH-001：分诊发布阻断审核队列

**故事：** 作为 Delivery Lead，我希望 Processing Center 能区分缺失 trace、低置信度、OCR、解析失败和 LLM 生成待审项，以便在发布前安排优先级。

### 验收标准

1. **Given** 一个 batch 包含多种 issue，**When** Processing Center 加载，**Then** 每类 issue 分别计数，并单独展示 publish-ready 内容。
2. **Given** 某项缺少 `source_trace`，**When** 评估下游资格，**Then** 它被阻断进入 Wiki、Graph 和 Ask。
3. **Given** UI 展示阻断队列，**When** 用户查看 action，**Then** action 保持 mock/contract-driven，直到真实 endpoint 实现。

### 溯源

- Requirements: REQ-REVIEW-PUBLISH-001, REQ-REVIEW-PUBLISH-008, REQ-REVIEW-PUBLISH-009

## US-REVIEW-PUBLISH-002：记录 SME 审核决策

**故事：** 作为 SME Reviewer，我希望用评论和 affected chunks 执行 approve、need-fix 或 OCR required，以便知识质量决策可审计。

### 验收标准

1. **Given** file 或 chunk 需要审核，**When** 我提交 `APPROVE`，**Then** 目标变为 `APPROVED`，并创建追加式 review record。
2. **Given** 我提交 `NEED_FIX` 或 `OCR_REQUIRED`，**When** 请求成功，**Then** 目标状态反映该动作，并继续阻断发布。
3. **Given** 我填写 comment 和 affected chunks，**When** 查询历史，**Then** reviewer、action、timestamp、comment 和 affected chunks 按时间顺序可见。

### 溯源

- Requirements: REQ-REVIEW-PUBLISH-002, REQ-REVIEW-PUBLISH-003

## US-REVIEW-PUBLISH-003：发布已批准 Markdown 到 Wiki 元数据

**故事：** 作为 Knowledge Space Admin，我希望只把已批准 Markdown 发布到 Wiki page 元数据，以便可信下游表面消费已审核内容。

### 验收标准

1. **Given** 候选项具备 `APPROVED` 状态、相对 Markdown path、confidence、source document IDs 和 source trace 覆盖，**When** 请求 publish，**Then** 创建或更新 `PUBLISHED` 状态的 Wiki page。
2. **Given** 候选项未审核、低置信、缺少 trace 或缺少 Markdown path，**When** 请求 publish，**Then** API 返回用户安全 validation error，且不修改 Wiki page。
3. **Given** 发布成功，**When** 读取 Wiki page，**Then** source document IDs、Markdown path、confidence、owner 和 last updated timestamp 被保留。

### 溯源

- Requirements: REQ-REVIEW-PUBLISH-004, REQ-REVIEW-PUBLISH-005, REQ-REVIEW-PUBLISH-007

## US-REVIEW-PUBLISH-004：保留原始证据和适配器边界

**故事：** 作为 Platform Engineer，我希望 publish 只更新元数据而不修改原始 artifact 或直连 adapter，以便 Atlas 保持可追溯和 parser-neutral。

### 验收标准

1. **Given** publish 操作成功，**When** 检查 raw source chunks 和 file artifact paths，**Then** 原始 parser output 和 source trace 保持不变。
2. **Given** 实现接受评审，**When** 检查依赖边界，**Then** publish logic 使用 metadata services/repositories，且不直接调用 parser、converter、model、vector、storage 或 search engine。
3. **Given** 出现错误，**When** 审核 response 和 log，**Then** 不暴露 raw secrets、private absolute paths 或 confidential content。

### 溯源

- Requirements: REQ-REVIEW-PUBLISH-006, REQ-REVIEW-PUBLISH-010, REQ-REVIEW-PUBLISH-011

## 依赖

- Phase 2 metadata API 的 entities 和 review history。
- Phase 3 adapter slices 保持在 seam 后，本切片不调用。
- Processing Center、Wiki 和 Global Chat trust messaging 的已接受 FE 基线。

## 范围外

- Graph node/edge extraction。
- Ask/RAG query execution。
- 生产 authentication/SSO/RBAC enforcement。
- 真实外部 model、vector、search、parser、converter 或 storage 执行。

## 待确认问题

- 初始 publish 是否支持批量操作。
- 首轮实现是否把 chunk-level review endpoints 作为一等能力。
