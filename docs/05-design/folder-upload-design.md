# Design: Folder Upload

## Status

Draft. Phase 1 FE, mock-only. Derived from `docs/04-architecture/folder-upload-architecture.md` and grounded in the FE baseline.

## Implementation Surface (authoritative)

The current Vue app (`frontend/src/App.vue`) is still an **iframe host** for `frontend/public/atlas-prototype.html`; no Vue feature components exist yet. Per `CLAUDE.md` (Prototype-as-baseline), the Phase-1 implementation of `folder-upload` lands in the single-file prototype and its mirror:

- **Primary:** `frontend/public/atlas-prototype.html` — extend the inline JS (`appData`, `uiState`, `renderDocs()`, copy map `text`/`copy()`, `statusClass`) with upload/inventory/batch/report state and render functions.
- **Mirror:** `prototypes/index.html` — must stay byte-identical to the primary after changes.
- **Typed model (future extraction target only):** the `features/folder-upload/*.vue` + `frontend/src/data/folderUploadMock.ts` layout in the architecture doc is the target for the later prototype→Vue extraction, not a Phase-1 deliverable. Codex must not scaffold those components in this slice unless the task list explicitly says so.

This keeps parity discipline: change the experience → update the static mirror + SDD in the same slice.

## Experience Principles

- Upload feels like a natural extension of the Documents tab, not a separate app.
- Nothing looks trusted before review: confidence and `REVIEW_REQUIRED` are visible from the inventory onward.
- Every state is reachable and reversible via visible controls (Cancel always returns cleanly).

## Layout & Interaction Model

### Documents tab (host, extended)

Keep the existing two-panel `doc-grid` (batch summary panel + file-tree panel). The `Upload Folder` / `Upload ZIP` buttons in the summary panel become active. When an upload flow is open, present the **Upload Review** surface as an inline panel replacing the `doc-grid`, or as a modal sheet over it (reuse the existing modal/sheet pattern used by All Settings). Recommended: modal sheet, consistent with `REQ-KS-022` settings-modal behavior.

### Upload Review surface

```
┌ Upload Review ─────────────────────────────── ✕ ┐
│ Source: Folder · "2026-06 IBM i Discovery"       │
│ Detected 238 files · 226 supported · 12 unsupported
│                                                  │
│ [ Inventory ]                                    │
│  /Discovery/Architecture/Target.pptx  pptx 1.2MB  ● conf 0.86  REVIEW_REQUIRED
│  /Discovery/BRD/BRD.docx              docx 480KB  ● conf 0.90  REVIEW_REQUIRED
│  ...                                             │
│ [ Unsupported (12) ]                             │
│  /Discovery/raw/notes.zzz             — unsupported type
│                                                  │
│           [ Cancel ]   [ Create Batch ]          │  ← Create disabled if 0 supported
└──────────────────────────────────────────────────┘
```

- Inventory rows reuse the `.file-row` / `.badge` visual style already in the prototype.
- Unsupported files render in a distinct group with a muted badge (`statusClass['UNSUPPORTED']`).
- `Create Batch` disabled state uses the existing disabled-button styling + a help line.

### Batch view (extended `renderDocs`)

After `Create Batch`, `renderDocs()` renders the newly created batch instead of the seeded baseline: same `metric-grid`, `progress-list`, and `.file-tree` markup, plus a new `View Report` button in the summary panel's `button-row`.

### Batch report surface

Reuse the modal/sheet pattern. Sections are collapsible lists: Inventory, Unsupported, Conversion Failures, Low Confidence, Review Required, Approved/Published. Each row shows path, status badge, confidence, review status, and a `source_trace:` line matching the Wiki `.trace` styling.

## File Tree

Build a nested structure from inventory `path` strings (split on `/`). Render folders as headers and files as `.file-row` entries with status badges. Cap at the documented max rows; when truncated, show a muted "showing first N of M" note. Keep the existing `.file-tree` container styling.

## Component Inventory (for future Vue extraction)

| Component | Responsibility | Props / State |
|---|---|---|
| `UploadReview` | Source header + inventory + unsupported + actions | `inventory: Inventory`, emits `create`, `cancel` |
| `InventoryList` | Render supported inventory rows | `files: InventoryFile[]` |
| `UnsupportedGroup` | Render unsupported rows | `files: InventoryFile[]` |
| `BatchReport` | Report sections | `report: Report` |
| (shared) `BatchView`/`FileTree` | metrics/progress/tree | `batch: Batch` |

These map 1:1 to the Phase-1 render functions so extraction is mechanical later.

## Data Contracts

Views consume `Inventory`, `Batch`, and `Report` exactly as defined in `docs/04-architecture/folder-upload-data-model.md`. Phase-1 mock data comes from inline seed objects (prototype) or `folderUploadMock.ts` (future). No view computes metrics independently — it reads `batch.metrics`.

## Visual System

- Reuse existing tokens, `.panel`, `.badge`, `.metric-grid`, `.progress`, `.file-tree`, `.trace`, modal/sheet classes. No new color system.
- Status badge colors reuse `statusClass` mapping already defined for baseline statuses; add mappings for any status not yet present (e.g. `NEW`, `UPLOADED`, `APPROVED`, `PUBLISHED`, `UNSUPPORTED`) using existing badge variants.

## Responsive Behavior

- Upload Review and Report sheets follow the existing responsive modal rules (`REQ-KS-023`): full-width on narrow viewports, centered sheet on wide.
- Inventory/file-tree lists scroll within the sheet; action buttons stay pinned/visible.

## Accessibility & Keyboard

- Upload triggers, Cancel, Create Batch, and View Report are real `<button>`s with labels.
- Modal sheets are focus-trapped and closable via the existing `✕` control and Escape (matching All Settings behavior).
- Status badges include text (the status string), not color alone.

## Test Hooks (stable selectors)

Add stable hooks for E2E: `data-testid="upload-folder"`, `upload-zip`, `upload-review`, `inventory-row`, `unsupported-row`, `create-batch`, `batch-metrics`, `batch-progress`, `file-tree`, `view-report`, `batch-report`. These back the Playwright smoke test in the tasks.

## Bilingual Copy Keys (add to the copy map)

`uploadReviewTitle`, `sourceFolder`, `sourceZip`, `detectedFiles`, `supportedCount`, `unsupportedCount`, `createBatch`, `createBatchDisabledHelp`, `cancel`, `viewReport`, `reportTitle`, `reportInventory`, `reportUnsupported`, `reportFailures`, `reportLowConfidence`, `reportReviewRequired`, `reportApproved`, `emptyNoFiles`, `emptyNoSupported`, `showingFirstN`. Each needs EN + zh values.

## Open Questions (resolved defaults)

- **Picker vs synthetic:** default synthetic seed; an optional non-functional "Choose folder…" affordance may be shown but must not read real files.
- **Single vs multiple batches:** single active batch replaces the baseline batch for the demo.
- **Sheet vs inline:** default modal sheet (consistent with All Settings). Codex may choose inline if parity is cleaner, recorded in the tasks.
