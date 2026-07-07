# Requirements: retrieval-quality-metrics

Status: Accepted by goal preauthorization
Last updated: 2026-07-07

## Goal

Add deterministic, mock-safe retrieval quality metrics for Trusted Ask and adjacent Wiki/Graph retrieval surfaces so Atlas teams can inspect answer quality signals without exposing raw source content, secrets, private paths, raw prompts, raw provider payloads, or production-grade governance claims.

## Scope

- Compute and expose safe metrics for retrieval runs, answers, citations, evidence coverage, citation health, review eligibility, confidence, and no-evidence refusal behavior.
- Preserve existing Ask, Graph, Wiki, review/publish, safe error, and authorization semantics.
- Use existing Spring Boot, PostgreSQL, Flyway, DTO, mapper, service, controller, Vue, TypeScript, and test patterns.
- Use mock/sample-safe deterministic data only.

## Out Of Scope

- `answer-review-governance`, `graph-from-wiki-extraction`, and new `ask-session-citations` behavior.
- Provider/model/vector adapter strategy changes.
- Real analytics, observability vendors, LLM providers, embedding providers, cloud services, company data, or production A/B evaluation.
- Raw source content, raw prompts, provider responses, stack traces, internal endpoints, private paths, secrets, billing controls, human rating workflows, or ML reranker tuning.

## Requirements

| ID | Requirement | Acceptance |
|---|---|---|
| REQ-RETRIEVAL-QUALITY-METRICS-001 | Atlas must compute deterministic retrieval quality metrics for Ask runs using stored run and evidence metadata. | AC-RETRIEVAL-QUALITY-METRICS-001: A test can compute the same metric result from the same mock-safe run and evidence records every time. |
| REQ-RETRIEVAL-QUALITY-METRICS-002 | Metrics must distinguish evidence coverage, citation health, confidence, review eligibility, and no-evidence refusal behavior. | AC-RETRIEVAL-QUALITY-METRICS-002: API and UI expose those categories as separate safe fields. |
| REQ-RETRIEVAL-QUALITY-METRICS-003 | Metrics must preserve source trace, review status, confidence, citation, and evidence metadata without exposing raw source content. | AC-RETRIEVAL-QUALITY-METRICS-003: DTOs contain IDs, statuses, counts, scores, page/section labels, and safe summaries only. |
| REQ-RETRIEVAL-QUALITY-METRICS-004 | Metrics must make review eligibility conservative and deterministic. | AC-RETRIEVAL-QUALITY-METRICS-004: Missing evidence, review-required evidence, low confidence, unhealthy citations, or failed/no-evidence runs are not reported as trusted review-eligible. |
| REQ-RETRIEVAL-QUALITY-METRICS-005 | Metrics APIs must use Atlas envelopes and safe error handling. | AC-RETRIEVAL-QUALITY-METRICS-005: Backend contract tests verify success and safe failure shapes. |
| REQ-RETRIEVAL-QUALITY-METRICS-006 | Frontend surfaces must display safe quality signals without implying production-grade retrieval governance. | AC-RETRIEVAL-QUALITY-METRICS-006: Trusted Ask, Graph, Wiki, or diagnostic UI shows quality signals with conservative labels and no raw internals. |
| REQ-RETRIEVAL-QUALITY-METRICS-007 | The implementation must not regress existing Ask, Graph, Wiki, review/publish, auth, rate-limit, safe-error, or adapter behavior. | AC-RETRIEVAL-QUALITY-METRICS-007: Existing verification plus focused tests pass. |
| REQ-RETRIEVAL-QUALITY-METRICS-008 | Traceability, roadmap, SDD gate evidence, verification evidence, commit, and push must be recorded. | AC-RETRIEVAL-QUALITY-METRICS-008: Closeout includes docs changed, code changed, tests changed, verification, residual risks, commit hash, and push target. |

## Assumptions

- This is Wave 4 / Ask And Graph Productization and remains prototype-safe.
- Metrics are local quality indicators, not final production retrieval governance.
- Existing Ask evidence snapshots are sufficient for this slice; new provider calls are not required.

## Dependencies

- Existing `ask-rag`, `knowledge-graph`, `review-publish`, `wiki-data-model`, `wiki-ingest-v0`, `wiki-linkify-lint`, and `rate-limit-safe-errors` foundations.

## Open Questions

None blocking within the attached goal's preauthorized boundary.
