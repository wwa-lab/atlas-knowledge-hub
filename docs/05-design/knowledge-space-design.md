# Design: Knowledge Space

## Status

Draft. Updated to match the stakeholder-accepted Phase 1 IA refinement: Global Chat lives outside Knowledge Space detail, and the detail surface focuses on Documents, Processing Center, Wiki, and Graph.

## Translation Note

This historical `knowledge-space` design document predates the bilingual SDD rule. This update records the accepted English design baseline first. A full Simplified Chinese companion for the historical Knowledge Space SDD set is deferred to a dedicated documentation synchronization pass rather than creating an incomplete partial translation in this prototype adjustment.

## Experience Principles

- Make conversation easy, but make trust visible.
- Keep Atlas product-first: a repeatable knowledge workflow, not a parser script.
- Prefer compact enterprise UI over marketing-style layout.
- Keep review, source trace, and confidence close to generated knowledge.
- Use Chinese UI labels where appropriate for the internal demo.
- Support bilingual demos without hiding canonical technical terms.
- Support day and night modes through shared tokens rather than duplicated layouts.
- Treat the current Phase 1 FE as the layout baseline; older Phase 0 dialogue-first notes are historical unless reintroduced explicitly.

## Page Structure

### Home

- Left sidebar: Knowledge, Agents, Shared Spaces, Chat, recent dialogue items, workspace summary, common settings shortcuts, connected tools, and user/account footer information.
- Sidebar settings shortcuts include member management, model management, API information, vector database engine, parsing engine, and storage engine.
- Sidebar includes an `全部设置` / `All Settings` entry that opens the full settings modal/sheet.
- Main area title: `知识库`.
- Primary first-screen experience: Knowledge Space library card grid.
- The create action uses a compact folder-plus icon button near the page title rather than a generic text-only block.
- A `我创建的` / created-by-me filter and count sits above the card grid.
- Knowledge Space cards show title, short description, document count, lightweight capability/status icons, and owner/created marker.
- The current Phase 1 baseline does not show the earlier dialogue hero on Home.

### Global Chat

Layout:

- Global Chat is opened from the sidebar `对话` / `Chat` entry.
- It is a workspace-level surface, not nested under a single Knowledge Space.
- Center composition uses a large chat prompt area with suggested prompts.
- A Knowledge Space selector sits above the input area and supports selecting multiple spaces.
- The selected Knowledge Space count is visible near the input controls.
- Model selection and send affordances remain in the input tool row.

Behavior:

- Selecting or deselecting a Knowledge Space updates the selected count without leaving the Chat view.
- Chat context is limited to selected Knowledge Spaces.
- Failed parses, low-confidence unreviewed content, and missing-source-trace content blocked by Processing Center are excluded from the mock chat context.
- All chat actions are visual-only in prototype mode and do not call model, search, or external APIs.

### Language Control

- Location: Settings > General Settings.
- Prototype languages: Chinese and English.
- Default: Chinese.
- The control appears as a regular settings form field, not as a global floating or top-right utility button.
- Primary navigation, headings, buttons, placeholders, tabs, section labels, and common helper text should switch.
- Domain terms and product nouns may remain canonical when translation would reduce clarity.

### Theme Control

- Location: Settings > General Settings.
- Modes: day and night.
- Default: day.
- Use CSS/design tokens for background, panel, soft panel, text, muted text, borders, primary color, and shadows.
- Avoid creating separate markup for night mode.
- Verify graph labels, badges, Wiki source trace blocks, review panes, and chat references in both modes.

### Responsive Behavior

- Use fluid grid tracks and `clamp()` constraints where fixed page chrome would otherwise break across display sizes.
- Desktop sidebar should scale within a small width range instead of using one fixed pixel value.
- Medium-width screens may collapse sidebar labels into icon-dense navigation while preserving settings access.
- Narrow mobile screens may stack sidebar above content and convert cards, panels, settings, review, Ask, and engine layouts into single-column flows.
- Settings modal should fit within the viewport on desktop and become a full-screen sheet on narrow mobile widths.
- Graph and member table content may use internal scrolling to prevent text and controls from overlapping.
- Avoid viewport-width font scaling; prefer stable type sizes with layout changes.
- Verify representative widths: wide desktop, 1440 desktop, 1280 laptop, tablet-width, and narrow mobile.

