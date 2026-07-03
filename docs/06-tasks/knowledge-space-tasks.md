# Tasks: Knowledge Space

## Status

Phase 1 frontend implementation started on 2026-07-02 after stakeholder authorization.

All `T-KS-001` through `T-KS-027` tasks are complete for the current static prototype scope, with one phase decision: `T-KS-014` records that Atlas should continue static prototype validation and should not scaffold Vue until stakeholder acceptance explicitly starts Phase 1.

Stakeholder authorization to start Phase 1 was given on 2026-07-02. Phase 1 starts with a Vue 3 + Vite + TypeScript frontend shell that reproduces the current FE behavior with mock data only. Backend, database, authentication, production persistence, and real adapter integrations remain out of scope until later phases.

After stakeholder review, the current Phase 1 FE is now the frontend baseline even where it differs from the earlier static prototype. `frontend/public/atlas-prototype.html` is the active FE fidelity file, and `prototypes/index.html` mirrors it for direct static review.

After subsequent stakeholder IA review, the accepted prototype baseline now uses Global Chat outside Knowledge Space detail, and the Knowledge Space detail tabs are `文档`, `处理中心`, `Wiki`, and `图谱`.

See `docs/00-context/knowledge-space-prototype-review.md` for review notes and verification evidence.

## Translation Note

This historical `knowledge-space` task document predates the bilingual SDD rule. This update records the accepted English task/evidence baseline first. Full Simplified Chinese synchronization for the historical Knowledge Space SDD set is deferred to a dedicated documentation pass.

## Implementation Gates

- Requirements, stories, spec, architecture, data flow, data model, design, and tasks are present.
- Stakeholders have accepted the prototype-level product flow.
- Backend implementation is still blocked until API guide and data model are reviewed.
- No task may introduce real company data or external cloud calls.

## Task Breakdown

| ID | Task | Depends On | Verification |
|---|---|---|---|
| T-KS-001 | Review current static prototype against requirements and record gaps. | Requirements | Manual checklist against `docs/03-spec/knowledge-space-spec.md`. |
| T-KS-002 | Harden home Knowledge Space card behavior and visual states. | T-KS-001 | Cards show all required fields and IBM i opens detail. |
| T-KS-003 | Harden home dialogue mode interactions. | T-KS-001 | Suggested prompts fill input; selected context is visible; no network call. |
| T-KS-004 | Harden space shell routing and tab state. | T-KS-001 | Back button and all tabs work; Wiki is default. |
| T-KS-005 | Harden document batch metrics and progress UI. | T-KS-001 | Batch counts and progress match mock data. |
| T-KS-006 | Harden file tree status rendering. | T-KS-005 | Required file paths and statuses render with readable badges. |
| T-KS-007 | Harden Wiki index, concept, metadata, and trace behavior. | T-KS-001 | Index highlights sections; trace/confidence/review visible. |
| T-KS-008 | Harden graph tooltip, legend, and node detail behavior. | T-KS-001 | Hover and click interactions work with inline SVG. |
| T-KS-009 | Harden review workbench layout and actions. | T-KS-001 | Source/Markdown panels, comments, buttons, and queue are visible. |
| T-KS-010 | Define review action state transition behavior for future implementation. | Data model | State transitions documented and aligned with `docs/review-workflow.md`. |
| T-KS-011 | Harden Ask mock answer, source references, and confidence display. | T-KS-001 | Ask shows answer, two sources, and medium confidence. |
| T-KS-012 | Validate source trace, confidence, and review metadata presence across surfaces. | T-KS-007, T-KS-008, T-KS-009, T-KS-011 | Manual traceability review. |
| T-KS-013 | Verify static prototype constraints. | All prototype tasks | No external dependencies, no network calls, no real data. |
| T-KS-014 | Decide implementation entry point: continue static prototype or scaffold Vue. | Stakeholder acceptance | Decision recorded in docs or ADR. |
| T-KS-015 | Add prototype language switching. | T-KS-001 | Chinese/English control updates primary UI copy without resetting current view. |
| T-KS-016 | Add prototype day/night mode. | T-KS-001 | Theme control updates shared tokens and all main views remain readable. |
| T-KS-017 | Define future UI preference persistence contract. | Data model | `UiPreference` and optional API guide entries documented. |
| T-KS-018 | Add prototype model management settings view. | T-KS-001 | Settings shows model cards and an edit panel with provider/API fields. |
| T-KS-019 | Define future model config and secret-handling contract. | Data model, API guide | `ModelConfig` and masked-secret API guidance documented. |
| T-KS-020 | Add prototype registration settings view. | T-KS-001 | Settings shows registration mode and mock sign-up preview without real account creation. |
| T-KS-021 | Add prototype Knowledge Space member management view. | T-KS-001 | Settings shows pending invitations, member table, roles, search, invite, and remove affordances. |
| T-KS-022 | Define future registration and membership contracts. | Data model, API guide | `UserAccount`, `SpaceMembership`, `SpaceInvitation`, `RegistrationPolicy`, and API guidance documented. |
| T-KS-023 | Add prototype account settings views. | T-KS-001 | Settings shows General Settings, User Information, and API Information with mock and masked data. |
| T-KS-024 | Add prototype data and extension engine settings. | T-KS-001 | Settings shows vector database, parser, and storage engine options as adapter-backed mock configs. |
| T-KS-025 | Define future account/API/engine contracts. | Data model, API guide | `AccountSettings`, `ApiAccessInfo`, `EngineConfig`, and API guidance documented. |
| T-KS-026 | Convert Settings from a main view into sidebar shortcuts plus an All Settings modal/sheet. | T-KS-018, T-KS-021, T-KS-023, T-KS-024 | Sidebar settings shortcuts open the correct modal panel; All Settings opens over the current view; close returns to the previous view. |
| T-KS-027 | Harden responsive prototype behavior across common viewport sizes. | T-KS-001, T-KS-026 | Sidebar, cards, settings modal, graph, review, Ask, member table, and engine layouts remain usable at wide desktop, laptop, tablet, and narrow mobile widths. |
| T-KS-028 | Start Phase 1 Vue frontend shell with mock data. | T-KS-014, stakeholder authorization | `frontend` has Vue 3, Vite, TypeScript, typed mock data, core Knowledge Space surfaces, and no backend/external service calls. |
| T-KS-029 | Add Phase 1 frontend verification baseline. | T-KS-028 | Frontend typecheck, build, unit/component tests, coverage, and E2E smoke test pass or skipped checks are explicitly reported with reasons. |
| T-KS-030 | Align static HTML and SDD docs to current Phase 1 FE baseline. | T-KS-028, stakeholder review | `prototypes/index.html` mirrors `frontend/public/atlas-prototype.html`; spec, design, tasks, and frontend README describe the current FE layout and interactions. |
| T-KS-031 | Align accepted IA refinement: global multi-Knowledge-Space chat, document management/detail drawer, processing center, Wiki index, and graph canvas. | Stakeholder IA review | Spec/design/tasks reflect the accepted IA; prototype mirror is byte-identical; build and E2E pass. |

