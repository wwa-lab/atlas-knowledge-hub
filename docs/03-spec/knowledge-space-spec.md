# Specification: Knowledge Space

## Status

Draft. Updated to reflect the stakeholder-accepted Phase 1 IA refinement: Global Chat is outside individual Knowledge Spaces; Knowledge Space detail focuses on document ingestion, processing quality gates, Wiki, and Graph.

## Translation Note

This historical `knowledge-space` SDD slice predates the bilingual SDD rule. This update records stakeholder-accepted prototype behavior in the English source-of-truth document first. A Simplified Chinese companion for the full historical Knowledge Space SDD set is deferred to a dedicated documentation synchronization pass so the current prototype IA does not drift while a partial translation is in progress.

## Source Documents

- `frontend/public/atlas-prototype.html`
- `prototypes/index.html`
- `docs/product-vision.md`
- `docs/mvp-scope.md`
- `docs/architecture.md`
- `docs/batch-processing-design.md`
- `docs/markdown-standard.md`
- `docs/knowledge-graph-design.md`
- `docs/review-workflow.md`
- `docs/technology-decisions.md`

For Phase 1, `frontend/public/atlas-prototype.html` is the current frontend baseline. `prototypes/index.html` mirrors that file for direct static review. Earlier Phase 0 prototype notes remain historical evidence, but the current FE layout and behavior are the source of truth for frontend alignment.

## Functional Requirements

### Home Knowledge Space Library

- The home view contains a page title and subtitle for knowledge base management.
- The current Phase 1 home view is a Knowledge Space library, not a dialogue-first screen.
- The home view shows a folder-plus create action, a `我创建的` / created-by-me filter with count, and a responsive grid of Knowledge Space cards.
- Knowledge Space cards show name, short description, document count, lightweight capability/status icons, and creator ownership marker.
- Selecting the `IBM i Modernization` knowledge base routes to the Knowledge Space detail experience.
- The home view must not show the earlier dialogue hero in the current Phase 1 baseline.

### Global Chat

- Chat / dialogue is a global workspace-level surface, not a tab inside a single Knowledge Space.
- The sidebar `对话` / `Chat` navigation opens the Global Chat view.
- Global Chat allows the user to select one or more Knowledge Spaces as answer context before asking a question.
- The selected Knowledge Space count is visible near the input controls.
- Global Chat must not directly query unparsed, failed, low-confidence, or missing-source-trace raw uploads.
- Content blocked by the Processing Center quality gates is excluded from the mock chat context.
- Prototype chat actions are visual-only and must not call models, search services, or external APIs.

### Create Knowledge Space

- The create action opens a full-screen create Knowledge Space panel in prototype mode.
- The panel uses a left step navigation and a right configuration area.
- The left step navigation includes Basic Info, Model Config, Vector Storage, Parser Engine, Image Processing, Audio Processing, Storage Engine, Chunk Settings, Knowledge Graph, and Advanced Settings.
- Basic Info supports a Knowledge Space type toggle for document and FAQ/question-answer modes.
- Basic Info supports index strategy cards for RAG retrieval and Wiki Knowledge Space.
- Document, FAQ, RAG, and Wiki choices are clickable mock interactions and update visible selection state.
- The panel includes Knowledge Space name input, optional description textarea, close, cancel, and create affordances.
- Create actions are visual-only in prototype mode and must not create backend resources.

### Language Switching

- The UI provides a language control inside Settings > General Settings.
- Prototype mode supports Chinese and English.
- Chinese is the default language for the internal demo.
- Switching language updates primary navigation, page headings, button labels, placeholders, helper text, tab labels, and common section titles.
- Switching language does not reset the current page, selected tab, selected Knowledge Space, selected create-panel choices, or current settings panel.
- Canonical domain terms such as `IBM i`, `RPG`, `Source Trace`, `SME Review`, `trinity-office`, and `document-normalize` may remain untranslated.

### Day And Night Mode

