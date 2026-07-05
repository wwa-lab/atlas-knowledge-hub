# 用户故事：Wiki 自动互链与质量检查

## 状态

供用户审阅的草稿。接受前仍阻塞实现。

## 用户故事

### US-WIKI-LINKIFY-LINT-001：接受 SDD 门

作为产品负责人，我希望代码改动前先有完整双语 SDD，这样切片范围可被审阅。

验收：

- 覆盖 REQ-WIKI-LINKIFY-LINT-001。
- Spec、API guide、tasks 和 traceability 都说明代码需等待用户接受。

### US-WIKI-LINKIFY-LINT-002：运行空间级 Wiki 维护

作为知识管理员，我希望为单个知识空间启动 linkify/lint run，从而一起检查 generated 和 published 页面。

验收：

- 覆盖 REQ-WIKI-LINKIFY-LINT-002、REQ-WIKI-LINKIFY-LINT-011。
- run response 包含 status、counts、updated page ids、issue ids 和 safe summary。

### US-WIKI-LINKIFY-LINT-003：添加安全 Wiki 链接

作为 SME reviewer，我希望常见页面标题和 alias 可以变成 Wiki 链接，同时不破坏 Markdown 格式。

验收：

- 覆盖 REQ-WIKI-LINKIFY-LINT-003 到 REQ-WIKI-LINKIFY-LINT-006。
- linkify 会跳过受保护 Markdown 区域，并保持幂等。

### US-WIKI-LINKIFY-LINT-004：查看链接完整性问题

作为 SME reviewer，我希望 broken links 和 orphan pages 被标记出来，从而决定编辑、合并或忽略。

验收：

- 覆盖 REQ-WIKI-LINKIFY-LINT-007 和 REQ-WIKI-LINKIFY-LINT-008。
- issue 可通过安全 issue API 和 UI warning 查看。

### US-WIKI-LINKIFY-LINT-005：查看证据质量问题

作为知识管理员，我希望缺失溯源、过期证据和内容过薄页面被标记，让 generated Wiki 内容保持可审核可信。

验收：

- 覆盖 REQ-WIKI-LINKIFY-LINT-009 和 REQ-WIKI-LINKIFY-LINT-010。
- issue 保留 page id、issue type、severity、evidence refs 和安全 message。

### US-WIKI-LINKIFY-LINT-006：保持产品安全边界

作为实现负责人，我希望现有 Wiki、ingest、Graph、Ask 表面保持信任边界，而 linkify/lint 只增加 warning。

验收：

- 覆盖 REQ-WIKI-LINKIFY-LINT-012 到 REQ-WIKI-LINKIFY-LINT-014。
- 现有 published pages 只有在已发布时才保持可信；generated pages 继续为 `REVIEW_REQUIRED`。

## 用户故事到需求映射

| Story | Requirements |
|---|---|
| US-WIKI-LINKIFY-LINT-001 | REQ-WIKI-LINKIFY-LINT-001 |
| US-WIKI-LINKIFY-LINT-002 | REQ-WIKI-LINKIFY-LINT-002, REQ-WIKI-LINKIFY-LINT-011 |
| US-WIKI-LINKIFY-LINT-003 | REQ-WIKI-LINKIFY-LINT-003, REQ-WIKI-LINKIFY-LINT-004, REQ-WIKI-LINKIFY-LINT-005, REQ-WIKI-LINKIFY-LINT-006 |
| US-WIKI-LINKIFY-LINT-004 | REQ-WIKI-LINKIFY-LINT-007, REQ-WIKI-LINKIFY-LINT-008 |
| US-WIKI-LINKIFY-LINT-005 | REQ-WIKI-LINKIFY-LINT-009, REQ-WIKI-LINKIFY-LINT-010 |
| US-WIKI-LINKIFY-LINT-006 | REQ-WIKI-LINKIFY-LINT-012, REQ-WIKI-LINKIFY-LINT-013, REQ-WIKI-LINKIFY-LINT-014 |
