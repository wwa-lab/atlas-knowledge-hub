# Traceability: Knowledge Space

## Status

Phase 1 FE implemented and current IA refinement accepted in the prototype baseline.

Current implementation status:

- `T-KS-001` through `T-KS-031` are complete for the current Phase 1 frontend/prototype scope.
- `T-KS-035` adds the mock-only Message Management settings panel for workspace chat history indexing.
- `T-KS-036` adds the mock-only Space Information settings panel for current-space metadata and local name/description editing.
- The accepted detail tabs are `文档`, `处理中心`, `Wiki`, and `图谱`.
- Global Chat / Trusted Ask is outside individual Knowledge Space detail and can select one or more Knowledge Spaces as answer context.
- `frontend/public/atlas-prototype.html` is the active Phase 1 fidelity baseline; `prototypes/index.html` mirrors it byte-identically.
- Backend, database, authentication, production persistence, real parser/converter/model/vector/storage adapters, and external network calls remain out of scope.
- Full Simplified Chinese synchronization for the historical Knowledge Space SDD set is deferred; this traceability status has a Chinese companion at `docs/00-context/knowledge-space-traceability.zh-CN.md`.

## Slice

`knowledge-space`

## Source Inputs

| Source | Role |
|---|---|
| `frontend/public/atlas-prototype.html` | Active Phase 1 FE fidelity baseline. |
| `prototypes/index.html` | Current static UI prototype and demo behavior. |
| `docs/product-vision.md` | Product purpose, target users, and trust-before-conversation principle. |
| `docs/mvp-scope.md` | MVP boundaries and acceptance shape. |
| `docs/architecture.md` | Adapter-based architecture and future stack direction. |
| `docs/batch-processing-design.md` | Batch, file item, status, and report model. |
| `docs/markdown-standard.md` | Markdown metadata, source trace, confidence, and review status rules. |
| `docs/knowledge-graph-design.md` | Graph node and edge model. |
| `docs/review-workflow.md` | SME review states and decisions. |
| `docs/technology-decisions.md` | Vue/Spring/PostgreSQL/Flyway timing and implementation constraints. |

## Artifact Map

| Stage | File |
|---|---|
| Requirements | `docs/01-requirements/knowledge-space-requirements.md` |
| User Stories | `docs/02-user-stories/knowledge-space-stories.md` |
| Specification | `docs/03-spec/knowledge-space-spec.md` |
| Architecture | `docs/04-architecture/knowledge-space-architecture.md` |
| Data Flow | `docs/04-architecture/knowledge-space-data-flow.md` |
| Data Model | `docs/04-architecture/knowledge-space-data-model.md` |
| Design | `docs/05-design/knowledge-space-design.md` |
| API Guide | `docs/05-design/contracts/knowledge-space-API_IMPLEMENTATION_GUIDE.md` |
| Tasks | `docs/06-tasks/knowledge-space-tasks.md` |

## Requirement Links

| Requirement | Stories | Spec Sections | Tasks |
|---|---|---|---|
| REQ-KS-001 Knowledge Space home | US-KS-001, US-KS-002 | Home Knowledge Space Library | T-KS-001, T-KS-002, T-KS-028, T-KS-030 |
| REQ-KS-002 Create Knowledge Space | US-KS-002 | Create Knowledge Space | T-KS-028, T-KS-030 |
| REQ-KS-003 Space detail tabs | US-KS-003 | Space Detail Navigation | T-KS-004, T-KS-031 |
| REQ-KS-004 Document management and batch status | US-KS-004 | Document Batch | T-KS-005, T-KS-006, T-KS-030, T-KS-031 |
| REQ-KS-005 LM Wiki | US-KS-005 | Wiki | T-KS-007, T-KS-031 |
| REQ-KS-006 Knowledge graph | US-KS-006 | Graph | T-KS-008, T-KS-031 |
| REQ-KS-007 Processing Center quality gates | US-KS-007 | Processing Center | T-KS-009, T-KS-010, T-KS-031 |
| REQ-KS-008 Global Chat / Trusted Ask | US-KS-008 | Global Chat, Trusted Ask | T-KS-011, T-KS-031 |
| REQ-KS-009 Trace and review metadata | US-KS-005, US-KS-007, US-KS-008 | Data Contracts, Document Batch, Processing Center, Wiki, Graph, Global Chat | T-KS-012, T-KS-031 |
| REQ-KS-010 Prototype constraints | All | Non-Functional Requirements | T-KS-013 |
| REQ-KS-013 Language switching | US-KS-009 | Language Switching | T-KS-015, T-KS-017 |
| REQ-KS-014 Day/night mode | US-KS-010 | Day And Night Mode | T-KS-016, T-KS-017 |
| REQ-KS-015 Model management | US-KS-011 | Model Management | T-KS-018, T-KS-019 |
| REQ-KS-016 Model secret safety | US-KS-011 | Model Management, Non-Functional Requirements | T-KS-018, T-KS-019 |
| REQ-KS-017 Registration | US-KS-012 | Registration | T-KS-020, T-KS-022 |
| REQ-KS-018 Space member management | US-KS-013 | Knowledge Space Member Management | T-KS-021, T-KS-022 |
| REQ-KS-019 Simple RBAC safety | US-KS-013 | Knowledge Space Member Management, Non-Functional Requirements | T-KS-021, T-KS-022 |
| REQ-KS-020 Account settings | US-KS-014 | Account Settings | T-KS-023, T-KS-025 |
| REQ-KS-021 Data and extension engines | US-KS-015 | Data And Extension Engines | T-KS-024, T-KS-025 |
| REQ-KS-022 Sidebar settings shell | US-KS-016 | Sidebar And Settings Shell | T-KS-026 |
| REQ-KS-023 Responsive layout | US-KS-017 | Responsive Layout | T-KS-027 |
| REQ-KS-024 Message Management | US-KS-008, US-KS-014 | Message Management, Global Chat, Account Settings | T-KS-035 |
| REQ-KS-025 Space Information | US-KS-013, US-KS-016 | Knowledge Space Information, Sidebar And Settings Shell | T-KS-036 |

## Gate Status

- Requirements: Historical draft baseline present.
- Stories: Historical draft baseline present.
- Spec: Updated to current Phase 1 IA baseline.
- Architecture: Historical draft baseline present; no backend/API work added by Phase 1 IA refinement.
- Design: Updated to current Phase 1 IA baseline.
- Tasks: Updated through `T-KS-036`.
- Implementation: Phase 1 frontend/prototype baseline complete for `T-KS-001` through `T-KS-036`.
- Verification: `npm run build`, `npm run e2e`, mirror diff, and `git diff --check` passed for `T-KS-031`; prior Phase 1 shell checks are recorded in `docs/06-tasks/knowledge-space-tasks.md`.

## Product Goal Batch 1 Status

| Phase | Task IDs | Maturity | Evidence |
|---|---|---|---|
| Phase A Product Experience Reset | `T-KS-032` | L2 Vue parity | `docs/00-context/evidence/phase-a-product-home.png`; Playwright `phase-a-b-product-shell.spec.ts`; no `iframe.product-frame` on primary path. |
| Phase B Knowledge Space Detail Parity | `T-KS-033` | L2 Vue parity | `docs/00-context/evidence/phase-b-space-detail.png`; Playwright tab switching across Documents, Processing Center, Wiki, and Graph. |

Batch 1 reuses the existing `knowledge-space` SDD as the source of truth because Phase A/B are product-surface parity work within the accepted frontend slice. No backend/API contract, architecture boundary, external provider, real data, or secret-handling scope was changed.