- The UI provides a theme control inside Settings > General Settings.
- Prototype mode supports day and night modes.
- Day mode is the default mode.
- Theme switching uses shared design tokens rather than per-component one-off colors.
- Switching theme must not reset current navigation state.
- Cards, popovers, Wiki content, SVG graph, review panes, and chat panels must remain readable in both modes.

### Sidebar And Settings Shell

- The persistent sidebar is the primary navigation and utility surface.
- The sidebar includes Knowledge, Agents, Shared Spaces, Chat, recent dialogue items, current workspace summary, common settings shortcuts, connected tool examples, and user/account footer information.
- Common settings shortcuts may open directly to Member Management, Model Management, API Information, vector database engine, parsing engine, or storage engine.
- The All Settings entry opens General Settings in a modal/sheet over the current product view rather than replacing Home or Knowledge Space detail.
- The settings modal/sheet contains a left-side grouped settings navigation and a right-side content area.
- Closing All Settings returns the user to the previously visible product view and preserves active tab, language, theme, selected model, and current settings panel.
- The settings modal/sheet is visual-only in prototype mode and must not call external services.

### Responsive Layout

- The prototype must adapt across common monitor and browser widths, including wide desktop, laptop, tablet, and narrow mobile viewports.
- The sidebar uses flexible width on desktop, icon-dense behavior on medium widths, and a stacked/top layout on narrow mobile widths.
- Knowledge Space cards use responsive grid columns rather than fixed counts.
- Settings modal/sheet fits the viewport, converts from two-column to single-column layout when needed, and can become full-screen on mobile.
- Wiki, graph, document, review, Ask, model, member, and engine layouts collapse to single-column or scrollable regions before content overlaps.
- Graph and member table surfaces may scroll internally when their content needs more horizontal space.
- Responsive changes must preserve current view state, active tab, language, theme, selected model, and selected settings panel.

### Model Management

- The Settings shell opens a Model Management view in prototype mode.
- The current Phase 1 view is a list-style management page, not a right-side edit-panel page.
- The view shows an Add Model action, a built-in-model information panel, model category filters, and model cards.
- Model category filters include all, chat, embedding, rerank, vision, and speech.
- The current mock baseline shows two managed model cards: `DeepSeek Flash` for chat and `text-embedding-v4` for embedding.
- Model cards show model name, provider/source summary, and a compact icon surface.
- Future provider/source examples may include Ollama, OpenAI-compatible API, DeepSeek, GitHub Models, GitHub Copilot available models, Azure OpenAI, Anthropic, and other custom providers.
- GitHub/Copilot copy must be careful: GitHub Copilot model availability depends on plan, client, organization policy, and supported-model availability; GitHub Models can be represented as an API-style provider using the available models catalog and inference endpoint.
- Detailed configuration fields such as provider, source type, model name, display name, Base URL, API Key status, custom request headers, multimodal support, and thinking-parameter format are future implementation concerns, not required in the current Phase 1 list baseline.
- Prototype mode must never include real API keys. It may display `已配置` / `Configured` as mock state.
- Add, test, and save actions are visual only in prototype mode and must not call external services.

### Registration

- The Settings area includes a Registration view in prototype mode.
- The Registration view shows whether self-registration is enabled.
- The prototype shows mock fields for name, email, password, and requested default Knowledge Space.
- The view may show policy hints such as approved email domain and invite-only mode.
- Registration actions are visual-only in prototype mode and must not create a real account, send email, or call external services.

### Knowledge Space Member Management

- The Settings area includes a Member Management view in prototype mode.
- The Member Management view is scoped to `IBM i Modernization` for the MVP demo.
- It shows pending invitations with count and empty/pending state.
- It shows active members with name, email, role, join time, and action affordances.
- It supports search input, invite action, role selector, link/invite affordance, and remove action in mock form.
- MVP roles are Owner, Admin, Reviewer, and Viewer.
- Only Owner/Admin should be described as able to invite or remove members in future implementation.
- Prototype mode must not implement production permission enforcement.

### Account Settings

