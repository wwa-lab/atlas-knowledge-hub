# Requirements: Parser Adapter

## Status

Draft. Phase 3 adapter slice. SDD only; no product code is implemented in this pass.

## Slice Contract

- **Goal:** Atlas can parse converted PDFs into traceable Markdown, extracted image assets, and source chunks through a product-facing parser adapter without coupling product workflows to `document-normalize`.
- **Slice:** `parser-adapter`
- **Phase:** 3 adapter
- **Sources:** `README.md`, `PROJECT_RULES.md`, `DEVELOPMENT_STANDARDS.md`, `docs/00-context/sdd-profile.md`, `docs/00-context/slice-roadmap.md`, `docs/01-requirements/requirement.md`, `docs/markdown-standard.md`, `docs/batch-processing-design.md`, `docs/architecture.md`, `docs/03-spec/converter-adapter-spec.md`, `docs/04-architecture/converter-adapter-data-model.md`, and the existing backend metadata entities verified during grounding.
- **Verification row:** Phase 3 adapter requires unit + integration tests against mock engines.
- **Hard constraints:** Parser/converter/model/vector/storage must go only through product-facing adapters; tools must never be called directly; one implementation must not be hardcoded; secrets and private paths must be masked; source trace, confidence, and review status must be preserved.

## In Scope

- A product-facing parser adapter contract for PDF-to-Markdown/image extraction.
- Capability metadata for a default `document-normalize` parser adapter with masked configuration.
- Parser run and per-file result behavior for `PDF_CONVERTED` files.
- Metadata write-back for `markdownPath`, `assetsPath`, confidence, safe errors, and `MARKDOWN_GENERATED` / `LOW_CONFIDENCE` / `OCR_REQUIRED` / `FAILED` status mapping.
- Source chunk creation with page, section, confidence, and review status.
- Markdown front matter and source trace block requirements aligned to `docs/markdown-standard.md`.
- Mock-engine unit/integration tests and seam guard expectations.
- Internal API and adapter contract guidance for implementation.

## Exclusions

- Real `document-normalize` binary/process execution beyond the adapter seam contract.
- OCR execution, LLM enrichment, Markdown rewrite by a model, graph extraction, Ask/RAG, publish-to-Wiki, storage adapter object operations, frontend screens, production authentication/RBAC, and production secret manager integration.
- Real company documents, private paths, raw logs, credentials, external cloud calls, or external network dependencies.

## Requirements

| ID | Requirement | Priority | Source / Rationale |
|---|---|---|---|
| REQ-PA-001 | Product workflow code must resolve parsing through a parser adapter registry and must not call `document-normalize`, OCR engines, command runners, or outbound HTTP clients directly outside adapter scope. | Must | REQ-PROD-015, REQ-PROD-017 |
| REQ-PA-002 | `document-normalize` must be represented as one replaceable parser adapter, not as the only possible parser implementation. | Must | REQ-PROD-016, REQ-PROD-017 |
| REQ-PA-003 | Parser capability metadata must expose adapter key, display name, version/status, supported input type `pdf`, output types `markdown` and `assets`, default marker, and masked configuration summary. | Must | Adapter Standards |
| REQ-PA-004 | A parser run must accept an existing batch, optional file selection, optional adapter key, and only eligible `PDF_CONVERTED` file items. | Must | Batch workflow |
| REQ-PA-005 | Successful parsing must produce a relative Markdown path, optional relative assets path, parser adapter identity, confidence, and `MARKDOWN_GENERATED` status. | Must | REQ-PROD-018, REQ-PROD-021 |
| REQ-PA-006 | Low-confidence parser output must be represented as `LOW_CONFIDENCE` and keep `reviewStatus=REVIEW_REQUIRED`. | Must | REQ-PROD-019, REQ-PROD-032 |
| REQ-PA-007 | Parser output that requires OCR must be represented as `OCR_REQUIRED`; this slice must not execute OCR. | Must | REQ-PROD-019 |
| REQ-PA-008 | Parser failures must be represented as `FAILED` with a sanitized user-safe error and no raw stderr/stdout, stack trace, secret, private endpoint, or absolute/private path. | Must | Security/Data Standards |
| REQ-PA-009 | Generated Markdown must include required front matter fields and page/chunk-level source trace blocks from `docs/markdown-standard.md`. | Must | REQ-PROD-021, REQ-PROD-023 |
| REQ-PA-010 | Parser results must create or update source chunk metadata with source file, page, section, confidence, and review status. | Must | REQ-PROD-018, REQ-PROD-023 |
| REQ-PA-011 | Parser result writes must reject unsafe paths, out-of-range confidence, unknown file IDs, and parser results for files outside the requested batch. | Must | Metadata/API safety |
| REQ-PA-012 | Automated verification must use mock/fake parser engines and must not require real documents, real `document-normalize`, external network calls, or external cloud services. | Must | Phase 3 verification row |
| REQ-PA-013 | Parser reports must summarize total targeted files, Markdown generated, low-confidence, OCR-required, failed, skipped/ineligible, and unsupported outcomes. | Should | REQ-PROD-013 |
| REQ-PA-014 | The slice must preserve existing converter metadata, source path, review status, and confidence unless the parser result safely updates the file-level confidence. | Must | Trace/review preservation |

## Acceptance

- Complete bilingual SDD artifacts exist for requirements, stories, spec, architecture, data flow, data model, design, API guide, tasks, and traceability.
- REQ/US/T IDs match across English and Simplified Chinese copies.
- Tasks map to requirement IDs and spec sections and include exact verification commands.
- API guide is included because this Phase 3 adapter slice defines internal API/adapter contracts.
- No product code is changed in this SDD pass.

## Open Questions

| ID | Question | Impact |
|---|---|---|
| OQ-PA-001 | Should real `document-normalize` execute as an in-process command wrapper or an external worker when implementation moves beyond mock engines? | Deployment detail; does not block mock adapter contract. |
| OQ-PA-002 | Should parser output create `wiki_page` rows immediately, or should Wiki page publication remain fully deferred to a later publish slice? | This SDD defaults to no `wiki_page` creation in parser-adapter. |
| OQ-PA-003 | What confidence threshold should classify output as `LOW_CONFIDENCE`? | This SDD commits to `< 0.80` for implementation consistency; adjust before coding if product wants another value. |
