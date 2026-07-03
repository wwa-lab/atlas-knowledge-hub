# Architecture: Folder Upload

## Status

Draft. Phase 1 FE, mock-only. Derived from `docs/03-spec/folder-upload-spec.md`.

## Scope & Principles

Frontend-only mock feature inside the existing Vue 3 + Vite + TypeScript app. No backend, no persistence, no network. All ingestion logic is simulated behind a small typed module so that a later phase can swap the mock for real adapter-backed calls without touching the views.

## Component Ownership

```
Documents Tab (knowledge-space shell, host)
        │  triggers
        ▼
UploadReview (folder-upload)
   ├─ InventoryList        (rows: path, type, size, badges, confidence, review)
   ├─ UnsupportedGroup     (distinct unsupported files)
   └─ actions: Create Batch / Cancel
        │  on Create Batch
        ▼
BatchView (folder-upload extends baseline renderDocs)
   ├─ BatchMetrics         (total / converted / markdown / review / failed / unsupported)
   ├─ BatchProgress        (4 stages, mock %)
   └─ FileTree             (hierarchy from inventory paths)
        │  View Report
        ▼
BatchReport (folder-upload)
   └─ report sections: inventory / unsupported / failures / low-confidence / review-required
```

All components consume typed data from a single `folderUploadMock` provider; none reads files or calls services directly.

## Boundaries

### FE component boundary

- **Owned by `folder-upload`:** `UploadReview`, `InventoryList`, `UnsupportedGroup`, `BatchReport`, and the batch-creation/progression logic. The `BatchView`/`FileTree` render is shared with the `knowledge-space` Documents tab and extended, not forked.
- **Owned by `knowledge-space`:** Documents-tab shell, tab routing, global copy map and theme tokens (extended with new keys).

### Backend/API boundary

None in scope. No API guide. Future `POST /batches`, inventory upload, and status polling belong to a later backend slice.

### Adapter boundary (documented, not implemented)

The mock ingestion module defines a conceptual seam matching future adapters, so the swap is mechanical later:

| Future adapter | Responsibility | Phase-1 mock stand-in |
|---|---|---|
| storage adapter | persist raw uploaded package + files | seeded in-memory inventory |
| converter adapter (`trinity-office`) | Office→PDF | mock `PDF_CONVERTED` status assignment |
| parser adapter (`document-normalize`) | PDF→Markdown + images | mock `MARKDOWN_GENERATED` + confidence |

Product logic (views) must never reference these tools by name or call them; it depends only on the `folderUploadMock` interface. This preserves `PROJECT_RULES.md` Adapter Boundaries and Parser Neutral rules.

## Data-Safety & Trace Constraints

- No real file content, private path, credential, or network egress is introduced (mock-only).
- Every generated file item and report entry preserves source-trace, confidence, and review-status (`docs/markdown-standard.md`); generated/low-confidence items default to `REVIEW_REQUIRED`.
- Deterministic processing (inventory, classification, status) precedes any future LLM step.

## Module Layout (proposed, for Codex)

```
frontend/src/
  data/
    folderUploadMock.ts        # seeded inventory + batch/report factory (pure, typed)
  features/folder-upload/
    UploadReview.vue
    InventoryList.vue
    UnsupportedGroup.vue
    BatchReport.vue
  types.ts                     # extend with FileItem/Batch/Inventory/Report types
```

Component/file boundaries follow the global coding-style rule (many small files, 200–400 lines typical). Exact placement is confirmed in the design doc against current `frontend/src` structure.

## Failure Handling

- Empty / all-unsupported inventories disable `Create Batch` and show a message (spec Empty & Error States).
- Inventory display is capped at a documented max row count to avoid pathological render cost.
- No throw-on-parse paths exist because there is no real parsing; guards are input-shape validations on the seeded mock.

## Non-Functional

- No new runtime dependency; reuse existing Vue/Vite toolchain.
- Keep the static mirror (`prototypes/index.html`) in sync with `frontend/public/atlas-prototype.html`.
- Accessibility: upload triggers and actions are real buttons with labels; inventory is a list/table with readable status badges.

## Open Questions

- Whether `folderUploadMock` should expose an async signature now (to mirror a future adapter Promise) or stay synchronous. Recommendation: async-shaped functions returning resolved mock data, so the later adapter swap needs no view changes. (Confirmed in design.)
