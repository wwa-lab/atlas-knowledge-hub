# Data Model: Wiki Data Model

## Status

Draft for user acceptance. Logical model for future implementation; exact SQL belongs in Flyway after acceptance.

## Overview

This slice extends the existing `atlas.wiki_page` table and adds four support tables. The model is additive and must preserve existing V1-V9 contracts, seeded mock data, and review-publish behavior.

## Entity Relationship Diagram

```text
┌──────────────┐ 1:N ┌──────────────┐
│ space        │────▶│ wiki_folder  │
└──────────────┘     └──────┬───────┘
        │ 1:N               │ 1:N optional parent
        ▼                   ▼
┌─────────────────────────────────┐
│ wiki_page                       │
│ folder_id nullable              │
└──────┬───────────┬──────────────┘
       │ 1:N       │ 1:N
       ▼           ▼
┌──────────────┐ ┌──────────────────┐
│ wiki_log     │ │ wiki_page_issue  │
└──────────────┘ └──────────────────┘

┌──────────────────────┐
│ wiki_generation_run  │ N:1 space, optional page
└──────────────────────┘
```

## Entity Definitions

### `wiki_page`

Existing table extended additively.

| Column | Logical type | Nullable | Description |
|---|---|---:|---|
| `id` | String | No | Stable page id. |
| `space_id` | String | No | Owning Knowledge Space. |
| `folder_id` | String | Yes | Optional folder. |
| `title` | String | No | Display title. |
| `slug` | String | No | Stable URL/lookup key scoped by `space_id`. |
| `page_type` | String enum | No | `INDEX`, `TOPIC`, `SOURCE_SUMMARY`, `ENTITY`, `CONCEPT`, `MANUAL`. |
| `markdown_path` | String | Yes | Safe relative Markdown artifact path. |
| `source_document_ids` | String array | Yes | Existing compatibility field. |
| `aliases` | String array | No | Alternate names for lookup/display. |
| `source_refs` | JSON | No | Safe source references such as file ids, labels, path labels, page/section hints. |
| `chunk_refs` | JSON | No | Safe chunk references by id and optional label. |
| `in_links` | String array | No | Incoming Wiki page slugs/ids. |
| `out_links` | String array | No | Outgoing Wiki page slugs/ids. |
| `version` | Integer | No | Starts at 1; increments on future refresh/update. |
| `source_mode` | String enum | No | `PUBLISHED_FILE`, `AUTO_GENERATED`, `MANUAL`, `HYBRID`. |
| `refresh_policy` | String enum | No | `MANUAL`, `ON_SOURCE_CHANGE`, `SCHEDULED`, `LOCKED`. |
| `confidence` | Decimal | Yes | 0 to 1. |
| `review_status` | String enum | No | Existing review status values. |
| `owner` | String | Yes | Safe owner label. |
| `last_updated` | Timestamp | Yes | Last metadata update time. |

Constraints and indexes:

- Primary key: `id`.
- Foreign keys: `space_id -> space(id)`, `folder_id -> wiki_folder(id)`.
- Unique: `(space_id, slug)`.
- Index: `(space_id, review_status, title)` for current list behavior.
- Index: `(space_id, page_type)`.
- Check: `version >= 1`.
- Check: enum values listed above.

Migration defaults for existing rows:

| Field | Default rule |
|---|---|
| `slug` | Deterministic slug derived from `markdown_path`, else `title`, else `id`; collision-resolved per space. |
| `page_type` | `SOURCE_SUMMARY`. |
| `aliases` | Empty array. |
| `source_refs` | Derived from `source_document_ids` as safe id refs. |
| `chunk_refs` | Empty JSON array unless source chunks can be safely derived during implementation. |
| `in_links` / `out_links` | Empty arrays. |
| `version` | `1`. |
| `source_mode` | `PUBLISHED_FILE`. |
| `refresh_policy` | `MANUAL`. |

### `wiki_folder`

| Column | Logical type | Nullable | Description |
|---|---|---:|---|
| `id` | String | No | Stable folder id. |
| `space_id` | String | No | Owning space. |
| `parent_folder_id` | String | Yes | Parent folder in same space. |
| `slug` | String | No | Folder slug scoped by space. |
| `name` | String | No | Display name. |
| `description` | String | Yes | Safe description. |
| `sort_order` | Integer | No | Deterministic display order. |
| `created_at` | Timestamp | No | Created time. |
| `updated_at` | Timestamp | No | Updated time. |

