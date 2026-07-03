# Data Model: Metadata API

## Status

Draft. Phase 2. Slice `metadata-api`. Persisted PostgreSQL schema (Flyway). Derived from `docs/03-spec/metadata-api-spec.md`, and reconciled with `docs/04-architecture/knowledge-space-data-model.md` and `docs/04-architecture/folder-upload-data-model.md`.

This is the **persisted** contract (DB tables), distinct from the FE in-memory TypeScript types. Field names use `snake_case` for columns; API DTOs may expose `camelCase`.

## Enums (persisted as text with a CHECK or enum type)

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

`file_status` is exactly the allowed set from `docs/batch-processing-design.md` — this slice adds none.

## Tables

### `space`  (REQ-MA-002)

| Column | Type | Notes |
|---|---|---|
| `id` | text PK | Stable slug, e.g. `ibm-i-modernization`. |
| `name` | text NOT NULL | Display name. |
| `description` | text | Product-facing summary. |
| `type` | space_type NOT NULL | `document` / `faq`. |
| `index_strategy` | index_strategy NOT NULL | `rag` / `wiki`. |
| `owner` | text | Responsible team/person. |
| `status` | space_status NOT NULL DEFAULT `HEALTHY` | Server-defaulted. |
| `document_count` | int NOT NULL DEFAULT 0 | Denormalized display counter (maintained by app). |
| `wiki_page_count` | int NOT NULL DEFAULT 0 | Display counter. |
| `review_count` | int NOT NULL DEFAULT 0 | Open review count. |
| `created_at` | timestamptz NOT NULL | Server-set. |
| `updated_at` | timestamptz NOT NULL | Server-set. |

### `batch`  (REQ-MA-003)

| Column | Type | Notes |
|---|---|---|
| `id` | text PK | Batch identifier. |
| `space_id` | text NOT NULL FK → `space.id` | Owning space. |
| `name` | text NOT NULL | Source package name. |
| `source_kind` | source_kind NOT NULL | `folder` / `zip`. |
| `owner` | text | Uploader/team. |
| `uploaded_at` | timestamptz NOT NULL | Creation time. |

Metrics are **not** stored — derived from `file_item` on read (spec State Model). Index: `(space_id, uploaded_at)`.

### `file_item`  (REQ-MA-004, 011)

| Column | Type | Notes |
|---|---|---|
| `id` | text PK | File item id. |
| `batch_id` | text NOT NULL FK → `batch.id` | Owning batch. |
| `source_path` | text NOT NULL | **Relative only**; traversal rejected at boundary. |
| `source_type` | source_type NOT NULL | Original type. |
| `status` | file_status NOT NULL | Allowed set only. |
| `confidence` | numeric(4,3) | 0–1. |
| `review_status` | review_status NOT NULL DEFAULT `REVIEW_REQUIRED` | Never `APPROVED` by default. |
| `pdf_path` | text | Relative artifact path if available. |
| `markdown_path` | text | Relative artifact path if available. |
| `assets_path` | text | Relative artifact path if available. |
| `error_message` | text | User-safe summary when failed. |
| `created_at` | timestamptz NOT NULL | Server-set. |

Index: `(batch_id, status)`.

### `source_chunk`  (REQ-MA-005, 011)

| Column | Type | Notes |
|---|---|---|
| `id` | text PK | Chunk id. |
| `file_item_id` | text NOT NULL FK → `file_item.id` | Owning file. |
| `source_file` | text NOT NULL | Source file name. |
| `page` | int | Page number when available. |
| `section` | text | Section label when available. |
| `confidence` | numeric(4,3) | Chunk-level confidence. |
| `review_status` | review_status NOT NULL DEFAULT `REVIEW_REQUIRED` | Chunk review state. |

Index: `(file_item_id)`.

### `review_record`  (REQ-MA-006) — append-only

| Column | Type | Notes |
|---|---|---|
| `id` | bigserial PK | Monotonic id. |
| `target_type` | text NOT NULL | `file` / `chunk`. |
| `target_id` | text NOT NULL | file_item or source_chunk id. |
| `action` | review_action NOT NULL | `APPROVE` / `NEED_FIX` / `OCR_REQUIRED`. |
| `reviewer` | text NOT NULL | Reviewer identity/team (mock). |
| `comment` | text | SME comment. |
| `affected_chunks` | text[] | Optional chunk ids. |
| `created_at` | timestamptz NOT NULL | Server timestamp. |

