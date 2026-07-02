# Specification: Knowledge Space

## Status

Draft.

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
- Tabs are `文档`, `Wiki`, `图谱`, `Review`, and `Ask`.
- The default tab is `Wiki`.

### Document Batch

- The Documents tab shows upload folder and upload ZIP actions.
- It shows batch name `2026-06 IBM i Discovery Package`.
- It shows total files, PDF converted, Markdown generated, review required, and failed counts.
- It shows progress for conversion, Markdown generation, source trace validation, and SME review.
- It shows a mock file tree with status labels.

### Wiki

- The Wiki tab uses a three-column layout: index/recent updates, main Wiki content, metadata.
- Wiki content must feel auto-generated and dense, not editorial marketing copy.
- Concept terms are visually clickable and expose short definitions.
- Major sections include source trace, confidence, and review status.
- Metadata includes source documents, confidence, review status, last updated, owner, and related concepts.

### Graph

- The Graph tab uses inline SVG without external libraries.
- The core node is `IBM i Modernization`.
- Node types include Wiki Page, Entity, Concept, Document, and Review Required.
- Node colors follow the prototype convention: blue, green, orange, gray, and red.
- Hovering a node shows a tooltip.
- Clicking a node updates a detail panel.
- Legend counts are shown on the right.

### Review

- The Review tab shows a source preview placeholder and Markdown preview side by side.
- The source preview includes page number and source filename.
- The Markdown preview includes a confidence badge.
- Review comments can be entered.
- Actions are Approve, Need Fix, and Mark OCR Required.
- A low-confidence queue is visible below.

### Ask

- The Ask tab shows a chat-like interface.
- The input placeholder is `Ask about IBM i Modernization...`.
- A mock answer explains Source Trace in the modernization workflow.
- The answer includes source references and confidence.

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
| REQ-KS-005 | Wiki tab renders index, content, metadata, concepts, trace, confidence, and review status. |
| REQ-KS-006 | Graph tab renders SVG nodes, legend, tooltip, and detail panel. |
| REQ-KS-007 | Review tab renders side-by-side review and low-confidence queue. |
| REQ-KS-008 | Ask tab renders answer, sources, and confidence. |
| REQ-KS-009 | Trace and review metadata remain visible in Wiki, Review, Graph, and Ask surfaces. |
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
