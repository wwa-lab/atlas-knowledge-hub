# Traceability: Wiki Ingest v0

## Status

Implementation verified for Auto Wiki ingest v0. The slice remains a review-required Wiki Foundation capability, not production readiness, review-gate completion, linkify/lint completion, connector sync, or model-assisted generation.

## Slice Contract

- **Slice:** `wiki-ingest-v0`
- **Goal:** Generate review-required Auto Wiki page candidates from approved source chunks and merge them into the existing Wiki data model.
- **Wave:** Wave 1 / Wiki Foundation
- **Maturity target:** Verified Auto Wiki ingest v0 implementation slice with deterministic review-required candidate generation, safe run/log/issue evidence, and explicit draft exposure.

## Source Documents

| Source | Status |
|---|---|
| `AGENTS.md` | Read. |
| `README.md` | Read. |
| `PROJECT_RULES.md` | Read. |
| `DEVELOPMENT_STANDARDS.md` | Read. |
| `docs/SDD-BOOTSTRAP.md` | Read. |
| `docs/SDD-BOOTSTRAP.zh-CN.md` | Read. |
| `docs/00-context/agent-goal-loop-workflow.md` | Read. |
| `docs/00-context/agent-goal-loop-workflow.zh-CN.md` | Read. |
| `docs/00-context/sdd-profile.md` | Read. |
| `docs/00-context/checklists/sdd-generation-gate.md` | Read. |
| `docs/00-context/checklists/sdd-generation-gate.zh-CN.md` | Read. |
| `docs/00-context/goal-prompts/master-goal-prompt.md` | Read. |
| `docs/00-context/goal-prompts/single-slice-goal-prompt.md` | Read. |
| `docs/01-requirements/requirement.md` | Read. |
| `docs/00-context/wiki-foundation-goal-plan.zh-CN.md` | Read. |
| `docs/00-context/execution-manifests/wiki-foundation-20260705.yaml` | Read. |
| `ROADMAP.md` | Read. |
| `ROADMAP.zh-CN.md` | Read. |
| `docs/00-context/slice-roadmap.md` | Read and updated with current next-phase wave queue status. |
| `docs/00-context/slice-roadmap.zh-CN.md` | Read and updated with current next-phase wave queue status. |
| `docs/07-acceptance/product-acceptance-report.zh-CN.md` | Read. |
| `docs/07-acceptance/internal-beta-readiness.zh-CN.md` | Read. |
| `docs/03-spec/full-stack-productization-spec.zh-CN.md` | Read. |
| `docs/06-tasks/full-stack-productization-tasks.zh-CN.md` | Read. |
| `docs/00-context/full-stack-productization-traceability.zh-CN.md` | Read. |
| `docs/00-context/wiki-data-model-traceability.md` | Read. |
| `WEKNORA_ANALYSIS_DIR` | Not read; environment variable was unset. |

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

Additional references read for review quality: `.agents/skills/_shared/grounding-rules.md`, `.agents/skills/review-doc-quality/references/completeness-criteria.md`, and `.agents/skills/review-doc-quality/references/phase-scope-guide.md`.

## Grounded Existing Implementation

