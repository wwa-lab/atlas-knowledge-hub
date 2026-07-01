# Architecture: Knowledge Space

## Status

Draft.

## Drivers

- Product-first workflow from dialogue and space browsing into reviewable knowledge.
- Static prototype first, Vue implementation later.
- Parser-neutral conversion and parsing boundaries.
- Source trace and review metadata as first-class data.
- No production backend or database until the relevant phase.

## System Context

In prototype mode, `prototypes/index.html` owns the complete UI and mock data.

In future implementation, the slice becomes a frontend product surface backed by internal APIs for spaces, batches, files, Wiki pages, review tasks, graph nodes/edges, and Ask evidence bundles.

## Components

| Component | Prototype Responsibility | Future Responsibility |
|---|---|---|
| Home Dialogue | Mock selected knowledge context and suggested questions. | Query selected spaces/files and create source-grounded Ask requests. |
| Knowledge Space Cards | Render mock spaces and status counts. | List spaces from backend metadata. |
| Space Shell | Breadcrumb, back, tabs, active view state. | Route-driven layout in Vue. |
| Documents View | Render upload controls, batch progress, and file statuses. | Upload package, poll batch status, show reports. |
| Wiki View | Render normalized content and metadata. | Render approved/draft Wiki pages from Markdown metadata. |
| Graph View | Inline SVG graph mock. | Graph viewer over reviewed nodes and evidence-backed edges. |
| Review View | Side-by-side source and Markdown review mock. | Load review tasks, record SME actions and comments. |
| Ask View | Mock chat answer with sources. | Ask over approved or clearly labeled evidence bundles. |
| Converter Adapter | Not executed in prototype. | Wrap Office-to-PDF tools such as `trinity-office`. |
| Parser Adapter | Not executed in prototype. | Wrap PDF-to-Markdown/OCR tools such as `document-normalize`, MinerU, Docling, or internal OCR. |

## Data Flow Summary

The durable workflow is:

```text
Knowledge Space
  -> Upload Batch
  -> File Inventory
  -> Converter Adapter
  -> Parser Adapter
  -> Markdown Normalizer
  -> Confidence + Source Trace Validation
  -> Review Queue
  -> Published Wiki
  -> Graph + Ask Evidence
```

## Integration Points

- Converter adapter for Office-to-PDF.
- Parser adapter for PDF-to-Markdown and OCR.
- Markdown normalizer for Atlas Markdown standard.
- Future backend API for workspace, batch, review, Wiki, graph, and Ask metadata.
- Future PostgreSQL persistence with Flyway migrations.

## Risks And Tradeoffs

- A WeKnora-style home dialogue can make the product feel conversational before knowledge is trustworthy. The UI must keep selected context, source references, and confidence visible.
- Static SVG is enough for MVP graph demonstration but should not be overfit as a graph engine.
- Future Ask should avoid implying approved truth when evidence is unreviewed.
- Parser adapter contracts must avoid leaking tool-specific fields into product UI.

## Related ADRs

No ADRs exist yet. If the project commits to a framework, backend contract, or graph technology, create an ADR rather than burying the decision in slice docs.