### Settings Shell

Layout:

- Settings opens as a modal/sheet over the current product view rather than replacing Home or Knowledge Space detail.
- The modal/sheet uses a two-column layout: grouped settings navigation on the left and active settings content on the right.
- The close affordance returns to the previously visible product view.
- Sidebar shortcuts may open directly to a specific settings sub-view.
- `All Settings` opens the General Settings sub-view by default.
- The visual model may be inspired by modern AI-native knowledge products, but Atlas must not copy WeKnora assets, proprietary styling, or implementation details.

Behavior:

- Opening settings must preserve current product view, selected Knowledge Space tab, language, theme, selected model, and selected settings panel.
- Switching settings sub-views must not reset the underlying product view.
- Prototype actions remain visual-only.

### Settings / Space Information

Layout:

- Space Information appears under the Space group in the Settings navigation.
- The view uses a compact row-based detail layout rather than nested cards.
- Rows show label, helper text, and value for space ID, name, description, status, created time, storage quota, used storage, and storage usage rate.
- Status appears as a compact badge.
- Name and description rows include small edit affordances with inline save/cancel controls.

Behavior:

- The view is scoped to the selected Knowledge Space and uses mock-safe metadata.
- Editing name or description updates only local Vue mock state and should visibly update the row value.
- Storage values are product-facing mock operational metadata and must not expose private bucket names, paths, endpoints, or credentials.
- Future production implementation must move writes, RBAC checks, and audit logging behind backend APIs.

### Settings / Model Management

Layout:

- Settings uses a compact enterprise management layout.
- The current Phase 1 baseline is a list-style model management page.
- Top area shows page title, description, close affordance, and an Add Model action.
- An information panel explains built-in models and links to management guidance in mock form.
- Category filters appear as horizontal tabs above the model cards.
- Model cards appear in a responsive grid and show model name, provider/source summary, and type icon.
- The current baseline does not include a persistent right-side edit/configuration panel.
- Future edit/create flows may use a modal or dedicated panel, but they should preserve the Atlas visual system instead of copying WeKnora styling.

Model categories:

- All.
- Chat.
- Embedding.
- Rerank.
- Vision.
- Speech.

Configuration fields:

- Detailed provider/source fields are future implementation fields, not required in the current Phase 1 list baseline.
- Future fields may include model source, provider, model name, display name, Base URL, API Key status, custom request headers, multimodal support, and thinking-parameter format.

GitHub/Copilot guidance:

- Show GitHub Models as an API-style provider.
- Show GitHub Copilot as a policy/availability-aware provider option rather than a generic Base URL-only provider.
- Explain that actual model availability depends on GitHub plan, GitHub Models catalog, Copilot client, and organization policies.
- Never show real tokens in the prototype.

Actions:

- Add Model.
- Future edit flows may include test connection, cancel, and save.

All actions are visual-only in prototype mode.

### Settings / Registration

Layout:

- Registration appears as a Settings sub-view.
- The page shows current registration mode: enabled, domain-restricted, or invite-only.
- The page includes a compact sign-up preview with name, email, password, requested space, and submit button.
- The page includes policy text explaining that prototype registration does not create real accounts.

Fields:

- Name.
- Email.
- Password.
- Requested default Knowledge Space.
- Approved email domain.
- Invite-only toggle.

### Settings / Member Management

Layout:

- Member Management appears as the default Settings sub-view.
- The top area shows title, short RBAC guidance, and audit log hint.
- A pending invitations section shows count and empty or pending state.
- A space members section shows search, invite button, link/copy affordance, and table.

Member table columns:

- Name and email.
- Role.
- Joined time.
- Actions.

Roles:

- Owner.
- Admin.
- Reviewer.
- Viewer.

Design notes:

- Owner role should be visually prominent.
- Role selectors are mock controls in prototype mode.
- Remove actions should be visible but not destructive in prototype mode.
- The UI should communicate that future RBAC enforcement belongs on the backend.

### Settings / Account

Group label: `账户`.

Views:

- General Settings.
- User Information.
- API Information.
- Message Management.

General Settings should include:

- Language.
- Theme.
- Default Knowledge Space.
- Notification preferences.

