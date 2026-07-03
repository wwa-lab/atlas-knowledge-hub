# Specification: Folder Upload

## Status

Draft. Phase 1 FE, mock-only. Behavior source of truth for the `folder-upload` slice. Derived from `docs/02-user-stories/folder-upload-stories.md`.

## Source Documents

- `docs/01-requirements/folder-upload-requirements.md`
- `docs/02-user-stories/folder-upload-stories.md`
- `docs/batch-processing-design.md` — Batch / File Item / File Status / Reports.
- `docs/markdown-standard.md` — trace / confidence / review metadata.
- `docs/mvp-scope.md` — mock batch status and file tree.
- FE baseline: `frontend/public/atlas-prototype.html` (`renderDocs()`, `batch`, `.file-tree`, `statusClass`), mirrored in `prototypes/index.html`.

## Scope

Interactive mock upload on the Documents tab: trigger → inventory → file tree → batch creation → progress/metrics → report. Mock-only, no network, no adapters, no persistence. FE parity with the baseline Documents tab is required.

## Constraints

- **Mock-only / no-network:** no real file read, decompression, upload, parsing, adapter call, or external request (REQ-FU-011).
- **Adapter-neutral (documented):** future ingestion routes through converter/parser/storage adapters; this slice must not hardcode or call any tool (REQ-FU-012).
- **Trace preserved:** every generated file item and report entry carries source-trace, confidence, review-status (REQ-FU-010).
- **Parity:** new surfaces reuse Documents-tab styling and stay mirrored to `prototypes/index.html` (REQ-FU-013).
- **Bilingual + themed:** all new copy in EN/zh map; day/night tokens respected (REQ-FU-015).

## View Behavior

### Documents Tab (host)

The Documents tab keeps its two-panel layout (batch summary panel + file tree panel). `Upload Folder` and `Upload ZIP` become active triggers. Before any upload in a session, the tab shows the current active batch (seeded baseline batch or the last created batch).

### Upload Flow (new)

Triggering an upload opens an **Upload Review** surface (inline panel or modal sheet over the Documents tab — see design) with these regions:

1. **Source header** — source kind (`Folder` or `ZIP`), source package name, detected file count, and a `Cancel` control.
2. **Inventory list** — one row per detected file: relative path, file type, mock size, supported/unsupported badge, mock confidence, mock review status.
3. **Unsupported group** — unsupported files listed or badged distinctly with a short reason.
4. **Primary actions** — `Create Batch` (enabled only when ≥1 supported file) and `Cancel`.

Confirming with `Create Batch` closes the Upload Review surface and updates the Documents tab to show the newly created batch (metrics, progress, file tree) plus a `View Report` control.

### Batch Report (new)

`View Report` opens a report surface summarizing the active batch: inventory count, unsupported list, conversion-failure list, low-confidence list, review-required list, and approved/published counts. Each listed item exposes source-trace, confidence, and review status.

## State Model

### Upload flow states

```
IDLE ──trigger(folder|zip)──▶ INVENTORY_READY
INVENTORY_READY ──cancel──▶ IDLE
INVENTORY_READY ──create(≥1 supported)──▶ BATCH_CREATED
INVENTORY_READY ──all-unsupported──▶ INVENTORY_EMPTY (Create Batch disabled)
INVENTORY_EMPTY ──cancel──▶ IDLE
BATCH_CREATED ──view-report──▶ REPORT_OPEN
REPORT_OPEN ──close──▶ BATCH_CREATED
```

### File status (per file item)

Uses the allowed set from `docs/batch-processing-design.md`: `NEW`, `UPLOADED`, `PDF_CONVERTED`, `PDF_CONVERT_FAILED`, `MARKDOWN_GENERATED`, `OCR_REQUIRED`, `LOW_CONFIDENCE`, `REVIEW_REQUIRED`, `APPROVED`, `PUBLISHED`, `FAILED`, `UNSUPPORTED`.

Mock progression on batch creation follows the design status flow:

```
NEW → UPLOADED → PDF_CONVERTED → MARKDOWN_GENERATED → REVIEW_REQUIRED → APPROVED → PUBLISHED
                     │                 │                    │
                     ▼                 ▼                    ▼
             PDF_CONVERT_FAILED   LOW_CONFIDENCE       OCR_REQUIRED
                     │
                     ▼
                   FAILED
Unsupported types → UNSUPPORTED (never enter the flow)
```