## Completion Matrix

| ID | Status | Evidence |
|---|---|---|
| T-KS-001 | Complete | Prototype review recorded in `docs/00-context/knowledge-space-prototype-review.md`. |
| T-KS-002 | Complete | Home cards show required fields and IBM i opens detail. |
| T-KS-003 | Complete | Suggested prompts fill input, selected context is visible, and no network calls exist. |
| T-KS-004 | Complete | Back button, default Wiki tab, and tab switching verified. |
| T-KS-005 | Complete | Document batch metrics and progress render from mock data. |
| T-KS-006 | Complete | File paths and status badges render with readable labels. |
| T-KS-007 | Complete | Wiki index highlight, concepts, metadata, trace, confidence, and review status verified. |
| T-KS-008 | Complete | Inline SVG graph nodes, legend, tooltip/click detail behavior verified. |
| T-KS-009 | Complete | Review source/Markdown panes, comments, actions, and queue verified. |
| T-KS-010 | Complete | Review state transitions documented in data flow, API guide, and `docs/review-workflow.md`. |
| T-KS-011 | Complete | Ask answer, two source references, and medium confidence verified. |
| T-KS-012 | Complete | Trace, confidence, and review metadata verified across Wiki, Review, Graph, and Ask surfaces. |
| T-KS-013 | Complete | Static constraints verified: no external dependency, network call, real secret, or private path introduced. |
| T-KS-014 | Complete | Original Phase 0 decision recorded; later superseded by 2026-07-02 stakeholder authorization to start Phase 1. |
| T-KS-015 | Complete | Language switching preserves UI state and updates primary UI copy. |
| T-KS-016 | Complete | Day/night token switching and readability verified in acceptance sweep. |
| T-KS-017 | Complete | `UiPreference` contract documented in data model. |
| T-KS-018 | Complete | Model settings view, edit panel, category filtering, and empty category state verified. |
| T-KS-019 | Complete | `ModelConfig` and masked secret behavior documented in data model and API guide. |
| T-KS-020 | Complete | Registration settings and mock sign-up preview verified as visual-only. |
| T-KS-021 | Complete | Member management includes pending invitation, members, roles, search, invite, copy-link, remove affordances. |
| T-KS-022 | Complete | `UserAccount`, `SpaceMembership`, `SpaceInvitation`, `RegistrationPolicy`, and API guidance documented. |
| T-KS-023 | Complete | General, User Information, and API Information settings verified with mock/masked data. |
| T-KS-024 | Complete | Vector, parser, and storage engine settings verified as adapter-backed mock configs. |
| T-KS-025 | Complete | `AccountSettings`, `ApiAccessInfo`, `EngineConfig`, and API guidance documented. |
| T-KS-026 | Complete | Sidebar shortcuts and All Settings modal behavior verified. |
| T-KS-027 | Complete | Responsive behavior verified across wide desktop, desktop, laptop, tablet, narrow, and mobile widths. |
| T-KS-028 | Complete | `frontend/` now contains a Vue 3 + Vite + TypeScript host for the accepted attachment prototype fidelity baseline. |
| T-KS-029 | Complete | Typecheck, build, Vitest coverage, and Playwright E2E smoke checks passed for the Phase 1 shell. |
| T-KS-030 | Complete | Current FE baseline mirrored from `frontend/public/atlas-prototype.html` to `prototypes/index.html`; SDD docs updated to current home library, create Knowledge Space, General Settings language/theme, and list-style model management behavior. |
| T-KS-031 | Complete | Global Chat moved outside Knowledge Space detail with multi-space selection; detail tabs are Documents, Processing Center, Wiki, and Graph; Documents, Processing Center, Wiki, and Graph layouts updated; `npm run build`, `npm run e2e`, mirror diff, and `git diff --check` passed. |

