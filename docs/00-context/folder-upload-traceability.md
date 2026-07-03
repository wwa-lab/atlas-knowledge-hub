# Traceability: Folder Upload

## Status

Implemented. Phase 1 FE, mock-only. Slice `folder-upload`.

Current implementation status:

- SDD artifact set is complete in English and Simplified Chinese.
- `T-FU-001` through `T-FU-013` are complete, except `T-FU-011`, which is explicitly optional/deferred for a later prototype-to-Vue extraction pass.
- Implementation is committed on `develop-leo` in `606da27 feat: implement folder upload prototype slice`.
- API guide remains intentionally omitted because this slice is frontend-only and mock-only.

## Slice

`folder-upload` — interactive mock folder/ZIP upload on the Documents tab: inventory → file tree → batch creation → mock report.

## Source Inputs

| Source | Role |
|---|---|
| `docs/00-context/slice-roadmap.md` | Slice backlog + Phase 1 FE verification/constraints row. |
| `docs/batch-processing-design.md` | Batch, File Item, File Status, Reports model. |
| `docs/mvp-scope.md` | Mock batch status and file tree scope. |
| `docs/markdown-standard.md` | Trace, confidence, review-status fields. |
| `docs/01-requirements/requirement.md` | Product-level requirements and adapter/trust principles. |
| `frontend/public/atlas-prototype.html` | FE fidelity baseline (`renderDocs()`, `batch`, `.file-tree`, `statusClass`). |
| `prototypes/index.html` | Static mirror for direct review. |
| `docs/03-spec/knowledge-space-spec.md` (REQ-KS-004) | Host Documents-tab behavior this slice extends. |

## Artifact Map

| Stage | EN | zh-CN |
|---|---|---|
| Requirements | `docs/01-requirements/folder-upload-requirements.md` | `…folder-upload-requirements.zh-CN.md` |
| User Stories | `docs/02-user-stories/folder-upload-stories.md` | `…folder-upload-stories.zh-CN.md` |
| Specification | `docs/03-spec/folder-upload-spec.md` | `…folder-upload-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/folder-upload-architecture.md` | `…folder-upload-architecture.zh-CN.md` |
| Data Flow | `docs/04-architecture/folder-upload-data-flow.md` | `…folder-upload-data-flow.zh-CN.md` |
| Data Model | `docs/04-architecture/folder-upload-data-model.md` | `…folder-upload-data-model.zh-CN.md` |
| Design | `docs/05-design/folder-upload-design.md` | `…folder-upload-design.zh-CN.md` |
| API Guide | **Omitted — see decision below** | — |
| Tasks | `docs/06-tasks/folder-upload-tasks.md` | `…folder-upload-tasks.zh-CN.md` |
| Traceability | `docs/00-context/folder-upload-traceability.md` | `…folder-upload-traceability.zh-CN.md` |

## API Guide Omission (recorded decision)

No backend/API contract is in scope for this Phase 1 FE, mock-only slice. Per the SDD profile and `atlas-sdd-generate-all`, the API guide is intentionally omitted. Future real ingestion (storage/converter/parser adapters, `POST /batches`, status polling) will be specified in a later backend slice.

## Requirement → Story → Spec → Task Links

| Requirement | Stories | Spec (Acceptance) | Tasks |
|---|---|---|---|
| REQ-FU-001 | US-FU-001, US-FU-002 | AC-FU-01 | T-FU-003, T-FU-013 |
| REQ-FU-002 | US-FU-003 | AC-FU-02 | T-FU-002, T-FU-004 |
| REQ-FU-003 | US-FU-004 | AC-FU-03 | T-FU-005 |
| REQ-FU-004 | US-FU-005 | AC-FU-04 | T-FU-006 |
| REQ-FU-005 | US-FU-006 | AC-FU-05 | T-FU-002, T-FU-007, T-FU-013 |
| REQ-FU-006 | US-FU-006, US-FU-007 | AC-FU-05 | T-FU-001, T-FU-007 |
| REQ-FU-007 | US-FU-007 | AC-FU-06 | T-FU-007 |
| REQ-FU-008 | US-FU-007 | AC-FU-07 | T-FU-007 |
| REQ-FU-009 | US-FU-008 | AC-FU-08 | T-FU-002, T-FU-008, T-FU-013 |
| REQ-FU-010 | US-FU-003, US-FU-008, US-FU-009 | AC-FU-09 | T-FU-001, T-FU-004, T-FU-008 |
| REQ-FU-011 | US-FU-001, US-FU-002, US-FU-006 | AC-FU-01 | T-FU-002, T-FU-003 |
| REQ-FU-012 | US-FU-009 | (Adapter notes) | T-FU-012 |
| REQ-FU-013 | US-FU-001, US-FU-005, US-FU-010 | AC-FU-10 | T-FU-010, T-FU-011, T-FU-012 |
| REQ-FU-014 | US-FU-004 | AC-FU-11 | T-FU-009 |
| REQ-FU-015 | US-FU-010 | AC-FU-12 | T-FU-010 |

## Slice Boundary vs knowledge-space

- `folder-upload` **owns:** upload trigger interaction, mock inventory, unsupported classification, file-tree construction, batch creation + mock status progression, batch report.
- `knowledge-space` **owns:** Documents-tab shell (REQ-KS-004), space navigation, global copy map and theme tokens. `folder-upload` extends `renderDocs()` and supersedes the static baseline batch rendering while preserving parity.
- Shared types (`FileStatus`, `Batch`, `FileItem`) must be reconciled in `frontend/src/types.ts`, not duplicated.

## Verification Evidence

Per the Phase 1 FE row of `docs/00-context/slice-roadmap.md`:

- `cd frontend && npm run typecheck` passed.
- `cd frontend && npm run test` passed.
- `cd frontend && npm run build` passed.
- `cd frontend && npm run e2e` passed, including the folder-upload smoke flow.
- `git diff --check` passed.
- `diff frontend/public/atlas-prototype.html prototypes/index.html` was empty.
- Diff review found no new network calls, dependencies, secrets, private paths, or real company data.

Evidence is also recorded in `docs/06-tasks/folder-upload-tasks.md`.

## Sub-Skills Used (generation pass)

`atlas-sdd-generate-all` orchestrated the chain: `req-to-user-story → user-story-to-spec → spec-to-architecture → architecture-to-design → design-to-tasks → review-doc-quality`.

## Key Assumptions Introduced

- Mock "selection" is a seeded in-memory sample, not a real file-picker result.
- Phase-1 implementation surface is the single-file prototype (`frontend/public/atlas-prototype.html` + mirror), because `frontend/src/App.vue` is still an iframe host with no Vue feature components. The `features/folder-upload/*.vue` layout is a documented future-extraction target only (T-FU-011, deferred).
- Single active batch replaces the baseline static batch for the demo.

## Open Questions

- Resolved for this slice: use deterministic synthetic selection only; no real file picker or filesystem read.
- Resolved for this slice: use a single active mock batch.
- Deferred: `folderUploadMock` extraction to `frontend/src/data/` or Vue feature components belongs to a later prototype-to-Vue extraction pass.

## Deferred Translations

None. Every artifact has an EN and a `.zh-CN.md` copy with identical REQ/US/T/AC IDs.
