# User Stories: Knowledge Space

## Status

Draft.

## Actors

- SME: validates parsed knowledge and approves publication.
- Developer: inspects technical source trace, dependencies, and graph relationships.
- BA: organizes business concepts and requirements evidence.
- Delivery Lead: tracks batch progress, review readiness, and knowledge coverage.
- Knowledge Consumer: asks source-grounded questions over trusted knowledge.

## Stories

### US-KS-001: Browse Knowledge Spaces

As a delivery lead, I want to see Knowledge Space cards, so that I can quickly understand document volume, Wiki readiness, review workload, ownership, and health.

#### Acceptance Criteria

- Given I open Atlas, when the home view loads, then I see the required Knowledge Space cards.
- Given a space has review work, when I view the card, then the status clearly indicates review is required.
- Given I click `IBM i Modernization`, when the action completes, then the detail view opens.

### US-KS-002: Ask From Selected Knowledge Context

As a knowledge consumer, I want to select a knowledge base and ask a question from the home view, so that I can start with conversation instead of navigation.

#### Acceptance Criteria

- Given I am on the home view, when I see the dialogue area, then I can identify the selected knowledge context.
- Given I click a suggested question, then it is inserted into the input.
- Given I choose `IBM i Modernization`, then I can continue into the space Ask experience.

### US-KS-003: Navigate Space Detail

As a project user, I want a detail view with document, Wiki, graph, review, and Ask tabs, so that I can move through the knowledge workflow without losing context.

#### Acceptance Criteria

- Given I enter `IBM i Modernization`, then I see breadcrumb and a back button.
- Given I open the space, then Wiki is the default tab.
- Given I click a tab, then only that tab content becomes active.

### US-KS-004: Track Batch Processing

As a delivery lead, I want to see batch counts, progress, and file statuses, so that I can understand conversion and review readiness.

#### Acceptance Criteria

- Given the Documents tab is open, then I see upload folder and upload ZIP actions.
- Given the batch is displayed, then I see total files, PDF converted, Markdown generated, review required, and failed counts.
- Given a file has a status, then the status label is visible next to the source path.

### US-KS-005: Read Traceable LM Wiki Content

As an SME, I want to read normalized Wiki content with source trace and confidence, so that I can validate whether the generated content is trustworthy.

#### Acceptance Criteria

- Given I open Wiki, then I see index, content, and metadata panels.
- Given I click an index item, then the relevant Wiki section is highlighted.
- Given a concept appears in the page, then it is visually clickable and can expose a short definition.
- Given content is generated or uncertain, then review status and confidence remain visible.

### US-KS-006: Explore The Knowledge Graph

As a developer, I want a lightweight graph of pages, entities, concepts, documents, and review-required nodes, so that I can inspect relationships and dependencies.

#### Acceptance Criteria

- Given I open Graph, then I see node colors by type and a legend with counts.
- Given I hover a node, then I see a tooltip.
- Given I click a node, then I see a detail panel.

### US-KS-007: Review Low-Confidence Content

As an SME, I want side-by-side source and Markdown review, so that I can approve, request fixes, or mark OCR required.

#### Acceptance Criteria

- Given I open Review, then I see source preview, page number, and source filename.
- Given I review Markdown, then I see confidence and can add comments.
- Given I choose a review action, then the intended action is visible in the interface.
- Given there are low-confidence pages, then they appear in a review queue.

### US-KS-008: Ask With Evidence

As a knowledge consumer, I want answers with source references and confidence, so that I can trust and verify the response.

#### Acceptance Criteria

- Given I open Ask, then I see a chat-like interface.
- Given the mock answer is shown, then it includes source references.
- Given the confidence is medium, then that confidence is visible.

### US-KS-009: Switch Product Language

As an internal user, I want to switch between Chinese and English UI labels, so that I can demo and use Atlas with mixed-language teams.

#### Acceptance Criteria

- Given I open Atlas, when I use the language control, then primary navigation, page headings, buttons, placeholders, and common helper text update.
- Given I switch language, then the current view and active tab remain stable.
- Given domain terms such as `IBM i`, `RPG`, `Source Trace`, and adapter names appear, then they may remain in their canonical product language.

### US-KS-010: Switch Day And Night Mode

As an internal user, I want to switch between day and night modes, so that the prototype remains comfortable in different demo environments.

#### Acceptance Criteria

- Given I open Atlas, when I use the theme control, then the UI switches between day and night mode.
- Given I switch theme, then cards, panels, Wiki, graph, review, and Ask remain readable.
- Given I switch theme, then the current view and active tab remain stable.

