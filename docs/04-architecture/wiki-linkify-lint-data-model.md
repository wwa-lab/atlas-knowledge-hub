# Data Model: Wiki Linkify And Lint

## Status

Draft for user review. Logical model only; exact migration belongs to implementation after acceptance.

## Existing Entities Reused

### `wiki_page`

| Field | Rule |
|---|---|
| `slug` | Canonical link target id. |
| `aliases` | Additional terms that may resolve to the canonical slug when unambiguous. |
| `markdown_path` | Artifact path to read and rewrite through safe storage. |
| `source_refs` / `chunk_refs` | Evidence for missing and stale source lint. |
| `in_links` | Canonical source slugs linking to the page. |
| `out_links` | Canonical target slugs linked from the page. |
| `version` | Incremented when linkify rewrites Markdown or link metadata changes. |
| `source_mode` | Preserved. |
| `refresh_policy` | Preserved. |
| `confidence` | Preserved. |
| `review_status` | Preserved. |

### `wiki_generation_run`

| Field | Rule |
|---|---|
| `mode` | Add `linkify-lint` to the accepted mode values. |
| `created_page_ids` | Empty for this slice. |
| `updated_page_ids` | Pages whose Markdown or link metadata changed. |
| `issue_ids` | Issues opened or refreshed by the run. |
| `safe_summary` | Counts only. |
| `safe_error` | Sanitized failure message. |

### `wiki_page_issue`

| Issue type | Use |
|---|---|
| `BROKEN_LINK` | Target slug is absent in the same space. |
| `ORPHAN_PAGE` | Page has no incoming links and is not exempt. |
| `THIN_CONTENT` | Page body is empty or too small after markup stripping. |
| `MISSING_SOURCE_REF` | Page lacks safe source and chunk refs. |
| `STALE_SOURCE` | Source document or chunk ref no longer resolves. |
| `REVIEW_REQUIRED` | Ambiguous alias or unsafe rewrite needs manual review. |

### `wiki_log_entry`

| Event | Use |
|---|---|
| `RUN_STARTED` | Run accepted. |
| `METADATA_UPDATED` | Link metadata or artifact changed. |
| `ISSUE_RECORDED` | Lint issue opened/refreshed. |
| `RUN_FINISHED` | Run ended. |

## DTOs

New or extended DTOs should include:

- `CreateWikiLinkifyLintRunRequest`
- `WikiLinkifyLintRunResponse`
- Optional `WikiIssueSummaryResponse`

DTOs must use safe labels and ids only. They must not include raw Markdown text.

## Indexing And Queries

- Existing `wiki_page(space_id, slug)` query path is required.
- Add or reuse issue query path for `spaceId`, `status`, `issueType`, and `pageId`.
- Keep query changes additive and Flyway-managed.
