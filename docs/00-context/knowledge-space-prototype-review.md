# Knowledge Space Prototype Review

## Status

Completed for `T-KS-001` on 2026-07-02.

## Scope

This review compares `prototypes/index.html` against:

- `docs/01-requirements/knowledge-space-requirements.md`
- `docs/02-user-stories/knowledge-space-stories.md`
- `docs/03-spec/knowledge-space-spec.md`
- `docs/05-design/knowledge-space-design.md`
- `docs/06-tasks/knowledge-space-tasks.md`

The review used static inspection and lightweight local checks. It did not include a full browser-based responsive pass across all viewport sizes.

## Checks Performed

| Check | Result | Evidence |
|---|---|---|
| Static JavaScript syntax | Passed | Extracted inline `<script>` from `prototypes/index.html` and ran `node --check`. |
| External dependency scan | Passed | No `script src`, stylesheet CDN, `fetch`, or `XMLHttpRequest` usage found. |
| Secret and private-path scan | Passed with notes | No raw API keys or local private paths found. Mock Base URLs and password placeholders are present as visual configuration data only. |
| SDD alignment review | Partial | Most prototype surfaces exist, with gaps listed below. |

## Coverage Summary

| Area | Status | Notes |
|---|---|---|
| Home cards | Covered | Cards show name, description, document count, Wiki pages, reviews, owner, updated date, and status. |
| Home dialogue mode | Partial | Suggested prompts fill the input and selected IBM i context routes to Ask. Typed question state is not preserved when language/theme re-render the home view. |
| Language switching | Partial | Shell, major headings, tabs, settings, and many controls switch. Some tab content labels remain hardcoded English or Chinese. |
| Day/night mode | Covered for static inspection | Theme tokens and night overrides exist. Needs visual pass for all views. |
| Sidebar settings shell | Covered | Common shortcuts open settings panels as a modal/sheet over the current product view. All Settings opens the general settings panel. |
| Responsive layout | Partial | CSS breakpoints exist for desktop, laptop, tablet, and mobile widths. Full viewport verification remains for `T-KS-027`. |
| Model management | Partial | Model cards, provider fields, API key status, GitHub Models/Copilot notes, and edit panel exist. Category tabs are visual only and do not filter model cards. |
| Registration | Covered | Mock registration mode and sign-up preview are present; password is a masked visual placeholder. |
| Member management | Partial | Members, roles, search, invite, copy-link, and remove affordances exist. Pending invitations currently demonstrate only the empty state. |
| Account settings | Covered | General settings, user information, and API information use mock data and masked/status-only API state. |
| Data and extension engines | Covered | Vector, parser, and storage settings show adapter-backed mock options. |
| Space detail navigation | Covered | IBM i opens with Wiki as the default tab; back button and tabs exist. |
| Documents tab | Covered | Upload affordances, batch metrics, progress, and file statuses exist. |
| Wiki tab | Covered | Three-column layout, index highlight, concept definitions, source trace, confidence, review status, and metadata exist. |
| Graph tab | Covered | Inline SVG, node types, legend counts, hover tooltip, and click detail panel exist. |
| Review tab | Covered | Source preview, Markdown preview, confidence badge, comments, review actions, and low-confidence queue exist. |
| Ask tab | Covered | Chat-like answer, two source references, confidence, and evidence policy panel exist. |

## Gaps To Address

| ID | Requirement / Task | Severity | Finding | Recommended Next Step |
|---|---|---|---|---|
| GAP-KS-001 | REQ-KS-013, T-KS-015 | Medium | Language/theme switches call `renderApp()`, which re-renders Home and drops any typed value in `#homeDialogInput`. The spec requires switching language not to reset the typed question. | Store the home dialogue input in `uiState` and restore it during `renderHome()`. |
| GAP-KS-002 | REQ-KS-013, T-KS-015 | Medium | Several visible labels are hardcoded rather than language-aware, including document metric labels, Wiki rail headings, metadata labels, graph legend labels, and some space badges. | Move remaining visible labels into the bilingual copy map. |
| GAP-KS-003 | REQ-KS-015, T-KS-018 | Low | Model category tabs show counts but do not filter or change active category state. | Add `selectedModelCategory` state and filter model cards by category. |
| GAP-KS-004 | REQ-KS-018, T-KS-021 | Low | Member management shows pending invitation count and empty state, but no pending invitation row is available in mock data to demonstrate the pending state. | Add at least one mock pending invitation or document that the current prototype intentionally demonstrates the empty state only. |
| GAP-KS-005 | REQ-KS-023, T-KS-027 | Medium | Responsive CSS exists, but no viewport evidence has been captured for wide desktop, laptop, tablet, and narrow mobile widths. | Run a browser viewport pass and fix any overlap, clipping, or unusable scrolling. |
| GAP-KS-006 | REQ-KS-014, T-KS-016 | Low | Night mode has token coverage, but readability has only been statically inspected. | Include night-mode checks in the same viewport pass as `T-KS-027`. |

## Recommended Task Order

1. Complete `T-KS-015` language switching hardening for state preservation and remaining hardcoded labels. Completed on 2026-07-02.
2. Complete `T-KS-018` model category filtering if model settings will be demoed interactively. Completed on 2026-07-02.
3. Complete `T-KS-021` pending invitation mock-state coverage if member management is part of the next demo. Completed on 2026-07-02.
4. Complete `T-KS-027` responsive and night-mode viewport verification across representative widths. Completed on 2026-07-02.
5. Re-run `T-KS-013` static prototype constraints after any prototype edits. Completed on 2026-07-02.

