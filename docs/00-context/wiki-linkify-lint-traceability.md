# Traceability: Wiki Linkify And Lint

## Status

Accepted by the current user. Implementation is unlocked for the accepted `wiki-linkify-lint` scope only.

## Slice Contract

- **Slice:** `wiki-linkify-lint`
- **Wave:** Wave 1 / Wiki Foundation
- **Goal:** Add deterministic Wiki linkification and quality lint warnings on top of the completed Wiki data model and ingest v0 foundation.
- **Maturity target:** SDD-ready Wiki Foundation maintenance slice. Not production readiness.

## Source Documents

| Source | Status |
|---|---|
| Goal objective attachment | Read. |
| `README.md` | Read. |
| `PROJECT_RULES.md` | Read. |
| `DEVELOPMENT_STANDARDS.md` | Read. |
| `docs/00-context/sdd-profile.md` | Read. |
| `ROADMAP.md` | Read. |
| `ROADMAP.zh-CN.md` | Read. |
| `docs/07-acceptance/product-acceptance-report.zh-CN.md` | Read. |
| `docs/00-context/wiki-foundation-goal-plan.zh-CN.md` | Read. |
| `docs/00-context/execution-manifests/wiki-foundation-20260705.yaml` | Read. |
| `docs/00-context/slice-roadmap.md` / `.zh-CN.md` | Read and updated. |
| `docs/00-context/wiki-data-model-traceability.md` | Read. |
| `docs/00-context/wiki-ingest-v0-traceability.md` | Read. |
| `docs/03-spec/wiki-ingest-v0-spec.md` | Read. |
| `docs/04-architecture/wiki-ingest-v0-data-model.md` | Read. |
| `docs/05-design/contracts/wiki-ingest-v0-API_IMPLEMENTATION_GUIDE.md` | Read. |

## SDD Skill Chain Evidence

| Skill | File read |
|---|---|
| `atlas-sdd-generate-all` | `.agents/skills/atlas-sdd-generate-all/SKILL.md` |
| `req-to-user-story` | `.agents/skills/req-to-user-story/SKILL.md` |
| `user-story-to-spec` | `.agents/skills/user-story-to-spec/SKILL.md` |
| `spec-to-architecture` | `.agents/skills/spec-to-architecture/SKILL.md` |
| `architecture-to-design` | `.agents/skills/architecture-to-design/SKILL.md` |
| `design-to-tasks` | `.agents/skills/design-to-tasks/SKILL.md` |
| `review-doc-quality` | `.agents/skills/review-doc-quality/SKILL.md` |
| `architecture-review` | `.agents/skills/architecture-review/SKILL.md` |

SDD skill chain used: yes.

## Grounded Implementation Anchors

| Area | Grounding |
|---|---|
| Wiki page metadata | `backend/src/main/java/com/atlas/metadata/domain/WikiPage.java` has slug, aliases, source refs, chunk refs, in/out links, version, source mode, refresh policy, confidence, and review status. |
| Issue records | `backend/src/main/java/com/atlas/metadata/domain/WikiPageIssue.java` stores safe issue type, severity, status, evidence refs, and message. |
| Run/log records | `WikiGenerationRun` and `WikiLogEntry` already support safe run summaries and lifecycle events. |
| Existing API surface | `ReviewPublishController` lists Wiki pages, generation runs, page logs, and page issues. |
| Existing ingest | `WikiIngestService` creates generated `REVIEW_REQUIRED` pages and safe run/log/issue evidence. |
| Frontend surface | `frontend/src/App.vue`, `frontend/src/api.ts`, and `frontend/src/types.ts` already render Wiki metadata and Processing Center issues. |

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-WIKI-LINKIFY-LINT-001 | US-WIKI-LINKIFY-LINT-001 | T-WIKI-LINKIFY-LINT-001, T-WIKI-LINKIFY-LINT-012 |
| REQ-WIKI-LINKIFY-LINT-002 | US-WIKI-LINKIFY-LINT-002 | T-WIKI-LINKIFY-LINT-002, T-WIKI-LINKIFY-LINT-003 |
| REQ-WIKI-LINKIFY-LINT-003 | US-WIKI-LINKIFY-LINT-003 | T-WIKI-LINKIFY-LINT-004 |
| REQ-WIKI-LINKIFY-LINT-004 | US-WIKI-LINKIFY-LINT-003 | T-WIKI-LINKIFY-LINT-005 |
| REQ-WIKI-LINKIFY-LINT-005 | US-WIKI-LINKIFY-LINT-003 | T-WIKI-LINKIFY-LINT-005 |
| REQ-WIKI-LINKIFY-LINT-006 | US-WIKI-LINKIFY-LINT-003 | T-WIKI-LINKIFY-LINT-006 |
| REQ-WIKI-LINKIFY-LINT-007 | US-WIKI-LINKIFY-LINT-004 | T-WIKI-LINKIFY-LINT-007 |
| REQ-WIKI-LINKIFY-LINT-008 | US-WIKI-LINKIFY-LINT-004 | T-WIKI-LINKIFY-LINT-007 |
| REQ-WIKI-LINKIFY-LINT-009 | US-WIKI-LINKIFY-LINT-005 | T-WIKI-LINKIFY-LINT-007 |
| REQ-WIKI-LINKIFY-LINT-010 | US-WIKI-LINKIFY-LINT-005 | T-WIKI-LINKIFY-LINT-007 |
| REQ-WIKI-LINKIFY-LINT-011 | US-WIKI-LINKIFY-LINT-002 | T-WIKI-LINKIFY-LINT-008 |
| REQ-WIKI-LINKIFY-LINT-012 | US-WIKI-LINKIFY-LINT-006 | T-WIKI-LINKIFY-LINT-010 |
| REQ-WIKI-LINKIFY-LINT-013 | US-WIKI-LINKIFY-LINT-006 | T-WIKI-LINKIFY-LINT-009 |
| REQ-WIKI-LINKIFY-LINT-014 | US-WIKI-LINKIFY-LINT-006 | T-WIKI-LINKIFY-LINT-011, T-WIKI-LINKIFY-LINT-012 |