User Information should include:

- Display name.
- Email.
- Organization.
- Global role.
- Recent activity summary.

API Information should include:

- API key status.
- Scope list.
- Last used time.
- Rotate and revoke affordances.
- Masked-secret guidance.

Message Management should include:

- Page title and helper text explaining that chat history can be indexed for semantic search.
- A single switch row for `启用消息索引` with a concise explanation of automatic indexing for new conversations.
- An `索引统计` section that shows a calm empty state when indexing is disabled or the Embedding model is not configured.
- A mock configuration/statistics state when enabled, using compact rows for Embedding model, indexed messages, indexed conversations, and last indexed time.
- Boundary copy explaining that real chat history, embeddings, and vector writes belong behind model/vector adapters and are not executed in prototype mode.

### Settings / Data And Extensions

Group label: `数据与扩展`.

Views:

- Vector Database Engine.
- Parsing Engine.
- Storage Engine.

Vector database cards:

- PostgreSQL / pgvector.
- Milvus.
- Qdrant.
- Elasticsearch or OpenSearch-style search.

Parsing engine cards:

- `document-normalize`.
- MinerU.
- Docling.
- PaddleOCR.
- Internal OCR.

Storage engine cards:

- Local filesystem.
- Internal artifact storage.
- S3-compatible object storage.
- NAS/shared storage.

Design notes:

- Each engine card should show type, status, default marker, and a short configuration summary.
- Copy should reinforce that engines are adapter-backed and not hardcoded.
- Add/test/save controls are visual-only in prototype mode.

### Create Knowledge Space

Elements:

- Full-screen create panel opened by the Home folder-plus action.
- Left step navigation: Basic Info, Model Config, Vector Storage, Parser Engine, Image Processing, Audio Processing, Storage Engine, Chunk Settings, Knowledge Graph, Advanced Settings.
- Right content area for the active step.
- Basic Info heading and helper text.
- Knowledge Space type segmented control: document and FAQ/question-answer.
- Index strategy cards: RAG retrieval and Wiki Knowledge Space.
- Knowledge Space name input.
- Optional description textarea with character count.
- Close, cancel, and create controls.

Behavior:

- Step navigation updates active visual state.
- Document/FAQ selection updates the active type.
- RAG/Wiki card selection updates the active index strategy.
- Create and cancel are visual-only in prototype mode.
- No backend resource is created in Phase 1.

### Space Detail

Elements:

- Breadcrumb: `知识库 > IBM i Modernization`.
- Back button.
- Upload folder and upload ZIP buttons.
- Tabs: `文档`, `处理中心`, `Wiki`, `图谱`.
- Default tab: `Wiki`.
- No Ask tab appears inside Knowledge Space detail; dialogue is handled by Global Chat.

### Product Goal Batch 1 Design Close-Out

Phase A and Phase B use the existing `knowledge-space` slice as the SDD source of truth.

- Phase A verifies the Vue product shell, sidebar, Home library, Global Chat, Settings overlay, and model management entry without using `iframe.product-frame` as the primary path.
- Phase B verifies the real Vue `IBM i Modernization` detail page, including breadcrumb, back action, upload folder/ZIP affordances, default Wiki tab, and tab switching across Documents, Processing Center, Wiki, and Graph.
- Screenshot evidence should capture the Home library and the Space Detail page from the Vite-served Vue app.

### Documents Tab

Elements:

- Left document category/tag rail.
- Main toolbar with document search, type/status/source filters, upload actions, date filters, and view toggle.
- Document table with columns for filename, tags, source, size, status, and updated time.
- Right-side document detail drawer opened by selecting a document row.
- Detail drawer with title, file type, actions, summary/front matter, uploaded time, preview/chunk actions, and Markdown preview.

Design notes:

- Status badges should distinguish success, parsing, review required, OCR required, and failed states.
- Keep adapter language visible where it clarifies architecture, but avoid tool-first copy.
- Failed rows should expose handling paths such as retry parse, view logs, and bulk delete in mock form.
- The document list is optimized for large document packages rather than a small batch summary only.

### Wiki Tab

Layout:

- Left rail: search, index/log navigation, category tabs, and Wiki page list.
- Right content area: dense auto-generated index page with summary/entity/concept links.

