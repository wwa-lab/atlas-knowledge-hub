# Architecture: Ask RAG

## Status

Draft. Derived from `docs/03-spec/ask-rag-spec.md` using `spec-to-architecture`.

## Overview

Ask RAG is a full-stack Phase 4 hardening slice. The frontend renders Trusted Ask states; the backend validates scoped questions, retrieves evidence through vector adapter boundaries, generates mock answers through model adapter boundaries, and persists safe Ask run evidence. Source trace, confidence, and review status remain the trust spine.

## Architectural Drivers

| Driver | Impact |
|---|---|
| Source-grounded answers | API and UI must expose evidence references for every answer. |
| Review-aware retrieval | Default approved-only policy prevents unreviewed content from being treated as trusted. |
| Adapter neutrality | Ask orchestration depends on vector/model service contracts, not provider implementations. |
| Phase 4 hardening | Audit, safe errors, seam guards, and E2E verification are required. |
| Data safety | No real company documents, raw prompts, private paths, provider payloads, or external calls in tests. |

## System Context

| Boundary | Responsibility |
|---|---|
| Trusted Ask UI | Collects question, shows answer/evidence/policy/errors, and preserves FE baseline behavior. |
| Ask API | Validates requests, enforces review policy, returns envelopes, and exposes Ask run detail. |
| Ask application service | Orchestrates retrieval, no-evidence refusal, model run, evidence persistence, and safe errors. |
| Vector adapter/service | Returns bounded similarity evidence with trace, confidence, and review status. |
| Model adapter/service | Generates mock safe answer output from evidence references and marks output review-required. |
| Metadata persistence | Stores Ask run and evidence records without mutating source/Wiki/graph/review state. |

## High-Level Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│ Users                                                        │
│ Knowledge user · SME reviewer · Delivery lead                │
└─────────────────────────┬────────────────────────────────────┘
                          │ HTTPS / JSON
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ Atlas Frontend                                               │
│ Trusted Ask tab · evidence list · review warnings            │
└─────────────────────────┬────────────────────────────────────┘
                          │ REST / ApiEnvelope
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ Spring Boot Metadata API                                    │
│ AskController · validation · safe envelope errors            │
├──────────────────────────────────────────────────────────────┤
│ Ask Application Service                                      │
│ scope policy · retrieval orchestration · model run link       │
├─────────────────────────┬────────────────────────────────────┤
│ Vector Service Contract  │ Model Service Contract             │
│ mock vector adapter      │ mock model adapter                 │
└──────────────┬───────────┴──────────────┬─────────────────────┘
               │                          │
               ▼                          ▼
┌──────────────────────────┐   ┌───────────────────────────────┐
│ PostgreSQL metadata      │   │ Adapter implementations        │
│ ask_run · ask_evidence   │   │ replaceable, mock-only in CI   │
└──────────────────────────┘   └───────────────────────────────┘
```

## Component Breakdown

### Frontend Components

- **Trusted Ask tab:** space-scoped question input, answer, evidence, confidence, policy note, review warning, loading, empty, and safe error states.
- **Evidence list:** displays source file/page or section, score/confidence, and review status.
- **Ask state mapper:** maps API statuses to UI states without implying trust for review-required output.

### Backend Services

- **Ask controller:** exposes `POST /api/spaces/{spaceId}/ask` and `GET /api/ask-runs/{runId}`.
- **Ask service:** owns validation, scope checks, retrieval policy, no-evidence refusal, model run orchestration, persistence, and response assembly.
- **Ask summary/evidence mapper:** builds safe DTOs and avoids raw source or prompt leakage.

### Integration Adapters

- **Vector boundary:** Ask calls vector query behavior through product-facing service/adapter contracts.
- **Model boundary:** Ask calls model run behavior through product-facing service/adapter contracts.
- **Forbidden direct dependencies:** model providers, vector databases, SDKs, CLIs, outbound HTTP clients, and provider-specific payloads outside adapter packages.

### Persistence

- `ask_run` records request and answer lifecycle.
- `ask_evidence` records traceable evidence rows.
- Existing `source_chunk`, `file_item`, `wiki_page`, `graph_node`, and review records are referenced but not mutated.

## Security And Reliability

- Request validation happens before adapter execution.
- No-evidence responses do not synthesize answers.
- Safe messages are bounded and sanitized.
- Adapter failures do not leak provider details.
- Seam guard tests protect adapter boundaries.
- E2E tests verify the UI displays evidence and refusal states.

## Risks And Tradeoffs

| ID | Risk | Mitigation |
|---|---|---|
| R-ASKRAG-001 | Users may over-trust generated answers. | Keep answer review status visible and `REVIEW_REQUIRED`. |
| R-ASKRAG-002 | Review-required evidence may be mixed with trusted evidence. | Default approved-only; explicit inclusion and warning required. |
| R-ASKRAG-003 | Model prompt policy could require future retention. | Current SDD stores safe summaries and references only. |

## Open Questions

- OQ-ASKRAG-001: Role policy for review-required evidence visibility.
- OQ-ASKRAG-002: Future review queue integration for Ask answers.
- OQ-ASKRAG-003: Reranking timing.
