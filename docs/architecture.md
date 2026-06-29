# Architecture

Atlas Knowledge Hub uses a lightweight, adapter-based architecture. The first version is a static prototype, but the design leaves clear boundaries for later implementation.

## Components

- Frontend: Knowledge Space UI, upload flow, batch status, Wiki browsing, graph view, review queue, and Ask screen. The current MVP is static HTML/CSS/JS; the implementation direction is Vue 3 + Vite + TypeScript after prototype validation.
- Backend: Future Java + Spring Boot internal API for workspaces, batches, files, reviews, metadata, and publication.
- Database: Future PostgreSQL storage for workspace, batch, file item, review, Wiki, source chunk, and graph metadata.
- Migrations: Future Flyway migrations for database schema changes.
- Converter layer: Adapter boundary for Office-to-PDF and PDF-to-Markdown conversion.
- Internal converter adapter: Wraps `trinity-office` and `document-normalize` without exposing tool-specific details to the product.
- Markdown normalizer: Converts parser output into standardized LM Wiki Markdown with front matter, source trace, confidence, and review status.
- Review workflow: Tracks low-confidence sections, SME decisions, comments, and publication status.
- Knowledge graph layer: Builds lightweight graph nodes and edges from approved Wiki content.
- File storage / metadata: Separates raw input, generated PDFs, extracted assets, normalized Markdown, reports, and published Wiki pages.

## Diagram

```text
+-----------------------+
|       Frontend        |
| static prototype now; |
| Vue 3/Vite/TS later   |
+-----------+-----------+
            |
            v
+-----------------------+
|      Backend API      |
| Java + Spring Boot    |
| workspace, batch, wiki|
+-----------+-----------+
            |
            v
+-----------------------+
| PostgreSQL + Flyway   |
| metadata, review,     |
| graph, source trace   |
+-----------+-----------+
            |
            v
+-----------------------+       +-----------------------+
|   Converter Layer     |------>| Internal Adapters     |
| adapter interfaces    |       | trinity-office        |
| parser-neutral calls  |       | document-normalize    |
+-----------+-----------+       +-----------------------+
            |
            v
+-----------------------+
| Markdown Normalizer   |
| front matter, trace,  |
| confidence, status    |
+-----------+-----------+
            |
            v
+-----------------------+       +-----------------------+
|   Review Workflow     |------>| File Storage /        |
| SME approve, fix, OCR |       | Metadata              |
+-----------+-----------+       +-----------------------+
            |
            v
+-----------------------+
| LM Wiki + Graph Layer |
| approved pages, nodes,|
| edges, future Ask     |
+-----------------------+
```

## Adapter Rule

No product workflow should call a parser or converter directly. All converter tools must be invoked through adapters so the implementation can switch among internal OCR, MinerU, Docling, PaddleOCR, `document-normalize`, or future engines.

## SDD Rule

Use lightweight SDD once implementation starts: a short spec, design notes, and task list for each meaningful feature phase. Keep Phase 0 documentation/prototype changes lightweight.