Behavior:

- Index/list item click changes or highlights the active index context in mock form.
- Concept-style links remain visually clickable.
- Source trace, confidence, and review status remain required metadata for generated Wiki content even when the default view is an index landing page.

### Graph Tab

Layout:

- Full-width inline SVG graph canvas.
- Floating search control on the canvas.
- Floating legend/control panel with node categories, fit-screen/hide-arrow controls, full-graph counts, and node detail.

Behavior:

- Hover node shows tooltip.
- Click node updates detail panel.
- Background nodes may be faint to communicate large-corpus density.
- Highlighted nodes and edges show the currently emphasized evidence path.

### Processing Center Tab

Layout:

- Top quality summary cards for large-batch processing.
- Left quality queue rail.
- Main issue table with issue name, type, source, count, status, and action.

Actions:

- Export issue list.
- Work priority queue.
- Retry parse.
- OCR.
- Repair trace.
- Review.

Design notes:

- This tab is a quality gate and triage workbench, not a single-document editing surface.
- It determines what can flow into Wiki, Graph, and Global Chat.
- Unresolved parse failures, missing source_trace, and unreviewed low-confidence output are blocked from trusted downstream surfaces.

### Trusted Ask / Global Chat

Layout:

- Trusted Ask is represented by the Global Chat surface.
- It is not rendered as a Knowledge Space tab.
- It includes selected Knowledge Spaces, suggested prompts, input, model selector, and send affordance.

Design notes:

- Ask consumes reviewed/published/source-traced knowledge assets.
- Ask does not directly query raw uploaded documents.

## Component Inventory For Future Vue Implementation

| Component | Responsibility |
|---|---|
| `AppShell` | Sidebar and main content layout. |
| `KnowledgeHome` | Home title, create action, created-by-me filter, and Knowledge Space card grid. |
| `CreateKnowledgeSpacePanel` | Full-screen create flow with step navigation and basic-info mock controls. |
| `KnowledgeSpaceCard` | Space summary card. |
| `SpaceDetailShell` | Breadcrumb, actions, and tabs. |
| `GlobalChat` | Workspace-level chat surface with multi-Knowledge-Space context selection. |
| `DocumentManager` | Document category rail, filters, table, failed-state actions, and upload actions. |
| `DocumentDetailDrawer` | Right-side document detail, parse-failure details, preview/chunk actions. |
| `ProcessingCenter` | Batch quality gates, quality queues, and issue table. |
| `WikiLayout` | Two-column index/page-list and dense generated index content. |
| `ConceptTerm` | Clickable/hoverable concept definitions. |
| `GraphViewer` | Full-canvas graph rendering, floating search, legend/control panel, and node detail. |
| `ModelManagement` | Settings view for Add Model action, built-in-model info, category filters, and model cards. |
| `ModelConfigPanel` | Future provider/source/API fields and advanced options when edit/create flows are implemented. |
| `RegistrationSettings` | Settings view for registration mode and sign-up preview. |
| `MemberManagement` | Settings view for Knowledge Space invitations, members, roles, and actions. |
| `AccountSettings` | Settings views for general preferences, user profile, and API information. |
| `EngineSettings` | Settings views for vector database, parser, and storage adapters. |

## Responsive Rules

- Wide desktop should show full multi-column layouts without excessive empty space.
- Laptop widths may reduce card columns and collapse secondary side panels below content.
- Tablet and narrow mobile widths should use stacked layouts, full-width settings sheet behavior, and internal scrolling for wide graph/table content.
- All supported widths must avoid incoherent overlap, clipped controls, and unreadable text.

## Visual System

- White panels on light gray page background.
- Atlas blue-gray primary action and focus color.
- Green remains available for positive status, created markers, and future success states.
- Rounded cards around 8px radius.
- Soft shadows.
- Compact but readable spacing.
- Avoid external image dependencies.

### Night Mode Tokens

- Page background should become near-black green/charcoal.
- Panels should use dark green-gray surfaces with visible borders.
- Primary green remains recognizable but should be bright enough for contrast.
- Muted text should be lighter than normal muted gray.
- Shadows should become subtle borders or low-opacity dark shadows.
- SVG graph labels and lines should remain legible against the graph canvas.
