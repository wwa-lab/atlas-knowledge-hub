# 数据模型：Metadata API

## 状态

草稿。Phase 2。切片 `metadata-api`。持久化 PostgreSQL schema（Flyway）。派生自 `docs/03-spec/metadata-api-spec.md`，并与 `docs/04-architecture/knowledge-space-data-model.md` 及 `docs/04-architecture/folder-upload-data-model.md` 对齐。

这是**持久化**契约（数据库表），区别于前端内存 TypeScript 类型。列名用 `snake_case`；API DTO 可暴露 `camelCase`。

## 枚举（以文本 + CHECK 或 enum 类型持久化）

```
file_status     : NEW | UPLOADED | PDF_CONVERTED | PDF_CONVERT_FAILED |
                  MARKDOWN_GENERATED | OCR_REQUIRED | LOW_CONFIDENCE |
                  REVIEW_REQUIRED | APPROVED | PUBLISHED | FAILED | UNSUPPORTED
review_status   : REVIEW_REQUIRED | APPROVED | NEED_FIX | OCR_REQUIRED | PUBLISHED
review_action   : APPROVE | NEED_FIX | OCR_REQUIRED
source_kind     : folder | zip
source_type     : pptx | docx | pdf | xlsx | image | unsupported
space_type      : document | faq
index_strategy  : rag | wiki
space_status    : HEALTHY | REVIEW_REQUIRED | PARSING
graph_node_type : KNOWLEDGE_SPACE | DOCUMENT | WIKI_PAGE | CONCEPT | ENTITY | SOURCE_CHUNK
graph_edge_type : CONTAINS | DERIVED_FROM | MENTIONS | DEFINES | RELATED_TO |
                  BELONGS_TO | USES | DEPENDS_ON | REVIEWED_BY
```

`file_status` 严格取自 `docs/batch-processing-design.md` 的允许集合——本切片不增加任何值。

## 表

### `space`（REQ-MA-002）

| 列 | 类型 | 说明 |
|---|---|---|
| `id` | text PK | 稳定 slug，如 `ibm-i-modernization`。 |
| `name` | text NOT NULL | 显示名。 |
| `description` | text | 面向产品的摘要。 |
| `type` | space_type NOT NULL | `document` / `faq`。 |
| `index_strategy` | index_strategy NOT NULL | `rag` / `wiki`。 |
| `owner` | text | 负责团队/人。 |
| `status` | space_status NOT NULL DEFAULT `HEALTHY` | 服务端默认。 |
| `document_count` | int NOT NULL DEFAULT 0 | 反规范化显示计数（应用维护）。 |
| `wiki_page_count` | int NOT NULL DEFAULT 0 | 显示计数。 |
| `review_count` | int NOT NULL DEFAULT 0 | 开放 review 计数。 |
| `created_at` | timestamptz NOT NULL | 服务端设置。 |
| `updated_at` | timestamptz NOT NULL | 服务端设置。 |

### `batch`（REQ-MA-003）

| 列 | 类型 | 说明 |
|---|---|---|
| `id` | text PK | 批次标识。 |
| `space_id` | text NOT NULL FK → `space.id` | 所属 space。 |
| `name` | text NOT NULL | 源包名。 |
| `source_kind` | source_kind NOT NULL | `folder` / `zip`。 |
| `owner` | text | 上传者/团队。 |
| `uploaded_at` | timestamptz NOT NULL | 创建时间。 |

metrics **不**存储——读取时由 `file_item` 派生（见规格状态模型）。索引：`(space_id, uploaded_at)`。

### `file_item`（REQ-MA-004、011）

| 列 | 类型 | 说明 |
|---|---|---|
| `id` | text PK | file item id。 |
| `batch_id` | text NOT NULL FK → `batch.id` | 所属批次。 |
| `source_path` | text NOT NULL | **仅相对**；边界处拒绝穿越。 |
| `source_type` | source_type NOT NULL | 原始类型。 |
| `status` | file_status NOT NULL | 仅允许集合。 |
| `confidence` | numeric(4,3) | 0–1。 |
| `review_status` | review_status NOT NULL DEFAULT `REVIEW_REQUIRED` | 绝不默认 `APPROVED`。 |
| `pdf_path` | text | 可用时的相对产物路径。 |
| `markdown_path` | text | 可用时的相对产物路径。 |
| `assets_path` | text | 可用时的相对产物路径。 |
| `error_message` | text | 失败时的用户安全摘要。 |
| `created_at` | timestamptz NOT NULL | 服务端设置。 |

索引：`(batch_id, status)`。

### `source_chunk`（REQ-MA-005、011）

| 列 | 类型 | 说明 |
|---|---|---|
| `id` | text PK | chunk id。 |
| `file_item_id` | text NOT NULL FK → `file_item.id` | 所属文件。 |
| `source_file` | text NOT NULL | 源文件名。 |
| `page` | int | 可用时的页码。 |
| `section` | text | 可用时的 section 标签。 |
| `confidence` | numeric(4,3) | chunk 级置信度。 |
| `review_status` | review_status NOT NULL DEFAULT `REVIEW_REQUIRED` | chunk review 状态。 |

索引：`(file_item_id)`。

### `review_record`（REQ-MA-006）—— 只追加

