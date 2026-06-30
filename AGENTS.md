# Agent Instructions

These instructions apply to Codex, Copilot, and other coding agents working in this repository.

## Required Reading

Before making changes, always read:

- `README.md`
- `PROJECT_RULES.md`
- Relevant files in `docs/`

## Product Direction

- Keep Atlas Knowledge Hub product-first, not script-first.
- For MVP, focus on the UI prototype, batch workflow model, Markdown standard, review workflow, and lightweight graph design.
- Prefer a simple static prototype before introducing frameworks.
- Do not create a production backend, production database, authentication, or complex permission logic until explicitly requested.

## Reference Product Policy

- WeKnora may be used only as a product experience and UX benchmark.
- The project may learn from WeKnora's information architecture, interaction flow, knowledge space concept, wiki experience, graph experience, and general product patterns.
- Do not copy WeKnora source code.
- Do not copy WeKnora project structure.
- Do not copy UI assets, icons, images, stylesheets, or proprietary-looking visual details.
- Do not reproduce implementation details from WeKnora.
- Implement Atlas Knowledge Hub independently according to this repository's own architecture, SDD documents, adapter-based converter design, Markdown standard, review workflow, and internal enterprise requirements.
- If a feature is inspired by WeKnora, describe it as "inspired by modern AI-native knowledge products" rather than copying exact behavior.
- Prefer internal product needs over external product parity.
- When proposing or implementing any feature that resembles WeKnora, first map it to an Atlas-specific use case, such as folder/ZIP upload, batch parsing, source trace, SME review, LM Wiki, knowledge graph, or trusted Ask.

## Architecture Rules

- Do not hardcode a single parser engine.
- Wrap parser and converter tools through adapter interfaces.
- Treat `trinity-office` and `document-normalize` as internal tools behind adapters.
- Preserve source trace fields in all Markdown and metadata.
- Treat LLM-generated content as review-required unless verified by an SME or deterministic validation.

## Security And Data Rules

- Do not introduce external cloud calls or external network dependencies.
- Do not commit real company documents, screenshots, credentials, logs, or confidential content.
- Use mock data only.
- Do not embed secrets, tokens, internal endpoints, or private file paths.

## Change Discipline

- Keep changes small, scoped, and documented.
- Update docs when behavior or architecture changes.
- Prefer deterministic processing before LLM enrichment.
- Keep generated documents, raw inputs, assets, reports, and Wiki pages separated.
