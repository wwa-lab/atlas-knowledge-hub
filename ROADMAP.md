# Atlas Knowledge Hub Product Roadmap

> Current canonical repository status: `docs/00-context/repo-status-roadmap.zh-CN.md`. This file keeps the A-J product narrative, maturity model, and historical roadmap detail; it is no longer the only status entry point.

This roadmap is the product execution plan for building Atlas Knowledge Hub into an internal enterprise knowledge-base management product inspired by modern AI-native knowledge products.

Chinese companion: [ROADMAP.zh-CN.md](ROADMAP.zh-CN.md).

WeKnora may be used as an experience benchmark for information architecture and workflow patterns. Atlas must not copy WeKnora source code, project structure, UI assets, proprietary styles, icons, or implementation details. Atlas implements its own product architecture: folder/ZIP upload, batch parsing, source trace, SME review, LM Wiki, knowledge graph, trusted Ask, and adapter-based infrastructure boundaries.

## Product Goal

Atlas should become a real Vue 3 + Spring Boot internal product for managing enterprise knowledge spaces:

- Create and browse Knowledge Spaces.
- Upload folders or ZIP packages.
- Convert and parse source documents through internal adapters.
- Normalize parsed content into traceable Markdown / LM Wiki pages.
- Gate low-confidence, failed, or LLM-generated content through SME review.
- Publish approved knowledge into Wiki, Graph, and trusted Ask surfaces.
- Manage members, registration policy, models, parser engines, vector engines, and storage engines.
- Keep all provider/tool integrations behind product-facing adapters.
- Never expose real secrets, private paths, or confidential company data in frontend code or test fixtures.

## Current State Snapshot

As of 2026-07-05, the repository contains a strong amount of architecture, SDD, mock implementation, and acceptance scaffolding, but the product is not yet a production-ready enterprise knowledge-base management system.

| Area | Current State | Product Maturity |
|---|---|---|
| Static prototype | Broad prototype exists with home, space detail, documents, processing center, Wiki, graph, Ask, settings, and model management. | High for demo reference. |
| Vue 3 product shell | Real Vue shell has started, but only partial product parity exists. Recent work began moving away from iframe/prototype-only behavior. | Early to mid. |
| Backend metadata/API | Spring Boot metadata and contract-style APIs exist for many slices. | Mid, still demo/control-plane oriented. |
| Upload/batch workflow | Mock/sample flows exist; real enterprise folder ingestion is not complete. | Early. |
| Converter/parser adapters | Adapter contracts and mock-engine verification exist; real internal runtime hardening remains deferred. | Mid architecture, early operational maturity. |
| Review/publish | Review state and publish concepts exist with tests. | Mid. |
| Wiki | Mock/API-backed publishing concepts exist, but full Vue Wiki product experience is incomplete. | Early to mid. |
| Knowledge graph | Graph contracts and demo surfaces exist; production-grade layout, scale, and evidence interaction remain incomplete. | Early to mid. |
| Trusted Ask | Review-aware Ask flow exists in mock/API form; production retrieval quality and governance are not complete. | Early to mid. |
| Model management | Prototype and partial Vue settings interactions exist; real backend persistence, secret handling, and connection testing are not complete. | Early. |
| Auth/RBAC/audit | Mostly deferred by project rules. | Not production-ready. |
| Deployment/ops | Not yet a first-class product track. | Not started. |

## Why The Previous Roadmap Looked Done But The Product Still Fell Short

The previous roadmap mixed different meanings of "done":

- SDD document set generated.
- Backend contract implemented.
- Mock adapter test passed.
- Prototype behavior exists in static HTML.
- A host-level Vue panel exists.
- The actual user-facing Vue product page matches the accepted product experience.

Those are not the same thing.

The slice roadmap correctly tracked many engineering tasks and contracts, but it did not sufficiently distinguish **task completion** from **product acceptance**. As a result, several slices could be marked implemented while the real Vue user experience still depended on iframe prototype behavior, adjacent host panels, or mock-only fragments. That made the repository look farther along than the actual product users would experience.

The correction is:

