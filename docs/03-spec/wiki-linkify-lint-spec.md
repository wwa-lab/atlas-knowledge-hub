# Specification: Wiki Linkify And Lint

## Status

 Accepted by the current user and implemented for the deterministic Wave 1 `wiki-linkify-lint` maturity target. This status does not claim production readiness.

## Overview

`wiki-linkify-lint` adds a deterministic Wiki maintenance run after `wiki-ingest-v0`. It reads current Wiki pages in one space, builds a slug/alias target index, safely inserts `[[slug]]` links into Markdown artifacts, updates `inLinks` and `outLinks`, detects quality issues, and records safe run/log/issue evidence. It does not approve, publish, trust, refresh, retract, or model-generate content.

## Actors

| Actor | Role |
|---|---|
| Knowledge manager | Starts a run and reviews issue counts. |
| SME reviewer | Uses issues and link metadata to decide edits. |
| Knowledge user | Sees review warnings but not new trust claims. |
| Implementation agent | Implements only after SDD acceptance. |

## Behavior

### S1. SDD Acceptance Gate

- The bilingual SDD set must exist before implementation.
- Implementation is blocked until the user accepts this SDD.
- The slice status must not claim production readiness.

### S2. Run Selection

- A run is scoped to one `spaceId`.
- Default page scope includes published and generated review-required pages.
- Optional request filters may target page ids.
- Cross-space page ids are rejected with a safe validation error.

### S3. Target Index

- Link targets are built from pages in the same space.
- Primary target key is `slug`.
- Alias keys are normalized but resolve back to the canonical target slug.
- Ambiguous aliases do not get auto-linked; affected pages receive a `REVIEW_REQUIRED` issue using the existing issue taxonomy.

### S4. Linkify Rules

- Insert `[[target-slug]]` for the first eligible mention of each target on a source page.
- Do not link a page to itself.
- Do not insert more than one outbound link for the same target slug per source page.
- Preserve Markdown content outside inserted links.
- Skip:
  - YAML frontmatter;
  - fenced code blocks;
  - inline code;
  - existing `[[wiki-link]]`;
  - standard Markdown links;
  - Markdown images.
- If the Markdown artifact cannot be read safely, do not rewrite it; record a safe issue/log entry.

### S5. Link Metadata

- `outLinks` stores canonical target slugs for each source page.
- `inLinks` stores canonical source slugs for each target page.
- Link arrays are sorted deterministically and contain no duplicates.
- Re-running the same content is idempotent.

### S6. Lint Rules

| Issue | Issue type | Rule |
|---|---|---|
| Broken Wiki link | `BROKEN_LINK` | Existing `[[slug]]` or computed outbound link has no target in the same space. |
| Orphan page | `ORPHAN_PAGE` | Page has no incoming links and is not an index/root page. |
| Missing source trace | `MISSING_SOURCE_REF` | Page has no `sourceRefs` and no `chunkRefs`. |
| Thin content | `THIN_CONTENT` | Markdown body is empty or below the accepted minimum word/character threshold after stripping markup. |
| Stale source | `STALE_SOURCE` | Referenced source document or source chunk no longer exists in metadata. |
| Ambiguous alias or unsafe rewrite | `REVIEW_REQUIRED` | Deterministic linkify cannot safely choose a target or cannot read/write an artifact. |

### S7. Evidence And Safety

- Each run writes `wiki_generation_run` with mode `linkify-lint` after the schema contract is extended.
- Lifecycle events use `wiki_log_entry`.
- Issues use `wiki_page_issue` with status `OPEN`.
- Repeated runs should update or replace open lint issues for the same page/type/evidence rather than creating unlimited duplicates.
- Logs, issues, and API responses contain safe metadata only.

### S8. UI Behavior

- Processing Center shows Wiki issue totals and blocking/non-blocking status.
- Wiki page detail shows issue badges or warnings for the selected page.
- UI wording must present issues as review-facing quality warnings.
- Generated `REVIEW_REQUIRED` pages remain draft/review-required.

## API Surface

Full payloads live in `docs/05-design/contracts/wiki-linkify-lint-API_IMPLEMENTATION_GUIDE.md`.

| Interface | Behavior |
|---|---|
| `POST /api/spaces/{spaceId}/wiki-linkify-lint-runs` | Starts deterministic linkify/lint for one space. |
| `GET /api/spaces/{spaceId}/wiki-generation-runs/{runId}` | Reuses safe run summary read shape after mode support is extended. |
| `GET /api/spaces/{spaceId}/wiki-page-issues` | Lists space-scoped Wiki issues with filters. |
| `GET /api/wiki-pages/{wikiPageId}/issues` | Existing page issue endpoint remains supported. |
| `GET /api/spaces/{spaceId}/review-queues` | Processing Center may include Wiki issue summary counts. |

## Acceptance Matrix

| Requirement | Observable check |
|---|---|
| REQ-WIKI-LINKIFY-LINT-001 | Complete bilingual SDD exists and user acceptance is recorded before code. |
| REQ-WIKI-LINKIFY-LINT-002 | API/service tests reject cross-space operation. |
| REQ-WIKI-LINKIFY-LINT-003 | Tests prove target index is same-space slug/alias based. |
| REQ-WIKI-LINKIFY-LINT-004 | Tests prove one outbound link per target per page. |
| REQ-WIKI-LINKIFY-LINT-005 | Markdown protected-region tests pass. |
| REQ-WIKI-LINKIFY-LINT-006 | Idempotency tests pass. |
| REQ-WIKI-LINKIFY-LINT-007 | Broken link issue test passes. |
| REQ-WIKI-LINKIFY-LINT-008 | Orphan page issue test passes. |
| REQ-WIKI-LINKIFY-LINT-009 | Missing source and thin content issue tests pass. |
| REQ-WIKI-LINKIFY-LINT-010 | Stale source metadata test passes without parser/model calls. |
| REQ-WIKI-LINKIFY-LINT-011 | Safety scans and log/response assertions pass. |
| REQ-WIKI-LINKIFY-LINT-012 | Existing publish/list/ingest tests pass. |
| REQ-WIKI-LINKIFY-LINT-013 | Vue tests/E2E show issue counts and warnings. |
| REQ-WIKI-LINKIFY-LINT-014 | Final verification evidence names run and skipped checks. |

## Resolved Decisions

- Linkify/lint is deterministic and local.
- Issue taxonomy reuses the existing `wiki_page_issue` enum values.
- `wiki_generation_run.mode` needs an additive schema update to allow `linkify-lint`.
- Linkify updates page link metadata; it does not change review status or trust state.
