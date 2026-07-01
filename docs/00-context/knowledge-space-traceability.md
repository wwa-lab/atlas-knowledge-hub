# Traceability: Knowledge Space

## Status

Draft.

## Slice

`knowledge-space`

## Source Inputs

| Source | Role |
|---|---|
| `prototypes/index.html` | Current static UI prototype and demo behavior. |
| `docs/product-vision.md` | Product purpose, target users, and trust-before-conversation principle. |
| `docs/mvp-scope.md` | MVP boundaries and acceptance shape. |
| `docs/architecture.md` | Adapter-based architecture and future stack direction. |
| `docs/batch-processing-design.md` | Batch, file item, status, and report model. |
| `docs/markdown-standard.md` | Markdown metadata, source trace, confidence, and review status rules. |
| `docs/knowledge-graph-design.md` | Graph node and edge model. |
| `docs/review-workflow.md` | SME review states and decisions. |
| `docs/technology-decisions.md` | Vue/Spring/PostgreSQL/Flyway timing and implementation constraints. |

## Artifact Map

| Stage | File |
|---|---|
| Requirements | `docs/01-requirements/knowledge-space-requirements.md` |
| User Stories | `docs/02-user-stories/knowledge-space-stories.md` |
| Specification | `docs/03-spec/knowledge-space-spec.md` |
| Architecture | `docs/04-architecture/knowledge-space-architecture.md` |
| Data Flow | `docs/04-architecture/knowledge-space-data-flow.md` |
| Data Model | `docs/04-architecture/knowledge-space-data-model.md` |
| Design | `docs/05-design/knowledge-space-design.md` |
| API Guide | `docs/05-design/contracts/knowledge-space-API_IMPLEMENTATION_GUIDE.md` |
| Tasks | `docs/06-tasks/knowledge-space-tasks.md` |

## Requirement Links

| Requirement | Stories | Spec Sections | Tasks |
|---|---|---|---|
| REQ-KS-001 Knowledge Space home | US-KS-001, US-KS-002 | Home And Dialogue Mode | T-KS-001, T-KS-002 |
| REQ-KS-002 Dialogue mode | US-KS-002, US-KS-008 | Home And Dialogue Mode, Ask | T-KS-003, T-KS-014 |
| REQ-KS-003 Space detail tabs | US-KS-003 | Space Detail Navigation | T-KS-004 |
| REQ-KS-004 Batch upload/status | US-KS-004 | Document Batch | T-KS-005, T-KS-006 |
| REQ-KS-005 LM Wiki | US-KS-005 | Wiki | T-KS-007 |
| REQ-KS-006 Knowledge graph | US-KS-006 | Graph | T-KS-008 |
| REQ-KS-007 Review workflow | US-KS-007 | Review | T-KS-009, T-KS-010 |
| REQ-KS-008 Source-grounded Ask | US-KS-008 | Ask | T-KS-011 |
| REQ-KS-009 Trace and review metadata | US-KS-005, US-KS-007, US-KS-008 | Data Contracts | T-KS-012 |
| REQ-KS-010 Prototype constraints | All | Non-Functional Requirements | T-KS-013 |
| REQ-KS-013 Language switching | US-KS-009 | Language Switching | T-KS-015, T-KS-017 |
| REQ-KS-014 Day/night mode | US-KS-010 | Day And Night Mode | T-KS-016, T-KS-017 |
| REQ-KS-015 Model management | US-KS-011 | Model Management | T-KS-018, T-KS-019 |
| REQ-KS-016 Model secret safety | US-KS-011 | Model Management, Non-Functional Requirements | T-KS-018, T-KS-019 |
| REQ-KS-017 Registration | US-KS-012 | Registration | T-KS-020, T-KS-022 |
| REQ-KS-018 Space member management | US-KS-013 | Knowledge Space Member Management | T-KS-021, T-KS-022 |
| REQ-KS-019 Simple RBAC safety | US-KS-013 | Knowledge Space Member Management, Non-Functional Requirements | T-KS-021, T-KS-022 |
| REQ-KS-020 Account settings | US-KS-014 | Account Settings | T-KS-023, T-KS-025 |
| REQ-KS-021 Data and extension engines | US-KS-015 | Data And Extension Engines | T-KS-024, T-KS-025 |
| REQ-KS-022 Sidebar settings shell | US-KS-016 | Sidebar And Settings Shell | T-KS-026 |
| REQ-KS-023 Responsive layout | US-KS-017 | Responsive Layout | T-KS-027 |

## Gate Status

- Requirements: Draft.
- Stories: Draft.
- Spec: Draft.
- Architecture: Draft.
- Design: Draft.
- Tasks: Draft.
- Implementation: Not started.