- Roadmap progress must be measured by product surfaces and user workflows, not only by SDD/task closure.
- "Implemented" must say whether it means static prototype, Vue UI, backend API, adapter contract, API-backed UI, or production-hardening.
- Every frontend slice must identify the actual user-facing surface that proves the outcome.
- A feature is not accepted just because an adjacent debug/workbench panel exists.
- Sample/reference screenshots represent interaction expectations, not isolated styling hints.

## Product Maturity Scale

Use this scale for roadmap status going forward.

| Level | Meaning | Acceptance Evidence |
|---|---|---|
| L0 Concept | Idea exists in docs or notes. | Product goal and scope are written. |
| L1 Prototype | Static/mock prototype exists. | Direct prototype review passes. |
| L2 Vue Parity | Real Vue component matches accepted prototype behavior. | Vue screenshot and interaction parity checklist pass; no iframe dependency for primary path. |
| L3 API-backed | Vue surface is connected to backend API with mock/sample-safe data. | API contract tests, Vue tests, and E2E cover the real surface. |
| L4 Internal Beta | End-to-end workflow works with internal runtimes and safe internal data. | Upload-to-Ask loop works in a controlled internal environment. |
| L5 Production-ready | Security, RBAC, audit, operations, scale, and governance are ready. | Deployment, monitoring, security, performance, and rollback checks pass. |

## Corrected Roadmap

### Phase A: Product Experience Reset

Goal: Make the real Vue app the product surface, not the iframe prototype or a narrow tool page.

Scope:

- Real Vue product shell.
- Persistent sidebar.
- Knowledge Space library homepage.
- Global Chat entry.
- Settings overlay.
- Prototype iframe only as reference/debug entry.
- Visual parity checklist against accepted prototype.

Acceptance:

- Opening the Vite app shows the real Vue product page by default.
- `iframe.product-frame` is not part of the primary product path.
- Home, sidebar navigation, settings overlay, and Knowledge Space cards match the accepted product direction.
- Screenshots are checked against the prototype baseline.

Status: Batch 1 checkpoint complete at L2 Vue Parity. Real Vue product shell is the default product path, with screenshot/E2E evidence recorded under `docs/00-context/evidence/`. This is not final product acceptance.

### Phase B: Knowledge Space Detail Parity

Goal: Rebuild the accepted Knowledge Space detail experience in Vue.

Scope:

- `IBM i Modernization` detail page.
- Breadcrumb and return-to-home.
- Upload folder / upload ZIP actions.
- Tabs: Documents, Processing Center, Wiki, Graph.
- Default tab: Wiki.
- State-preserving tab switching.

Acceptance:

- Clicking the IBM i card opens the real Vue Knowledge Space detail page.
- Documents, Processing Center, Wiki, and Graph are real Vue views.
- Layout and interactions match the prototype baseline closely enough for stakeholder demo.

Status: Batch 1 checkpoint complete at L2 Vue Parity. The real Vue `IBM i Modernization` detail page opens from the home card, defaults to Wiki, and supports Documents, Processing Center, Wiki, and Graph tab switching with screenshot/E2E evidence. This is not final product acceptance.

### Phase C: Upload To Batch Mock Loop

Goal: Make the product entry workflow understandable and testable before real parser integration.

Scope:

- Folder/ZIP upload mock.
- File inventory.
- Supported/unsupported file counts.
- Batch creation.
- File tree.
- Batch report.
- Parse/conversion status mapping.

Acceptance:

- User can simulate uploading a folder/ZIP package.
- User sees inventory, creates a batch, and reviews status/report output.
- Failed conversion, OCR required, low confidence, review required, approved, and published states are visible.

Status: Batch 2 checkpoint complete at L2 Vue Parity. The real Vue Knowledge Space detail page now supports mock upload review, inventory, create batch, status mapping, source trace, and batch report evidence. This is not final product acceptance.

### Phase D: Processing Center

Goal: Turn review and data-quality gates into a first-class enterprise workbench.

Scope:

- Total document count.
- Parse failures.
- OCR required.
- Low confidence.
- Missing `source_trace`.
- LLM-generated review required.
- Ready to publish.
- Queue rows with source, type, count, status, and mock actions.

Acceptance:

- Users can see what blocks Wiki, Graph, and Ask eligibility.
- Processing Center explains why a document or chunk is excluded.
- Actions are visible and mock-safe until real remediation is implemented.

