# Tasks: Knowledge Space

## Status

Draft.

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
