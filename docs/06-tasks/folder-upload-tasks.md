# Tasks: Folder Upload

## Status

Draft — ready for implementation handoff. Phase 1 FE, mock-only. Derived from `docs/05-design/folder-upload-design.md`. Behavior source of truth: `docs/03-spec/folder-upload-spec.md`.

## Implementation Gates

- Requirements, stories, spec, architecture, data flow, data model, and design are present (this slice).
- **No backend/API in scope** — API guide intentionally omitted (recorded in `docs/00-context/folder-upload-traceability.md`).
- Mock-only: no real upload/parse, no adapters, no network, no new dependency.
- Primary implementation surface is `frontend/public/atlas-prototype.html`, mirrored byte-identically to `prototypes/index.html`. Do **not** scaffold `features/folder-upload/*.vue` unless a task says so (T-FU-011 is the only, optional, extraction task).
- Preserve source trace, confidence, and review status on every generated item.

## Task Breakdown

| ID | Task | Owner | Depends On | Maps To | Verification |
|---|---|---|---|---|---|
| T-FU-001 | Extend `frontend/src/types.ts` (and/or the prototype's inline type notes) with `FileStatus`, `ReviewStatus`, `SourceKind`, `SourceType`, `UploadFlowState`, `SourceTrace`, `InventoryFile`, `Inventory`, `FileItem`, `BatchMetrics`, `BatchProgressStage`, `Batch`, `ReportEntry`, `Report`, `FolderUploadProvider`. Reconcile with existing baseline types (extend, don't duplicate). | Codex | data-model | REQ-FU-006, REQ-FU-010 | `cd frontend && npm run typecheck` passes; `FileStatus` equals the allowed set from `docs/batch-processing-design.md`. |
| T-FU-002 | Add a seeded mock provider (`createInventory`, `createBatch`, `buildReport`) implementing `FolderUploadProvider` with async-shaped returns and representative status distribution. Prototype: inline seed + factory functions; keep pure/deterministic. | Codex | T-FU-001 | REQ-FU-002, REQ-FU-005, REQ-FU-009, REQ-FU-011 | Unit test: inventory has supported+unsupported; batch metrics derive from fileItems; report sections non-empty for a seeded batch. |
| T-FU-003 | Make `Upload Folder` / `Upload ZIP` open the Upload Review surface (modal sheet, All-Settings pattern). Wire `IDLE → INVENTORY_READY/INVENTORY_EMPTY`. No network. | Codex | T-FU-002 | REQ-FU-001, REQ-FU-011, REQ-FU-013 | Click opens surface; devtools network tab shows no request (AC-FU-01). |
| T-FU-004 | Render the inventory list: per-file path, type, mock size, supported badge, confidence, review status, reusing `.file-row`/`.badge`. | Codex | T-FU-003 | REQ-FU-002, REQ-FU-010 | Every row shows path/type/size (AC-FU-02); rows carry confidence + review status (AC-FU-09). |
| T-FU-005 | Render the unsupported group distinctly with `UNSUPPORTED` badge + reason; add missing `statusClass` mappings (`NEW`,`UPLOADED`,`APPROVED`,`PUBLISHED`,`UNSUPPORTED`). | Codex | T-FU-004 | REQ-FU-003 | Unsupported files badged/grouped (AC-FU-03). |
| T-FU-006 | Build the hierarchical file tree from inventory paths (split on `/`), with per-file status badges and a truncation note past the display cap. | Codex | T-FU-004 | REQ-FU-004 | Nested folders/files render from paths (AC-FU-04); cap respected without crash. |
| T-FU-007 | Implement `Create Batch` (enabled iff ≥1 supported): create batch via provider, transition to `BATCH_CREATED`, and re-render Documents tab (`renderDocs`) with the new batch metrics + progress. Disabled state shows help. | Codex | T-FU-002, T-FU-004 | REQ-FU-005, REQ-FU-006, REQ-FU-007, REQ-FU-008 | Batch has time/owner/name/count + per-file status (AC-FU-05); metrics + 4-stage progress render (AC-FU-06, AC-FU-07). |
| T-FU-008 | Add `View Report` opening the report surface with all six sections; each entry shows path, status, confidence, review status, and a `source_trace:` line. | Codex | T-FU-007 | REQ-FU-009, REQ-FU-010 | Report lists inventory/unsupported/failures/low-confidence/review-required with counts (AC-FU-08); trace present (AC-FU-09). |
| T-FU-009 | Handle empty/error states: no files, all-unsupported (Create disabled + message), Cancel (restore prior batch), oversized cap note. | Codex | T-FU-003, T-FU-007 | REQ-FU-014 | Each state behaves per spec table with no crash (AC-FU-11). |
| T-FU-010 | Add all new copy keys to the EN/zh copy map; ensure language + day/night switch preserve upload/batch/report state. | Codex | T-FU-003..T-FU-008 | REQ-FU-015, REQ-FU-013 | Language/theme switch updates new copy and preserves state (AC-FU-12). |
| T-FU-011 | (Optional / deferred) Extract the render functions into `features/folder-upload/*.vue` + `frontend/src/data/folderUploadMock.ts` once prototype→Vue extraction begins. Not required for this slice's acceptance. | Codex | T-FU-001..T-FU-010 | REQ-FU-013 | Only if scheduled: components render identically; typecheck/build pass. |
| T-FU-012 | Sync `prototypes/index.html` byte-identical to `frontend/public/atlas-prototype.html`; document the deferred API guide + adapter-neutral future in traceability. | Codex | T-FU-003..T-FU-010 | REQ-FU-012, REQ-FU-013 | `diff frontend/public/atlas-prototype.html prototypes/index.html` is empty (AC-FU-10). |
| T-FU-013 | Add a Playwright smoke spec for the critical flow using the `data-testid` hooks: open upload → inventory → create batch → view report. | Codex | T-FU-003..T-FU-008 | REQ-FU-001, REQ-FU-005, REQ-FU-009 | `cd frontend && npm run e2e` passes for the folder-upload spec. |

