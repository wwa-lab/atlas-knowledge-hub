# Roadmap

## Phase 0: Static HTML Prototype

- Create a no-build prototype.
- Show Knowledge Space cards.
- Demo one IBM i Modernization space.
- Include Documents, Wiki, Graph, Review, and Ask tabs with mock data.

## Phase 1: Folder/Zip Upload Mock

- Simulate folder and zip upload.
- Show file inventory, file tree, and batch creation.
- Generate mock statuses and reports.
- Start lightweight SDD artifacts for feature implementation decisions.

## Phase 2: Batch Document Conversion Integration

- Scaffold Java + Spring Boot backend only when integration work begins.
- Add converter adapter interfaces.
- Wrap `trinity-office` for Office-to-PDF conversion.
- Wrap `document-normalize` for PDF-to-Markdown and image extraction.
- Keep execution local or internal only.
- Add PostgreSQL and Flyway only when persistent batch metadata is needed.

## Phase 3: Markdown Normalization And Review Workflow

- Normalize parser output to the standard Markdown format.
- Preserve source trace, confidence, and review status.
- Add low-confidence detection and SME review actions.

## Phase 4: LM Wiki Browsing

- Publish approved Markdown into browsable Wiki pages.
- Support page metadata, source trace, backlinks, and concept highlighting.

## Phase 5: Lightweight Knowledge Graph

- Extract initial nodes and edges from approved Wiki pages.
- Visualize document, concept, entity, and source chunk relationships.
- Keep the graph inspectable and explainable.

## Phase 6: Ask/RAG Integration

- Add internal-only Ask capabilities over approved content.
- Use source-grounded answers with citations.
- Keep generated answers review-aware and traceable.