| 列 | 类型 | 说明 |
|---|---|---|
| `id` | bigserial PK | 单调 id。 |
| `target_type` | text NOT NULL | `file` / `chunk`。 |
| `target_id` | text NOT NULL | file_item 或 source_chunk id。 |
| `action` | review_action NOT NULL | `APPROVE` / `NEED_FIX` / `OCR_REQUIRED`。 |
| `reviewer` | text NOT NULL | Reviewer 身份/团队（mock）。 |
| `comment` | text | SME 评论。 |
| `affected_chunks` | text[] | 可选 chunk id。 |
| `created_at` | timestamptz NOT NULL | 服务端时间戳。 |

行绝不更新或删除（只追加）。索引：`(target_type, target_id, created_at)`。

### `wiki_page`（REQ-MA-007；仅建表，本切片无端点）

| 列 | 类型 | 说明 |
|---|---|---|
| `id` | text PK | 页 id。 |
| `space_id` | text NOT NULL FK → `space.id` | 所属 space。 |
| `title` | text NOT NULL | 页标题。 |
| `markdown_path` | text | 相对 Markdown 位置。 |
| `source_document_ids` | text[] | 使用的文档。 |
| `confidence` | numeric(4,3) | 页级置信度。 |
| `review_status` | review_status NOT NULL DEFAULT `REVIEW_REQUIRED` | 页 review 状态。 |
| `owner` | text | 负责 reviewer/团队。 |
| `last_updated` | timestamptz | 最后更新。 |

### `graph_node`（REQ-MA-007；仅建表，本切片无端点）

| 列 | 类型 | 说明 |
|---|---|---|
| `id` | text PK | 节点 id。 |
| `space_id` | text NOT NULL FK → `space.id` | 所属 space。 |
| `label` | text NOT NULL | 显示标签。 |
| `type` | graph_node_type NOT NULL | 节点类型。 |
| `review_status` | review_status NOT NULL DEFAULT `REVIEW_REQUIRED` | gate 关系。 |
| `evidence_chunk_ids` | text[] | 支撑证据。 |

### `graph_edge`（REQ-MA-007；仅建表，本切片无端点）

| 列 | 类型 | 说明 |
|---|---|---|
| `id` | text PK | 边 id。 |
| `space_id` | text NOT NULL FK → `space.id` | 所属 space。 |
| `source_node_id` | text NOT NULL FK → `graph_node.id` | 源。 |
| `target_node_id` | text NOT NULL FK → `graph_node.id` | 目标。 |
| `type` | graph_edge_type NOT NULL | 关系。 |
| `evidence_chunk_ids` | text[] | 可信边所需。 |
| `review_status` | review_status NOT NULL DEFAULT `REVIEW_REQUIRED` | 边 review 状态。 |

## Migration（Flyway，有序、不可变）

- `V1__init_schema.sql` —— 枚举类型（或 CHECK 约束）、全部 8 张表、FK、索引。
- `V2__seed_mock_metadata.sql` —— 与前端基线对齐的 mock/示例行：spaces、≥1 批次、跨代表性状态的 file item、chunk、一条 review 记录，以及示例 wiki-page/graph 行。**无真实公司数据、无 secret，仅相对路径。**

`spring.jpa.hibernate.ddl-auto=validate`。migration 按版本排序、发布后绝不编辑；新改动即新版本。

## 不变量

- `file_status` 值严格为 `docs/batch-processing-design.md` 集合；不增加。
- `review_status` 对生成/低置信度内容默认 `REVIEW_REQUIRED`；只有显式 review 动作推进；`PUBLISHED` 只由后续发布切片设置。
- `source_path` 与所有产物路径为相对；持久化前拒绝绝对/穿越路径。
- 批次 metrics 由 `file_item` 派生，绝不存储（单一真实来源）。
- `review_record` 只追加；无更新/删除。
- 任何列不存储原始 secret、凭证、私有端点或真实公司内容。

## 与前端类型的关系

`file_status`、`review_status`、batch/file 形态须与 `frontend/src/types.ts`（`FileStatus`、`ReviewStatus`、`Batch`、`FileItem`）及 folder-upload 数据模型对齐。前端 mock 与本 schema 大小写不同（camelCase vs snake_case）时，由 DTO 映射层桥接。

- **`file_status`：值完全一致。** 持久化集合等于前端 `FileStatus` 联合与 `docs/batch-processing-design.md`——相同字符串，无分叉。
- **`review_status`：有意分叉。** 持久化集合（`REVIEW_REQUIRED | APPROVED | NEED_FIX | OCR_REQUIRED | PUBLISHED`）遵循 **REQ-PROD-030**，而 `frontend/src/types.ts:44` 当前定义了更窄的 `ReviewStatus = 'REVIEW_REQUIRED' | 'APPROVED' | 'REJECTED'`。这是刻意的超集而非缺陷：产品 review 模型（Approve / Need Fix / OCR Required / Published）对持久化具权威性。DTO 层把遗留的前端 `REJECTED` 映射为 `NEED_FIX`；重新对齐前端 `ReviewStatus` 类型是 FE 切换时的后续（T-MA-014）。**不得**把持久化集合悄悄收敛为前端类型。