- The Settings area includes account settings views for General Settings, User Information, and API Information.
- General Settings shows language, theme, default Knowledge Space, and notification preferences.
- User Information shows mock profile fields such as display name, email, organization, role, and recent activity.
- API Information shows API key status, scopes, last used time, rotate/revoke affordances, and safety copy.
- Prototype mode must never display real API keys or credentials.

### Data And Extension Engines

- The Settings area includes engine settings views for Vector Database Engine, Parsing Engine, and Storage Engine.
- Vector Database Engine shows mock adapter options such as PostgreSQL/pgvector, Milvus, Qdrant, and Elasticsearch/OpenSearch-style search.
- Parsing Engine shows parser adapter options such as `document-normalize`, MinerU, Docling, PaddleOCR, internal OCR, and Copilot Vision as future candidates.
- Storage Engine shows storage adapter options such as local filesystem, internal artifact storage, S3-compatible object storage, and NAS/shared storage.
- Each engine view shows default engine, status, adapter type, configuration summary, and add/test/save affordances.
- Prototype mode must not call any engine or external service.
- Future implementation must keep these engines behind adapter boundaries.

### Space Detail Navigation

- The detail view shows breadcrumb `知识库 > IBM i Modernization`.
- The detail view includes a back button returning to home.
- Tabs are `文档`, `处理中心`, `Wiki`, and `图谱`.
- The default tab is `Wiki`.
- Ask / dialogue is intentionally outside this tab set and belongs to Global Chat.

### Document Batch

- The Documents tab uses a document management layout for large package review.
- The left rail shows document category/tag search and empty category state.
- The main area shows document search, type/status/source filters, date filters, upload actions, and list/grid view affordances.
- The document list table shows filename, tag, source, size, status, and updated time.
- Failed parse rows are visibly marked and can expose failure handling affordances.
- Selecting a document row opens a right-side detail drawer over the document list.
- The detail drawer shows title, file type, summary/front matter, uploaded time, content preview, preview action, chunk action, and close controls.
- Failed document details show parse failure reason and mock next actions such as retry parse and view logs.
- Upload folder and upload ZIP actions remain mock-only and continue to open the upload review / batch flow.

### Wiki

- The Wiki tab uses a two-column knowledge index layout: a left rail for search, index/log navigation, category tabs, and page list; a right content area for the selected index page.
- Wiki content must feel auto-generated and dense, not editorial marketing copy.
- The index content summarizes the Knowledge Space and lists summary/entity/concept entries.
- Concept-style links remain visually clickable.
- Source trace, confidence, and review status remain required metadata for generated Wiki content, even when the index landing page is the default view.

### Graph

- The Graph tab uses an inline SVG graph canvas without external libraries.
- The current prototype layout is a wide graph canvas with a floating search box and a floating legend/control panel.
- The core node is `IBM i Modernization`.
- Node types include Wiki Page, Entity, Concept, Document, and Review Required.
- Node colors follow the prototype convention: blue, green, orange, gray, and red.
- Hovering a node shows a tooltip.
- Clicking a node updates a detail panel.
- The graph may show faint background nodes and highlighted evidence paths in mock form to represent a large Knowledge Space.
- Legend/control content is overlaid on the graph canvas rather than fixed as a separate right column.

### Processing Center

- The Processing Center replaces the old per-document Review tab for the large-batch workflow.
- It is a quality gate and issue triage workbench for large uploads such as 14k-document packages.
- It summarizes batch-scale quality states: total documents, parse failures, OCR required, low confidence, missing source_trace, and ready to publish.
- It shows quality queues such as parse failures, missing source_trace, low-confidence chunks, OCR required, and LLM-generated review-required items.
- It shows issue rows with issue name, type, source, count, status, and mock action.
- Unresolved Processing Center issues block content from publish-ready Wiki, Graph, and Global Chat contexts.
- Actions such as retry, OCR, repair trace, review, export issues, and work priority queue are visual-only in prototype mode.

### Trusted Ask

- Trusted Ask is implemented through Global Chat, not as a Knowledge Space detail tab.
- Answers should be based on selected Knowledge Spaces and only on reviewed, published, or clearly source-traced knowledge assets.
- Failed parses, low-confidence unreviewed content, and missing-source-trace items are excluded from the mock answer context.

