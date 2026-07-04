# 用户故事：全栈产品化

## 状态

实现草案。由 `req-to-user-story` 流程派生。

## 用户故事

### US-FSP-001：浏览 API-backed spaces

作为知识使用者，我希望从后端看到真实 Knowledge Spaces，以便从 live metadata 进入当前产品工作流。

验收标准：

1. Given 前端已打开，when `GET /api/spaces` 成功，then 用户看到来自 API 的 space 列表。
2. Given spaces API 处于 loading、empty 或失败，then UI 展示明确状态。
3. Given 用户选择一个 space card，then 应用加载 `GET /api/spaces/{spaceId}`。

### US-FSP-002：创建 metadata-only 示例批次

作为交付负责人，我希望从浏览器创建安全示例批次，以便在不使用真实公司文件的情况下验证 upload-to-review 闭环。

验收标准：

1. Given 已选择 space，when 用户开始 sample upload，then UI 将 metadata-only inventory POST 到 `POST /api/spaces/{spaceId}/batches`。
2. Given batch 已创建，then UI 通过 API 加载 batch files 和 source chunks。
3. Given 创建失败，then UI 展示安全错误且不暗示 batch 已创建。

### US-FSP-003：审核并发布可信 Wiki

作为 SME reviewer，我希望批准文件并发布到 Wiki，以便可信知识进入下游能力。

验收标准：

1. Given 一个带 source trace 的 review-required 文件，when reviewer 批准它，then `POST /api/files/{fileId}/reviews` 记录审核。
2. Given 文件已批准，when 用户发布它，then `POST /api/files/{fileId}/publish` 返回 `PUBLISHED` Wiki page。
3. Given Wiki pages 被刷新，then 发布页面出现在 `GET /api/spaces/{spaceId}/wiki-pages`。

### US-FSP-004：检查图谱证据

作为知识消费者，我希望 Graph tab 展示来自已发布文件的 API-backed evidence，以便在信任关系前验证证据。

验收标准：

1. Given 文件已发布，when graph projection 被刷新，then graph API 返回 evidence-backed nodes 或 edges。
2. Given 用户选择 graph evidence，then 可见 source chunk id、source file、confidence、review status。
3. Given graph access 失败，then UI 展示错误状态，而不是假装 mock data 是 live。

### US-FSP-005：带引用 Ask

作为知识消费者，我希望在所选 space 内提问并看到答案证据，以便判断答案是否可信。

验收标准：

1. Given approved/published evidence 已通过后端索引，when 用户提问，then `POST /api/spaces/{spaceId}/ask` 返回已存储 Ask run。
2. Given Ask run 存在，then `GET /api/ask-runs/{runId}` 返回 answer 与 citations。
3. Given answer 已生成，then UI 展示 `REVIEW_REQUIRED` answer status 和 evidence rows。

### US-FSP-006：禁用未接通功能

作为用户，我希望不可用控件明确 disabled，以便不会把原型 affordance 误认为可用产品功能。

验收标准：

1. Given UI 功能仍为 mock-only 或未接通，then 其主要操作 disabled 并标注 coming soon。
2. Given P0 flow 已接通，then 其操作仅在前置条件满足时 enabled。
3. Given 用户尝试不可用路径，then UI 显示非破坏性的 coming-soon 信息。

### US-FSP-007：保留验证层

作为平台维护者，我希望现有 E2E 层保持完整，以便产品化不回归当前验收门。

验收标准：

1. Given 现有命令被运行，then first-layer、second-layer、provider-backed 脚本保留其用途和入口。
2. Given 新 UI-driven E2E 运行，then 主闭环通过浏览器操作完成，而不是只用 API setup 绕过前端。
3. Given 验证发现 skipped checks，then 最终报告列出跳过项和原因。
