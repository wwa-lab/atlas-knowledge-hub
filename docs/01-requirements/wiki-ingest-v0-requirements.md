# Requirements: Wiki Ingest v0

## Status

Accepted by user. Product code may proceed strictly against the accepted SDD and task list.

## Slice Contract

- **Slice:** `wiki-ingest-v0`
- **Wave:** Wave 1 / Wiki Foundation
- **Goal:** Generate review-required Auto Wiki page candidates from approved source chunks and merge them into the existing Wiki data model without breaking the current API-backed Wiki path.
- **Prerequisite:** `wiki-data-model` is complete and provides page slug, reference, generation run, log, and issue metadata.
- **Maturity target:** Auto Wiki ingest v0 SDD readiness first, then implementation after user acceptance. This is not linkify/lint, production RBAC, connector sync, or production readiness.

## Requirements

| ID | Requirement | Priority | Phase |
|---|---|---|---|
| REQ-WIKI-INGEST-V0-001 | The complete bilingual SDD set must exist and be accepted before product code changes. | Must | SDD |
| REQ-WIKI-INGEST-V0-002 | The ingest run must select only approved source chunks with source trace, confidence, and review status. | Must | Backend |
| REQ-WIKI-INGEST-V0-003 | The ingest run must produce deterministic Wiki page candidates with slug, title, page type, aliases, source refs, chunk refs, confidence, and review-required status. | Must | Backend |
| REQ-WIKI-INGEST-V0-004 | Candidate generation must use deterministic summarization in v0; any future model-assisted generation must go through the Atlas ModelAdapter and remain out of this slice. | Must | Adapter |
| REQ-WIKI-INGEST-V0-005 | Re-running the same approved evidence set must be idempotent: update or merge the same slug instead of creating duplicate pages. | Must | Backend |
| REQ-WIKI-INGEST-V0-006 | Existing manually published `PUBLISHED_FILE` Wiki pages must remain compatible and must not be silently overwritten by generated candidates. | Must | Backend |
| REQ-WIKI-INGEST-V0-007 | All generated or model-assisted content must remain `REVIEW_REQUIRED` until a future review-gate slice approves it. | Must | Governance |
| REQ-WIKI-INGEST-V0-008 | Ingest runs must write safe `wiki_generation_run` and `wiki_log_entry` records without raw source text, prompts, provider payloads, secrets, or private paths. | Must | Backend |
| REQ-WIKI-INGEST-V0-009 | Failures and partial failures must be represented as safe run status and safe error summaries. | Must | Backend |
| REQ-WIKI-INGEST-V0-010 | The API must expose start-run and read-run contracts using the Atlas response envelope. | Must | API |
| REQ-WIKI-INGEST-V0-011 | The Vue Wiki and Processing Center surfaces must show generated draft/review-required ingest status without implying trusted publication. | Should | Frontend |
| REQ-WIKI-INGEST-V0-012 | Parser, converter, model, vector, storage, and search behavior must remain behind Atlas adapter boundaries. | Must | Architecture |
| REQ-WIKI-INGEST-V0-013 | The slice must use mock/sample-safe data only and must not introduce external cloud calls by default. | Must | Security |
| REQ-WIKI-INGEST-V0-014 | Verification must cover backend contracts, idempotency, review-required defaulting, frontend status display, E2E regression, diff hygiene, secret/private-path scan, and network/dependency scan. | Must | Verification |

## Explicit Exclusions

- Linkify and lint rule execution.
- Refresh/retract, stale-source handling, and protected manual edits.
- Production authentication, RBAC, audit retention, or secret-manager integration.
- Connector sync, manual URL ingest, and worker dead-letter queues.
- Real company documents, external provider calls by default, raw prompts, raw provider logs, or confidential payloads.

## Assumptions

- Existing source chunks can be reviewed and approved before this run starts.
- Existing `wiki_generation_run` and `wiki_log_entry` metadata can be reused for run tracking.
- v0 writes a generated Markdown artifact with a deterministic safe summary and stores only a safe relative `markdownPath`.

## Resolved Decisions

- v0 creates `TOPIC` pages only. `ENTITY` and `CONCEPT` candidates move to later graph/wiki extraction slices.
- v0 writes generated Markdown artifacts immediately, using deterministic safe summaries.
- v0 confidence aggregation uses the minimum included chunk confidence.
