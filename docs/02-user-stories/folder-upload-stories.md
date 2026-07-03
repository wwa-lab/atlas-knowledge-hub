# User Stories: Folder Upload

## Status

Draft. Phase 1 FE, mock-only. Derived from `docs/01-requirements/folder-upload-requirements.md`.

## Actors

- **Knowledge Space Admin** — uploads document packages and creates batches.
- **Delivery Lead** — tracks batch progress and reviews the report.
- **SME Reviewer** — downstream consumer of review-required items (queue handoff only in this slice).

## Stories

### US-FU-001 — Start a folder upload (mock)

**As a** Knowledge Space Admin, **I want** to click `Upload Folder` on the Documents tab, **so that** I can begin bringing a project document folder into the space without leaving the demo.

Covers: REQ-FU-001, REQ-FU-011, REQ-FU-013.

- **Given** I am on the Documents tab of a Knowledge Space, **when** I click `Upload Folder`, **then** a mock upload flow opens showing a generated file inventory and **no** network request is made.
- **Given** the flow is open, **when** I inspect it, **then** it visually matches the Documents-tab styling (panels, badges, buttons) of the baseline.

### US-FU-002 — Start a ZIP upload (mock)

**As a** Knowledge Space Admin, **I want** to click `Upload ZIP`, **so that** I can simulate ingesting a compressed document package.

Covers: REQ-FU-001, REQ-FU-002, REQ-FU-011.

- **Given** I am on the Documents tab, **when** I click `Upload ZIP`, **then** the same mock inventory flow opens, labelled as a ZIP source package.

### US-FU-003 — Review the file inventory

**As a** Knowledge Space Admin, **I want** to see every detected file with its path, type, and size, **so that** I can confirm the package contents before processing.

Covers: REQ-FU-002, REQ-FU-010.

- **Given** an upload flow is open, **when** the inventory renders, **then** each row shows original relative path, file type, and a mock size.
- **Given** the inventory renders, **when** I look at each row, **then** it carries mock confidence and review-status affordances consistent with the markdown standard.

### US-FU-004 — See unsupported files flagged

**As a** Knowledge Space Admin, **I want** unsupported files clearly marked, **so that** I know which files will not be converted.

Covers: REQ-FU-003, REQ-FU-009.

- **Given** the inventory contains a file whose type is not supported, **when** it renders, **then** it is marked `UNSUPPORTED` and grouped or badged distinctly.
- **Given** all files are unsupported, **when** the inventory renders, **then** an empty-result message explains that no files can be processed. (REQ-FU-014)

### US-FU-005 — Browse the file tree

**As a** Delivery Lead, **I want** the inventory shown as a hierarchical file tree, **so that** I can understand the package folder structure.

Covers: REQ-FU-004, REQ-FU-013.

- **Given** an inventory with nested paths, **when** the file tree renders, **then** folders and nested files appear in a readable hierarchy with per-file status badges.

### US-FU-006 — Create a batch from the inventory

**As a** Knowledge Space Admin, **I want** to confirm the inventory and create a batch, **so that** the package becomes a tracked processing run.

Covers: REQ-FU-005, REQ-FU-006, REQ-FU-011.

- **Given** a non-empty inventory, **when** I confirm it, **then** a new batch is created with upload time, owner, source package name, total file count, and an initial status per file.
- **Given** a batch is created, **when** it appears, **then** each file item shows a status drawn from the allowed File Status set.

### US-FU-007 — Track batch progress and statuses

**As a** Delivery Lead, **I want** to see batch summary metrics and processing progress, **so that** I can gauge how far the package has been processed.

Covers: REQ-FU-006, REQ-FU-007, REQ-FU-008.

- **Given** a created batch, **when** I view it, **then** I see total / PDF converted / Markdown generated / review required / failed / unsupported metrics.
- **Given** a created batch, **when** I view it, **then** I see a progress list for Office→PDF, PDF→Markdown, Source Trace check, and SME Review with mock percentages.
- **Given** the batch is in a mock progression, **when** statuses advance, **then** transitions follow the `docs/batch-processing-design.md` status flow.

### US-FU-008 — View the batch report

**As a** Delivery Lead, **I want** a mock processing report, **so that** I can see inventory, unsupported files, failures, low-confidence, and review-required items in one place.

Covers: REQ-FU-009, REQ-FU-010.

- **Given** a created batch, **when** I open its report, **then** I see counts and lists for inventory, unsupported, conversion failures, low-confidence pages, and review-required items.
- **Given** the report renders, **when** I inspect any item, **then** source-trace, confidence, and review-status values are present.

### US-FU-009 — Trust metadata is preserved

**As an** SME Reviewer, **I want** trace, confidence, and review status on every generated item, **so that** review-required content is never presented as trusted.

Covers: REQ-FU-010, REQ-FU-012.

- **Given** any generated file item or report entry, **when** I inspect it, **then** it carries source-trace, confidence, and review-status fields.
- **Given** a low-confidence or generated item, **when** it renders, **then** it defaults to `REVIEW_REQUIRED` rather than approved.

### US-FU-010 — Use it bilingually and themed

**As a** user, **I want** the upload flow in my language and theme, **so that** it stays consistent with the rest of Atlas.

Covers: REQ-FU-015, REQ-FU-013.

- **Given** the flow is open, **when** I switch language, **then** all new labels/messages update and my current inventory/batch state is preserved.
- **Given** the flow is open, **when** I switch day/night mode, **then** all new surfaces remain readable via shared tokens.

## Assumptions

- Batch creation and status progression are simulated in memory; there is no persistence between reloads.
- The mock inventory is seeded from a representative sample resembling `frontend/public/atlas-prototype.html` `batch.files`.

## Dependencies

- Existing `knowledge-space` Documents-tab shell and copy map.
- `docs/batch-processing-design.md` status model; `docs/markdown-standard.md` metadata fields.

## Out Of Scope

- Real upload, parsing, adapters, persistence, and API (see requirements Out Of Scope).

## Open Questions

- Directory picker vs synthetic-only selection (see requirements). Default: synthetic with optional non-functional picker.
- Multiple listed batches vs single active batch. Default: single active batch.
