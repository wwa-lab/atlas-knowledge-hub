# Tasks: Wiki Linkify And Lint

## Status

Draft for user review. Do not implement until accepted.

## Task Checklist

| ID | Task | Requirements | Verification |
|---|---|---|---|
| T-WIKI-LINKIFY-LINT-001 | Record user acceptance of this bilingual SDD before product code changes. | REQ-WIKI-LINKIFY-LINT-001 | Traceability contains accepted gate before implementation. |
| T-WIKI-LINKIFY-LINT-002 | Add additive backend schema support for `wiki_generation_run.mode = linkify-lint` and required issue query paths. | REQ-WIKI-LINKIFY-LINT-002, REQ-WIKI-LINKIFY-LINT-011 | Flyway migration validates under `cd backend && mvn verify`. |
| T-WIKI-LINKIFY-LINT-003 | Add request/response DTOs and API endpoints for starting a linkify/lint run and listing space issues. | REQ-WIKI-LINKIFY-LINT-002, REQ-WIKI-LINKIFY-LINT-014 | API contract tests cover happy path, validation, and safe errors. |
| T-WIKI-LINKIFY-LINT-004 | Implement deterministic target indexing from same-space slugs and aliases. | REQ-WIKI-LINKIFY-LINT-003 | Unit/service tests cover aliases, ambiguity, and cross-space exclusion. |
| T-WIKI-LINKIFY-LINT-005 | Implement protected-region-aware Markdown linkify utility. | REQ-WIKI-LINKIFY-LINT-004, REQ-WIKI-LINKIFY-LINT-005 | Unit tests cover frontmatter, fenced code, inline code, existing Wiki links, Markdown links, images, self links, and one link per target. |
| T-WIKI-LINKIFY-LINT-006 | Update Wiki page `inLinks` and `outLinks` deterministically and idempotently. | REQ-WIKI-LINKIFY-LINT-006 | Service tests prove repeated runs do not duplicate links or reorder unexpectedly. |
| T-WIKI-LINKIFY-LINT-007 | Implement lint issue rules for broken links, orphan pages, missing refs, stale refs, thin content, and review-required ambiguity. | REQ-WIKI-LINKIFY-LINT-007, REQ-WIKI-LINKIFY-LINT-008, REQ-WIKI-LINKIFY-LINT-009, REQ-WIKI-LINKIFY-LINT-010 | Service tests cover every issue type and no-model/no-parser behavior. |
| T-WIKI-LINKIFY-LINT-008 | Persist safe run, log, and issue evidence with deterministic duplicate handling. | REQ-WIKI-LINKIFY-LINT-011 | Repository/service tests assert safe summaries, issue status, and no duplicate explosion. |
| T-WIKI-LINKIFY-LINT-009 | Update Vue Processing Center and Wiki page surfaces to show issue counts and page warnings. | REQ-WIKI-LINKIFY-LINT-013 | `cd frontend && npm run typecheck && npm run test && npm run build`; E2E if UI path changes. |
| T-WIKI-LINKIFY-LINT-010 | Run regression tests for publish/list/ingest and ensure review statuses are preserved. | REQ-WIKI-LINKIFY-LINT-012 | Focused backend tests plus `cd backend && mvn verify`. |
| T-WIKI-LINKIFY-LINT-011 | Run full verification and safety scans. | REQ-WIKI-LINKIFY-LINT-014 | Backend verify, frontend checks, E2E when applicable, `npm run e2e:second-layer`, `git diff --check`, secret/path scan, network/dependency scan. |
| T-WIKI-LINKIFY-LINT-012 | Update traceability, roadmap status, and residual risk notes after implementation. | REQ-WIKI-LINKIFY-LINT-001, REQ-WIKI-LINKIFY-LINT-014 | Context files list changed docs/code, verification evidence, skipped checks, and next slice. |

## Implementation Order

Execute tasks in ID order after acceptance. If implementation would diverge from `docs/03-spec/wiki-linkify-lint-spec.md`, stop and update the SDD or ask for direction before coding around it.

## Required Verification Commands After Implementation

```bash
cd backend && mvn verify
cd frontend && npm run typecheck && npm run test && npm run build
cd frontend && npm run e2e
npm run e2e:second-layer
git diff --check
```

Also run focused secret/private-path and network/dependency scans over changed files.