| Area | Grounding |
|---|---|
| Wiki page refs and review state | `backend/src/main/java/com/atlas/metadata/domain/WikiPage.java:115` creates published Wiki metadata with source chunk references; getters expose refs and status. |
| Published Wiki compatibility | `backend/src/main/java/com/atlas/metadata/service/ReviewPublishService.java:138` publishes only approved Markdown files; `ReviewPublishService.java:144` blocks missing source trace. |
| Existing upload/parser path | `backend/src/main/java/com/atlas/metadata/service/IngestionService.java:58` ingests PDF/ZIP uploads and starts local parser runs; `IngestionService.java:168` creates file metadata as review-required. |
| Frontend API shape | `frontend/src/types.ts:365` defines the current `ApiWikiPage` fields including slug, refs, source mode, refresh policy, confidence, and published status. |

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-WIKI-INGEST-V0-001 | US-WIKI-INGEST-V0-001 | T-WIKI-INGEST-V0-001, T-WIKI-INGEST-V0-010 |
| REQ-WIKI-INGEST-V0-002 | US-WIKI-INGEST-V0-002 | T-WIKI-INGEST-V0-003, T-WIKI-INGEST-V0-008 |
| REQ-WIKI-INGEST-V0-003 | US-WIKI-INGEST-V0-002 | T-WIKI-INGEST-V0-004, T-WIKI-INGEST-V0-008 |
| REQ-WIKI-INGEST-V0-004 | US-WIKI-INGEST-V0-003 | T-WIKI-INGEST-V0-004, T-WIKI-INGEST-V0-009 |
| REQ-WIKI-INGEST-V0-005 | US-WIKI-INGEST-V0-004 | T-WIKI-INGEST-V0-005, T-WIKI-INGEST-V0-008 |
| REQ-WIKI-INGEST-V0-006 | US-WIKI-INGEST-V0-004 | T-WIKI-INGEST-V0-005 |
| REQ-WIKI-INGEST-V0-007 | US-WIKI-INGEST-V0-002 | T-WIKI-INGEST-V0-004, T-WIKI-INGEST-V0-007 |
| REQ-WIKI-INGEST-V0-008 | US-WIKI-INGEST-V0-005 | T-WIKI-INGEST-V0-006 |
| REQ-WIKI-INGEST-V0-009 | US-WIKI-INGEST-V0-005 | T-WIKI-INGEST-V0-006 |
| REQ-WIKI-INGEST-V0-010 | US-WIKI-INGEST-V0-005 | T-WIKI-INGEST-V0-002 |
| REQ-WIKI-INGEST-V0-011 | US-WIKI-INGEST-V0-006 | T-WIKI-INGEST-V0-007 |
| REQ-WIKI-INGEST-V0-012 | US-WIKI-INGEST-V0-003 | T-WIKI-INGEST-V0-004, T-WIKI-INGEST-V0-009 |
| REQ-WIKI-INGEST-V0-013 | US-WIKI-INGEST-V0-003 | T-WIKI-INGEST-V0-003, T-WIKI-INGEST-V0-009 |
| REQ-WIKI-INGEST-V0-014 | US-WIKI-INGEST-V0-006 | T-WIKI-INGEST-V0-008, T-WIKI-INGEST-V0-009, T-WIKI-INGEST-V0-010 |