## T-KS-001 Review Result

Static review found that the prototype already covers most required Knowledge Space surfaces: home cards, dialogue mode, space tabs, Documents, Wiki, Graph, Review, Ask, settings shell, model settings, registration, member management, account settings, and engine settings.

Follow-up gaps are tracked in `docs/00-context/knowledge-space-prototype-review.md`. The highest-priority gaps are:

- Preserve typed home dialogue input across language/theme re-renders.
- Move remaining hardcoded labels into the bilingual copy map.
- Verify responsive and night-mode behavior across representative viewport widths.
- Decide whether model category tabs and pending invitation rows need interactive demo behavior before stakeholder review.

## T-KS-015 Implementation Result

The prototype language switching hardening is complete for the current static prototype scope.

Implemented behavior:

- Home dialogue typed input is stored in UI state and survives language/theme re-renders.
- Suggested prompt clicks update the same stored input state.
- Documents metrics, Wiki labels, Graph legend labels, Review source/page labels, space state badges, member RBAC labels, and model credential empty-state labels now use the bilingual copy map.
- Canonical product and evidence terms remain stable where translation would reduce traceability.

## Acceptance Sweep Result

`T-KS-013`, `T-KS-016`, and `T-KS-027` are complete for the current static prototype scope.

Checks performed:

- Static JavaScript syntax validation.
- Diff hygiene check.
- External dependency and network-call scan.
- Secret/private-path scan.
- Chrome headless interaction checks for language/theme state preservation, tab activation, and settings modal behavior.
- Chrome headless viewport checks across wide desktop, desktop, laptop, tablet, narrow, and mobile sizes for Home, Wiki, Graph, Review, Ask, member settings, model settings, and parser engine settings.

Result:

- No external dependency, network call, real secret, or private path was introduced.
- No body-level horizontal overflow, settings-modal viewport escape, or unexpected text/control overflow remains.
- Night mode remained readable in the checked surfaces.
- Model settings text-button and long-model-name wrapping issues found during acceptance were fixed in the prototype.

## T-KS-018 And T-KS-021 Implementation Result

The remaining prototype demo hardening tasks are complete for the current static prototype scope.

Implemented behavior:

- Model category tabs now maintain selected category state and filter visible model cards.
- Empty model categories show an explicit empty state.
- Member Management now shows a mock pending invitation row as well as active members.
- Pending invitation rows include invitee identity, role, invite time, inviter, pending status, and a resend affordance.

## Full Task Sweep Result

The full Phase 0 `knowledge-space` task sweep completed on 2026-07-02.

Chrome headless verification covered Home cards, dialogue input, space routing, Documents, Wiki, Graph, Review, Ask, language/theme switching, all Settings panels, model category filtering, pending invitations, settings modal behavior, and responsive layout.

Result:

