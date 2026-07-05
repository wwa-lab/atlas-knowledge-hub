# Requirements: Wiki Linkify And Lint

## Status

Draft for user review. Product code must not start until this bilingual SDD set is accepted.

## Goal

Add a deterministic Wiki maintenance pass that injects safe Wiki links, refreshes page link metadata, detects basic Wiki quality issues, and exposes review-facing issue statistics without changing trust or publication rules.

## Scope

In scope:

- Space-scoped linkify/lint runs over existing Wiki pages.
- Alias and slug based `[[wiki-link]]` insertion.
- Skipping YAML frontmatter, fenced code blocks, inline code, existing Wiki links, Markdown links, and images.
- Updating `inLinks` and `outLinks` metadata.
- Detecting broken Wiki links, orphan pages, missing source refs, stale source refs, and thin content.
- Writing safe issues to `wiki_page_issue`.
- Recording run and log evidence through existing Wiki run/log tables.
- Showing issue counts in the Processing Center and issue details on Wiki pages.

Out of scope:

- SME approval gates, trusted publication workflow changes, refresh/retract behavior, connector sync, model-assisted linting, production RBAC, external calls, and real company data.

## Requirements

| ID | Requirement | Acceptance |
|---|---|---|
| REQ-WIKI-LINKIFY-LINT-001 | Complete bilingual SDD must exist and be accepted before implementation. | EN and `.zh-CN.md` artifacts exist with matching REQ/US/T IDs; product code is unchanged in the SDD pass. |
| REQ-WIKI-LINKIFY-LINT-002 | A run must be scoped to one Knowledge Space. | API rejects unknown spaces and never reads or writes pages outside the space. |
| REQ-WIKI-LINKIFY-LINT-003 | Link targets must come from current page slugs and aliases in the same space. | Tests prove cross-space pages are not linked. |
| REQ-WIKI-LINKIFY-LINT-004 | Linkify must insert at most one link per target slug per source page. | Repeated aliases on one page produce one outbound link. |
| REQ-WIKI-LINKIFY-LINT-005 | Linkify must skip protected Markdown regions. | Tests cover YAML frontmatter, fenced code, inline code, existing Wiki links, Markdown links, and images. |
| REQ-WIKI-LINKIFY-LINT-006 | Link metadata must be deterministic and idempotent. | Re-running the same content does not duplicate links or change link order unexpectedly. |
| REQ-WIKI-LINKIFY-LINT-007 | Lint must detect broken Wiki links. | Broken `[[slug]]` references create `BROKEN_LINK` issues. |
| REQ-WIKI-LINKIFY-LINT-008 | Lint must detect orphan pages. | Pages with no incoming links create `ORPHAN_PAGE` issues unless they are index/root pages. |
| REQ-WIKI-LINKIFY-LINT-009 | Lint must detect missing source refs and thin content. | Pages with no safe source/chunk refs create `MISSING_SOURCE_REF`; very short bodies create `THIN_CONTENT`. |
| REQ-WIKI-LINKIFY-LINT-010 | Lint must detect stale source refs using deterministic metadata only. | Missing referenced source documents or chunks create `STALE_SOURCE`; no parser/model call is required. |
| REQ-WIKI-LINKIFY-LINT-011 | Run, log, and issue evidence must be safe. | Responses and logs contain counts, ids, labels, and safe locators only; no raw source text, secrets, private paths, stack traces, prompts, or provider payloads. |
| REQ-WIKI-LINKIFY-LINT-012 | Existing trusted publish and generated draft behavior must not regress. | Existing Wiki list/detail/publish behavior and generated `REVIEW_REQUIRED` draft exposure continue to pass tests. |
| REQ-WIKI-LINKIFY-LINT-013 | Processing Center and Wiki UI must show issue counts without implying production readiness. | Vue displays issue counts/statuses as review-facing warnings, not automatic approval. |
| REQ-WIKI-LINKIFY-LINT-014 | Verification must cover backend contracts, persistence, frontend display, idempotency, and safety scans. | Task verification includes backend tests, frontend tests/build/E2E when code is implemented, `git diff --check`, secret/path scan, and network/dependency scan. |

## Constraints

- Deterministic by default; no model provider, parser, converter, vector, search, or external network calls.
- Preserve source trace, confidence, and review status on all Wiki pages.
- Use existing adapter boundaries and local artifact storage patterns.
- Use mock/sample-safe data only.
- Completion of this slice means Wiki Foundation linkify/lint capability, not internal beta or production readiness.