## SDD Artifact Set

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/wiki-linkify-lint-requirements.md` | `docs/01-requirements/wiki-linkify-lint-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/wiki-linkify-lint-stories.md` | `docs/02-user-stories/wiki-linkify-lint-stories.zh-CN.md` |
| Spec | `docs/03-spec/wiki-linkify-lint-spec.md` | `docs/03-spec/wiki-linkify-lint-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/wiki-linkify-lint-architecture.md` | `docs/04-architecture/wiki-linkify-lint-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/wiki-linkify-lint-data-flow.md` | `docs/04-architecture/wiki-linkify-lint-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/wiki-linkify-lint-data-model.md` | `docs/04-architecture/wiki-linkify-lint-data-model.zh-CN.md` |
| Design | `docs/05-design/wiki-linkify-lint-design.md` | `docs/05-design/wiki-linkify-lint-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/wiki-linkify-lint-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/wiki-linkify-lint-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/wiki-linkify-lint-tasks.md` | `docs/06-tasks/wiki-linkify-lint-tasks.zh-CN.md` |
| Traceability | `docs/00-context/wiki-linkify-lint-traceability.md` | `docs/00-context/wiki-linkify-lint-traceability.zh-CN.md` |

## Human Acceptance Handoff

Review these documents before accepting implementation:

- `docs/03-spec/wiki-linkify-lint-spec.md`
- `docs/06-tasks/wiki-linkify-lint-tasks.md`
- `docs/05-design/contracts/wiki-linkify-lint-API_IMPLEMENTATION_GUIDE.md`

Acceptance means approving deterministic linkify/lint, safe issue recording, link metadata updates, Processing Center/Wiki warnings, and no trust-state changes.

## SDD Quality Review Result

| Gate | Result | Evidence |
|---|---|---|
| Required context | Passed | Goal objective attachment, repository rules, SDD profile, roadmap/progress docs, product acceptance report, Wiki data-model traceability, Wiki ingest traceability/spec/data model/API guide, and implementation anchors were read. |
| Skill chain evidence | Passed | `atlas-sdd-generate-all`, `req-to-user-story`, `user-story-to-spec`, `spec-to-architecture`, `architecture-to-design`, `design-to-tasks`, `review-doc-quality`, and `architecture-review` skill files were read and used for this SDD pass. |
| Expected artifacts exist | Passed | All 20 English and Simplified Chinese `wiki-linkify-lint` SDD files exist. |
| Bilingual ID parity | Passed | Focused parity checks found matching `REQ`, `US`, and `T` IDs across English and Chinese companion files. |
| Deferred-decision scan | Passed | No `TBD`, `TODO`, `FIXME`, `implementation will decide`, or `grep later` text found in the generated SDD files. |
| Architecture/API review | Passed | The SDD records additive API and persistence changes, run mode constraint update, safe issue reuse, no trust-state changes, and no direct parser/model/provider calls. |
| Diff hygiene | Passed | `git diff --check` returned no findings after this SDD pass. |
| Secret/private-path scan | Passed with expected policy-text hits | Focused scan hits were guardrail phrases such as "secret" and "private path"; no raw credential, private absolute path, or real data value was found. |
| Network/dependency scan | Passed with expected policy-text hits | Focused scan hits were negative constraints such as "no external cloud call"; no new external call or dependency instruction was introduced. |
| Product-code gate | Passed | No backend or frontend product code was changed in this SDD pass. |

## SDD Gate Acceptance Note

- **SDD gate result:** Accepted by user; implementation is unlocked for the accepted scope only.
- **User acceptance required before code:** satisfied.
- **Product code changed after acceptance:** yes.
- **Decision:** accepted and implemented for the deterministic Wave 1 maturity target.

Recommended implementation handoff after acceptance:

```text
Implement the wiki-linkify-lint slice strictly against docs/03-spec/wiki-linkify-lint-spec.md and docs/06-tasks/wiki-linkify-lint-tasks.md: complete every task in ID order, respect the stated constraints and verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```

## Implementation Completion Evidence

| Task | Result | Evidence |
|---|---|---|
| T-WIKI-LINKIFY-LINT-001 | Completed | User acceptance was recorded before product code changes. |
| T-WIKI-LINKIFY-LINT-002 | Completed | Added `V12__wiki_linkify_lint.sql`; extended issue repository query paths and run mode response mapping. |
| T-WIKI-LINKIFY-LINT-003 | Completed | Added `POST /api/spaces/{spaceId}/wiki-linkify-lint-runs` and `GET /api/spaces/{spaceId}/wiki-page-issues` DTO/controller/service paths with API contract coverage. |
| T-WIKI-LINKIFY-LINT-004 | Completed | Implemented same-space slug/alias target indexing with ambiguous term suppression and `REVIEW_REQUIRED` issue recording. |
| T-WIKI-LINKIFY-LINT-005 | Completed | Added protected-region-aware `WikiMarkdownLinkifier` with unit tests for frontmatter, code, Markdown links/images, existing Wiki links, self links, and one-link-per-target behavior. |
| T-WIKI-LINKIFY-LINT-006 | Completed | `WikiPage.applyLinkMetadata` updates sorted unique `inLinks`/`outLinks` only when link metadata or artifact content changes. |
| T-WIKI-LINKIFY-LINT-007 | Completed | Implemented broken link, orphan page, missing source, stale source, thin content, unreadable artifact, and ambiguous alias warnings. |
| T-WIKI-LINKIFY-LINT-008 | Completed | Run/log/issue evidence uses safe summaries and deterministic issue ids to avoid duplicate explosion. |
| T-WIKI-LINKIFY-LINT-009 | Completed | Vue Processing Center and Wiki page detail show space/page Wiki issue warnings. |
| T-WIKI-LINKIFY-LINT-010 | Completed | Publish/list/ingest regression coverage was included in focused and full backend verification. |
| T-WIKI-LINKIFY-LINT-011 | Completed | Backend, frontend, E2E, second-layer E2E, diff hygiene, secret/path, and network/dependency scans were run. |
| T-WIKI-LINKIFY-LINT-012 | Completed | Traceability, roadmap, spec, and task status were updated after implementation. |

## Verification Results

| Check | Result | Notes |
|---|---|---|
| TDD RED | Failed as expected before implementation | `cd backend && mvn -q -Dtest=WikiMarkdownLinkifierTest,WikiLinkifyLintServiceTest,WikiLinkifyLintApiContractIT -DfailIfNoTests=false test` failed because linkify/lint classes did not yet exist. |
| Focused backend | Passed | `cd backend && mvn -q -Dtest=WikiMarkdownLinkifierTest,WikiLinkifyLintServiceTest,WikiLinkifyLintApiContractIT,ReviewPublishApiContractIT -DfailIfNoTests=false test` |
| Full backend tests | Passed | `cd backend && mvn -q test`; expected existing `GlobalExceptionHandlerTest` stack trace appeared with exit code 0. |
| Backend verify | Passed | `cd backend && mvn -q verify`; Flyway validated through V12. |
| Focused frontend unit | Passed | `cd frontend && npm run test -- App.test.ts`; 12 focused tests passed. |
| Frontend quality gate | Passed | `cd frontend && npm run typecheck && npm run test && npm run build`; 18 Vitest tests passed. |
| Frontend E2E | Passed after mock-route fix | First run exposed a missing E2E mock for `/wiki-page-issues`; after adding the mock route, `cd frontend && npm run e2e` passed with 13 tests. |
| Second-layer E2E | Passed | `npm run e2e:second-layer` passed with local Postgres/backend/frontend and 2 Playwright tests. |
| Diff hygiene | Passed | `git diff --check` returned no findings before documentation closeout. |
| Secret/private-path scan | Passed | Broad scan hit existing frontend names such as `safeToken`/`apiKeyInput`; diff-only scan found no new secret or private-path hits. |
| Network/dependency scan | Passed | Broad scan hit existing API base/fetch/Ollama/DeepSeek references; diff-only scan found no new external call or dependency. |
| Design fidelity review | Passed with minor correction applied | Review found ambiguous alias auto-linking risk and over-broad `updatedPageIds`; both were corrected and covered by focused backend tests. |

## Code vs Design Review Result

- **Alignment rating:** 96%.
- **Verdict:** Aligned with acceptable maturity boundaries.
- **Corrections applied:** ambiguous aliases are no longer auto-linked, affected pages receive `REVIEW_REQUIRED`, and `updatedPageIds` now reports changed or would-change pages rather than every scanned page.
- **Blockers before next Wave 1 closeout:** none identified for the accepted deterministic linkify/lint scope.
- **Not included by design:** SME approval workflow, refresh/retract automation, connector/runtime integration, model-assisted linking, production RBAC, and production readiness.

## Residual Risks

- Markdown linkification is intentionally deterministic and conservative; complex natural-language entity linking is deferred.
- Existing issue taxonomy is reused; ambiguous alias and unsafe artifact cases map to `REVIEW_REQUIRED`.
- The implementation is local and deterministic; richer parser/runtime integration remains gated behind a later `real-office-parser-runtime` slice.
- The UI exposes review-facing warnings, not final SME approval or trust-state transitions.