Status: Batch 2 checkpoint complete at L2 Vue Parity. The real Vue Processing Center now shows quality-gate metrics and queue explanations for Wiki, Graph, and Ask eligibility. This is not final product acceptance.

### Phase E: LM Wiki

Goal: Make approved knowledge browsable, dense, and traceable.

Scope:

- Wiki page list.
- Index page.
- Markdown content area.
- Source trace blocks.
- Confidence and review status.
- Concept/entity links.
- Page metadata.

Acceptance:

- Wiki feels like generated enterprise knowledge, not marketing content.
- Important content shows source trace and review state.
- Wiki pages are ready to become API-backed without redesign.

Status: Batch 3 checkpoint complete at L2 Vue Parity. The real Vue Wiki tab now provides a browsable page index, Markdown-like content, source trace, confidence, review status, entity links, and page metadata. This is not final product acceptance.

### Phase F: Knowledge Graph

Goal: Provide an explainable graph over approved knowledge assets.

Scope:

- Graph canvas.
- Node types: Wiki Page, Entity, Concept, Document, Review Required.
- Node click/hover.
- Detail panel.
- Evidence/source trace.
- Search and legend.

Acceptance:

- Users can inspect why a node or relation exists.
- Graph remains source-trace aware.
- Graph is part of the Knowledge Space detail experience, not an adjacent workbench panel.

Status: Batch 3 checkpoint complete at L2 Vue Parity. The real Vue Graph tab now provides a source-trace-aware canvas, typed nodes, search, legend, node detail, confidence, review status, and evidence. This is not final product acceptance.

### Phase G: Trusted Ask

Goal: Make source-grounded Ask a workspace-level product surface.

Scope:

- Global Chat.
- Multi-Knowledge-Space context selection.
- Question input.
- Model selector.
- Answer area.
- Evidence citations.
- Review-aware eligibility rules.

Acceptance:

- Ask answers only use reviewed, published, or clearly source-traced content.
- Failed parses, low-confidence unreviewed content, and missing-source-trace items are excluded.
- Answer includes evidence references.

Status: Batch 3 checkpoint complete at L2 Vue Parity. The real Vue global Chat surface now provides context selection, question input, model selector, evidence citations, no-evidence refusal, and review-required warning states. This is not final product acceptance.

### Phase H: Settings And Administration

Goal: Provide enterprise management surfaces without pretending production security exists before it does.

Scope:

- General settings.
- Member management.
- Registration policy.
- Model management.
- API information.
- Vector database engine.
- Parser engine.
- Storage engine.

Acceptance:

- Settings opens as a shell-level overlay.
- Model management supports list, add, edit, save/cancel, and masked API key state.
- Other settings panels have real Vue structure and clear mock/production boundaries.
- No raw secrets are stored or displayed.

Status: Batch 4 checkpoint complete at L2 Vue Parity. The real Vue settings overlay now includes non-empty general, member, registration, API, model, vector, parser, and storage management panels. Model management supports list, add, edit, cancel, save, masked API key replace/remove, and mock-safe test connection feedback. This is not final product acceptance.

### Phase I: API-backed Vue Cutover

Goal: Replace mock-only Vue surfaces with backend API data in the real product path.

Priority order:

1. Knowledge Space list/detail metadata.
2. Batch/file/chunk metadata.
3. Review queues.
4. Wiki pages.
5. Graph nodes/edges/evidence.
6. Ask runs and citations.
7. Model configuration metadata.

Acceptance:

- Real Vue product surfaces call Atlas APIs.
- Existing API contracts remain stable.
- Mock/sample data remains safe and deterministic.
- Tests cover the actual product surfaces.

Status: Batch 6 checkpoint complete at L3 API-backed for priority items 1-7. The real Vue product path now consumes Atlas APIs for Knowledge Space list/detail metadata, batch/file/chunk metadata, review queues, Wiki pages, Graph evidence, Ask runs/citations, and masked model capability metadata. Phase J hardening remains pending. This is not final product acceptance.

### Phase J: Internal Beta Hardening

Goal: Move from demo to controlled internal trial.

Scope:

- Real internal document packages.
- Runtime adapter configuration.
- RBAC.
- Audit logging.
- Error recovery.
- Large-batch performance.
- Secret handling.
- Deployment and monitoring.

