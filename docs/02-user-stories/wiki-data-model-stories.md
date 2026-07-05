# User Stories: Wiki Data Model

## Status

Draft for user acceptance. Derived from `wiki-data-model` requirements.

## Story Index

| Story | Title | Requirements |
|---|---|---|
| US-WIKI-DATA-MODEL-001 | Review the SDD contract before code | REQ-WIKI-DATA-MODEL-001 |
| US-WIKI-DATA-MODEL-002 | Identify Wiki pages by stable slug and type | REQ-WIKI-DATA-MODEL-002, 003, 009 |
| US-WIKI-DATA-MODEL-003 | Preserve traceable source and chunk references | REQ-WIKI-DATA-MODEL-002, 012, 013 |
| US-WIKI-DATA-MODEL-004 | Browse minimal Wiki folders | REQ-WIKI-DATA-MODEL-004, 008 |
| US-WIKI-DATA-MODEL-005 | Inspect generation runs, logs, and issues | REQ-WIKI-DATA-MODEL-005, 006, 007, 008 |
| US-WIKI-DATA-MODEL-006 | See new Wiki metadata in Vue safely | REQ-WIKI-DATA-MODEL-010, 011 |
| US-WIKI-DATA-MODEL-007 | Verify compatibility and safety gates | REQ-WIKI-DATA-MODEL-014, 015 |

## US-WIKI-DATA-MODEL-001: Review the SDD contract before code

As a product owner,
I want the full bilingual SDD set accepted before implementation,
so that Wiki Foundation scope does not drift into ingest, linkify, RBAC, or production provider work.

### Acceptance Criteria

1. **Given** the slice is in SDD mode
   **When** the artifact set is generated
   **Then** every required English file has a `.zh-CN.md` companion with matching IDs.
2. **Given** product code has not started
   **When** the SDD is awaiting review
   **Then** implementation tasks state that product code is blocked until user acceptance.

### Notes / Assumptions

- Codex may author SDD docs when it follows the project SDD skill chain.

### Dependencies

- `PROJECT_RULES.md`, `DEVELOPMENT_STANDARDS.md`, and `docs/00-context/sdd-profile.md`.

### Out of Scope

- Product code changes before acceptance.

### Open Questions

- None.

## US-WIKI-DATA-MODEL-002: Identify Wiki pages by stable slug and type

As a knowledge user,
I want Wiki pages to have stable slugs and page types,
so that future Auto Wiki flows can locate and refresh pages without depending on display titles.

### Acceptance Criteria

1. **Given** a published page exists in a space
   **When** I request it by slug within that space
   **Then** the API returns the matching page metadata only for that space.
2. **Given** an existing publish/list flow runs
   **When** a page is created or republished
   **Then** existing id/list APIs still return the page and include the new fields.
3. **Given** two spaces have the same slug
   **When** slug lookup is scoped to each space
   **Then** each space receives only its own page.

### Notes / Assumptions

- Slug uniqueness is per `space_id`.

### Dependencies

- Existing review-publish Wiki APIs and `wiki_page` table.

### Out of Scope

- Auto slug conflict resolution UI.

### Open Questions

- OQ-WIKI-DATA-MODEL-003.

## US-WIKI-DATA-MODEL-003: Preserve traceable source and chunk references

As an SME reviewer,
I want each Wiki page to carry source refs and chunk refs,
so that review, graph, and Ask evidence can point back to approved knowledge safely.

### Acceptance Criteria

1. **Given** a Wiki page response includes source refs
   **When** the frontend renders metadata
   **Then** source refs are visible as safe IDs/labels, not raw document content.
2. **Given** a page has chunk refs
   **When** graph or Ask flows consume page metadata later
   **Then** source trace, confidence, and review status remain available.
3. **Given** legacy pages only have `sourceDocumentIds`
   **When** migration is applied
   **Then** compatibility values are backfilled or safely defaulted without data loss.

### Notes / Assumptions

- This slice stores reference metadata only; it does not dereference raw files.

### Dependencies

- Existing `source_chunk`, review-publish, graph, and Ask contracts.

### Out of Scope

- Link validation and stale-source checks.

### Open Questions

- OQ-WIKI-DATA-MODEL-002.

## US-WIKI-DATA-MODEL-004: Browse minimal Wiki folders

As a knowledge space administrator,
I want Wiki folders to group pages in a simple hierarchy,
so that future Wiki navigation can be built on persistent metadata.

### Acceptance Criteria

1. **Given** a space has folders
   **When** I call the folder list/tree endpoint
   **Then** I receive folders scoped to that space with parent-child relationships.
2. **Given** existing pages have no folder
   **When** the migration runs
   **Then** pages remain readable and are not forced into an unsafe default folder.

### Notes / Assumptions

- Folder permissions are not modeled in this slice.

### Dependencies

- Knowledge Space metadata.

### Out of Scope

- Drag/drop ordering, inheritance, RBAC, and folder write UI.

### Open Questions

- OQ-WIKI-DATA-MODEL-003.

## US-WIKI-DATA-MODEL-005: Inspect generation runs, logs, and issues

As a delivery lead,
I want to inspect generation run metadata, Wiki logs, and page issues,
so that future Auto Wiki maintenance has safe operational records from the start.

### Acceptance Criteria

1. **Given** generation run records exist
   **When** I list runs for a space
   **Then** I see status, source mode, safe counts, timestamps, and no raw provider payloads.
2. **Given** log entries exist
   **When** I list page logs
   **Then** I see bounded lifecycle events without secrets, raw content, or private paths.
3. **Given** page issues exist
   **When** I list issues
   **Then** I see type, severity, status, and safe evidence references.

### Notes / Assumptions

- Initial implementation may seed none or sample-safe rows; no rule engine creates issues in this slice.

### Dependencies

- Wiki page and Knowledge Space records.

### Out of Scope

- Running generation, linkify, lint, or repair workflows.

### Open Questions

- OQ-WIKI-DATA-MODEL-001.

## US-WIKI-DATA-MODEL-006: See new Wiki metadata in Vue safely

As a knowledge user,
I want the Wiki tab to show the new API-backed metadata,
so that I can distinguish generated, published, manual, and refreshable Wiki pages.

### Acceptance Criteria

1. **Given** API Wiki pages include the new fields
   **When** I open the Wiki tab
   **Then** slug, page type, aliases, refs, links, version, source mode, and refresh policy are visible.
2. **Given** no API pages are available
   **When** I open the Wiki tab
   **Then** deterministic sample-safe fallback content remains available.
3. **Given** existing graph and Ask E2E flows run
   **When** Wiki metadata display changes
   **Then** those flows do not regress.

### Notes / Assumptions

- Display can show counts for large arrays and exact IDs for small sample sets.

### Dependencies

- Frontend API types and existing Wiki tab state.

### Out of Scope

- A full Wiki editor or navigation redesign.

### Open Questions

- None.

## US-WIKI-DATA-MODEL-007: Verify compatibility and safety gates

As an implementation reviewer,
I want the slice to prove compatibility and safety,
so that Wiki Foundation does not weaken existing trusted knowledge flows.

### Acceptance Criteria

1. **Given** implementation is complete
   **When** verification runs
   **Then** backend, frontend, E2E, second-layer, diff, secret/path, and network/dependency checks are reported.
2. **Given** adapter boundaries exist
   **When** the implementation is scanned
   **Then** no parser/converter/model/vector/storage/search engine calls are introduced outside Atlas adapter boundaries.

### Notes / Assumptions

- Any skipped check must be named with reason.

### Dependencies

- Accepted SDD and task checklist.

### Out of Scope

- Production readiness claim.

### Open Questions

- None.
