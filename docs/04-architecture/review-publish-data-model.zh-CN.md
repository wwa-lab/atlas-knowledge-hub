# 数据模型：Review Publish

## 状态

草稿。扩展 Phase 2 metadata model 的 publish 行为。

## 实体关系摘要

```text
space 1:N file_item
space 1:N wiki_page
file_item 1:N source_chunk
file_item 1:N review_record
wiki_page N:1 space
wiki_page N:N source documents via source_document_ids
```

## 复用实体

### `file_item`

相关字段：

- `id`
- `batch_id`
- `source_path`（仅相对路径）
- `status`
- `confidence`
- `review_status`
- `markdown_path`
- `error_message`

Publish 读取 `file_item`；当实现选择 file-level publication status 时，仅可在显式 publish transition 中把 `review_status` 更新为 `PUBLISHED`。Raw source path 和 artifact paths 保持不变。

### `source_chunk`

相关字段：

- `id`
- `file_item_id`
- `source_file`
- `page`
- `section`
- `confidence`
- `review_status`

Publish 读取 source chunks 用于验证 trace coverage，不修改 source chunks。

### `review_record`

追加式字段：

- `id`
- `target_type`
- `target_id`
- `action`
- `reviewer`
- `comment`
- `affected_chunks`
- `created_at`

本切片不增加 update 或 delete 行为。

### `wiki_page`

相关字段：

- `id`
- `space_id`
- `title`
- `markdown_path`
- `source_document_ids`
- `confidence`
- `review_status`
- `owner`
- `last_updated`

Publish 创建或更新该实体，并设置 `review_status=PUBLISHED`。

## 枚举

```text
review_status: REVIEW_REQUIRED | APPROVED | NEED_FIX | OCR_REQUIRED | PUBLISHED
review_action: APPROVE | NEED_FIX | OCR_REQUIRED
```

## 状态规则

- Generated 或 LLM-modified content 默认 `REVIEW_REQUIRED`。
- `APPROVE` 结果为 `APPROVED`；publish 是独立操作。
- `NEED_FIX` 和 `OCR_REQUIRED` 阻断 publish。
- `PUBLISHED` 只对通过 eligibility 的内容有效。

## 校验规则

| Field | Rule |
|---|---|
| `markdown_path` | Publish 必需；相对路径；无 traversal。 |
| `source_document_ids` | Publish 至少一个 id。 |
| `confidence` | Publish 必需；0 到 1。 |
| `review_status` | Publish 前必须为 `APPROVED`。 |
| `source_trace` | 必须至少存在一个 source chunk 或等价 trace record。 |

## 延后模型工作

- Dedicated publish audit entity 延后，除非实现评审发现 review history 不足。
- 生产 user identity 和 RBAC tables 延后到 security/auth 切片。
- Graph nodes/edges 和 Ask indexes 不由本切片修改。
