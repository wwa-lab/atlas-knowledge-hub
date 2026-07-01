# Requirements: Knowledge Space

## Status

Draft.

## Context

The Knowledge Space slice turns the Atlas prototype into an implementation-ready product slice. It covers the home dialogue mode, Knowledge Space cards, IBM i Modernization detail tabs, batch status, LM Wiki, graph, review, and Ask surfaces.

This slice remains mock-data and prototype-first until stakeholders accept the product shape.

## Goals

- Make the WeKnora-style dialogue mode a first-class entry point.
- Preserve the Knowledge Space management model.
- Demonstrate the full workflow from upload to trusted Wiki, graph, review, and Ask.
- Keep source trace, confidence, and review status visible across Wiki, graph, review, and Ask.
- Define contracts that can later map to Vue, Spring Boot, PostgreSQL, and adapters.

## In Scope

- Home page with dialogue mode and Knowledge Space cards.
- Knowledge Space detail layout for `IBM i Modernization`.
- Document batch upload/status view using mock data.
- LM Wiki page with concepts, source trace, and metadata.
- Lightweight graph view with node details and evidence-oriented labels.
- Review workflow view for low-confidence content.
- Ask view with source references and confidence.
- Language switching for the main product UI.
- Day and night display modes for demo and future implementation.
- Model management for configuring dialogue model providers and model entries.
- Mock registration flow for first-time users.
- Knowledge Space member management with invitations, roles, and member list.
- Account settings for general preferences, user profile, and API access information.
- Data and extension settings for vector database, parsing, and storage engines.
- Sidebar-based settings entry points with an All Settings modal/sheet.
- Responsive behavior across common desktop, laptop, tablet, and narrow mobile viewport widths.
- Data and state contracts for implementation planning.

## Out Of Scope

- Production backend.
- Production database.
- Production-grade authentication, SSO, or complex permission backend.
- Real OCR, real RAG, or real parser execution.
- External cloud calls or external network dependencies.
- Real company documents, screenshots, credentials, logs, or private paths.

## Requirements

| ID | Requirement | Source | Priority |
|---|---|---|---|
| REQ-KS-001 | The home view must show Knowledge Space cards with name, description, document count, Wiki page count, review count, owner, last updated, and status. | Prototype request, `docs/mvp-scope.md` | Must |
| REQ-KS-002 | The home view must include a dialogue mode where users can select a knowledge base or file context and ask a question. | WeKnora reference, prototype feedback | Must |
| REQ-KS-003 | Selecting `IBM i Modernization` must open a space detail view with breadcrumb, back button, and tabs for Documents, Wiki, Graph, Review, and Ask. | Prototype request | Must |
| REQ-KS-004 | The Documents tab must show folder/ZIP upload affordances, batch summary, progress, file tree, and parser/conversion statuses. | `docs/batch-processing-design.md` | Must |
| REQ-KS-005 | The Wiki tab must present normalized LM Wiki content with concept terms, source trace, confidence, review status, source documents, owner, and related concepts. | `docs/markdown-standard.md` | Must |
| REQ-KS-006 | The Graph tab must show a lightweight explainable graph with node types, counts, hover/click details, and evidence-oriented relationships. | `docs/knowledge-graph-design.md` | Must |
| REQ-KS-007 | The Review tab must show side-by-side source preview and Markdown preview with confidence, comments, review actions, and low-confidence queue. | `docs/review-workflow.md` | Must |
| REQ-KS-008 | The Ask tab and home dialogue mode must return source-grounded mock answers with source references and confidence. | `docs/product-vision.md` | Must |
| REQ-KS-009 | All generated or LLM-influenced knowledge must remain review-required unless approved or deterministically validated. | `PROJECT_RULES.md`, `docs/review-workflow.md` | Must |
| REQ-KS-010 | The prototype must remain static, single-file when in prototype mode, and must not add external dependencies or network calls. | `README.md`, `PROJECT_RULES.md` | Must |
| REQ-KS-011 | Future implementation must keep parser and converter tools behind adapters. | `docs/architecture.md` | Must |
| REQ-KS-012 | The slice must separate raw inputs, generated PDFs, Markdown, assets, reports, and published Wiki content in its data model. | `PROJECT_RULES.md` | Should |
| REQ-KS-013 | The UI must support language switching for primary navigation, controls, headings, helper text, and common status labels. | Product feedback | Must |
| REQ-KS-014 | The UI must support day and night modes using shared design tokens so all main views remain readable. | Product feedback | Must |
| REQ-KS-015 | The Settings area must include model management for configuring dialogue models from Ollama, OpenAI-compatible APIs, GitHub Models/Copilot-available model sources, and other providers. | Product feedback, GitHub Models/Copilot docs | Must |
| REQ-KS-016 | Model configuration must avoid storing real API keys in the prototype and must clearly distinguish mock configured state from real credentials. | `PROJECT_RULES.md` | Must |
| REQ-KS-017 | The Settings area must include a mock registration configuration and sign-up preview for users entering Atlas for the first time. | Product feedback | Must |
| REQ-KS-018 | A Knowledge Space must support member management with pending invitations, active members, roles, join time, search, invite, and remove affordances. | Product feedback, WeKnora reference | Must |
| REQ-KS-019 | Member management must keep RBAC simple for MVP with Owner, Admin, Reviewer, and Viewer roles, and must not imply production-grade permission enforcement in the static prototype. | `PROJECT_RULES.md` | Must |
| REQ-KS-020 | The Settings area must include account settings for general preferences, user information, and API information using mock data only. | Product feedback, WeKnora reference | Must |
| REQ-KS-021 | The Settings area must include data and extension engine settings for vector database, parsing, and storage engines without binding Atlas to one implementation. | Product feedback, `PROJECT_RULES.md` | Must |
| REQ-KS-022 | Settings must be discoverable from the persistent sidebar through common shortcuts, and All Settings must open as a modal/sheet over the current product view instead of replacing the main Knowledge Space page. | Product feedback, WeKnora reference | Must |
| REQ-KS-023 | The prototype must adapt to common display resolutions so sidebar, cards, settings modal, document/wiki/graph/review/ask layouts, and controls remain readable without incoherent overlap. | Product feedback, prototype quality gate | Must |

## Open Questions

- Should the home dialogue mode query one selected knowledge base or support multiple simultaneous selections in MVP?
- Should Ask be available before content is reviewed, or should unreviewed answers be explicitly labeled as draft?
- Which graph relationships must be editable by SMEs in the first implementation phase?
- Is the first implementation target still Vue 3 + Vite + TypeScript, or should the static prototype remain the only UI artifact for another validation round?
- Which languages are required beyond Chinese and English?
- Should language and theme preferences be local-only in MVP or saved to a user profile later?
- Should Atlas use GitHub Models API directly, GitHub Copilot available-model policies, or both as separate provider types?
- Which model types must be supported in MVP: chat, embedding, rerank, vision, speech, or all of them as placeholders?
- Should self-registration be open to approved email domains only, invite-only, or both?
- Which Knowledge Space roles should be allowed to invite or remove members in MVP?
- Which API information should be visible to normal users versus admins?
- Which vector database, parser, and storage engines should be available in the first real implementation phase?