## Verification Plan (Phase 1 FE row)

Run from `frontend/` before reporting done:

```bash
cd frontend
npm run typecheck
npm run test
npm run build
npm run e2e
```

Plus the CLAUDE.md baseline checks from repo root:

```bash
git diff --check
diff frontend/public/atlas-prototype.html prototypes/index.html   # must be empty
# scan the diff for new network calls / dependencies / secrets / private paths
```

Name any skipped check and why — never imply an unrun check passed.

## Constraints (per task, apply throughout)

- **Mock-only / no-network / no-dependency:** no `fetch`/`XHR`/websocket, no new npm package, no real file read.
- **Adapter-neutral:** views depend only on `FolderUploadProvider`; never name or call `trinity-office` / `document-normalize` / any tool.
- **Trace preserved:** generated items carry source-trace, confidence, review status; generated/low-confidence default to `REVIEW_REQUIRED`.
- **Parity:** update `prototypes/index.html` in the same change; reuse existing styles/tokens.

## Divergence Rule

If a task cannot be met against `docs/03-spec/folder-upload-spec.md` as written, stop and surface the spec/task mismatch (update spec/design/tasks first) instead of coding around it.

## Completion Matrix

| ID | Status | Evidence |
|---|---|---|
| T-FU-001 | Done | `frontend/src/types.ts`; `cd frontend && npm run typecheck` passed. |
| T-FU-002 | Done | Inline seeded provider in `frontend/public/atlas-prototype.html`; `frontend/src/folderUploadPrototype.test.ts`; `cd frontend && npm run test` passed. |
| T-FU-003 | Done | `Upload Folder` / `Upload ZIP` open `data-testid="upload-review"` with no network calls added; `cd frontend && npm run e2e` passed. |
| T-FU-004 | Done | Inventory rows render path/type/size/confidence/review status with `data-testid="inventory-row"`; `cd frontend && npm run e2e` passed. |
| T-FU-005 | Done | Unsupported group renders `UNSUPPORTED` rows and expanded status mappings; `cd frontend && npm run e2e` passed. |
| T-FU-006 | Done | File tree is built from inventory paths with status badges and cap note support; `cd frontend && npm run e2e` passed. |
| T-FU-007 | Done | `Create Batch` creates derived metrics and four progress stages; `cd frontend && npm run e2e` passed. |
| T-FU-008 | Done | `View Report` opens six report sections with `source_trace:` lines; `cd frontend && npm run e2e` passed. |
| T-FU-009 | Done | Empty/all-unsupported/cancel/truncation handling implemented in the upload state/render helpers; `cd frontend && npm run test` and `cd frontend && npm run e2e` passed. |
| T-FU-010 | Done | EN/zh copy keys added; render state survives re-render/theme/language changes; `cd frontend && npm run build` passed. |
| T-FU-011 | Deferred (optional) | Extraction not scheduled in this slice. |
| T-FU-012 | Done | `prototypes/index.html` synced; `diff frontend/public/atlas-prototype.html prototypes/index.html` is empty. Traceability already records the omitted API guide and adapter-neutral future. |
| T-FU-013 | Done | `frontend/tests/e2e/folder-upload.spec.ts`; `cd frontend && npm run e2e` passed. |