Acceptance:

- A controlled user can run: upload package -> batch processing -> Processing Center -> Wiki -> Graph -> Trusted Ask.
- Sensitive data and secrets are protected.
- Operations team can deploy, monitor, and roll back.

Status: Batch 7 checkpoint complete at L4 readiness preparation for mock/sample-safe internal beta planning. The readiness report identifies controlled-trial entry criteria, safe configuration templates, L4-ready areas, L3/lower gaps, and residual P0/P1/P2 risks. Batch 8 Final evidence整理 is complete in `docs/07-acceptance/product-acceptance-report.zh-CN.md`; Atlas is ready for user acceptance review, but this is not production readiness or final product acceptance.

## Current Next-Phase Wave Progress

Status on 2026-07-05:

- Wave 1 / `wiki-foundation` / `wiki-data-model`: complete as a Wiki Foundation data-model slice. Atlas now has additive `wiki_page` metadata fields, minimal folder/run/log/issue read models, backend read APIs, contract/repository coverage, and Vue Wiki metadata rendering.
- Maturity statement: this is still a Wiki Foundation data base, not Auto Wiki ingest completion and not production readiness.
- Wave 1 / `wiki-ingest-v0`: implementation is verified as an Auto Wiki ingest v0 foundation: deterministic review-required candidates, safe run/log/issue evidence, explicit draft listing, and full local verification. This is not linkify/lint, review-gate, connector, model-assisted generation, or production readiness.
- Wave 1 / `wiki-linkify-lint`: implementation is verified as the Wiki Foundation linkify/lint base: deterministic Wiki link insertion, link metadata refresh, lint issue recording, and Processing Center/Wiki warnings. This is not review-gate, refresh/retract, connector, model-assisted generation, or production readiness.
- Current active slice completed: Wave 2 / `real-office-parser-runtime` is implemented and verified. Controlled internal `trinity-office` and `document-normalize` runtime execution now sits behind existing adapter boundaries, while default CI remains mock-safe. Optional runtime smoke tests self-skip until approved local runtime env vars and binaries are available.
- Wave 3 / `runtime-smoke-config-and-runbook`: implemented as command-level runtime smoke readiness. The bilingual runbook documents smoke env vars, Spring adapter property distinctions, skip/pass/fail outcomes, safe evidence rules, troubleshooting, and rollback. Default CI remains mock-safe; approved local pass evidence is skipped until approved runtime command values are provided. This is not production operations readiness.
- Wave 3 / `auth-space-rbac`: closeout verified for backend-enforced current-user context, local mock auth, future SSO/OIDC boundary, space membership, role matrix, protected API guards, and permission-aware UI. Dedicated role E2E is now included in the default frontend E2E suite. This is not production SSO/OIDC readiness.

## Near-Term Execution Plan

Do these next, in order:

1. **Vue Product Shell Parity**
   - Real Vue home, sidebar, Knowledge Space cards, global chat, settings overlay.
   - No iframe in primary path.
   - Screenshot parity against prototype.

2. **Knowledge Space Detail Parity**
   - Real Vue space detail.
   - Documents, Processing Center, Wiki, Graph tabs.
   - Preserve state and layout.

3. **Upload To Wiki Mock Loop**
   - Folder/ZIP mock upload.
   - Inventory -> batch -> status -> Processing Center -> Wiki.

4. **Wiki/Graph/Ask Product Depth**
   - Make the three consuming surfaces trace-aware and demo-worthy in Vue.

5. **API-backed Cutover**
   - Connect the real Vue surfaces to existing backend contracts one workflow at a time.

## Roadmap Rules Going Forward

- Do not mark a roadmap item as product-complete unless the real user-facing surface is complete.
- Record maturity level: L1 prototype, L2 Vue parity, L3 API-backed, L4 internal beta, or L5 production.
- Keep `docs/00-context/slice-roadmap.md` for SDD/task execution status, not as the only product truth.
- Keep this `ROADMAP.md` focused on product maturity and user-facing outcomes.
- Every roadmap close-out must include screenshots or E2E evidence for the real Vue surface.
- Adjacent workbench/debug panels do not count as product completion.
- Prototype parity is required before API-backed expansion for frontend surfaces.
