# Traceability: Wiki Data Model

## Status

Accepted by the current user and implemented on 2026-07-05. The `wiki-data-model` slice is complete as a Wiki Foundation data-model base. This does not complete Auto Wiki ingest and does not imply production readiness.

## Slice Contract

- **Slice:** `wiki-data-model`
- **Goal:** Upgrade Atlas Wiki from a published metadata API-backed surface into an Auto Wiki-ready data model foundation.
- **Phase:** 4 hardening / Wiki Foundation.
- **Current maturity target:** Wiki Foundation data-model readiness. This is not Auto Wiki ingest completion and not production readiness.

## Source Documents

| Source | Status |
|---|---|
| `README.md` | Read. |
| `PROJECT_RULES.md` | Read. |
| `DEVELOPMENT_STANDARDS.md` | Read. |
| `docs/00-context/sdd-profile.md` | Read. |
| `docs/01-requirements/requirement.md` | Read. |
| `docs/00-context/wiki-foundation-goal-plan.zh-CN.md` | Read. |
| `docs/00-context/execution-manifests/wiki-foundation-20260705.yaml` | Read. |
| `ROADMAP.zh-CN.md` | Read. |
| `docs/07-acceptance/product-acceptance-report.zh-CN.md` | Read. |
| `docs/07-acceptance/internal-beta-readiness.zh-CN.md` | Read. |
| `docs/03-spec/full-stack-productization-spec.zh-CN.md` | Read. |
| `docs/06-tasks/full-stack-productization-tasks.zh-CN.md` | Read. |
| `docs/00-context/full-stack-productization-traceability.zh-CN.md` | Read. |
| `WEKNORA_ANALYSIS_DIR` | Not read; environment variable was unset. |

## Grounded Existing Implementation

| Area | Grounding |
|---|---|
| Existing Wiki domain | `WikiPage` stores id, space id, title, Markdown path, source document ids, confidence, review status, owner, and last updated. |
| Existing Wiki API | `ReviewPublishController` exposes publish, list pages, and get page by id. |
| Existing publish behavior | `ReviewPublishService` validates approved file, relative Markdown path, confidence, and source chunks before publishing. |
| Existing schema | `V1__init_schema.sql` creates `atlas.wiki_page`; V2 seeds one sample row. |
| Existing frontend | `ApiWikiPage` and `App.vue` map API Wiki pages and preserve fallback sample pages. |
| Existing E2E | Phase I4-I7 Playwright covers API-backed Wiki, graph, Ask, and model metadata. |

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-WIKI-DATA-MODEL-001 | US-WIKI-DATA-MODEL-001 | T-WIKI-DATA-MODEL-001, T-WIKI-DATA-MODEL-011 |
| REQ-WIKI-DATA-MODEL-002 | US-WIKI-DATA-MODEL-002, US-WIKI-DATA-MODEL-003 | T-WIKI-DATA-MODEL-002, T-WIKI-DATA-MODEL-003, T-WIKI-DATA-MODEL-005 |
| REQ-WIKI-DATA-MODEL-003 | US-WIKI-DATA-MODEL-002 | T-WIKI-DATA-MODEL-003, T-WIKI-DATA-MODEL-006, T-WIKI-DATA-MODEL-007 |
| REQ-WIKI-DATA-MODEL-004 | US-WIKI-DATA-MODEL-004 | T-WIKI-DATA-MODEL-002, T-WIKI-DATA-MODEL-003, T-WIKI-DATA-MODEL-006 |
| REQ-WIKI-DATA-MODEL-005 | US-WIKI-DATA-MODEL-005 | T-WIKI-DATA-MODEL-002, T-WIKI-DATA-MODEL-003, T-WIKI-DATA-MODEL-006 |
| REQ-WIKI-DATA-MODEL-006 | US-WIKI-DATA-MODEL-005 | T-WIKI-DATA-MODEL-002, T-WIKI-DATA-MODEL-003, T-WIKI-DATA-MODEL-006 |
| REQ-WIKI-DATA-MODEL-007 | US-WIKI-DATA-MODEL-005 | T-WIKI-DATA-MODEL-002, T-WIKI-DATA-MODEL-003, T-WIKI-DATA-MODEL-006 |
| REQ-WIKI-DATA-MODEL-008 | US-WIKI-DATA-MODEL-005 | T-WIKI-DATA-MODEL-004, T-WIKI-DATA-MODEL-006, T-WIKI-DATA-MODEL-007 |
| REQ-WIKI-DATA-MODEL-009 | US-WIKI-DATA-MODEL-002 | T-WIKI-DATA-MODEL-005, T-WIKI-DATA-MODEL-007, T-WIKI-DATA-MODEL-009 |
| REQ-WIKI-DATA-MODEL-010 | US-WIKI-DATA-MODEL-006 | T-WIKI-DATA-MODEL-008, T-WIKI-DATA-MODEL-009 |
| REQ-WIKI-DATA-MODEL-011 | US-WIKI-DATA-MODEL-006 | T-WIKI-DATA-MODEL-008, T-WIKI-DATA-MODEL-009 |
| REQ-WIKI-DATA-MODEL-012 | US-WIKI-DATA-MODEL-003, US-WIKI-DATA-MODEL-006 | T-WIKI-DATA-MODEL-004, T-WIKI-DATA-MODEL-005, T-WIKI-DATA-MODEL-008 |
| REQ-WIKI-DATA-MODEL-013 | US-WIKI-DATA-MODEL-003, US-WIKI-DATA-MODEL-007 | T-WIKI-DATA-MODEL-002, T-WIKI-DATA-MODEL-005, T-WIKI-DATA-MODEL-007 |
| REQ-WIKI-DATA-MODEL-014 | US-WIKI-DATA-MODEL-007 | T-WIKI-DATA-MODEL-010 |
| REQ-WIKI-DATA-MODEL-015 | US-WIKI-DATA-MODEL-007 | T-WIKI-DATA-MODEL-007, T-WIKI-DATA-MODEL-009, T-WIKI-DATA-MODEL-010, T-WIKI-DATA-MODEL-011 |