- Full task assertions: 26 passed, 0 failed.
- Responsive overflow sweep: passed with no unexpected body-level horizontal overflow, modal viewport escape, or control text overflow.
- Static syntax, diff hygiene, network/dependency scan, and secret/private-path scan passed.
- Phase decision at that time: remain in Phase 0 static prototype validation until stakeholders explicitly accept the prototype and start Phase 1 Vue implementation. This condition was later satisfied by 2026-07-02 stakeholder authorization.

## Test Plan

Prototype phase:

- Open `prototypes/index.html` directly in a browser.
- Verify home dialogue mode and Knowledge Space cards.
- Verify IBM i card opens detail with Wiki as default.
- Verify every tab switches and renders expected content.
- Verify graph hover and click behavior.
- Verify language switching on home and every space tab.
- Verify day/night mode on home and every space tab.
- Verify Settings > Model Management renders without external calls and contains no real secrets.
- Verify Settings > Registration renders without storing passwords or creating accounts.
- Verify Settings > Member Management renders with mock members, roles, and invitations only.
- Verify Settings > Account views render with mock user/API data and no raw secrets.
- Verify Settings > Data and Extension engine views render with adapter-backed mock configs only.
- Verify sidebar settings shortcuts and All Settings open a modal/sheet over the current view, and closing returns to the previous product view.
- Verify responsive behavior at representative wide desktop, laptop, tablet, and narrow mobile widths.
- Verify no external network calls or dependencies are present.

Future implementation phase:

- Unit tests for data mappers, status mapping, and component behavior.
- Integration tests for API contracts when backend exists.
- E2E tests for create space, upload batch, review content, publish Wiki, graph exploration, and Ask evidence flow.

## Phase 1 Frontend Shell Result

Started Phase 1 on 2026-07-02 after stakeholder authorization.

Implemented scope:

- Vue 3 + Vite + TypeScript application under `frontend/`.
- The current FE prototype is preserved as `frontend/public/atlas-prototype.html` and hosted by the Vue app as the Phase 1 fidelity baseline.
- `prototypes/index.html` mirrors the current FE prototype for direct static review.
- Core FE behavior remains available for Home Knowledge Space library, create Knowledge Space panel, Knowledge Space detail, Documents, Wiki, Graph, Review, Ask, language/theme controls in General Settings, and Settings modal.
- Typed mock data and tests remain in place as the starting point for later component extraction.
- Phase 1 remains mock-only: no backend, database, authentication, production persistence, external service calls, real credentials, or concrete parser/vector/storage/model integrations.

Verification result:

- `npm run typecheck` passed.
- `npm run build` passed.
- `npm run test:coverage` passed with 4 tests and above-threshold coverage.
- `npm run e2e` passed with 1 Playwright Chromium smoke test.

## T-KS-030 Current FE Baseline Alignment Result

Completed on 2026-07-02 after stakeholder direction to use the current FE as the source of truth.

Updated scope:

- `prototypes/index.html` now mirrors `frontend/public/atlas-prototype.html`.
- `docs/03-spec/knowledge-space-spec.md` describes the current Home Knowledge Space library, create Knowledge Space panel, General Settings language/theme controls, and list-style Model Management page.
- `docs/05-design/knowledge-space-design.md` describes the same current FE layout and preserves Atlas visual direction instead of treating WeKnora as a style source.
- `frontend/README.md` explains the current FE baseline and static mirror relationship.

Verification result:

- Static HTML mirror and SDD alignment are verified as part of the Phase 1 frontend check set.

## T-KS-031 IA Refinement Result

Completed after stakeholder review of the current prototype.

Updated scope:

- Global Chat is outside Knowledge Space detail and is opened from the sidebar Chat entry.
- Global Chat supports selecting multiple Knowledge Spaces as answer context and shows the selected count.
- Knowledge Space detail tabs are now Documents, Processing Center, Wiki, and Graph.
- Documents uses a large-package document management layout with category rail, filters, document table, failed-state handling, bulk action bar, and a right-side document detail drawer.
- Processing Center replaces the old single-document Review workbench and now represents batch quality gates for parse failures, OCR required, low confidence, missing source_trace, LLM-generated review-required content, and ready-to-publish content.
- Wiki uses a two-column index layout with page/category list and dense index content.
- Graph uses a full-canvas inline SVG layout with floating search, legend/control panel, faint background graph density, and highlighted evidence paths.
- Ask / Trusted Ask is represented by Global Chat and consumes only reviewed, published, or clearly source-traced knowledge assets.

Verification result:

- `cd frontend && npm run build` passed.
- `cd frontend && npm run e2e` passed.
- `diff frontend/public/atlas-prototype.html prototypes/index.html` was empty.
- `git diff --check` passed.