### US-KS-011: Manage Dialogue Models

As an administrator, I want to configure model providers and model entries, so that Atlas can use approved dialogue models such as DeepSeek, Ollama local models, GitHub Models, or Copilot-available models.

#### Acceptance Criteria

- Given I open Settings, then I can see a Model Management area.
- Given I inspect configured models, then I can distinguish chat, embedding, rerank, vision, and speech model categories.
- Given I edit a model, then I can see provider, model name, display name, Base URL, API Key status, custom headers, multimodal support, and thinking-parameter options.
- Given the prototype shows API Key state, then it never displays a real key.
- Given GitHub/Copilot is shown, then the UI explains that availability depends on GitHub Models catalog, Copilot plan, and organization policy.

### US-KS-012: Register For Atlas

As a first-time user, I want a clear registration entry point, so that I can request or create access to Atlas without relying on an administrator to manually create my account.

#### Acceptance Criteria

- Given I open Settings, then I can see a registration configuration or preview area.
- Given registration is enabled, then the prototype shows the required fields for name, email, password, and default space access request.
- Given the prototype shows registration, then it clearly uses mock data and does not create a real account.
- Given registration is restricted, then the UI can indicate approved domain or invite-only rules.

### US-KS-013: Manage Knowledge Space Members

As a space owner, I want to invite and manage Knowledge Space members, so that the right SMEs, developers, and delivery users can access the space with appropriate roles.

#### Acceptance Criteria

- Given I open Settings, then I can see a Member Management area.
- Given there are pending invitations, then I can see the pending invitation count and status.
- Given I view active members, then I can see name, email, role, join time, and actions.
- Given I manage a member, then roles are limited to Owner, Admin, Reviewer, and Viewer in MVP.
- Given I remove or invite a member in prototype mode, then the UI is visual-only and does not call a backend.

### US-KS-014: Manage Account Settings

As a user, I want account settings for general preferences, profile details, and API information, so that I can understand my Atlas identity and access configuration.

#### Acceptance Criteria

- Given I open Settings, then I can navigate to general settings, user information, and API information.
- Given I view general settings, then I can see language, theme, default space, and notification preferences.
- Given I view user information, then I can see name, email, role, organization, and recent activity as mock data.
- Given I view API information, then I can see API key status, scope, last used time, and rotate/revoke affordances without exposing a real key.

### US-KS-015: Configure Data And Extension Engines

As an administrator, I want to configure vector database, parsing, and storage engines, so that Atlas can stay adapter-based while showing the infrastructure choices behind knowledge processing.

#### Acceptance Criteria

- Given I open Settings, then I can navigate to vector database engine, parsing engine, and storage engine settings.
- Given I view vector database settings, then I can see mock engines such as PostgreSQL/pgvector, Milvus, and Qdrant.
- Given I view parsing engine settings, then I can see mock parser adapters such as `document-normalize`, MinerU, Docling, PaddleOCR, and internal OCR.
- Given I view storage engine settings, then I can see mock storage backends for local filesystem, object storage, and internal artifact storage.
- Given engines are displayed, then the UI clearly treats them as adapter-backed options and does not call external services.

### US-KS-016: Open Settings From The Sidebar

As an Atlas user, I want common settings shortcuts in the persistent sidebar and a full settings modal, so that I can adjust workspace, account, model, and engine configuration without losing my current Knowledge Space context.

#### Acceptance Criteria

- Given I am on Home or a Knowledge Space detail tab, when I click a sidebar settings shortcut, then Settings opens as a modal/sheet over the current view.
- Given I click All Settings, then the full Settings modal opens with grouped navigation on the left and active settings content on the right.
- Given I close Settings, then I return to the previously visible product view without resetting the active tab, language, theme, selected model, or selected settings panel.
- Given I switch between settings sub-views, then the underlying product view remains unchanged.

### US-KS-017: Use Atlas Across Display Sizes

As an internal demo user, I want the prototype to adapt to different monitor and browser sizes, so that the product can be reviewed on wide monitors, laptops, tablets, and narrow mobile windows without layout breakage.

#### Acceptance Criteria

- Given I open Atlas on a wide desktop viewport, then the sidebar, cards, and settings modal use available space without becoming sparse or oversized.
- Given I open Atlas on a laptop-width viewport, then cards and multi-column panels reflow without overlapping text or controls.
- Given I open Atlas on tablet or narrow mobile widths, then the sidebar, cards, settings modal, Review, Ask, and engine panels collapse into usable stacked layouts.
- Given graph or member table content needs more width, then the surface can scroll internally rather than overlapping surrounding UI.