## SDD Artifact Set

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/wiki-data-model-requirements.md` | `docs/01-requirements/wiki-data-model-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/wiki-data-model-stories.md` | `docs/02-user-stories/wiki-data-model-stories.zh-CN.md` |
| Spec | `docs/03-spec/wiki-data-model-spec.md` | `docs/03-spec/wiki-data-model-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/wiki-data-model-architecture.md` | `docs/04-architecture/wiki-data-model-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/wiki-data-model-data-flow.md` | `docs/04-architecture/wiki-data-model-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/wiki-data-model-data-model.md` | `docs/04-architecture/wiki-data-model-data-model.zh-CN.md` |
| Design | `docs/05-design/wiki-data-model-design.md` | `docs/05-design/wiki-data-model-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/wiki-data-model-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/wiki-data-model-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/wiki-data-model-tasks.md` | `docs/06-tasks/wiki-data-model-tasks.zh-CN.md` |
| Traceability | `docs/00-context/wiki-data-model-traceability.md` | `docs/00-context/wiki-data-model-traceability.zh-CN.md` |

## Verification Plan

- SDD gate: file existence, bilingual ID match, acceptance before code.
- Backend: `cd backend && mvn verify`.
- Frontend: `cd frontend && npm run typecheck && npm run test && npm run build`.
- E2E: `cd frontend && npm run e2e`.
- Full stack: `npm run e2e:second-layer`.
- Hygiene: `git diff --check`.
- Safety: focused secret/private-path scan and focused network/dependency scan.

## Implementation Completion Evidence

| Task | Status | Evidence |
|---|---|---|
| T-WIKI-DATA-MODEL-001 | Complete | Current user explicitly accepted the SDD before product code changes. |
| T-WIKI-DATA-MODEL-002 | Complete | Added `backend/src/main/resources/db/migration/V10__wiki_data_model.sql`; `cd backend && mvn verify` applied/validated 11 migrations through V10. |
| T-WIKI-DATA-MODEL-003 | Complete | Extended `WikiPage`; added `WikiFolder`, `WikiGenerationRun`, `WikiLogEntry`, `WikiPageIssue`, `WikiReference`, and matching repositories. |
| T-WIKI-DATA-MODEL-004 | Complete | Extended `WikiPageResponse`; added reference/folder/run/log/issue response DTOs and mapper coverage. |
| T-WIKI-DATA-MODEL-005 | Complete | Existing publish/list/id-detail APIs remain in `ReviewPublishController`; publish defaults now fill slug, refs, version, source mode, and refresh policy. |
| T-WIKI-DATA-MODEL-006 | Complete | Added slug, folders, generation runs, logs, and issues read endpoints under the accepted API guide. |
| T-WIKI-DATA-MODEL-007 | Complete | Added backend service/API/repository assertions in `ReviewPublishServiceTest`, `ReviewPublishApiContractIT`, and `RepositoryIT`. |
| T-WIKI-DATA-MODEL-008 | Complete | Vue Wiki tab renders slug, page type, aliases, refs, link counts, version, source mode, and refresh policy while preserving sample fallback safety. |
| T-WIKI-DATA-MODEL-009 | Complete | Updated frontend unit and Playwright coverage for API-backed Wiki metadata display and existing Graph/Ask regressions. |
| T-WIKI-DATA-MODEL-010 | Complete | Full verification and focused safety scans completed. |
| T-WIKI-DATA-MODEL-011 | Complete | This traceability file and roadmap/progress status were updated with completion evidence. |

## Verification Results

| Check | Result | Evidence |
|---|---|---|
| Backend verify | Passed | `cd backend && mvn verify` passed: 98 unit tests and 39 integration tests, with Flyway validating 11 migrations through V10. |
| Frontend typecheck/test/build | Passed | `cd frontend && npm run typecheck && npm run test && npm run build` passed; 18 Vitest tests passed and production build succeeded. |
| Frontend E2E | Passed | `cd frontend && npm run e2e` passed 13 Playwright tests. |
| Second-layer E2E | Passed | `npm run e2e:second-layer` passed 2 local full-stack Playwright tests. |
| Diff hygiene | Passed | `git diff --check` returned no findings. |
| Secret/private-path scan | Passed | Focused scan of changed text files found no raw secrets, credentials, or private paths. |
| Network/dependency scan | Passed with reviewed existing hits | Focused scan found only existing model configuration/mock URL text; diff scan found no new external URL, dependency declaration, or network client. |

## Residual Risks

- `wiki-data-model` provides the data-model/API/UI foundation only; Auto Wiki ingest, linkify/lint, refresh/retract, production RBAC, connector sync, and operations hardening remain future slices.
- Support records are minimal safe metadata and sample-safe seed/test records; they do not represent a completed generation or lint workflow.
- `WEKNORA_ANALYSIS_DIR` remained unset during SDD generation, so no local WeKnora analysis files were read in this slice.

## SDD Quality Gate Results

Recorded for the SDD-only pass before user acceptance:

| Gate | Result | Evidence |
|---|---|---|
| Expected artifacts exist | Passed | All 20 bilingual SDD files for `wiki-data-model` exist. |
| Bilingual ID parity | Passed | `REQ`, `US`, `T`, and `OQ` IDs match across English and Chinese companions. |
| Objective field coverage | Passed | Required fields `slug`, `page_type`, `aliases`, `source_refs`, `chunk_refs`, `in_links`, `out_links`, `version`, `source_mode`, and `refresh_policy` are represented in the SDD set. |
| Objective table coverage | Passed | `wiki_folder`, `wiki_generation_run`, `wiki_log_entry`, and `wiki_page_issue` are represented in requirements, spec/design, data model, API guide, and tasks. |
| Deferred-decision scan | Passed | No `TBD`, `TODO`, `FIXME`, `implementation will decide`, or `grep later` text found in the generated SDD files. |
| Diff hygiene | Passed | `git diff --check` reported no whitespace errors. |
| New-doc whitespace | Passed | Focused trailing-whitespace scan over new SDD files returned no findings after cleanup. |
| Secret/private-path scan | Passed with expected policy-text hits | Hits were only guardrail phrases such as "do not store secrets"; no raw secret, private path, or credential value was found. |
| Network/dependency scan | Passed with expected policy-text hits | Hits were only exclusion/guardrail text; no new dependency or external call instruction was introduced. |
| Product-code gate | Passed | No backend or frontend product code was changed in this SDD pass. |

## User Acceptance Review Checklist

Use this checklist to accept or request revisions to the SDD before implementation starts.

| Review item | Acceptance signal |
|---|---|
| Scope | The SDD covers Wiki Foundation data model work only and does not claim Auto Wiki ingest, linkify/lint, production RBAC, or provider integration. |
| Data model | The `wiki_page` extension and four support tables match the desired first-slice foundation. |
| API surface | Existing publish/list/id-detail APIs remain compatible, and the new slug/folder/run/log/issue read endpoints are acceptable. |
| Frontend behavior | Vue Wiki metadata display and fallback sample safety are sufficient for this slice. |
| Verification | The task list includes backend, frontend, E2E, second-layer, diff, secret/path, and network/dependency checks. |
| Open questions | OQ-WIKI-DATA-MODEL-001 through OQ-WIKI-DATA-MODEL-003 are acceptable to defer to later slices. |
| Implementation gate | After acceptance, Codex may execute `T-WIKI-DATA-MODEL-001` through `T-WIKI-DATA-MODEL-011` in order. |

## Open Questions

- OQ-WIKI-DATA-MODEL-001: Future generated-page initial status.
- OQ-WIKI-DATA-MODEL-002: Future checksum/freshness metadata.
- OQ-WIKI-DATA-MODEL-003: Future folder ordering behavior.

## SDD Quality Notes

- Skill chain applied: `atlas-sdd-generate-all`, `req-to-user-story`, `user-story-to-spec`, `spec-to-architecture`, `architecture-to-design`, `design-to-tasks`, and `review-doc-quality` checklist.
- Architecture review concerns addressed in SDD: adapter boundaries, additive Flyway migration, DTO/API compatibility, safe metadata only.
- Product code not changed in this SDD pass.
