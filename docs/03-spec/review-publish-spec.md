# Specification: Review Publish

## Status

Draft. Slice `review-publish`. Phase 4 hardening. Derived from `user-story-to-spec`.

## Overview

Review Publish hardens the bridge between SME review and trusted Wiki publication. The slice turns approved, source-traced Markdown metadata into published Wiki page records and blocks unreviewed, low-confidence, missing-trace, failed, or LLM-generated content from trusted Wiki, Graph, and Ask contexts.

## Source Stories

| Story | Capability |
|---|---|
| US-REVIEW-PUBLISH-001 | Triage publish-blocking review queues. |
| US-REVIEW-PUBLISH-002 | Record SME review decisions. |
| US-REVIEW-PUBLISH-003 | Publish approved Markdown to Wiki metadata. |
| US-REVIEW-PUBLISH-004 | Preserve raw evidence and adapter boundaries. |

## Actors

- **SME Reviewer:** Reviews generated Markdown or chunks and decides whether content can advance.
- **Delivery Lead:** Monitors batch readiness and blocked quality queues.
- **Knowledge Space Admin:** Publishes approved content to the Wiki metadata layer.
- **Knowledge Consumer:** Sees only trusted/published knowledge in Wiki, Graph, and Ask surfaces.

## Functional Scope

### Review Queue And Processing Center

- The system lists publish-blocking categories: parser failure, OCR required, low confidence, missing `source_trace`, and LLM-generated review-required content.
- Publish-ready content is separate from blocked content.
- Queue counts must reconcile with the same metadata used by API responses.
- UI actions must not imply production RBAC enforcement until the auth slice exists.

### SME Review State Machine

- Valid review actions are `APPROVE`, `NEED_FIX`, and `OCR_REQUIRED`.
- Resulting target states are `APPROVED`, `NEED_FIX`, and `OCR_REQUIRED`.
- `PUBLISHED` is not a review action result; it is set only by publish.
- Review history is append-only and chronological.

### Publish Eligibility

A candidate is eligible only when all conditions are true:

- Review status is `APPROVED`.
- Markdown path is present and relative.
- Confidence is present.
- At least one source document id is present.
- Source trace coverage exists for the publish target.
- Target is not parser-failed, unsupported, OCR-required, need-fix, or missing-trace.

### Publish Result

- Publish creates or updates a Wiki page metadata record.
- The published record has `reviewStatus=PUBLISHED`.
- The record preserves title, space id, Markdown path, source document ids, confidence, owner, and last updated timestamp.
- Raw parser output, source chunks, and original source paths are not mutated by publish.

### Downstream Trust Boundary

- Wiki reads published page metadata from this slice.
- Graph extraction and Ask/RAG use published/approved/source-traced content as input but are implemented in later slices.
- Unresolved Processing Center issues remain excluded from trusted downstream contexts.

## Non-Functional Requirements

- API responses use the existing Atlas envelope and user-safe error style.
- No external network calls are introduced.
- No raw secrets, private absolute paths, or real company data are introduced.
- Parser, converter, model, vector, storage, and search engines stay behind product-facing adapters and are not called directly in publish logic.
- Tests cover full unit, integration, and E2E paths for touched layers.

## State Model

```text
REVIEW_REQUIRED --APPROVE--> APPROVED --PUBLISH--> PUBLISHED
REVIEW_REQUIRED --NEED_FIX--> NEED_FIX
REVIEW_REQUIRED --OCR_REQUIRED--> OCR_REQUIRED
APPROVED --NEED_FIX--> NEED_FIX
APPROVED --OCR_REQUIRED--> OCR_REQUIRED

PUBLISHED is terminal for the published Wiki metadata until a future revision workflow is accepted.
```

## API Behavior

- `GET /api/spaces/{spaceId}/review-queues` returns queue counts and representative items.
- `POST /api/files/{fileId}/reviews` continues to append review records and update review status.
- `POST /api/files/{fileId}/publish` publishes an eligible file to Wiki metadata.
- `GET /api/spaces/{spaceId}/wiki-pages` lists published Wiki page metadata.
- `GET /api/wiki-pages/{wikiPageId}` returns one published Wiki page metadata record.

## Acceptance Matrix

| Requirement | Spec Section | Observable Check |
|---|---|---|
| REQ-REVIEW-PUBLISH-001 | Review Queue And Processing Center | Queue API/UI separates blocked issue categories. |
| REQ-REVIEW-PUBLISH-002 | SME Review State Machine | Review transition tests prove action/status mapping. |
| REQ-REVIEW-PUBLISH-003 | SME Review State Machine | History list remains append-only and chronological. |
| REQ-REVIEW-PUBLISH-004 | Publish Eligibility | Ineligible candidates return 400/409 without mutation. |
| REQ-REVIEW-PUBLISH-005 | Publish Result | Wiki page response shows `PUBLISHED` and preserved metadata. |
| REQ-REVIEW-PUBLISH-006 | Publish Result | Raw file/chunk metadata is unchanged after publish. |
| REQ-REVIEW-PUBLISH-007 | API Behavior | Error responses contain no stack traces, secrets, or absolute paths. |
| REQ-REVIEW-PUBLISH-008 | Review Queue And Processing Center | FE renders readiness, blocked counts, and published status. |
| REQ-REVIEW-PUBLISH-009 | Downstream Trust Boundary | Graph/Ask remain consumers only; no generation implementation. |
| REQ-REVIEW-PUBLISH-010 | Non-Functional Requirements | Dependency scan finds no direct engine calls from publish logic. |
| REQ-REVIEW-PUBLISH-011 | Non-Functional Requirements | Unit, integration, E2E, no-network, and secret scans pass. |

## Out Of Scope

- Real production auth/RBAC enforcement.
- Graph extraction implementation.
- Ask/RAG generation.
- Real parser/converter/model/vector/storage/search execution.
- Bulk publish unless accepted during implementation planning.

## Risks And Open Questions

- Chunk-level review may require endpoint expansion if file-level review is too coarse.
- Bulk publish may be requested by stakeholders because the Processing Center is batch-scale.
- Production RBAC will need a later slice before exposing these endpoints beyond trusted internal use.

## Product Goal Batch 2 Vue Parity Addendum

Product Goal Batch 2 extends the Processing Center portion of this slice into the real Vue Knowledge Space detail page.

| Phase | Vue Product Acceptance |
|---|---|
| Phase D Processing Center | The real Vue `IBM i Modernization` Processing Center shows total documents, parse failures, OCR required, low confidence, missing `source_trace`, LLM-generated review-required items, ready-to-publish count, and queue rows explaining why content is blocked from Wiki, Graph, and Ask. |

This addendum changes only the Vue product surface maturity. It does not add new backend/API behavior, production RBAC, real remediation workers, or external provider calls.

## Product Goal Batch 3 Vue Parity Addendum

Product Goal Batch 3 extends the published Wiki portion of this slice into the real Vue Knowledge Space detail page.

| Phase | Vue Product Acceptance |
|---|---|
| Phase E LM Wiki | The real Vue `IBM i Modernization` Wiki tab provides a browsable Wiki index, dense Markdown-like content sections, page metadata, entity links, confidence, review status, and visible `source_trace` blocks for published, approved, and review-required sample pages. |

This addendum changes only the Vue product surface maturity. It does not add a real Markdown generator, real document content, new backend/API behavior, or external provider calls.
