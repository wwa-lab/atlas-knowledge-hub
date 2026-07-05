# Requirements: Wiki Data Model

## Status

Draft for user acceptance. Slice `wiki-data-model`. Phase 4 hardening / Wiki Foundation.

## Goal

Upgrade Atlas Wiki from a published metadata surface into the data-model foundation required for future Auto Wiki generation, refresh, linking, linting, and review workflows without implementing those workflows in this slice.

## Slice Contract

- **Scope:** Extend `wiki_page` metadata; add minimal `wiki_folder`, `wiki_generation_run`, `wiki_log_entry`, and `wiki_page_issue` support; expose read-oriented API contracts for page slug lookup, folders, logs, and issues; show visible new metadata in the Vue Wiki tab.
- **Exclusions:** Auto Wiki ingest pipeline, linkify/lint rule engine, production auth/RBAC, real company documents, external cloud calls, new model providers, raw parser/converter/model/vector/storage execution.
- **Grounded baseline:** Existing backend `wiki_page` contains `id`, `space_id`, `title`, `markdown_path`, `source_document_ids`, `confidence`, `review_status`, `owner`, `last_updated`; existing APIs publish/list/get pages by id; existing Vue Wiki tab maps API pages and falls back to sample-safe pages.
- **Verification row:** Phase 4 hardening requires backend verification, frontend typecheck/test/build/E2E, second-layer E2E where feasible, diff hygiene, secret/private-path scan, and network/dependency scan.
- **Human gate:** No product code implementation starts until this SDD set is accepted by the current user.

## Requirements

| ID | Requirement | Priority | Phase |
|---|---|---:|---|
| REQ-WIKI-DATA-MODEL-001 | The SDD set for `wiki-data-model` must exist in English and Simplified Chinese with identical REQ/US/T IDs before product code changes begin. | Must | 4 |
| REQ-WIKI-DATA-MODEL-002 | `wiki_page` must add `slug`, `page_type`, `aliases`, `source_refs`, `chunk_refs`, `in_links`, `out_links`, `version`, `source_mode`, and `refresh_policy` while preserving existing publish/list/id-detail behavior. | Must | 4 |
| REQ-WIKI-DATA-MODEL-003 | Slug lookup must be scoped by Knowledge Space and must not expose pages from another space. | Must | 4 |
| REQ-WIKI-DATA-MODEL-004 | `wiki_folder` must provide the minimal hierarchy needed to group Wiki pages by space without implementing permission inheritance or production RBAC. | Must | 4 |
| REQ-WIKI-DATA-MODEL-005 | `wiki_generation_run` must record future generation/refresh run metadata without executing an ingest pipeline in this slice. | Must | 4 |
| REQ-WIKI-DATA-MODEL-006 | `wiki_log_entry` must record safe Wiki lifecycle events and must not store raw document text, prompts, secrets, provider payloads, or private paths. | Must | 4 |
| REQ-WIKI-DATA-MODEL-007 | `wiki_page_issue` must represent future maintenance findings such as stale source, broken link, orphan, thin content, and review required, without implementing rule evaluation. | Must | 4 |
| REQ-WIKI-DATA-MODEL-008 | API responses must include the new Wiki page fields and minimal folder/log/issue read models using Atlas `ApiEnvelope` conventions and user-safe errors. | Must | 4 |
| REQ-WIKI-DATA-MODEL-009 | Existing `POST /api/files/{fileId}/publish`, `GET /api/spaces/{spaceId}/wiki-pages`, and `GET /api/wiki-pages/{wikiPageId}` must remain backward compatible for current tests and E2E flows. | Must | 4 |
| REQ-WIKI-DATA-MODEL-010 | Vue Wiki views must display visible new metadata, including slug, page type, aliases, source/chunk refs, link counts or link ids, version, source mode, and refresh policy when API data provides it. | Must | 4 |
| REQ-WIKI-DATA-MODEL-011 | Vue Wiki fallback sample safety must remain: when API pages are unavailable, the UI must use deterministic mock/sample-safe content only. | Must | 4 |
| REQ-WIKI-DATA-MODEL-012 | The slice must preserve source trace, confidence, and review status across Wiki page responses, Graph/Ask downstream evidence, and frontend display. | Must | 4 |
| REQ-WIKI-DATA-MODEL-013 | Database migration must be additive and must not break existing V1-V9 contracts, seeded mock/sample data, or review-publish behavior. | Must | 4 |
| REQ-WIKI-DATA-MODEL-014 | Parser, converter, model, vector, storage, and search behavior must remain behind existing Atlas adapter boundaries; this slice must not call engines directly. | Must | 4 |
| REQ-WIKI-DATA-MODEL-015 | Tests must cover backend page new fields, slug lookup, folder/log/issue minimal reads, frontend metadata rendering, existing review-publish/graph/Ask regressions, and safety scans. | Must | 4 |

## Assumptions

- Existing `source_document_ids` remains as a compatibility field while `source_refs` and `chunk_refs` add richer future-proof references.
- `slug` is unique per `space_id`; duplicate slugs across different spaces are allowed.
- `page_type`, `source_mode`, `refresh_policy`, issue type/status, and generation run status are constrained string enums in the database and domain model.
- Folder membership is optional for existing pages to avoid breaking seeded data.
- WeKnora analysis files were not read in this pass because `WEKNORA_ANALYSIS_DIR` was not set; only Atlas-owned docs and code were used.

## Open Questions

- OQ-WIKI-DATA-MODEL-001: Should future ingest-generated pages start as `REVIEW_REQUIRED` or as draft records with a separate publish transition?
- OQ-WIKI-DATA-MODEL-002: Should `source_refs` keep only IDs/labels, or should it later include stable checksums for source freshness checks?
- OQ-WIKI-DATA-MODEL-003: Should folder ordering be manual, generated by slug, or deferred until the Wiki navigation UX slice?