In Phase 1 the progression is a deterministic seeded assignment (each mock file gets a fixed target status), not a live timer. A file item never transitions to `APPROVED`/`PUBLISHED` automatically from generated content — those require the future review slice.

## Interaction Rules

- `Upload Folder` and `Upload ZIP` both open the Upload Review surface; they differ only in the recorded source kind and package label.
- `Create Batch` is disabled when zero supported files are present; the disabled state shows a tooltip/help explaining why.
- `Cancel` discards the pending inventory and returns to the Documents tab unchanged.
- Language switch during any state preserves the current inventory/batch/report state (mirrors REQ-KS-013 behavior).
- No action performs a network request or writes to disk.

## Empty & Error States

| Situation | Behavior |
|---|---|
| No files detected in selection | Show "No files found in this package" and disable `Create Batch`. |
| All files unsupported | Show unsupported group + "No supported files to process"; `Create Batch` disabled. |
| Cancelled upload | Return to Documents tab with the previous active batch intact. |
| Oversized mock package (guard) | Cap displayed inventory at a documented max (e.g. 500 rows) with a "showing first N" note; no crash. |

## Trace, Confidence & Review

- Every inventory row and batch file item carries `confidence` (0–1 mock) and `review_status` (defaulting to `REVIEW_REQUIRED` for generated/low-confidence items).
- Report entries carry a source-trace reference (source_file + mock page/chunk) consistent with `docs/markdown-standard.md`.
- No item is labelled trusted/approved by default.

## Adapter & Future-Real Notes (documented, not implemented)

- Real ingestion will pass the selected package to a **storage adapter** (persist raw inputs), a **converter adapter** (`trinity-office` Office→PDF), and a **parser adapter** (`document-normalize` PDF→Markdown/images) — all behind product-facing interfaces, never called directly.
- Deterministic processing (inventory, classification, status) precedes any future LLM enrichment; LLM output stays review-required.

## API / Interface Expectations

None in this slice. No backend/API contract is in scope; the API guide is intentionally omitted and this omission is recorded in `docs/00-context/folder-upload-traceability.md`. Future endpoints (e.g. `POST /batches`) will be specified in a later backend slice.

## Acceptance Matrix

| Check | Requirement | Observable result |
|---|---|---|
| AC-FU-01 | REQ-FU-001, 011 | Clicking Upload Folder/ZIP opens the Upload Review surface with no network request (verify via devtools network tab empty). |
| AC-FU-02 | REQ-FU-002 | Every inventory row shows path, type, and mock size. |
| AC-FU-03 | REQ-FU-003 | Unsupported files are badged `UNSUPPORTED` and grouped/distinct. |
| AC-FU-04 | REQ-FU-004 | File tree renders nested folders/files from inventory paths. |
| AC-FU-05 | REQ-FU-005, 006 | Create Batch yields a batch with upload time, owner, package name, count, and per-file status from the allowed set. |
| AC-FU-06 | REQ-FU-007 | Batch shows total/PDF converted/Markdown generated/review required/failed/unsupported metrics. |
| AC-FU-07 | REQ-FU-008 | Progress list shows the four processing stages with mock percentages. |
| AC-FU-08 | REQ-FU-009 | Report lists inventory, unsupported, failures, low-confidence, review-required with counts. |
| AC-FU-09 | REQ-FU-010 | Each item/report entry exposes source-trace, confidence, and review-status; generated items default to REVIEW_REQUIRED. |
| AC-FU-10 | REQ-FU-013 | New surfaces match Documents-tab styling; `prototypes/index.html` mirrors `frontend/public/atlas-prototype.html`. |
| AC-FU-11 | REQ-FU-014 | Empty / all-unsupported / cancelled states behave per the table with no crash. |
| AC-FU-12 | REQ-FU-015 | Language and theme switches update new copy and preserve state. |

## Open Questions

- Directory picker vs synthetic selection (design decision; default synthetic + optional non-functional picker).
- Single active batch vs batch list (default single active batch).