## SDD Artifact Set

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/wiki-ingest-v0-requirements.md` | `docs/01-requirements/wiki-ingest-v0-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/wiki-ingest-v0-stories.md` | `docs/02-user-stories/wiki-ingest-v0-stories.zh-CN.md` |
| Spec | `docs/03-spec/wiki-ingest-v0-spec.md` | `docs/03-spec/wiki-ingest-v0-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/wiki-ingest-v0-architecture.md` | `docs/04-architecture/wiki-ingest-v0-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/wiki-ingest-v0-data-flow.md` | `docs/04-architecture/wiki-ingest-v0-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/wiki-ingest-v0-data-model.md` | `docs/04-architecture/wiki-ingest-v0-data-model.zh-CN.md` |
| Design | `docs/05-design/wiki-ingest-v0-design.md` | `docs/05-design/wiki-ingest-v0-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/wiki-ingest-v0-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/wiki-ingest-v0-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/wiki-ingest-v0-tasks.md` | `docs/06-tasks/wiki-ingest-v0-tasks.zh-CN.md` |
| Traceability | `docs/00-context/wiki-ingest-v0-traceability.md` | `docs/00-context/wiki-ingest-v0-traceability.zh-CN.md` |

## Human Acceptance Handoff

Review these documents before accepting implementation:

- `docs/03-spec/wiki-ingest-v0-spec.md`
- `docs/06-tasks/wiki-ingest-v0-tasks.md`
- `docs/05-design/contracts/wiki-ingest-v0-API_IMPLEMENTATION_GUIDE.md`

Acceptance means the user approves the v0 scope: deterministic Auto Wiki candidate generation from approved, traceable source chunks; review-required generated pages; safe run/log/issue evidence; idempotent slug merge; and no trusted-page overwrite. Acceptance does not approve linkify/lint, review approval workflow, refresh/retract, connector sync, production RBAC, model-assisted generation, external provider calls, real company data, or production readiness.

After acceptance, implementation should start with T-WIKI-INGEST-V0-001 and proceed in task order. Recommended implementation handoff:

```text
Implement the wiki-ingest-v0 slice strictly against docs/03-spec/wiki-ingest-v0-spec.md and docs/06-tasks/wiki-ingest-v0-tasks.md: complete every task in ID order, respect the stated constraints and verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```

## SDD Gate Acceptance Note

- **SDD gate result:** Accepted by user; implementation is unlocked for the accepted scope only.
- **Slice:** `wiki-ingest-v0`
- **Skill chain evidence:** Project-local SDD chain recorded as used; `architecture-review` recorded because API, persistence, adapter boundary, security, and data-flow behavior are in scope.
- **Documents checked:** Complete bilingual SDD artifact set, `docs/00-context/checklists/sdd-generation-gate.md`, required repository rules, execution manifest, roadmap/progress docs, and grounded implementation anchors.
- **Issues found:** No critical SDD gate issue remains in the current traceability evidence. Residual risks are listed below.
- **User acceptance required before code:** yes.
- **Decision:** accepted.

## Implementation Completion Evidence

| Task | Completion evidence |
|---|---|
| T-WIKI-INGEST-V0-001 | User accepted the bilingual SDD gate before product code changes. |
| T-WIKI-INGEST-V0-002 | Added `CreateWikiIngestRunRequest`, `WikiIngestRunResponse`, `WikiIngestController`, and start/read API contract coverage. |
| T-WIKI-INGEST-V0-003 | `WikiIngestService` selects only space-scoped approved, traceable chunks and records eligible/excluded counts. |
| T-WIKI-INGEST-V0-004 | Deterministic candidate builder creates `TOPIC`, `AUTO_GENERATED`, `ON_SOURCE_CHANGE`, `REVIEW_REQUIRED` pages, writes generated Markdown through local artifact storage, aggregates confidence by minimum chunk confidence, and rejects `model-assisted` v0 mode. |
| T-WIKI-INGEST-V0-005 | Space-scoped slug lookup merges generated candidates and records safe issues without overwriting trusted `PUBLISHED_FILE` pages. |
| T-WIKI-INGEST-V0-006 | `wiki_generation_run`, `wiki_log_entry`, and `wiki_page_issue` evidence is persisted with safe summaries, page ids, issue ids, and counts. |
| T-WIKI-INGEST-V0-007 | Vue fetches `GET /api/spaces/{spaceId}/wiki-pages?includeDrafts=true` for Wiki surfaces and can render generated `REVIEW_REQUIRED` / `AUTO_GENERATED` status without trusted claims. |
| T-WIKI-INGEST-V0-008 | Backend service/API tests and frontend type/unit/build/E2E coverage were added or updated. |
| T-WIKI-INGEST-V0-009 | Full verification gates and focused safety scans passed; see verification results below. |
| T-WIKI-INGEST-V0-010 | This traceability file, task status, and roadmap rows record completion evidence and residual risks. |

## Verification Results

| Check | Result | Evidence |
|---|---|---|
| Backend focused unit tests | Passed | `cd backend && mvn -q -Dtest=WikiIngestServiceTest,ReviewPublishServiceTest -DfailIfNoTests=false test` |
| Backend focused API contract tests | Passed | `cd backend && mvn -q -Dtest=WikiIngestApiContractIT,ReviewPublishApiContractIT -DfailIfNoTests=false test` |
| Backend full unit tests | Passed | `cd backend && mvn -q test` |
| Backend verify with PostgreSQL/Testcontainers | Passed | `cd backend && mvn -q verify`; Flyway applied through `V11__wiki_ingest_v0_run_counts.sql`. |
| Frontend typecheck/unit/build | Passed | `cd frontend && npm run typecheck && npm run test && npm run build` |
| Frontend E2E | Passed | `cd frontend && npm run e2e`; 13 Playwright tests passed. |
| Second-layer full-stack E2E | Passed | `npm run e2e:second-layer`; 2 Playwright tests passed against local backend/frontend/PostgreSQL. |
| Diff hygiene | Passed | `git diff --check` returned no findings. |
| Focused secret/private-path scan | Passed with reviewed false positive | New ingest-file diff scan returned no findings; broader scoped diff only matched the word `password` in a negative API contract assertion. |
| Focused network/dependency scan | Passed | New ingest backend files contain no direct HTTP/provider/parser/vector/storage engine coupling; generated Markdown uses existing `LocalArtifactStorageService`. |

## SDD Quality Notes

- Skill chain applied: `atlas-sdd-generate-all`, `req-to-user-story`, `user-story-to-spec`, `spec-to-architecture`, `architecture-to-design`, `design-to-tasks`, and `review-doc-quality` checklist.
- Architecture review concerns addressed in SDD: adapter boundaries, API contract, persistence state, safe logs, deterministic-only v0 behavior, generated Markdown artifact output, and review-required generated output.
- Product code changed only after user acceptance and stayed scoped to the accepted `wiki-ingest-v0` implementation tasks.

## SDD Quality Review Result

| Gate | Result | Evidence |
|---|---|---|
| Required context | Passed | Repository rules, SDD bootstrap docs, goal-loop workflow docs, execution manifest, SDD generation checklist, roadmap/progress docs, and active slice SDD docs were read or recorded in source evidence. |
| Companion files | Passed | All 20 English and Simplified Chinese `wiki-ingest-v0` SDD files exist. |
| ID parity | Passed | `REQ`, `US`, and `T` IDs match across English and Chinese companions. |
| Deferred-decision scan | Passed | Open candidate decisions were resolved: v0 uses `TOPIC` pages, generated Markdown artifacts, minimum confidence aggregation, and deterministic-only generation. |
| Phase discipline | Passed | User accepted the SDD before implementation; product code was then changed strictly against the accepted spec/tasks. |
| Adapter boundary | Passed | v0 rejects or disables model-assisted mode; future model use must go through ModelAdapter. |
| Safety language | Passed | SDD excludes real company data, raw secrets, raw prompts, provider payloads, private paths, stack traces, and default external calls. |
| Context status | Passed | `docs/00-context/slice-roadmap.md` and `.zh-CN.md` record `wiki-ingest-v0` as implementation verified for the current maturity target. |
| Acceptance handoff | Passed | Traceability names the review documents, accepted v0 scope, exclusions, and implementation handoff command. |
| SDD gate acceptance note | Passed | Traceability records the SDD gate result, documents checked, accepted decision state, and implementation scope boundary. |
| Diff hygiene | Passed | `git diff --check` returned no findings after this SDD pass. |

## Residual Risks

- Generated deterministic candidates are intentionally thin and remain `REVIEW_REQUIRED`; they are not trusted Ask or Graph evidence until later governance slices.
- `wiki_page_issue.issue_type` uses the existing safe issue taxonomy for trusted slug collisions rather than adding a collision-specific enum in this slice.
- Existing worktree contains prior `wiki-data-model` and roadmap/documentation changes; they were not reverted or restyled.
- `WEKNORA_ANALYSIS_DIR` was unset, so local WeKnora analysis files were not read.

## Resolved Decisions

- v0 creates `TOPIC` pages only.
- v0 writes generated Markdown artifacts with deterministic safe summaries and safe source/chunk labels.
- v0 aggregates confidence using the minimum included chunk confidence.
- v0 rejects or disables model-assisted mode; future model assistance must use ModelAdapter.