## Decision

The prototype is ready for targeted hardening, not for Phase 1 Vue scaffolding yet. The highest-value next implementation work is to close the language/state and responsive verification gaps while keeping the prototype static and mock-only.

After the 2026-07-02 full task sweep, all current Phase 0 `knowledge-space` tasks are complete for the static prototype scope. Atlas should remain in static prototype validation until stakeholders explicitly accept the product flow and authorize Phase 1 Vue implementation. Do not scaffold Vue, backend, database, authentication, or production integrations as part of this completed Phase 0 slice.

## Follow-Up Updates

### 2026-07-02: `T-KS-015` Language Switching Hardening

Addressed `GAP-KS-001` by storing the home dialogue input in `uiState.homeQuestion`, updating it from manual typing and suggested prompt clicks, and rendering it back into `#homeDialogInput` after language/theme re-renders.

Addressed the highest-impact part of `GAP-KS-002` by moving Documents metrics, Wiki rail labels, Wiki metadata labels, Graph legend labels, Review source/page labels, space state badges, member RBAC count text, and model credential empty-state labels into the bilingual copy map.

Canonical domain terms and evidence values such as `IBM i`, `Source Trace`, `SME Review`, `source_trace`, `REVIEW_REQUIRED`, adapter names, source filenames, and model provider names remain intentionally stable across languages.

### 2026-07-02: Prototype Acceptance Sweep

Completed the acceptance-oriented checks for `T-KS-013`, `T-KS-016`, and `T-KS-027`.

Automated Chrome DevTools Protocol checks covered:

- Language and theme switching preserve the Home dialogue input.
- `IBM i Modernization` opens the Wiki tab by default.
- Documents, Graph, Review, and Ask tabs activate correctly.
- Settings opens as a modal over the active product view and closes back to that view.
- Wide desktop, desktop, laptop, tablet, narrow, and mobile viewports were checked across Home, Wiki, Graph, Review, Ask, Member settings, Model settings, and Parser Engine settings.
- No body-level horizontal overflow, settings-modal viewport escape, or non-allowed text/control overflow remained after fixing Model settings button and card wrapping.

Visual screenshot spot checks covered mobile Model settings, mobile Graph, and wide desktop Wiki in night mode. Graph and member table horizontal scrolling remain allowed internal behavior by spec.

Static checks covered:

- Inline JavaScript syntax validation with `node --check`.
- Diff hygiene with `git diff --check`.
- Dependency/network scan for `fetch`, `XMLHttpRequest`, external scripts, stylesheets, and CDN references.
- Secret/private-path scan for raw API key or token assignments and private absolute paths.

One issue was found and fixed during the sweep: Model settings used fixed-width icon-button styling for text actions such as Replace, Remove, and Add Header, and the embedding model card allowed a long model name to overflow. CSS now lets those text actions size to content and lets model card text wrap.

### 2026-07-02: Model And Member Demo Hardening

Completed `T-KS-018` and `T-KS-021` follow-ups from the prototype review.

Updates:

- Model Management now keeps `selectedModelCategory` in UI state.
- Model category tabs filter visible model cards for all, chat, embedding, rerank, vision, and speech categories.
- Empty model categories show a clear empty state instead of rendering a blank grid.
- Member Management now includes one mock pending invitation row with invitee, email, role, invite time, inviter, pending badge, and resend affordance.

Verification:

- Chrome headless interaction check confirmed pending invitation visibility.
- Chrome headless interaction check confirmed all-model, embedding-only, and empty-category model filtering behavior.

### 2026-07-02: Full Phase 0 Task Sweep

Completed the full `T-KS-001` through `T-KS-027` task sweep for the static prototype scope.

Coverage:

- Home cards and dialogue mode.
- Knowledge Space routing and tab state.
- Documents metrics and file status badges.
- Wiki index, concepts, metadata, trace, confidence, and review status.
- Graph inline SVG nodes, legend, and click detail behavior.
- Review workbench panes, comments, actions, and queue.
- Ask answer, source references, and confidence.
- Language and theme switching.
- Settings modal, model management, registration, member management, account/API settings, and engine settings.
- Data/model/API contract presence for review actions, UI preferences, model configs, registration/membership, account/API, and engine configs.
- Static prototype constraints and responsive behavior.

Verification result:

- Full task assertions: 26 passed, 0 failed.
- Responsive sweep: no unexpected body-level horizontal overflow, modal escape, or text/control overflow across wide desktop, desktop, laptop, tablet, narrow, and mobile viewports.
- Static checks: JavaScript syntax, diff hygiene, dependency/network scan, and secret/private-path scan passed.

### 2026-07-02: Phase 1 FE Baseline Alignment

Stakeholder review later authorized Phase 1 and clarified that the current FE should be treated as the frontend source of truth, even where it differs from the earlier Phase 0 prototype.

Alignment update:

- `frontend/public/atlas-prototype.html` is the active Phase 1 FE fidelity baseline.
- `prototypes/index.html` mirrors the current FE baseline for direct static review.
- The SDD spec, design, and task docs now describe the current Home Knowledge Space library, create Knowledge Space panel, General Settings language/theme controls, and list-style Model Management page.
- Earlier Phase 0 dialogue-first notes remain historical review evidence, not the current UI target unless reintroduced explicitly.