Constraints:

- Unique `(space_id, slug)`.
- Parent folder must belong to the same space when present.
- No permission inheritance in this slice.

### `wiki_generation_run`

| Column | Logical type | Nullable | Description |
|---|---|---:|---|
| `id` | String | No | Stable run id. |
| `space_id` | String | No | Owning space. |
| `page_id` | String | Yes | Optional target page. |
| `status` | String enum | No | `REQUESTED`, `RUNNING`, `SUCCEEDED`, `PARTIAL_FAILED`, `FAILED`, `CANCELLED`. |
| `source_mode` | String enum | No | Same value set as page `source_mode`. |
| `refresh_policy` | String enum | No | Same value set as page `refresh_policy`. |
| `requested_by` | String | Yes | Safe actor label. |
| `input_source_refs` | JSON | No | Safe input references. |
| `created_page_ids` | String array | No | Safe page ids. |
| `updated_page_ids` | String array | No | Safe page ids. |
| `issue_ids` | String array | No | Safe issue ids. |
| `safe_summary` | String | Yes | Safe summary. |
| `safe_error` | String | Yes | Sanitized error. |
| `started_at` | Timestamp | Yes | Start time. |
| `finished_at` | Timestamp | Yes | End time. |

### `wiki_log_entry`

| Column | Logical type | Nullable | Description |
|---|---|---:|---|
| `id` | String | No | Stable log id. |
| `space_id` | String | No | Owning space. |
| `page_id` | String | Yes | Related page. |
| `run_id` | String | Yes | Related generation run. |
| `event_type` | String enum | No | `PUBLISHED`, `REPUBLISHED`, `METADATA_UPDATED`, `RUN_STARTED`, `RUN_FINISHED`, `ISSUE_RECORDED`. |
| `actor` | String | Yes | Safe actor label. |
| `message` | String | Yes | Sanitized message. |
| `metadata` | JSON | No | Safe metadata only. |
| `created_at` | Timestamp | No | Event time. |

### `wiki_page_issue`

| Column | Logical type | Nullable | Description |
|---|---|---:|---|
| `id` | String | No | Stable issue id. |
| `space_id` | String | No | Owning space. |
| `page_id` | String | No | Affected page. |
| `issue_type` | String enum | No | `STALE_SOURCE`, `BROKEN_LINK`, `ORPHAN_PAGE`, `THIN_CONTENT`, `REVIEW_REQUIRED`, `MISSING_SOURCE_REF`. |
| `severity` | String enum | No | `INFO`, `LOW`, `MEDIUM`, `HIGH`. |
| `status` | String enum | No | `OPEN`, `ACKNOWLEDGED`, `RESOLVED`, `IGNORED`. |
| `evidence_refs` | JSON | No | Safe evidence ids/labels. |
| `message` | String | Yes | Sanitized summary. |
| `created_at` | Timestamp | No | Created time. |
| `resolved_at` | Timestamp | Yes | Resolved time. |

## State Models

Generation run:

```text
REQUESTED -> RUNNING -> SUCCEEDED
REQUESTED -> RUNNING -> PARTIAL_FAILED
REQUESTED -> RUNNING -> FAILED
REQUESTED -> CANCELLED
```

Issue:

```text
OPEN -> ACKNOWLEDGED -> RESOLVED
OPEN -> IGNORED
ACKNOWLEDGED -> RESOLVED
```

## Field Mapping

| Existing field/source | New field | Mapping |
|---|---|---|
| `source_document_ids` | `source_refs` | Convert each id to `{ "type": "FILE", "id": "<id>" }`. |
| `source_chunk.id` | `chunk_refs` | Convert to `{ "type": "SOURCE_CHUNK", "id": "<id>" }` when available. |
| `markdown_path` | `slug` | Derive stable slug from filename/path when no explicit slug exists. |
| Publish operation | `wiki_log_entry` | Optional safe lifecycle entry after implementation acceptance. |

## Safety Rules

- JSON fields store safe IDs, labels, enum values, counts, and relative references only.
- No raw document text, private absolute paths, provider payloads, raw prompts, API keys, or stack traces.
- All write behavior remains local metadata behavior; no external cloud calls.
