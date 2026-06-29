# Project Rules

## Product First

Atlas Knowledge Hub is an internal knowledge product, not a one-off converter script. Features should support repeatable user workflows: upload, convert, normalize, review, publish, browse, and ask.

## Parser Neutral

The parser architecture must remain adapter-based. Do not bind the product to one parser engine. Current and future engines may include `document-normalize`, MinerU, Docling, PaddleOCR, internal OCR, or Copilot Vision.

The internal tools `trinity-office` and `document-normalize` are wrapped as converter adapters.

## Trace And Review

- Source trace is mandatory for Markdown and metadata.
- Review status is mandatory for Markdown and metadata.
- LLM-generated content is review-required unless verified.
- Confidence values must be retained when available.

## Data Safety

- Do not commit real company documents.
- Do not commit confidential screenshots, credentials, logs, exports, or raw customer content.
- Use mock/sample files only.

## Lightweight MVP

Keep the MVP small and understandable. Prefer static HTML/CSS/JS and documentation before introducing a framework, service mesh, database, or queue.

## Technology Decisions

- Use lightweight SDD for feature planning when implementation begins.
- Frontend direction: Vue 3 + Vite + TypeScript after prototype validation.
- Backend direction: Java + Spring Boot.
- Database direction: PostgreSQL.
- Migration direction: Flyway.
- Do not introduce framework scaffolding, a production database, or migration files before the project reaches the relevant implementation phase or the user explicitly asks.

## Workspace Separation

Keep these assets separate:

- Raw documents.
- Generated Markdown.
- Extracted images/assets.
- Batch reports.
- Published Wiki pages.

## Processing Order

Prefer deterministic processing before LLM enrichment:

1. File inventory.
2. Office-to-PDF conversion.
3. PDF parsing.
4. Markdown normalization.
5. Confidence and trace validation.
6. Review queue.
7. Optional LLM enrichment.
8. SME approval.
9. Wiki publication.