Rows are never updated or deleted (append-only). Index: `(target_type, target_id, created_at)`.

### `wiki_page`  (REQ-MA-007; table only, no endpoint this slice)

| Column | Type | Notes |
|---|---|---|
| `id` | text PK | Page id. |
| `space_id` | text NOT NULL FK → `space.id` | Owning space. |
| `title` | text NOT NULL | Page title. |
| `markdown_path` | text | Relative Markdown location. |
| `source_document_ids` | text[] | Documents used. |
| `confidence` | numeric(4,3) | Page-level confidence. |
| `review_status` | review_status NOT NULL DEFAULT `REVIEW_REQUIRED` | Page review state. |
| `owner` | text | Responsible reviewer/team. |
| `last_updated` | timestamptz | Last update. |

### `graph_node`  (REQ-MA-007; table only, no endpoint this slice)

| Column | Type | Notes |
|---|---|---|
| `id` | text PK | Node id. |
| `space_id` | text NOT NULL FK → `space.id` | Owning space. |
| `label` | text NOT NULL | Display label. |
| `type` | graph_node_type NOT NULL | Node type. |
| `review_status` | review_status NOT NULL DEFAULT `REVIEW_REQUIRED` | Gate relation. |
| `evidence_chunk_ids` | text[] | Backing evidence. |

### `graph_edge`  (REQ-MA-007; table only, no endpoint this slice)

| Column | Type | Notes |
|---|---|---|
| `id` | text PK | Edge id. |
| `space_id` | text NOT NULL FK → `space.id` | Owning space. |
| `source_node_id` | text NOT NULL FK → `graph_node.id` | Source. |
| `target_node_id` | text NOT NULL FK → `graph_node.id` | Target. |
| `type` | graph_edge_type NOT NULL | Relationship. |
| `evidence_chunk_ids` | text[] | Required for trustable edges. |
| `review_status` | review_status NOT NULL DEFAULT `REVIEW_REQUIRED` | Edge review state. |

## Migrations (Flyway, ordered, immutable)

- `V1__init_schema.sql` — enum types (or CHECK constraints), all 8 tables, FKs, indexes.
- `V2__seed_mock_metadata.sql` — mock/sample rows aligned to FE baseline: spaces, ≥1 batch, file items across representative statuses, chunks, one review record, plus sample wiki-page/graph rows. **No real company data, no secrets, relative paths only.**

`spring.jpa.hibernate.ddl-auto=validate`. Migrations are version-ordered and never edited after release; new changes are new versions.

## Invariants

- `file_status` values are exactly the `docs/batch-processing-design.md` set; no additions.
- `review_status` defaults to `REVIEW_REQUIRED` for generated/low-confidence content; only an explicit review action advances it; `PUBLISHED` is set only by a later publish slice.
- `source_path` and all artifact paths are relative; absolute/traversal paths are rejected before persistence.
- Batch metrics are derived from `file_item`, never stored (single source of truth).
- `review_record` is append-only; no update/delete.
- No column stores a raw secret, credential, private endpoint, or real company content.

## Relationship To FE Types

`file_status`, `review_status`, batch/file shapes must reconcile with `frontend/src/types.ts` (`FileStatus`, `ReviewStatus`, `Batch`, `FileItem`) and the folder-upload data model. Where the FE mock and this schema differ in casing (camelCase vs snake_case), the DTO mapping layer bridges them.

- **`file_status`: identical values.** The persisted set equals the FE `FileStatus` union and `docs/batch-processing-design.md` — same strings, no divergence.
- **`review_status`: intentional divergence.** The persisted set (`REVIEW_REQUIRED | APPROVED | NEED_FIX | OCR_REQUIRED | PUBLISHED`) follows **REQ-PROD-030**, whereas `frontend/src/types.ts:44` currently defines a narrower `ReviewStatus = 'REVIEW_REQUIRED' | 'APPROVED' | 'REJECTED'`. This is a deliberate superset, not a bug: the product review model (Approve / Need Fix / OCR Required / Published) is authoritative for persistence. The DTO layer maps a legacy FE `REJECTED` onto `NEED_FIX`; realigning the FE `ReviewStatus` type is a follow-up at FE cutover (T-MA-014). Do **not** silently collapse the persisted set to the FE type.
