# 数据模型：Wiki 自动互链与质量检查

## 状态

供用户审阅的草稿。这里只定义逻辑模型；具体 migration 在接受后实现。

## 复用现有实体

### `wiki_page`

| Field | Rule |
|---|---|
| `slug` | canonical link target id。 |
| `aliases` | 当无歧义时可解析到 canonical slug 的补充术语。 |
| `markdown_path` | 通过安全 storage 读取和重写的 artifact path。 |
| `source_refs` / `chunk_refs` | missing 与 stale source lint 的证据。 |
| `in_links` | 链接到本页面的 canonical source slugs。 |
| `out_links` | 本页面链接出去的 canonical target slugs。 |
| `version` | Markdown 被重写或 link metadata 变化时递增。 |
| `source_mode` | 保持不变。 |
| `refresh_policy` | 保持不变。 |
| `confidence` | 保持不变。 |
| `review_status` | 保持不变。 |

### `wiki_generation_run`

| Field | Rule |
|---|---|
| `mode` | 在可接受 mode values 中增加 `linkify-lint`。 |
| `created_page_ids` | 本切片为空。 |
| `updated_page_ids` | Markdown 或 link metadata 变化的页面。 |
| `issue_ids` | 本 run 打开或刷新的 issues。 |
| `safe_summary` | 只包含计数。 |
| `safe_error` | 脱敏后的失败消息。 |

### `wiki_page_issue`

| Issue type | Use |
|---|---|
| `BROKEN_LINK` | Target slug 在同空间不存在。 |
| `ORPHAN_PAGE` | 页面没有 incoming links 且不豁免。 |
| `THIN_CONTENT` | 页面正文去除 markup 后为空或过短。 |
| `MISSING_SOURCE_REF` | 页面缺少安全 source 与 chunk refs。 |
| `STALE_SOURCE` | Source document 或 chunk ref 不再可解析。 |
| `REVIEW_REQUIRED` | Alias 有歧义或 unsafe rewrite 需要人工审核。 |

### `wiki_log_entry`

| Event | Use |
|---|---|
| `RUN_STARTED` | Run 已接受。 |
| `METADATA_UPDATED` | Link metadata 或 artifact 已变化。 |
| `ISSUE_RECORDED` | Lint issue 已打开/刷新。 |
| `RUN_FINISHED` | Run 结束。 |

## DTOs

新增或扩展 DTO 应包括：

- `CreateWikiLinkifyLintRunRequest`
- `WikiLinkifyLintRunResponse`
- 可选 `WikiIssueSummaryResponse`

DTO 必须只使用安全 labels 与 ids，不得包含 raw Markdown text。

## 索引与查询

- 需要现有 `wiki_page(space_id, slug)` 查询路径。
- 增加或复用按 `spaceId`、`status`、`issueType`、`pageId` 查询 issue 的路径。
- 查询变更保持增量，并由 Flyway 管理。