## Non-Functional Requirements

- The Phase 1 frontend baseline is served by a Vue 3 + Vite + TypeScript host while preserving the current static HTML/CSS/JavaScript prototype for visual fidelity.
- `frontend/public/atlas-prototype.html` and `prototypes/index.html` must stay aligned until the prototype is extracted into Vue components.
- Prototype HTML must not use CDN, external images, or external network calls.
- Mock data only.
- No secrets, credentials, real company documents, private paths, logs, or confidential screenshots.
- The UI should be polished for a 1440px+ product demo and responsive enough for laptop screens.
- Future implementation should keep UI components replaceable and data-driven.
- Language and theme preferences may be local-only in prototype mode.
- Future implementation may persist language and theme as user preferences.
- Model configurations are mock-only in prototype mode.
- Future implementation must store model secrets in a secret manager or encrypted backend storage, not in frontend code.
- Registration and member management are mock-only in prototype mode.
- Future implementation must store credentials securely and enforce RBAC server-side.
- Account and API information are mock-only in prototype mode and must not expose real credentials.
- Engine settings are mock-only in prototype mode and must preserve parser-neutral and storage-neutral adapter boundaries.

## Domain Model

See `docs/04-architecture/knowledge-space-data-model.md`.

## Workflows

See `docs/04-architecture/knowledge-space-data-flow.md`.

## API / Interface Expectations

See `docs/05-design/contracts/knowledge-space-API_IMPLEMENTATION_GUIDE.md`.

## Acceptance Matrix

| Requirement | Acceptance Source |
|---|---|
| REQ-KS-001 | Home Knowledge Space library renders title, create action, created-by-me filter, and responsive cards. |
| REQ-KS-002 | Create Knowledge Space panel opens from the folder-plus action and supports document/FAQ and RAG/Wiki selection state. |
| REQ-KS-003 | IBM i card opens detail view with default Wiki tab and back button. |
| REQ-KS-004 | Documents tab renders batch counts, progress, file tree, and statuses. |
| REQ-KS-005 | Wiki tab renders the two-column index layout with category/page list and dense generated index content. |
| REQ-KS-006 | Graph tab renders the wide SVG graph canvas, floating search, floating legend/control panel, tooltip, and detail panel. |
| REQ-KS-007 | Processing Center renders batch quality gates, queues, issue table, and mock remediation actions. |
| REQ-KS-008 | Global Chat renders outside Knowledge Space detail and supports selecting multiple Knowledge Spaces as answer context. |
| REQ-KS-009 | Trace and review metadata remain visible or enforced across Documents, Processing Center, Wiki, Graph, and Global Chat contexts. |
| REQ-KS-010 | Static prototype has no external dependency or network call. |
| REQ-KS-013 | Settings > General Settings language control switches primary UI copy between Chinese and English without changing view state. |
| REQ-KS-014 | Settings > General Settings theme control switches day/night mode while preserving readability across all main views. |
| REQ-KS-015 | Settings opens Model Management as a list page with Add Model, built-in-model info, category filters, and model cards. |
| REQ-KS-016 | Prototype model API Key state is mock-only and no real secrets appear in source. |
| REQ-KS-017 | Settings shows registration configuration and a mock sign-up preview without creating real accounts. |
| REQ-KS-018 | Settings shows Knowledge Space member management with invitations, member table, roles, and actions. |
| REQ-KS-019 | Member management uses simple MVP roles and labels prototype RBAC as visual-only. |
| REQ-KS-020 | Settings shows General Settings, User Information, and API Information using mock data and masked secrets. |
| REQ-KS-021 | Settings shows vector database, parsing, and storage engine configuration as adapter-backed mock options. |
| REQ-KS-022 | Sidebar settings shortcuts open the Settings modal/sheet, All Settings does not replace the current product view, and closing returns to the previous view. |
| REQ-KS-023 | Prototype remains readable across wide desktop, laptop, tablet, and mobile viewport widths without incoherent text/control overlap. |
