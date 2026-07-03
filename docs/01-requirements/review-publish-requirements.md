# Requirements: Review Publish

## Status

Draft. Slice `review-publish`. Phase 4 hardening.

## Goal

Enable SMEs and delivery leads to approve traceable Markdown and publish only approved content into Wiki-ready metadata while keeping blocked, low-confidence, or LLM-generated content out of trusted Wiki, Graph, and Ask surfaces.

## Slice Contract

- **Scope:** SME review state machine hardening, publish eligibility checks, approved Markdown publish metadata, review history, API/UI contracts, and verification planning for full-stack implementation.
- **Exclusions:** Real authentication/SSO, production RBAC enforcement, external model/vector/search calls, graph extraction implementation, Ask/RAG answer generation, real document uploads, real company documents, and production secret storage rollout.
- **Sources:** `docs/01-requirements/requirement.md`, `docs/review-workflow.md`, `docs/markdown-standard.md`, `docs/knowledge-graph-design.md`, `docs/00-context/slice-roadmap.md`, `docs/03-spec/knowledge-space-spec.md`, `docs/04-architecture/metadata-api-data-model.md`, `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md`, `frontend/public/atlas-prototype.html`, and `prototypes/index.html`.
- **Verification row:** Phase 4 hardening requires full unit + integration + E2E for the touched layer.
- **Constraints row:** Preserve source trace, confidence, and review status on all Markdown/metadata; LLM output stays review-required until verified; new endpoints require an API guide.

## Requirements

| ID | Requirement | Priority | Phase |
|---|---|---:|---|
| REQ-REVIEW-PUBLISH-001 | The system must expose review queues for publish-blocking conditions: missing `source_trace`, low confidence, OCR required, parser failure, and LLM-generated review-required content. | Must | 4 |
| REQ-REVIEW-PUBLISH-002 | The system must support SME review actions `APPROVE`, `NEED_FIX`, and `OCR_REQUIRED` and map them to `APPROVED`, `NEED_FIX`, and `OCR_REQUIRED` states without allowing direct publish from unreviewed content. | Must | 4 |
| REQ-REVIEW-PUBLISH-003 | Review history must remain append-only and must record reviewer, action, timestamp, comment, affected chunks, and target identity. | Must | 4 |
| REQ-REVIEW-PUBLISH-004 | Publish eligibility must require `APPROVED` review status, relative Markdown path, at least one source document reference, source trace coverage, and non-null confidence. | Must | 4 |
| REQ-REVIEW-PUBLISH-005 | Publishing must create or update Wiki page metadata with `PUBLISHED` review status while preserving source document IDs, Markdown path, confidence, owner, and last updated timestamp. | Must | 4 |
| REQ-REVIEW-PUBLISH-006 | Publishing must not mutate raw parser output, source chunks, or original source paths; corrections and publish metadata must remain separate from raw artifacts. | Must | 4 |
| REQ-REVIEW-PUBLISH-007 | Publish attempts that fail eligibility must return user-safe validation errors and must not leak stack traces, secrets, private paths, or raw confidential content. | Must | 4 |
| REQ-REVIEW-PUBLISH-008 | The frontend must show Processing Center publish readiness, blocked counts, review actions, and published Wiki status using the accepted FE baseline without implying production RBAC is already enforced. | Must | 4 |
| REQ-REVIEW-PUBLISH-009 | Trusted downstream surfaces must consume only published or explicitly approved/source-traced content from this slice; graph extraction and Ask generation remain separate slices. | Must | 4 |
| REQ-REVIEW-PUBLISH-010 | The implementation must use existing product-facing backend boundaries and must not call parser, converter, model, vector, storage, or search engines directly. | Must | 4 |
| REQ-REVIEW-PUBLISH-011 | Tests must cover review transitions, publish eligibility gates, append-only history, API envelopes, frontend publish state rendering, no-network behavior, and secret/private-path safety. | Must | 4 |

## Assumptions

- Phase 3 model, storage, parser, and converter adapters are treated as available through their product-facing seams, but this slice does not execute those adapters.
- Phase 2 metadata API has already introduced review status enums, review history, source chunks, file items, and deferred `wiki_page` metadata.
- Authorization is represented by role-aware contracts and tests, but production auth/RBAC implementation is outside this slice unless a future accepted SDD adds it.

## Open Questions

- Should chunk-level review and file-level review share one endpoint family or have separate review targets in the implementation slice?
- Should publish support bulk publish in this slice, or only one page/file at a time?
- What role labels should be used once production RBAC is accepted: Owner/Admin/Reviewer/Viewer only, or organization-specific roles?
