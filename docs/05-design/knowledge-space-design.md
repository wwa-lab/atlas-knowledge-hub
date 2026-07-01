# Design: Knowledge Space

## Status

Draft.

## Experience Principles

- Make conversation easy, but make trust visible.
- Keep Atlas product-first: a repeatable knowledge workflow, not a parser script.
- Prefer compact enterprise UI over marketing-style layout.
- Keep review, source trace, and confidence close to generated knowledge.
- Use Chinese UI labels where appropriate for the internal demo.
- Support bilingual demos without hiding canonical technical terms.
- Support day and night modes through shared tokens rather than duplicated layouts.

## Page Structure

### Home

- Left sidebar: Knowledge, Agents, Shared Spaces, Chat, recent dialogue items, workspace summary, common settings shortcuts, connected tools, and user/account footer information.
- Sidebar settings shortcuts include member management, model management, API information, vector database engine, parsing engine, and storage engine.
- Sidebar includes an `全部设置` / `All Settings` entry that opens the full settings modal/sheet.
- Sidebar footer area includes compact language and theme controls.
- Main area title: `知识库`.
- Primary first-screen experience: dialogue mode inspired by WeKnora.
- Below dialogue mode: Knowledge Space cards.

### Language Control

- Location: persistent shell, preferably sidebar footer or top utility area.
- Prototype languages: Chinese and English.
- Default: Chinese.
- The control should be compact and visible without competing with product actions.
- Primary navigation, headings, buttons, placeholders, tabs, section labels, and common helper text should switch.
- Domain terms and product nouns may remain canonical when translation would reduce clarity.

### Theme Control

- Location: persistent shell next to language control.
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
- `All Settings` opens the general settings sub-view by default.
- The visual model may be inspired by modern AI-native knowledge products, but Atlas must not copy WeKnora assets, proprietary styling, or implementation details.

Behavior:

- Opening settings must preserve current product view, selected Knowledge Space tab, language, theme, selected model, and selected settings panel.
- Switching settings sub-views must not reset the underlying product view.
- Prototype actions remain visual-only.

### Settings / Model Management

Layout:

- Settings uses a compact enterprise configuration layout.
- Left/main area shows model categories and model cards.
- Right-side configuration panel shows the selected model details.
- The visual language may reference WeKnora-style model configuration without copying implementation details.

Model categories:

- All.
- Chat.
- Embedding.
- Rerank.
- Vision.
- Speech.

Configuration fields:

- Model source: Ollama or API.
- Provider.
- Model name.
- Display name.
- Base URL.
- API Key status.
- Custom request headers.
- Multimodal support.
- Thinking parameter format.

GitHub/Copilot guidance:

- Show GitHub Models as an API-style provider.
- Show GitHub Copilot as a policy/availability-aware provider option rather than a generic Base URL-only provider.
- Explain that actual model availability depends on GitHub plan, GitHub Models catalog, Copilot client, and organization policies.
- Never show real tokens in the prototype.

Actions:

- Test connection.
- Cancel.
- Save.

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

### Home Dialogue Mode

Elements:

- Hero title and subtitle.
- Suggested question chips.
- Knowledge base selector popover.
- Selected knowledge context card.
- Large input with placeholder.
- Tool row: quick answer, scope, image/file affordances, selected context indicator, model selector, send.

Behavior:

- Suggested question click fills the input.
- Selecting `IBM i Modernization` may route to the space Ask tab.
- No real model call in prototype mode.

### Space Detail

Elements:

- Breadcrumb: `知识库 > IBM i Modernization`.
- Back button.
- Upload folder and upload ZIP buttons.
- Tabs: `文档`, `Wiki`, `图谱`, `Review`, `Ask`.
- Default tab: `Wiki`.

### Documents Tab

Elements:

- Batch summary panel.
- Progress bars.
- File tree with status badges.

Design notes:

- Status badges should distinguish success, parsing, review required, OCR required, and failed states.
- Keep adapter language visible where it clarifies architecture, but avoid tool-first copy.

### Wiki Tab

Layout:

- Left rail: search, index, recent updates.
- Center: dense auto-generated LM Wiki page.
- Right rail: metadata and related concepts.

Behavior:

- Index item click highlights a section.
- Concept hover/title exposes short definition.
- Source trace block remains visible under major sections.

### Graph Tab

Layout:

- Main inline SVG canvas.
- Right legend and node detail panel.

Behavior:

- Hover node shows tooltip.
- Click node updates detail panel.
- Legend shows counts by node type.

### Review Tab

Layout:

- Left source preview.
- Right Markdown preview.
- Low-confidence queue below.

Actions:

- Approve.
- Need Fix.
- Mark OCR Required.

### Ask Tab

Layout:

- Chat panel with user question and answer.
- Source reference list.
- Confidence badge.
- Supporting evidence/coverage side panel.

## Component Inventory For Future Vue Implementation

| Component | Responsibility |
|---|---|
| `AppShell` | Sidebar and main content layout. |
| `KnowledgeHome` | Home title, dialogue mode, and card list. |
| `DialogueMode` | Suggested prompts, selected context, and chat input. |
| `KnowledgeSpaceCard` | Space summary card. |
| `SpaceDetailShell` | Breadcrumb, actions, and tabs. |
| `DocumentBatchPanel` | Batch metrics and progress. |
| `FileTreeStatusList` | Source file paths and statuses. |
| `WikiLayout` | Index, content, metadata rails. |
| `ConceptTerm` | Clickable/hoverable concept definitions. |
| `GraphViewer` | Lightweight graph rendering and node detail. |
| `ReviewWorkbench` | Side-by-side source and Markdown review. |
| `AskPanel` | Chat-like question/answer surface with sources. |
| `ModelManagement` | Settings view for configured models and model editing. |
| `ModelConfigPanel` | Provider/source/API fields and advanced options. |
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
- Green primary action and focus color.
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
