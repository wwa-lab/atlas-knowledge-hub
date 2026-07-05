# Architecture: Wiki Linkify And Lint

## Status

Draft for user review.

## Component Ownership

| Component | Responsibility |
|---|---|
| `WikiLinkifyLintController` | Accepts space-scoped run requests and returns Atlas envelopes. |
| `WikiLinkifyLintService` | Orchestrates target indexing, Markdown rewrite, lint, run evidence, logs, and issues. |
| Markdown linkifier utility | Performs deterministic protected-region-aware link insertion. |
| Lint rule utilities | Evaluate broken links, orphan pages, missing refs, stale refs, thin content, and ambiguous aliases. |
| Existing repositories | Read/write `wiki_page`, `wiki_generation_run`, `wiki_log_entry`, `wiki_page_issue`, source documents, and chunks. |
| Existing artifact storage | Reads and writes generated/published Markdown artifacts through the current local storage abstraction. |
| Vue Processing Center / Wiki tab | Displays issue totals and page-level warnings. |

## Boundaries

- No parser, converter, model, vector, storage engine, or search provider is called directly.
- Artifact reads/writes use the existing local artifact storage service or a product-facing wrapper.
- No external cloud call or provider-backed generation is introduced.
- Existing review/publish and ingest APIs remain compatible.

## Persistence Changes

Implementation is expected to add one small Flyway migration:

- Extend `ck_wiki_generation_run_mode` to include `linkify-lint`.
- Add repository support for space-scoped issue listing and issue de-duplication if needed.

No new table is expected unless implementation proves existing issue/run/log tables cannot safely support the contract.

## Security And Safety

- Responses include ids, counts, issue types, severity, status, and safe locators.
- Raw Markdown body content should not be copied into logs or issue messages.
- Private absolute paths, stack traces, secrets, prompts, provider payloads, and real company data are forbidden.
- Validation errors are actionable but sanitized.

## Architecture Review Notes

- API contract changes are additive.
- Persistence change is limited to the run mode constraint and optional query support.
- Source trace, confidence, and review status are preserved.
- Generated pages remain `REVIEW_REQUIRED`; published pages remain published only if already trusted.
- The slice does not introduce production auth/RBAC.
