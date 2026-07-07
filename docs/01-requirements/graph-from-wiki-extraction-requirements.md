# Requirements: graph-from-wiki-extraction

Status: Accepted by prompt preauthorization on 2026-07-07.

## Goal

Build deterministic graph extraction from eligible Wiki pages so the Graph surface is driven by reviewed, source-traced Wiki content instead of standing apart as a mock/demo surface.

## Scope

- Extract graph nodes, edges, and evidence snapshots from Wiki pages that are `APPROVED` or `PUBLISHED`, have source trace, and satisfy confidence rules.
- Preserve Wiki page metadata, source trace, section/chunk evidence, review status, confidence, and safe evidence summaries.
- Extend the existing Spring Boot Graph API, deterministic adapter, Flyway schema, DTOs, and Vue Graph UI.
- Keep all extraction local, deterministic, idempotent, and mock/sample-safe.

## Out Of Scope

- retrieval-quality-metrics, answer-review-governance, ask-session-citations.
- Model-assisted extraction, embeddings, external graph service, external cloud provider, real company data.
- Auth/RBAC/audit/secret/rate-limit semantic changes.
- Production graph layout, graph ML deduplication, manual graph editor, SIEM/export.

## Requirements

| ID | Requirement | Priority | Acceptance |
|---|---|---|---|
| REQ-GRAPH-FROM-WIKI-EXTRACTION-001 | Extraction MUST use only approved, published, or explicitly eligible Wiki pages. | Must | `REVIEW_REQUIRED`, `NEED_FIX`, `OCR_REQUIRED`, low-confidence, and missing-trace pages are skipped or warning-only. |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-002 | Extraction MUST be deterministic and idempotent for the same Wiki input. | Must | Re-running projection produces stable graph IDs and does not duplicate nodes, edges, or run items. |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-003 | Extracted graph nodes MUST include Wiki-derived `WIKI_PAGE`, `DOCUMENT`, `CONCEPT`, and `ENTITY` candidates where supported by deterministic metadata. | Must | Node records include safe labels, review status, confidence, chunk evidence, and Wiki page evidence. |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-004 | Extracted graph edges MUST preserve source trace and evidence snapshots. | Must | Edge records carry chunk IDs, Wiki page IDs, confidence, review status, and safe evidence detail. |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-005 | Evidence responses MUST NOT expose raw secrets, private paths, raw documents, internal endpoints, provider payloads, or confidential content. | Must | API and UI display safe identifiers and source trace metadata only. |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-006 | Existing graph query, node detail, projection run, review action, and downstream refresh surfaces MUST remain backward compatible. | Must | Existing graph tests continue to pass. |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-007 | Frontend Graph UI MUST show Wiki-derived evidence in an inspectable way. | Must | Detail panel distinguishes source chunk and Wiki page evidence. |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-008 | Verification MUST cover extraction, idempotency, eligibility filtering, and evidence preservation. | Must | Backend unit/API tests, frontend tests, E2E, and closeout gate pass. |

## SDD Skill Chain Evidence

SDD skill chain used: yes.

- `.agents/skills/atlas-sdd-generate-all/SKILL.md`
- `.agents/skills/req-to-user-story/SKILL.md`
- `.agents/skills/user-story-to-spec/SKILL.md`
- `.agents/skills/spec-to-architecture/SKILL.md`
- `.agents/skills/architecture-to-design/SKILL.md`
- `.agents/skills/design-to-tasks/SKILL.md`
- `.agents/skills/review-doc-quality/SKILL.md`
- `.agents/skills/architecture-review/SKILL.md`
