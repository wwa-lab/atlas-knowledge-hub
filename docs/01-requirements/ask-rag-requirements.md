# Requirements: Ask RAG

## Status

Draft. Phase 4 hardening slice. Generated with `atlas-sdd-generate-all`.

## Slice Contract

| Field | Value |
|---|---|
| Goal | Atlas users can ask questions in a Knowledge Space and receive source-grounded, review-aware answers over approved knowledge with visible evidence, confidence, and safe failure behavior. |
| Slice | `ask-rag` |
| Phase | 4 hardening |
| Scope | Trusted Ask UI behavior, Ask API contract, retrieval orchestration over approved vector evidence, model-adapter answer generation, answer evidence bundle, review-awareness, audit trail, safe errors, tests, and verification. |
| Exclusions | Real external model calls, real external vector services, new parser/converter behavior, graph extraction, Wiki publish state machine, production SSO/RBAC implementation beyond internal guard checks, raw prompt/source retention, streaming, and provider cost governance. |
| Verification | Phase 4 hardening: full unit + integration + E2E for the touched layer. Also run `git diff --check`, dependency/network scans, and secret/private-path scans. |
| Constraints | Preserve source trace, confidence, and review status on all Markdown/metadata; LLM output stays review-required until verified; model/vector/search behavior stays behind adapters; secrets are masked/status-only; mock engines only; no external network calls. |

## Source Context

- `README.md`
- `PROJECT_RULES.md`
- `AGENTS.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `docs/00-context/slice-roadmap.md`
- `docs/01-requirements/requirement.md`
- `docs/product-vision.md`
- `docs/mvp-scope.md`
- `docs/markdown-standard.md`
- `docs/review-workflow.md`
- `docs/knowledge-graph-design.md`
- `docs/03-spec/metadata-api-spec.md`
- `docs/03-spec/vector-adapter-spec.md`
- `docs/03-spec/model-adapter-spec.md`
- FE baseline: `frontend/public/atlas-prototype.html`, `prototypes/index.html`

## Product Requirements

| ID | Requirement | Priority | Source |
|---|---|---|---|
| REQ-ASKRAG-001 | Ask must answer questions inside a selected Knowledge Space, not across unscoped raw uploads or unrelated spaces. | Must | REQ-PROD-039, roadmap |
| REQ-ASKRAG-002 | Ask retrieval must default to approved or published knowledge evidence only. Review-required evidence may be included only when explicitly requested and must remain visibly flagged. | Must | REQ-PROD-041, REQ-PROD-042 |
| REQ-ASKRAG-003 | Each answer must include source references with source file, page or section, source chunk id, review status, and confidence when available. | Must | REQ-PROD-040, Markdown standard |
| REQ-ASKRAG-004 | Ask orchestration must use product-facing vector and model adapter contracts; product services must not call vector DBs, model providers, SDKs, CLIs, or external HTTP clients directly. | Must | Adapter rules |
| REQ-ASKRAG-005 | Answer generation must preserve an evidence bundle containing retrieval matches, model run reference, review policy, answer confidence, and safe message. | Must | Phase 4 hardening |
| REQ-ASKRAG-006 | LLM-generated answer text must default to `REVIEW_REQUIRED` and must not mutate Wiki, source chunk, graph, file, or review status. | Must | Review workflow |
| REQ-ASKRAG-007 | Ask API responses must use the Atlas envelope and expose user-safe errors only. | Must | Metadata API |
| REQ-ASKRAG-008 | Ask requests must validate space id, question length, review policy, result limit, optional filters, and unsafe input patterns before any adapter execution. | Must | Security standards |
| REQ-ASKRAG-009 | Ask audit records must capture request metadata, selected policies, safe answer summary, evidence references, status, timestamps, and requested-by identity without raw secrets or confidential source text. | Must | Phase 4 hardening |
| REQ-ASKRAG-010 | The Trusted Ask UI must show the answer, confidence, evidence list, review-required warning, empty/no-evidence state, and safe error state aligned with the current FE baseline. | Must | FE baseline |
| REQ-ASKRAG-011 | Ask must provide deterministic mock behavior for automated tests and must not require provider accounts, external network calls, or real company documents. | Must | Project data rules |
| REQ-ASKRAG-012 | Ask must handle no approved evidence by refusing to answer from untrusted content and explaining that approved evidence is required. | Must | Product principle |
| REQ-ASKRAG-013 | Ask must keep review-required evidence separate from trusted evidence in both API and UI. | Should | REQ-PROD-042 |
| REQ-ASKRAG-014 | Ask implementation tasks must include unit, integration, API contract, E2E, seam guard, dependency/network, and secret/private-path verification commands. | Must | Roadmap verification |

## Non-Functional Requirements

| Category | Requirement |
|---|---|
| Security | No raw credentials, provider payloads, private endpoints, private paths, prompt dumps, stack traces, or confidential source text in responses, persisted safe summaries, logs, or tests. |
| Reliability | Mock vector and model adapters must support deterministic success, no-evidence, review-required evidence, validation failure, adapter failure, and partial evidence scenarios. |
| Traceability | Every answer references the requirement, evidence chunks, review status, confidence, model run, vector query policy, and task verification path. |
| Extensibility | Future model, vector, search, rerank, and storage engines remain replaceable through adapter contracts. |
| Accessibility | Trusted Ask UI states must be keyboard reachable, readable in day/night modes, and responsive without overlapping text. |

## Assumptions

- Phase 3 metadata, vector, storage, and model adapter foundations are available before implementation starts.
- `review-publish` and `knowledge-graph` may still be separate Phase 4 slices; `ask-rag` consumes approved evidence and does not implement publish or graph extraction.
- Production auth/RBAC may be represented by internal guard checks and audit fields in this slice, but full SSO and enterprise policy setup remain outside this SDD unless accepted separately.

## Out Of Scope

- Real external cloud model or vector provider calls.
- Real document ingestion, parsing, conversion, storage backends, or graph extraction.
- Publishing answers back to Wiki or approving generated answer text.
- Raw prompt/source retention, streaming, chat history across spaces, cost/quota governance, and provider administration.

## Open Questions

| ID | Question | Impact |
|---|---|---|
| OQ-ASKRAG-001 | Should review-required evidence ever be available to non-reviewer users? | Affects future RBAC policy; default remains approved-only. |
| OQ-ASKRAG-002 | Should Ask answers become review queue items in a later slice? | Current slice stores answer evidence only and keeps generated output review-required. |
| OQ-ASKRAG-003 | Should reranking be part of first Ask implementation or a follow-up? | Current slice can use vector score order and model-adapter mock answer generation without a separate reranker. |
